package com.sajdatime.app

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajdatime.core.AppLocale
import com.sajdatime.core.BellRules
import com.sajdatime.core.PermissionDoor
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.Permission
import com.sajdatime.app.data.SetupStep
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.ExportEvent
import com.sajdatime.app.ui.BellActions
import com.sajdatime.app.ui.TrackerActions
import com.sajdatime.app.ui.JamaahActions
import com.sajdatime.app.ui.TrackerEvent
import com.sajdatime.app.ui.MainScaffold
import com.sajdatime.app.ui.SajdaViewModel
import com.sajdatime.app.ui.onboarding.OnboardingScreen
import com.sajdatime.app.ui.theme.SajdaTimeTheme

class MainActivity : ComponentActivity() {

    /**
     * Every screen in the app renders through this Activity, so pinning its configuration
     * to the app's own language covers the whole UI in one place. See AppLocale.kt for why
     * that is necessary at all, and for what it deliberately does not do.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    private val viewModel: SajdaViewModel by viewModels()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        // Usage counts (opt-in only): a yes or no, and only from the setup screen. This
        // launcher also serves Times and Settings later, which must not be counted as setup.
        if (!viewModel.state.value.settings.onboardingComplete) {
            viewModel.recordPermission(Permission.LOCATION, results.values.any { it })
        }
        viewModel.refreshLocation()
    }

    /** True between the end of setup and the system's answer to the notification prompt. */
    private var awaitingSetupNotificationResult = false

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // Declining only silences alerts; times still display. The answer is counted
        // (opt-in only) when it came from the end of setup, which is the only place we ask.
        if (awaitingSetupNotificationResult) {
            awaitingSetupNotificationResult = false
            viewModel.recordPermission(Permission.NOTIFICATIONS, granted)
        }
    }

    /**
     * The system ringtone picker: alarms, ringtones and any audio the user has added. Since
     * 1.5.0 Sunni users can also pick a built-in adhan in Settings (data/BuiltInAdhan.kt);
     * this is "a sound from your phone", for everyone.
     *
     * Only an OK changes anything. Backing out used to arrive here with no data and reset the
     * sound to the phone's default, which would also have thrown away a chosen built-in adhan.
     */
    private val alarmSoundPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = result.data
            ?.getParcelableExtraCompat<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        // Any app can answer the picker intent. Only a content:// or android.resource:// sound is
        // kept: a file:// one makes posting the alarm throw, which post() catches, so the prayer alert
        // would silently never appear (independent security review, 17 Oct 2026). Null is the
        // phone's default, as before.
        if (uri != null && !isAcceptedAlarmSound(uri.scheme)) return@registerForActivityResult
        viewModel.setAlarmSound(storedSound(uri, RingtoneManager.TYPE_ALARM))
    }

    /** The same, for the notification sound (18 Oct 2026). A launcher of its own, so no state says which. */
    private val notificationSoundPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val uri = result.data
            ?.getParcelableExtraCompat<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        if (uri != null && !isAcceptedAlarmSound(uri.scheme)) return@registerForActivityResult
        viewModel.setNotificationSound(storedSound(uri, RingtoneManager.TYPE_NOTIFICATION))
    }

    /**
     * The prayer record's save and load (1.6.0): the system's own save and open dialogs. They hand back
     * a content:// address with access granted for this one write or read, so the app needs no storage
     * permission, keeps nothing afterwards and sends nothing anywhere.
     */
    private val trackerSave = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(viewModel::saveTrackerTo)
    }

    /** Several types, because some pickers label a .json file as plain or unknown; the codec checks the content. */
    private val trackerLoad = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::loadTrackerFrom)
    }

    private val trackerActions = object : TrackerActions {
        override fun setEnabled(on: Boolean) { viewModel.setTrackerEnabled(on) }
        override fun setShowStreak(on: Boolean) { viewModel.setTrackerShowStreak(on) }
        override fun tick(date: java.time.LocalDate, slot: com.sajdatime.core.PrayerSlot, ticked: Boolean) {
            viewModel.tick(date, slot, ticked)
        }
        override fun pause(fromYesterday: Boolean) { viewModel.pauseTracker(fromYesterday) }
        override fun resume() { viewModel.resumeTracker() }
        override fun adjustFastsToMakeUp(delta: Int) { viewModel.adjustFastsToMakeUp(delta) }
        override fun save() = runCatching { trackerSave.launch(viewModel.trackerFileName()) }
            .getOrElse { toast(getString(R.string.tracker_save_failed)) }
        override fun load() = runCatching {
            trackerLoad.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
        }.getOrElse { toast(getString(R.string.tracker_load_failed_format)) }
        override fun delete() { viewModel.deleteTracker() }
    }

    /**
     * Whether the phone lets SajdaTime post at all: Android 13's permission and the older "turn off for this
     * app" switch (not channels). Read again in [onResume], so the bells turn green the moment the user
     * comes back from the phone's settings.
     */
    private var notificationsBlocked by mutableStateOf(false)

    private fun readNotificationsBlocked() = !NotificationManagerCompat.from(this).areNotificationsEnabled()

    /**
     * Chosen before anything is launched, so a second "Don't allow" is never answered by Settings
     * (BellRules.permissionDoor). A result that is a denial does nothing, and nothing here is counted
     * as setup, so awaitingSetupNotificationResult stays false.
     */
    private fun openNotificationDoor() {
        val systemCanAsk = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.canPost(this)
        when (
            BellRules.permissionDoor(
                blocked = readNotificationsBlocked(),
                systemCanAsk = systemCanAsk,
                rationale = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS),
                askedBefore = viewModel.state.value.settings.notificationPermissionAsked,
            )
        ) {
            PermissionDoor.ASK -> {
                viewModel.setNotificationPermissionAsked()
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            PermissionDoor.OPEN_SETTINGS -> Notifications.openAppNotificationSettings(this)
            PermissionDoor.NONE -> Unit
        }
    }

    private val bellActions = object : BellActions {
        override suspend fun off(slot: com.sajdatime.core.PrayerSlot) = viewModel.bellOff(slot)
        override fun on(slot: com.sajdatime.core.PrayerSlot) {
            viewModel.bellOn(slot)
            if (readNotificationsBlocked()) {
                // Asking again after a refusal brings the notice back (it was closed on purpose before).
                viewModel.setNotificationsNoticeDismissed(false)
                openNotificationDoor()
            }
        }
        override fun undo(slot: com.sajdatime.core.PrayerSlot) { viewModel.bellOn(slot) }
        override fun allow() = openNotificationDoor()
        override fun dismissNotice() { viewModel.setNotificationsNoticeDismissed(true) }
    }

    private val jamaahActions = object : JamaahActions {
        override fun setRule(slot: com.sajdatime.core.PrayerSlot, rule: com.sajdatime.core.JamaahRule?) {
            viewModel.setJamaahRule(slot, rule)
        }
        override fun setJumuah(time: java.time.LocalTime?) { viewModel.setJumuah(time) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The window's direction must come from the app's language (AppLocale.wrap above), not be
        // left to the platform. Below Android 13 a language picked in the app restarts this screen
        // with recreate(), and on the Android 12 emulator (19 Oct 2026) the new screen kept the old
        // one's direction: Urdu words laid out left to right after choosing Urdu, and English words
        // right to left after choosing "Phone language" from Urdu, which CLAUDE.md forbids. A fresh
        // launch was always right. HANDOVER §15 lesson 152.
        window.decorView.layoutDirection = resources.configuration.layoutDirection
        // Here as well as in the ViewModel: a language change recreates this screen but keeps the
        // ViewModel, so this is where the channel names in the phone's settings catch up with it.
        Notifications.ensureChannels(this)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            // Read outside the theme, not inside it: the choice decides which scheme the
            // theme builds, so it cannot come from within the theme's own content.
            SajdaTimeTheme(choice = state.settings.themeChoice) {

                // The export result is a user-visible side effect, so it fires once per
                // attempt and is then consumed. Failures are reported too: a tap that
                // silently does nothing is worse than an error.
                LaunchedEffect(state.exportEvent) {
                    when (val event = state.exportEvent) {
                        null -> Unit
                        is ExportEvent.Share -> sharePdf(event.uri)
                        is ExportEvent.Saved -> toast(getString(R.string.export_saved, event.fileName))
                        ExportEvent.Failed -> toast(getString(R.string.export_failed))
                    }
                    if (state.exportEvent != null) viewModel.consumeExportEvent()
                }

                LaunchedEffect(state.trackerEvent) {
                    when (val event = state.trackerEvent) {
                        null -> Unit
                        is TrackerEvent.Saved -> toast(getString(R.string.tracker_saved, event.fileName))
                        is TrackerEvent.Loaded -> toast(
                            listOfNotNull(
                                resources.getQuantityString(R.plurals.tracker_loaded, event.days, event.days)
                                    .takeIf { event.days > 0 || event.fasts == null },
                                event.fasts?.let { resources.getQuantityString(R.plurals.tracker_fasts_line, it, it) },
                            ).joinToString(" "),
                        )
                        TrackerEvent.SaveFailed -> toast(getString(R.string.tracker_save_failed))
                        is TrackerEvent.LoadFailed -> toast(
                            getString(if (event.tooNew) R.string.tracker_load_failed_newer else R.string.tracker_load_failed_format),
                        )
                    }
                    if (state.trackerEvent != null) viewModel.consumeTrackerEvent()
                }

                PreviewBuildFrame {
                    when {
                        state.loading -> Unit

                        !state.settings.onboardingComplete -> OnboardingScreen(
                            state = state,
                            onRequestLocationPermission = ::requestLocation,
                            onSearchCity = viewModel::searchCity,
                            onUseDefaultLocation = viewModel::useDefaultLocation,
                            onSelectSect = viewModel::setSect,
                            onSelectMadhab = viewModel::setMadhab,
                            onSelectMethod = viewModel::setMethod,
                            onFinish = {
                                // Counted only for users who opted in. Exact alarms have no
                                // result callback, so this is the *state* at the end of setup.
                                viewModel.recordSetupStep(SetupStep.FINISH)
                                viewModel.recordPermission(
                                    Permission.EXACT_ALARM,
                                    PrayerAlarmScheduler.canScheduleExact(this@MainActivity),
                                )
                                requestNotificationsIfNeeded()
                                viewModel.completeOnboarding()
                            },
                            onSetAnalytics = viewModel::setAnalyticsEnabled,
                            onSetupStep = viewModel::recordSetupStep,
                        )

                        else -> {
                            MainScaffold(
                                state = state,
                                onExport = viewModel::exportPdf,
                                onSetSect = viewModel::setSect,
                                onSetMadhab = viewModel::setMadhab,
                                onSetMethod = viewModel::setMethod,
                                onSetAlert = viewModel::setAlert,
                                onSetAdjustment = viewModel::setAdjustment,
                                onSetHijriOffset = viewModel::setHijriOffsetDays,
                                onResetAdjustments = viewModel::resetAdjustments,
                                onSetOngoingBadge = viewModel::setOngoingBadge,
                                onSetAlarmOnApproximateDays = viewModel::setAlarmOnApproximateDays,
                                onPickAlarmSound = {
                                    openAlarmSoundPicker(state.settings.alarmSoundUri, state.settings.phoneAlarmSoundUri)
                                },
                                onPickBuiltInAdhan = viewModel::setBuiltInAdhan,
                                onPickNotificationSound = {
                                    openNotificationSoundPicker(
                                        state.settings.notificationSoundUri,
                                        state.settings.phoneNotificationSoundUri,
                                    )
                                },
                                onPickBuiltInNotificationAdhan = viewModel::setBuiltInNotificationAdhan,
                                onSetAlarmRespectsSilent = viewModel::setAlarmRespectsSilent,
                                onRefreshLocation = ::requestLocation,
                                onSearchCity = viewModel::searchCity,
                                onQiblaVisible = viewModel::setQiblaVisible,
                                onSetThemeChoice = viewModel::setThemeChoice,
                                onDismissExactAlarmNotice = viewModel::dismissExactAlarmNotice,
                                onDismissMethodNotice = viewModel::dismissMethodNotice,
                                onDismissRamadanNotice = viewModel::dismissRamadanNotice,
                                onDismissEid = viewModel::dismissEidGreeting,
                                onSetRamadanCard = viewModel::setRamadanCardEnabled,
                                onSetSuhoorMinutes = viewModel::setSuhoorReminderMinutes,
                                onSetSuhoorStyle = viewModel::setSuhoorReminderStyle,
                                onSetSuhoorAdhan = viewModel::setSuhoorUsesAdhan,
                                onSetPrayerReminderMinutes = viewModel::setPrayerReminderMinutes,
                                onSetPrayerReminder = viewModel::setPrayerReminder,
                                onSetAnalytics = viewModel::setAnalyticsEnabled,
                                onScreenViewed = viewModel::recordScreen,
                                tracker = trackerActions,
                                jamaah = jamaahActions,
                                bell = bellActions,
                                notificationsBlocked = notificationsBlocked,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        notificationsBlocked = readNotificationsBlocked()
        // Coordinates may be stale after travel, and alarms may have been cleared by the
        // system, so a foreground visit always refreshes both.
        if (viewModel.state.value.settings.coordinates != null) viewModel.refreshLocation()
        // And the widgets: a language just chosen has reached the resources by now, and a widget
        // drawn while the app was away is brought up to date at the same moment.
        viewModel.refreshWidgets()
    }

    private fun requestLocation() {
        locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            awaitingSetupNotificationResult = true
            // Counts as asked, so a later bell tap after a refusal here goes to the phone's settings rather
            // than launching a system ask some phones no longer show (Opus review, 24 Oct 2026). A deliberate trade:
            // someone who backs out of the setup prompt without choosing also gets the phone's settings on the
            // first bell tap, where they can still allow. Better than a tap that shows nothing; do not undo.
            viewModel.setNotificationPermissionAsked()
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAlarmSoundPicker(current: String, phoneSound: String) =
        runCatching {
            alarmSoundPicker.launch(soundPickerIntent(current, phoneSound, R.string.settings_alarm_sound, RingtoneManager.TYPE_ALARM))
        }

    private fun openNotificationSoundPicker(current: String, phoneSound: String) =
        runCatching {
            notificationSoundPicker.launch(
                soundPickerIntent(current, phoneSound, R.string.settings_notification_sound, RingtoneManager.TYPE_NOTIFICATION),
            )
        }

    /**
     * The phone's own sound list. TYPE_ALL, so alarms, ringtones and notification tones are all offered
     * whichever is being chosen; [defaultType] only decides where it opens when nothing is chosen yet.
     */
    private fun soundPickerIntent(current: String, phoneSound: String, title: Int, defaultType: Int): Intent {
        // A built-in adhan is not in the phone's list, so the picker opens on the phone sound it replaced.
        val existing = if (BuiltInAdhan.of(current) == null) current else phoneSound
        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(title))
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            // The picker's "Default" row. Without this, with TYPE_ALL, it meant the default *ringtone*,
            // so a person going back to "default" got their ringtone on every prayer (review, 18 Oct 2026).
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(defaultType))
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                existing.takeIf { it.isNotBlank() }?.let(Uri::parse)
                    ?: RingtoneManager.getDefaultUri(defaultType),
            )
        }
    }

    /**
     * What is saved for a pick: "" when it is the phone's default for [defaultType], which keeps a
     * notification on its original channel and follows the phone if its default tone changes later.
     */
    private fun storedSound(uri: Uri?, defaultType: Int): String =
        uri?.takeIf { it != RingtoneManager.getDefaultUri(defaultType) }?.toString().orEmpty()

    private fun toast(message: String) =
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()

    private fun sharePdf(uri: Uri) {
        val share = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            // ClipData is what actually grants the share sheet permission to read the
            // file; without it the chooser cannot render a preview of the timetable.
            .apply { clipData = ClipData.newRawUri(getString(R.string.action_save_timetable), uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, getString(R.string.action_save_timetable)))
    }
}

@Suppress("DEPRECATION")
private inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name)
    }

/**
 * Wraps the whole UI in a build-type banner — or, in every build a user can install, in
 * nothing at all.
 *
 * `preview_build_notice` is empty in `main` and overridden only in `src/rtl/`, the
 * developer-only build that forces the app right-to-left with its English words left in
 * place (see that file). Every time the owner has seen that build on an emulator he has,
 * correctly, reported the app as broken — four times by 15 Aug 2026 — because English laid
 * out right-to-left *is* broken and nothing on the screen said "this is a layout test".
 * Now something does, on every screen, in a colour nothing else in the app uses for a
 * band. The check is a resource, not `BuildConfig`, for the same reason `app_language_tag`
 * is: it is one string, it needs no build feature, and the shipping build pays a single
 * `isBlank()` for it.
 *
 * The status-bar inset is taken by the banner and consumed before the content, so the
 * scaffold underneath does not add it a second time. That branch is only compiled into a
 * layout when the notice is non-blank; the shipping build lays out exactly as before.
 */
@Composable
private fun PreviewBuildFrame(content: @Composable () -> Unit) {
    val notice = stringResource(R.string.preview_build_notice)
    if (notice.isBlank()) {
        content()
        return
    }
    Column(Modifier.fillMaxSize()) {
        Text(
            text = notice,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Box(
            Modifier
                .weight(1f)
                .consumeWindowInsets(WindowInsets.statusBars),
        ) { content() }
    }
}

/** The sound address schemes the alarm channel can safely play: a media or settings provider, or an app's raw resource. */
internal fun isAcceptedAlarmSound(scheme: String?): Boolean =
    scheme == "content" || scheme == "android.resource"
