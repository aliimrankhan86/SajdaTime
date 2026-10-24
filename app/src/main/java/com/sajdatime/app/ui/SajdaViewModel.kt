package com.sajdatime.app.ui

import com.sajdatime.app.data.placeName
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.EidDay
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.Madhab
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PlaceLabelPolicy
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.Sect
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.CityLookup
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.data.CompassRepository
import com.sajdatime.app.data.LocationRepository
import com.sajdatime.app.data.FirebaseUsageSink
import com.sajdatime.app.data.Permission
import com.sajdatime.app.data.Screen
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.data.SetupStep
import com.sajdatime.app.data.TrackerData
import com.sajdatime.app.data.TrackerFileCodec
import com.sajdatime.app.data.TrackerRepository
import com.sajdatime.core.TickState
import com.sajdatime.core.JamaahPlan
import com.sajdatime.core.JamaahResult
import com.sajdatime.core.JamaahRule
import com.sajdatime.core.DayKind
import com.sajdatime.core.TrackerLogic
import com.sajdatime.app.data.UsageCounts
import com.sajdatime.app.R
import com.sajdatime.app.data.WatchSync
import com.sajdatime.app.notify.DailyRescheduleWorker
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.OngoingBadge
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.widget.WidgetUpdater
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
    /** City search failed and the phone has no network: "not found" would be untrue. */
    OFFLINE,
}

/**
 * The result of an export, delivered to the UI once and then cleared.
 *
 * [Failed] exists because the previous version swallowed every export error: the user
 * tapped "Save timetable as PDF", chose a range, and absolutely nothing happened.
 */
/**
 * What happened to a save or load of the prayer record, shown once as a message and then cleared,
 * the same way as [ExportEvent].
 */
sealed interface TrackerEvent {
    data class Saved(val fileName: String) : TrackerEvent
    data class Loaded(val days: Int) : TrackerEvent
    data object SaveFailed : TrackerEvent
    data class LoadFailed(val tooNew: Boolean) : TrackerEvent
}

/**
 * The prayer record as the Times screen and the history sheet draw it. Present only while the
 * tracker is on, so nothing about it reaches the Times screen otherwise.
 */
data class TrackerState(
    val data: TrackerData,
    val today: LocalDate,
    val streak: Int,
    /**
     * The first day the record covers: days before it are "before tracking began" (no ring, not
     * selectable). Worked out once here, not in a getter: up to 49 rings ask for it on every
     * recomposition, and the screen recomposes each second for the clock (review, 23 Oct 2026).
     */
    val started: LocalDate = today,
) {
    val paused: Boolean get() = TrackerLogic.isPaused(today, data.pauses)

    fun state(date: LocalDate, slot: PrayerSlot, due: Boolean): TickState =
        TrackerLogic.tickState(data.records[date], slot, due, TrackerLogic.isPaused(date, data.pauses))

    fun isPaused(date: LocalDate): Boolean = TrackerLogic.isPaused(date, data.pauses)

    /** What a day's ring shows in the week card and the grid (a rule, so it lives in :core). */
    fun dayKind(date: LocalDate): DayKind = TrackerLogic.dayKind(date, today, started, data.pauses)

    companion object {
        fun build(data: TrackerData, today: LocalDate) = TrackerState(
            data, today, TrackerLogic.streak(data.records, data.pauses, today),
            TrackerLogic.started(data.startedOn, data.records, data.pauses, today),
        )
    }
}

/**
 * What the tracker's controls do, as one parameter rather than eight threaded through every screen.
 * MainActivity binds it to this ViewModel and to the system's save and open dialogs.
 */
interface TrackerActions {
    fun setEnabled(on: Boolean)
    fun setShowStreak(on: Boolean)
    fun tick(date: LocalDate, slot: PrayerSlot, ticked: Boolean)
    fun pause(fromYesterday: Boolean)
    fun resume()
    fun save()
    fun load()
    fun delete()
}

/**
 * What the bell on a prayer row and the "Allow notifications" notice do (1.6.0 step 3). MainActivity binds
 * it to this ViewModel and to the system's permission ask.
 */
interface BellActions {
    /** Turns [slot] off in one edit; true if a reminder that would have fired went off with it. */
    suspend fun off(slot: PrayerSlot): Boolean

    /** The user's own tap: on, then, if the phone blocks notifications, the permission door. */
    fun on(slot: PrayerSlot)

    /** Undo: on again, never the door. */
    fun undo(slot: PrayerSlot)

    /** The notice's Allow: the same door the bell opens. */
    fun allow()
    fun dismissNotice()
}

/** What the Jama'ah times screen changes. Bound to this ViewModel by MainActivity. */
interface JamaahActions {
    /** null clears that prayer. */
    fun setRule(slot: PrayerSlot, rule: JamaahRule?)
    fun setJumuah(time: java.time.LocalTime?)
}

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
    /**
     * Present only while the user's own calendar is inside Ramadan or in the 3 days before it
     * ([com.sajdatime.core.RamadanInfo.showsNotice]). Null the rest of the year, so nothing
     * about Ramadan reaches the Times screen then.
     */
    val ramadan: RamadanState? = null,
    /**
     * The Eid whose day it is on the user's own calendar, or null on every other day. Computed
     * from the same date and the same Islamic date shift as the Home header, so the greeting and
     * the header cannot disagree.
     */
    val eid: EidDay? = null,
    /**
     * Today's jama'ah for each prayer that has one set, already checked against its window
     * ([JamaahPlan]). Empty for everyone who has set none.
     */
    val jamaah: Map<PrayerSlot, JamaahResult> = emptyMap(),
    /** The prayer record, while "My prayers" is on. See [TrackerState]. */
    val tracker: TrackerState? = null,
    /** True when a record exists, even with the tracker off, so Settings can always offer Delete. */
    val trackerHasData: Boolean = false,
    /**
     * The record has been read once. Until then My prayers draws nothing: drawn from the empty
     * record first, every ring would animate up when the real one arrived (no entrance animation).
     */
    val trackerLoaded: Boolean = false,
    val trackerEvent: TrackerEvent? = null,
)

class SajdaViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    private val locationRepository = LocationRepository(application)
    private val cityLookup = CityLookup(application)
    private val compassRepository = CompassRepository(application)
    private val trackerRepository = TrackerRepository(application)

    /**
     * Optional usage counts. [UsageCounts.apply] is the only thing that ever switches
     * Firebase on, and only for a user who opted in. `analytics_allowed` is false in the
     * debug, rtl and sideload builds, so only the shipped release can ever send anything.
     */
    private val usage = UsageCounts(
        sink = FirebaseUsageSink(application),
        allowed = application.resources.getBoolean(R.bool.analytics_allowed),
    )

    /** Sensor collection runs only while the Qibla screen is on-screen. */
    private var compassJob: Job? = null

    /**
     * Declared before `init`: Kotlin runs property initialisers and `init` blocks in file order, and the
     * settings collector in `init` can deliver at once (Main.immediate), calling publishTracker() while a
     * field declared further down is still null. That crashed cold starts on 23 Oct 2026 (HANDOVER §15, 179).
     */
    private var trackerData = TrackerData()

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            var first = true
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings, loading = false) }
                publishTracker()
                // Re-applies the saved choice on every launch. A user who never opted in
                // causes no Firebase call here at all.
                usage.apply(settings.analyticsEnabled)
                recalculate()
                // MainActivity.onResume refreshes the location on every foreground visit —
                // except the first. On a cold start onCreate, onStart and onResume run in
                // one main-thread pass, so this collector has not delivered yet and onResume
                // sees `coordinates == null` and skips. Found on 15 Aug 2026 while verifying
                // the place label: a cold-opened app kept the stored city and only refreshed
                // after being backgrounded once. So the first load does the refresh itself.
                if (first && settings.coordinates != null) refreshLocation()
                first = false
            }
        }
        viewModelScope.launch {
            // The record is read whether or not the tracker is on, so Settings knows there is
            // something to delete; it is only handed to the Times screen while the tracker is on.
            trackerRepository.data.collect { data ->
                trackerData = data
                _state.update { it.copy(trackerLoaded = true) }
                publishTracker()
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
                // My prayers keeps its own day, because with no saved place there is no `today` above
                // and nothing else would roll it over: ticks after midnight went to yesterday (review).
                else if (snapshot.tracker?.let { it.today != LocalDate.now(ZoneId.systemDefault()) } == true) publishTracker()
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
            // The town name is a label, not an input to any calculation, and asking for it
            // can send the approximate position to the phone's geocoding provider (Google,
            // on most phones). So ask only when there is no label yet or the phone has moved
            // far enough that the old one could be wrong, not on every foreground visit.
            // The coordinates themselves are always stored fresh. See PlaceLabelPolicy.
            val before = _state.value.settings
            val city = if (PlaceLabelPolicy.needsNewLabel(before.coordinates, before.cityName, coordinates)) {
                locationRepository.cityName(coordinates)
            } else {
                before.cityName
            }
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
                // A null from the lookup means "not found" or "no network", and the second
                // told a user with no data that Lahore does not exist (research F10).
                val problem = if (isOnline()) LocationProblem.CITY_NOT_FOUND else LocationProblem.OFFLINE
                _state.update { it.copy(resolvingLocation = false, problem = problem) }
                return@launch
            }
            settingsRepository.setLocation(result.coordinates, result.city)
            _state.update { it.copy(resolvingLocation = false, problem = null) }
        }
    }

    /**
     * ACCESS_NETWORK_STATE comes in through Firebase's manifest, not ours: WidgetManifestTest
     * pins our own permission list, and the merged one already has it. If it ever goes, this
     * answers "online" and the message falls back to "not found", as before.
     */
    private fun isOnline(): Boolean = runCatching {
        val cm = getApplication<Application>().getSystemService(android.net.ConnectivityManager::class.java)
        @Suppress("DEPRECATION") // activeNetworkInfo is the one call that works back to API 24.
        cm?.activeNetworkInfo?.isConnected == true
    }.getOrDefault(true)

    // --- settings --------------------------------------------------------------------

    /**
     * Applies the choice **before** saving it, so collection is already on by the time the
     * first setup or screen event is logged right after the user taps Yes. The settings
     * flow then reports the same value and [UsageCounts.apply] does nothing.
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        usage.apply(enabled)
        // The setup location answer came before the consent step (moved there 17 Oct 2026), so it
        // was held and goes now, once, after a yes. ponytail: held in memory only, so a process
        // death between the two loses that one count; nothing else depends on it.
        if (enabled) heldSetupLocation?.let { usage.permission(Permission.LOCATION, it) }
        heldSetupLocation = null
        viewModelScope.launch { settingsRepository.setAnalyticsEnabled(enabled) }
    }

    // Each of these is a no-op unless the user has opted in. See UsageCounts.
    fun recordScreen(screen: Screen) = usage.screen(screen)

    fun recordSetupStep(step: SetupStep) = usage.setupStep(step)

    fun recordPermission(permission: Permission, granted: Boolean) {
        if (permission == Permission.LOCATION && !_state.value.settings.analyticsEnabled) heldSetupLocation = granted
        usage.permission(permission, granted)
    }

    private var heldSetupLocation: Boolean? = null

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


    fun setAlarmOnApproximateDays(allowed: Boolean) =
        viewModelScope.launch { settingsRepository.setAlarmOnApproximateDays(allowed) }

    fun dismissExactAlarmNotice() =
        viewModelScope.launch { settingsRepository.dismissExactAlarmNotice() }

    fun dismissMethodNotice() =
        viewModelScope.launch { settingsRepository.dismissMethodNotice() }

    suspend fun bellOff(slot: PrayerSlot): Boolean = settingsRepository.bellOff(slot)

    fun bellOn(slot: PrayerSlot) = viewModelScope.launch { settingsRepository.bellOn(slot) }

    fun setNotificationsNoticeDismissed(dismissed: Boolean) =
        viewModelScope.launch { settingsRepository.setNotificationsNoticeDismissed(dismissed) }

    fun setNotificationPermissionAsked() =
        viewModelScope.launch { settingsRepository.setNotificationPermissionAsked(true) }

    fun setRamadanCardEnabled(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setRamadanCardEnabled(enabled) }

    fun dismissRamadanNotice(hijriYear: Int) =
        viewModelScope.launch { settingsRepository.dismissRamadanNotice(hijriYear) }

    fun setSuhoorReminderMinutes(minutes: Int) =
        viewModelScope.launch { settingsRepository.setSuhoorReminderMinutes(minutes) }

    fun setSuhoorReminderStyle(style: AlertStyle) =
        viewModelScope.launch { settingsRepository.setSuhoorReminderStyle(style) }

    fun setSuhoorUsesAdhan(adhan: Boolean) =
        viewModelScope.launch { settingsRepository.setSuhoorUsesAdhan(adhan) }

    fun setPrayerReminderMinutes(minutes: Int) =
        viewModelScope.launch { settingsRepository.setPrayerReminderMinutes(minutes) }

    fun setPrayerReminder(slot: PrayerSlot, on: Boolean) =
        viewModelScope.launch { settingsRepository.setPrayerReminder(slot, on) }

    fun dismissEidGreeting(code: Int) =
        viewModelScope.launch { settingsRepository.dismissEidGreeting(code) }

    fun setAlarmRespectsSilent(respects: Boolean) =
        viewModelScope.launch { settingsRepository.setAlarmRespectsSilent(respects) }

    fun setAlarmSound(uri: String) =
        viewModelScope.launch { settingsRepository.setAlarmSound(uri) }

    fun setBuiltInAdhan(adhan: BuiltInAdhan) =
        viewModelScope.launch { settingsRepository.setBuiltInAdhan(adhan) }

    fun setNotificationSound(uri: String) =
        viewModelScope.launch { settingsRepository.setNotificationSound(uri) }

    fun setBuiltInNotificationAdhan(adhan: BuiltInAdhan) =
        viewModelScope.launch { settingsRepository.setBuiltInNotificationAdhan(adhan) }

    fun setJamaahRule(slot: PrayerSlot, rule: JamaahRule?) =
        viewModelScope.launch { settingsRepository.setJamaahRule(slot, rule) }

    fun setJumuah(time: java.time.LocalTime?) =
        viewModelScope.launch { settingsRepository.setJumuah(time) }

    fun setJamaahReminderMinutes(minutes: Int) =
        viewModelScope.launch { settingsRepository.setJamaahReminderMinutes(minutes) }

    fun setFastReminder(kind: com.sajdatime.core.FastKind, on: Boolean) =
        viewModelScope.launch { settingsRepository.setFastReminder(kind, on) }

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
                // A new exporter each time, not one held by this ViewModel: it pins its language when
                // it is made, and the ViewModel outlives a language change (the screen restarts, the
                // ViewModel does not). Held once, an Arabic PDF came out in Urdu after switching from
                // Urdu (Android 12 emulator, 19 Oct 2026).
                PrayerPdfExporter(getApplication()).export(
                    range = range,
                    coordinates = coordinates,
                    cityName = settings.placeName(com.sajdatime.core.AppLocale.wrap(getApplication())),
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

    // --- my prayers (the tracker) ----------------------------------------------------

    /** Rebuilds the tracker state from the record in hand. Called when the record, the switch or the day changes. */
    private fun publishTracker() {
        val today = LocalDate.now(ZoneId.systemDefault())
        _state.update {
            it.copy(
                tracker = if (it.settings.trackerEnabled) TrackerState.build(trackerData, today) else null,
                trackerHasData = !trackerData.isEmpty,
            )
        }
    }

    fun setTrackerEnabled(enabled: Boolean) =
        viewModelScope.launch {
            if (enabled) trackerRepository.markStarted(LocalDate.now(ZoneId.systemDefault()))
            else trackerRepository.clearStartIfEmpty()
            settingsRepository.setTrackerEnabled(enabled)
        }

    fun setTrackerShowStreak(show: Boolean) =
        viewModelScope.launch { settingsRepository.setTrackerShowStreak(show) }

    fun tick(date: LocalDate, slot: PrayerSlot, ticked: Boolean) =
        viewModelScope.launch { trackerRepository.setTicked(date, slot, ticked) }

    /** Today, or yesterday for someone who notices a day late. Nothing further back (Ali, 16 Oct 2026). */
    fun pauseTracker(fromYesterday: Boolean) = viewModelScope.launch {
        val today = LocalDate.now(ZoneId.systemDefault())
        trackerRepository.pause(if (fromYesterday) today.minusDays(1) else today)
    }

    fun resumeTracker() =
        viewModelScope.launch { trackerRepository.resume(LocalDate.now(ZoneId.systemDefault())) }

    fun deleteTracker() = viewModelScope.launch { trackerRepository.deleteAll() }

    fun trackerFileName(): String = TrackerFileCodec.fileName(LocalDate.now(ZoneId.systemDefault()))

    /** Writes the record to the file the person chose in the system's save dialog. */
    fun saveTrackerTo(uri: Uri) = viewModelScope.launch {
        val app = getApplication<Application>()
        val event = runCatching {
            withContext(Dispatchers.IO) {
                val text = TrackerFileCodec.encode(trackerRepository.current(), LocalDate.now(ZoneId.systemDefault()))
                app.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(text.toByteArray()) }
            }
            TrackerEvent.Saved(uri.lastPathSegment?.substringAfterLast('/') ?: trackerFileName())
        }.getOrDefault(TrackerEvent.SaveFailed)
        _state.update { it.copy(trackerEvent = event) }
    }

    /** Reads a saved file and adds it to the record. Never removes anything already here. */
    fun loadTrackerFrom(uri: Uri) = viewModelScope.launch {
        val app = getApplication<Application>()
        val event = runCatching {
            val text = withContext(Dispatchers.IO) {
                app.contentResolver.openInputStream(uri)!!.use { String(it.readNBytesCompat(MAX_TRACKER_FILE_BYTES)) }
            }
            val result = trackerRepository.merge(TrackerFileCodec.decode(text))
            TrackerEvent.Loaded(result.daysAdded + result.daysUpdated)
        }.getOrElse { e -> TrackerEvent.LoadFailed(tooNew = (e as? TrackerFileCodec.Unreadable)?.tooNew == true) }
        _state.update { it.copy(trackerEvent = event) }
    }

    fun consumeTrackerEvent() = _state.update { it.copy(trackerEvent = null) }

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
            _state.update { it.copy(today = null, next = null, current = null, ramadan = null, eid = null, jamaah = emptyMap()) }
            // The "open SajdaTime to set a location" widget still follows a Theme change (19 Oct 2026).
            withContext(Dispatchers.Default) { WidgetUpdater.refreshAll(getApplication<Application>(), settings) }
            return
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()

        val computed = withContext(Dispatchers.Default) {
            val today = PrayerEngine.compute(coordinates, LocalDate.now(zone), settings.calculationPrefs)
            val next: NextPrayer = PrayerEngine.nextPrayer(coordinates, settings.calculationPrefs, now, zone)
            val current = PrayerEngine.currentPrayer(coordinates, settings.calculationPrefs, now, zone)
            // Built from the same `today` the list shows, so the card cannot disagree with it.
            // Recomputed on every trigger that already exists: Fajr and Maghrib are prayers, so
            // the phase changes exactly when `next` rolls over, and the date change tick covers
            // midnight. No new timer.
            val ramadan = RamadanState.build(coordinates, settings.calculationPrefs, today)
            val eid = HijriCalendar.eidOn(today.date, settings.calculationPrefs.hijriOffset)
            val jamaah = JamaahPlan.on(today, zone, settings.jamaah)
            Computed(today, next, current, ramadan, eid, jamaah)
        }

        publishTracker()
        _state.update {
            it.copy(
                today = computed.today,
                next = computed.next,
                current = computed.current,
                ramadan = computed.ramadan,
                eid = computed.eid,
                jamaah = computed.jamaah,
                now = now,
                qiblaBearing = QiblaEngine.bearingToKaaba(coordinates),
                qiblaDistanceKm = QiblaEngine.distanceToKaabaKm(coordinates),
            )
        }

        withContext(Dispatchers.Default) {
            val context = getApplication<Application>()
            PrayerAlarmScheduler.reschedule(context, settings)
            OngoingBadge.refresh(context, settings)
            WidgetUpdater.refreshAll(context, settings)
            WatchSync.publish(context, settings)
        }
    }

    /**
     * Redraws the home screen widgets from the settings in hand. Called when the app comes to the
     * foreground, which is also when a language chosen in the picker has finished taking effect (the
     * activity is recreated with the new resources first), so the widgets change language with the app.
     */
    fun refreshWidgets() {
        viewModelScope.launch(Dispatchers.Default) {
            WidgetUpdater.refreshAll(getApplication<Application>(), _state.value.settings)
        }
    }
}

/** Ten years of ticks is under 100 KB; anything far larger is not a SajdaTime file. */
private const val MAX_TRACKER_FILE_BYTES = 2 * 1024 * 1024

/** InputStream.readNBytes is API 33; this reads at most [max] bytes on every version. */
private fun java.io.InputStream.readNBytesCompat(max: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (out.size() < max) {
        val n = read(buffer, 0, minOf(buffer.size, max - out.size()))
        if (n < 0) break
        out.write(buffer, 0, n)
    }
    return out.toByteArray()
}

/** One recalculation's results, carried out of the background block together. */
private data class Computed(
    val today: DayPrayerTimes,
    val next: NextPrayer,
    val current: PrayerSlot?,
    val ramadan: RamadanState?,
    val eid: EidDay?,
    val jamaah: Map<PrayerSlot, JamaahResult>,
)
