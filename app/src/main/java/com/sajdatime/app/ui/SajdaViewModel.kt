package com.sajdatime.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sajdatime.app.core.CalcMethod
import com.sajdatime.app.core.Coordinates
import com.sajdatime.app.core.DayPrayerTimes
import com.sajdatime.app.core.Madhab
import com.sajdatime.app.core.NextPrayer
import com.sajdatime.app.core.PrayerEngine
import com.sajdatime.app.core.PrayerSlot
import com.sajdatime.app.core.Sect
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.CityLookup
import com.sajdatime.app.data.LocationRepository
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.DailyRescheduleWorker
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.OngoingBadge
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.pdf.PrayerPdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** What went wrong while trying to establish a location, if anything. */
enum class LocationProblem {
    PERMISSION_DENIED,
    NO_FIX,
    CITY_NOT_FOUND,
}

data class UiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val today: DayPrayerTimes? = null,
    val next: NextPrayer? = null,
    val now: Instant = Instant.now(),
    val problem: LocationProblem? = null,
    val resolvingLocation: Boolean = false,
    val exportedFile: Uri? = null,
)

class SajdaViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val locationRepository = LocationRepository(application)
    private val cityLookup = CityLookup()
    private val exporter = PrayerPdfExporter(application)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings, loading = false) }
                recalculate()
            }
        }
        startClock()
        DailyRescheduleWorker.enqueue(application)
        Notifications.ensureChannels(application)
    }

    /** Drives the live countdown. One tick per second, only while the app is in memory. */
    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val now = Instant.now()
                _state.update { it.copy(now = now) }
                // Roll over to the next prayer (and next day) without a manual refresh.
                val next = _state.value.next
                if (next != null && !now.isBefore(next.at)) recalculate()
                delay(1_000)
            }
        }
    }

    // --- location --------------------------------------------------------------------

    fun refreshLocation() {
        viewModelScope.launch {
            _state.update { it.copy(resolvingLocation = true, problem = null) }
            if (!locationRepository.hasPermission()) {
                _state.update {
                    it.copy(resolvingLocation = false, problem = LocationProblem.PERMISSION_DENIED)
                }
                return@launch
            }
            val coordinates = locationRepository.currentLocation()
            if (coordinates == null) {
                _state.update {
                    it.copy(resolvingLocation = false, problem = LocationProblem.NO_FIX)
                }
                return@launch
            }
            val city = locationRepository.cityName(coordinates)
            settingsRepository.setLocation(coordinates, city)
            _state.update { it.copy(resolvingLocation = false, problem = null) }
        }
    }

    /** Manual fallback when location permission is declined. Requires internet once. */
    fun searchCity(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(resolvingLocation = true, problem = null) }
            val result = cityLookup.search(query)
            if (result == null) {
                _state.update {
                    it.copy(resolvingLocation = false, problem = LocationProblem.CITY_NOT_FOUND)
                }
                return@launch
            }
            settingsRepository.setLocation(result.coordinates, result.city)
            _state.update { it.copy(resolvingLocation = false, problem = null) }
        }
    }

    // --- settings --------------------------------------------------------------------

    fun setSect(sect: Sect) = viewModelScope.launch { settingsRepository.setSect(sect) }

    fun setMadhab(madhab: Madhab) = viewModelScope.launch { settingsRepository.setMadhab(madhab) }

    fun setMethod(method: CalcMethod) = viewModelScope.launch { settingsRepository.setMethod(method) }

    fun setNotify(slot: PrayerSlot, enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setNotify(slot, enabled) }

    fun setOngoingBadge(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setOngoingBadge(enabled) }

    fun completeOnboarding() = viewModelScope.launch { settingsRepository.completeOnboarding() }

    // --- export ----------------------------------------------------------------------

    fun exportPdf(range: PrayerPdfExporter.Range) {
        val settings = _state.value.settings
        val coordinates = settings.coordinates ?: return
        viewModelScope.launch {
            val uri = runCatching {
                exporter.export(
                    range = range,
                    coordinates = coordinates,
                    cityName = settings.cityName,
                    prefs = settings.calculationPrefs,
                )
            }.getOrNull()
            _state.update { it.copy(exportedFile = uri) }
        }
    }

    fun consumeExportedFile() = _state.update { it.copy(exportedFile = null) }

    // --- recalculation ---------------------------------------------------------------

    /**
     * Recomputes today's times and the next prayer, then republishes alarms and the
     * badge so a settings change takes effect immediately rather than at the next tick.
     */
    private suspend fun recalculate() {
        val settings = _state.value.settings
        val coordinates: Coordinates = settings.coordinates ?: run {
            _state.update { it.copy(today = null, next = null) }
            return
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()

        val computed = withContext(Dispatchers.Default) {
            val today = PrayerEngine.compute(coordinates, LocalDate.now(zone), settings.calculationPrefs)
            val next: NextPrayer = PrayerEngine.nextPrayer(coordinates, settings.calculationPrefs, now, zone)
            today to next
        }

        _state.update { it.copy(today = computed.first, next = computed.second, now = now) }

        withContext(Dispatchers.Default) {
            val context = getApplication<Application>()
            PrayerAlarmScheduler.reschedule(context, settings)
            OngoingBadge.refresh(context, settings)
        }
    }
}
