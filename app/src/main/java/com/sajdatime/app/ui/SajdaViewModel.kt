package com.sajdatime.app.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.Madhab
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.Sect
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.CityLookup
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.data.CompassRepository
import com.sajdatime.app.data.LocationRepository
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.data.WatchSync
import com.sajdatime.app.notify.DailyRescheduleWorker
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.OngoingBadge
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.pdf.PrayerPdfExporter
import kotlinx.coroutines.Job
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
    /** Device heading in degrees from true north. Null when there is no compass. */
    val compassHeading: Double? = null,
    val compassAccuracy: CompassAccuracy = CompassAccuracy.UNAVAILABLE,
    /** Qibla bearing in degrees from true north. Null until a location is known. */
    val qiblaBearing: Double? = null,
    val qiblaDistanceKm: Double? = null,
)

class SajdaViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val locationRepository = LocationRepository(application)
    private val cityLookup = CityLookup()
    private val exporter = PrayerPdfExporter(application)
    private val compassRepository = CompassRepository(application)

    /** Sensor collection runs only while the Qibla screen is on-screen. */
    private var compassJob: Job? = null

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

    fun setAlertStyle(style: AlertStyle) =
        viewModelScope.launch { settingsRepository.setAlertStyle(style) }

    fun setAlarmSound(uri: String) =
        viewModelScope.launch { settingsRepository.setAlarmSound(uri) }

    fun completeOnboarding() = viewModelScope.launch { settingsRepository.completeOnboarding() }

    fun markDisclaimerSeen() = viewModelScope.launch { settingsRepository.markDisclaimerSeen() }

    /**
     * Last resort when neither GPS nor a city search produced a position: fall back to
     * Makkah so the app still works, and flag it so the UI can say so plainly rather
     * than presenting someone else's prayer times as their own.
     */
    fun useDefaultLocation() = viewModelScope.launch {
        settingsRepository.useDefaultLocation()
        _state.update { it.copy(problem = null, resolvingLocation = false) }
    }

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

    // --- qibla -----------------------------------------------------------------------

    /**
     * Starts or stops sensor collection as the Qibla screen comes and goes. The
     * magnetometer is not free, so it is never left running behind other screens.
     */
    fun setQiblaVisible(visible: Boolean) {
        compassJob?.cancel()
        compassJob = null

        val coordinates = _state.value.settings.coordinates
        if (!visible || coordinates == null) {
            _state.update { it.copy(compassHeading = null) }
            return
        }

        if (!compassRepository.hasCompass()) {
            _state.update {
                it.copy(compassHeading = null, compassAccuracy = CompassAccuracy.UNAVAILABLE)
            }
            return
        }

        compassJob = viewModelScope.launch {
            compassRepository.headings(coordinates).collect { reading ->
                _state.update {
                    it.copy(
                        compassHeading = reading.trueHeading,
                        compassAccuracy = reading.accuracy,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassJob?.cancel()
    }

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

        _state.update {
            it.copy(
                today = computed.first,
                next = computed.second,
                now = now,
                qiblaBearing = QiblaEngine.bearingToKaaba(coordinates),
                qiblaDistanceKm = QiblaEngine.distanceToKaabaKm(coordinates),
            )
        }

        withContext(Dispatchers.Default) {
            val context = getApplication<Application>()
            PrayerAlarmScheduler.reschedule(context, settings)
            OngoingBadge.refresh(context, settings)
            WatchSync.publish(context, settings)
        }
    }
}
