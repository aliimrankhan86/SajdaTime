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
import com.sajdatime.app.ui.theme.ThemeChoice
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

/**
 * The result of an export, delivered to the UI once and then cleared.
 *
 * [Failed] exists because the previous version swallowed every export error: the user
 * tapped "Save timetable as PDF", chose a range, and absolutely nothing happened.
 */
sealed interface ExportEvent {
    data class Saved(val fileName: String) : ExportEvent
    data class Share(val uri: Uri) : ExportEvent
    data object Failed : ExportEvent
}

data class UiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val today: DayPrayerTimes? = null,
    val next: NextPrayer? = null,
    /**
     * The prayer whose window is open right now, or null between sunrise and Dhuhr when
     * none is. Separate from [next] on purpose: they are never the same prayer, and the
     * question "what can I still pray" is not the question "what is coming".
     */
    val current: PrayerSlot? = null,
    val now: Instant = Instant.now(),
    val problem: LocationProblem? = null,
    val resolvingLocation: Boolean = false,
    val exportEvent: ExportEvent? = null,
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
    private val cityLookup = CityLookup(application)
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
                val snapshot = _state.value
                // Roll over to the next prayer without a manual refresh.
                val prayerPassed = snapshot.next?.let { !now.isBefore(it.at) } == true
                // And roll over the day, which is *not* the same trigger. Between midnight
                // and Fajr no prayer passes, so on the day-list alone this loop had nothing
                // to fire on: an emulator left open and stepped to 01:00 was still showing
                // the previous day's list, with Maghrib two minutes late. The header was
                // already fixed for this (see hijriToday) and the times were not.
                val dayChanged = snapshot.today
                    ?.let { it.date != LocalDate.now(ZoneId.systemDefault()) } == true
                // Sunrise is a third trigger and it is invisible to the other two. It is
                // not a prayer, so it never appears in `next` and nothing above fires on
                // it — yet it is the exact moment Fajr stops being the prayer you can
                // still pray. Without this the "Now" marker would sit on Fajr all morning
                // and only move when Dhuhr came in, which is the bug the sunrise entry in
                // DayPrayerTimes.ordered exists to prevent and would have been reintroduced
                // here, one layer up, for free.
                val sunrisePassed = snapshot.current == PrayerSlot.FAJR &&
                    snapshot.today?.times?.get(PrayerSlot.SUNRISE)
                        ?.let { !now.isBefore(it) } == true
                if (prayerPassed || dayChanged || sunrisePassed) recalculate()
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

    /** [style] of null silences that prayer. */
    fun setAlert(slot: PrayerSlot, style: AlertStyle?) =
        viewModelScope.launch { settingsRepository.setAlert(slot, style) }

    fun setAdjustment(slot: PrayerSlot, minutes: Int) =
        viewModelScope.launch { settingsRepository.setAdjustment(slot, minutes) }

    fun setHijriOffsetDays(days: Int) =
        viewModelScope.launch { settingsRepository.setHijriOffsetDays(days) }

    fun resetAdjustments() = viewModelScope.launch { settingsRepository.clearAdjustments() }

    fun setOngoingBadge(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setOngoingBadge(enabled) }

    fun setAlarmRespectsSilent(respects: Boolean) =
        viewModelScope.launch { settingsRepository.setAlarmRespectsSilent(respects) }

    fun dismissExactAlarmNotice() =
        viewModelScope.launch { settingsRepository.dismissExactAlarmNotice() }

    fun dismissMethodNotice() =
        viewModelScope.launch { settingsRepository.dismissMethodNotice() }

    fun setAlarmSound(uri: String) =
        viewModelScope.launch { settingsRepository.setAlarmSound(uri) }

    fun setThemeChoice(choice: ThemeChoice) =
        viewModelScope.launch { settingsRepository.setThemeChoice(choice) }

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
        val coordinates = settings.coordinates ?: run {
            _state.update { it.copy(exportEvent = ExportEvent.Failed) }
            return
        }
        viewModelScope.launch {
            val event = runCatching {
                exporter.export(
                    range = range,
                    coordinates = coordinates,
                    cityName = settings.cityName,
                    prefs = settings.calculationPrefs,
                )
            }.fold(
                onSuccess = { outcome ->
                    when (outcome) {
                        is PrayerPdfExporter.Outcome.SavedToDownloads ->
                            ExportEvent.Saved(outcome.fileName)

                        is PrayerPdfExporter.Outcome.ReadyToShare ->
                            ExportEvent.Share(outcome.uri)
                    }
                },
                onFailure = { ExportEvent.Failed },
            )
            _state.update { it.copy(exportEvent = event) }
        }
    }

    fun consumeExportEvent() = _state.update { it.copy(exportEvent = null) }

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
                        // UNAVAILABLE carries a placeholder heading of zero. Passing that
                        // through would draw a needle and say "turn right 143 degrees"
                        // off a reading that does not exist; null makes the Qibla screen
                        // fall back to the written bearing instead.
                        compassHeading = reading.trueHeading
                            .takeIf { reading.accuracy != CompassAccuracy.UNAVAILABLE },
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
            _state.update { it.copy(today = null, next = null, current = null) }
            return
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()

        val computed = withContext(Dispatchers.Default) {
            val today = PrayerEngine.compute(coordinates, LocalDate.now(zone), settings.calculationPrefs)
            val next: NextPrayer = PrayerEngine.nextPrayer(coordinates, settings.calculationPrefs, now, zone)
            val current = PrayerEngine.currentPrayer(coordinates, settings.calculationPrefs, now, zone)
            Triple(today, next, current)
        }

        _state.update {
            it.copy(
                today = computed.first,
                next = computed.second,
                current = computed.third,
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
