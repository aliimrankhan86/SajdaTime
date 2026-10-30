package com.sajdatime.app.ui

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.Screen
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.components.LocalBottomBarClearance
import com.sajdatime.app.ui.components.UsageCountsConsentDialog
import com.sajdatime.app.ui.components.WholeWords
import com.sajdatime.app.ui.components.readingWidth
import com.sajdatime.app.ui.components.scaledLineHeight
import com.sajdatime.app.ui.home.HomeScreen
import com.sajdatime.app.ui.prayers.MyPrayersScreen
import com.sajdatime.app.ui.qibla.QiblaScreen
import com.sajdatime.app.ui.settings.SettingsChooser
import com.sajdatime.app.ui.settings.SettingsScreen
import com.sajdatime.app.ui.theme.GlassBarAlpha
import com.sajdatime.app.ui.theme.LocalDarkTheme
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.core.BellMessage
import com.sajdatime.core.BellRules
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.labelRes
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Top-level destinations. Four is inside the five-item guidance. */
enum class Destination(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    TIMES(R.string.nav_times, Icons.Rounded.Schedule, Icons.Outlined.Schedule),
    QIBLA(R.string.nav_qibla, Icons.Rounded.Explore, Icons.Outlined.Explore),
    PRAYERS(R.string.nav_tracker, Icons.Rounded.CheckCircle, Icons.Outlined.CheckCircle),
    SETTINGS(R.string.nav_settings, Icons.Rounded.Settings, Icons.Outlined.Settings),
}

@Composable
fun MainScaffold(
    state: UiState,
    onExport: (PrayerPdfExporter.Range) -> Unit,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetAdjustment: (PrayerSlot, Int) -> Unit,
    onSetHijriOffset: (Int) -> Unit,
    onResetAdjustments: () -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onSetAlarmOnApproximateDays: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onPickBuiltInAdhan: (BuiltInAdhan) -> Unit,
    onPickNotificationSound: () -> Unit,
    onPickBuiltInNotificationAdhan: (BuiltInAdhan) -> Unit,
    onSetAlarmRespectsSilent: (Boolean) -> Unit,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onQiblaVisible: (Boolean) -> Unit,
    onSetThemeChoice: (ThemeChoice) -> Unit,
    onDismissExactAlarmNotice: () -> Unit,
    onDismissMethodNotice: () -> Unit,
    onDismissRamadanNotice: (Int) -> Unit,
    onDismissEid: (Int) -> Unit,
    onSetRamadanCard: (Boolean) -> Unit,
    onSetSuhoorMinutes: (Int) -> Unit,
    onSetSuhoorStyle: (AlertStyle) -> Unit,
    onSetSuhoorAdhan: (Boolean) -> Unit,
    onSetPrayerReminderMinutes: (Int) -> Unit,
    onSetPrayerReminder: (PrayerSlot, Boolean) -> Unit,
    onSetAnalytics: (Boolean) -> Unit,
    onScreenViewed: (Screen) -> Unit,
    tracker: TrackerActions,
    jamaah: JamaahActions,
    bell: BellActions,
    notificationsBlocked: Boolean,
) {
    // Saveable, not remember. With a plain remember, rotating the phone rebuilt the
    // composition from scratch and dropped the user back on Times — so anyone holding
    // the Qibla compass and turning the phone sideways to line it up lost the screen
    // they were using, which is precisely when they were using it.
    var destination by rememberSaveable { mutableStateOf(Destination.TIMES) }

    // A chooser the Times screen asked Settings to open. Held here, not in either screen,
    // because it has to outlive the tab switch that delivers it. Settings nulls it once
    // opened, so it cannot reopen on rotation or on the next visit to the tab.
    var settingsRequest by rememberSaveable { mutableStateOf<SettingsChooser?>(null) }

    // People who installed before usage counts existed never saw the setup question, so ask
    // them once. Yes or No is saved either way and it never returns. Not dismissible, so a
    // stray tap outside is not recorded as an answer. Nothing is sent until they say yes.
    if (!state.loading && state.settings.onboardingComplete && !state.settings.analyticsAnswered) {
        UsageCountsConsentDialog(
            onYes = { onSetAnalytics(true) },
            onNo = { onSetAnalytics(false) },
            onDismiss = {},
        )
    }
    val openSetting: (SettingsChooser) -> Unit = {
        settingsRequest = it
        destination = Destination.SETTINGS
    }

    // Usage counts (opt-in only; a no-op otherwise): one fixed word per main tab, sent only
    // when the tab changes. Never anything about what is on the screen. My prayers has no word: opening
    // it is never counted (owner's rule for the tracker; UsageCountsTest pins the three screens).
    LaunchedEffect(destination) {
        when (destination) {
            Destination.TIMES -> Screen.TIMES
            Destination.QIBLA -> Screen.QIBLA
            Destination.PRAYERS -> null
            Destination.SETTINGS -> Screen.SETTINGS
        }?.let(onScreenViewed)
    }

    // The one snackbar host, above the glass bar, so a message can follow the user to any tab. Its only
    // message today is "Paused from today" with Undo, and Undo is resume (a pause started and ended on
    // the same day leaves nothing behind, TrackerLogic.resume).
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pausedMessage = stringResource(R.string.tracker_paused_from_today)
    val undoLabel = stringResource(R.string.action_undo)
    val pauseToday: () -> Unit = {
        tracker.pause(false)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(pausedMessage, actionLabel = undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) tracker.resume()
        }
    }

    // The bell's message (1.6.0 step 3): only when a bell goes off, with Undo, and one at a time like the
    // pause message. Which wording comes from what the edit actually removed (BellRules.message). The
    // suhoor part follows the scheduler: minutes above 0 and Ramadan today or tomorrow on the user's own
    // calendar (the evening before 1 Ramadan the reminder already fires for the next Fajr).
    val resources = LocalResources.current
    var undoneBell by remember { mutableStateOf<PrayerSlot?>(null) }
    var offMessageFor by remember { mutableStateOf<PrayerSlot?>(null) }
    val onBell: (PrayerSlot, Boolean) -> Unit = { slot, on ->
        undoneBell = null
        if (on) {
            // That prayer's own "alert off" message and its Undo are now stale; any other message stays.
            if (offMessageFor == slot) snackbar.currentSnackbarData?.dismiss()
            bell.on(slot)
        } else {
            scope.launch {
                val removed = bell.off(slot)
                val settings = state.settings
                val day = state.today?.date ?: LocalDate.now()
                val shift = settings.calculationPrefs.hijriOffset
                val suhoor = settings.suhoorReminderMinutes > 0 &&
                    (HijriCalendar.isRamadan(day, shift) || HijriCalendar.isRamadan(day.plusDays(1), shift))
                val name = resources.getString(slot.labelRes)
                val text = when (BellRules.message(slot, removed, suhoor)) {
                    BellMessage.OFF -> resources.getString(R.string.bell_off_message, name)
                    BellMessage.OFF_AND_REMINDER -> resources.getString(R.string.bell_off_reminder_message, name)
                    BellMessage.OFF_FAJR_RAMADAN -> resources.getString(R.string.bell_off_message_fajr_ramadan)
                    BellMessage.OFF_AND_REMINDER_FAJR_RAMADAN ->
                        resources.getString(R.string.bell_off_reminder_message_fajr_ramadan)
                }
                snackbar.currentSnackbarData?.dismiss()
                offMessageFor = slot
                val result = snackbar.showSnackbar(text, actionLabel = undoLabel, duration = SnackbarDuration.Short)
                if (offMessageFor == slot) offMessageFor = null
                if (result == SnackbarResult.ActionPerformed) {
                    undoneBell = slot
                    bell.undo(slot)
                }
            }
        }
    }

    // The magnetometer only runs while the Qibla tab is actually on screen.
    DisposableEffect(destination) {
        onQiblaVisible(destination == Destination.QIBLA)
        onDispose { onQiblaVisible(false) }
    }

    // Quiet Glass (17 Oct 2026): no bottomBar slot. The tab bar floats over the content in the
    // same Box, so the screens scroll softly underneath it; each screen ends with
    // BottomBarClearance() so its last row can still be scrolled clear of the bar.
    // The top inset is no longer taken here (1.6, step 5): Times draws its sky under the status bar, and the
    // other three tabs take the same inset one level down (ReadingFrame), so nothing else moves.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) { padding ->
        var barHeight by remember { mutableStateOf(0.dp) }
        val density = LocalDensity.current
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
          CompositionLocalProvider(LocalBottomBarClearance provides barHeight) {
            AnimatedContent(
                targetState = destination,
                // A crossfade, not a slide: tabs are siblings, so directional motion
                // would imply a hierarchy that is not there.
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "destination",
            ) { current ->
                when (current) {
                    Destination.TIMES -> HomeScreen(
                        state = state,
                        onExport = onExport,
                        onChangeLocation = onRefreshLocation,
                        onSearchCity = onSearchCity,
                        onDismissExactAlarmNotice = onDismissExactAlarmNotice,
                        onDismissMethodNotice = onDismissMethodNotice,
                        onDismissRamadanNotice = onDismissRamadanNotice,
                        onDismissEid = onDismissEid,
                        onSetRamadanCard = onSetRamadanCard,
                        onOpenSetting = openSetting,
                        onBell = onBell,
                        snapBell = undoneBell,
                        bellMessageShowing = snackbar.currentSnackbarData != null,
                        notificationsBlocked = notificationsBlocked,
                        onAllowNotifications = bell::allow,
                        onDismissNotificationsNotice = bell::dismissNotice,
                    )

                    Destination.QIBLA -> ReadingFrame {
                        QiblaScreen(state = state, onUseGps = onRefreshLocation, onSearchCity = onSearchCity)
                    }

                    Destination.PRAYERS -> ReadingFrame {
                        MyPrayersScreen(
                            state = state,
                            tracker = tracker,
                            onPause = pauseToday,
                            onUseGps = onRefreshLocation,
                            onSearchCity = onSearchCity,
                        )
                    }

                    Destination.SETTINGS -> ReadingFrame {
                        SettingsScreen(
                            state = state,
                            request = settingsRequest,
                            onRequestHandled = { settingsRequest = null },
                            onSetSect = onSetSect,
                            onSetMadhab = onSetMadhab,
                            onSetMethod = onSetMethod,
                            onSetAlert = onSetAlert,
                            onSetAdjustment = onSetAdjustment,
                            onSetHijriOffset = onSetHijriOffset,
                            onResetAdjustments = onResetAdjustments,
                            onSetOngoingBadge = onSetOngoingBadge,
                            onSetRamadanCard = onSetRamadanCard,
                            onSetSuhoorMinutes = onSetSuhoorMinutes,
                            onSetSuhoorStyle = onSetSuhoorStyle,
                            onSetSuhoorAdhan = onSetSuhoorAdhan,
                            onSetPrayerReminderMinutes = onSetPrayerReminderMinutes,
                            onSetPrayerReminder = onSetPrayerReminder,
                            onSetAlarmOnApproximateDays = onSetAlarmOnApproximateDays,
                            onPickAlarmSound = onPickAlarmSound,
                            onPickBuiltInAdhan = onPickBuiltInAdhan,
                            onPickNotificationSound = onPickNotificationSound,
                            onPickBuiltInNotificationAdhan = onPickBuiltInNotificationAdhan,
                            onSetAlarmRespectsSilent = onSetAlarmRespectsSilent,
                            onRefreshLocation = onRefreshLocation,
                            onSearchCity = onSearchCity,
                            onSetThemeChoice = onSetThemeChoice,
                            onSetAnalytics = onSetAnalytics,
                            tracker = tracker,
                            jamaah = jamaah,
                        )
                    }
                }
            }
          }
            GlassTabBar(
                current = destination,
                onSelect = { destination = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { barHeight = with(density) { it.height.toDp() } },
            )
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = barHeight))
        }
    }
}

/**
 * The floating tab bar: a pill that hovers over the content, translucent so what scrolls
 * underneath shows through faintly (GlassBarAlpha, contrast checked against pure black and
 * pure white behind it in ColorContrastTest).
 *
 * Always labelled: icon-only navigation is guesswork for anyone who has not used the app
 * before. The selected tab has three signals, not one: a filled icon, a tinted pill and a
 * bolder label, so it reads on a greyscale screen too. Tabs are `selectable` with Role.Tab so a
 * screen reader says "selected". A light tick of haptic feedback on change, nothing more.
 */
@Composable
@VisibleForTesting
internal fun GlassTabBar(
    current: Destination,
    onSelect: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(32.dp)
    val haptics = LocalHapticFeedback.current
    Row(
        // Top, not centred (30 Oct 2026): a label on two lines made its tab taller, and centring lifted
        // that tab's icon out of line with the other three (Bengali "আমার নামাজ" at 2.0, emulator).
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            // Light lifts on a soft shadow; dark has none to show, so a hairline does the work.
            .then(if (LocalDarkTheme.current) Modifier else Modifier.shadow(10.dp, shape, clip = false))
            .clip(shape)
            .background(scheme.surface.copy(alpha = GlassBarAlpha))
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), shape)
            .selectableGroup(),
    ) {
        Destination.entries.forEach { item ->
            val selected = item == current
            val pill by animateColorAsState(
                if (selected) scheme.secondaryContainer else Color.Transparent,
                label = "tab pill",
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = selected, role = Role.Tab) {
                        if (!selected) {
                            haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                            onSelect(item)
                        }
                    }
                    .padding(vertical = 8.dp),
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(pill)
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        contentDescription = null,
                        tint = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.height(Space.Tight))
                Text(
                    text = stringResource(item.labelRes),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    ).scaledLineHeight(),
                    color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    // Wraps between words, shrinks only while a word would split. One line that shrinks was
                    // tried on 30 Oct 2026 and measured worse: on a 320dp phone at 2.0 "My prayers" and
                    // "আমার নামাজ" still ended in an ellipsis at the floor (LargeTextAndTalkBackDeviceTest).
                    autoSize = WholeWords(MaterialTheme.typography.labelMedium.fontSize),
                )
            }
        }
    }
}

/**
 * The three tabs that are not Times (1.6, step 5): the status bar inset the Scaffold used to apply, taken here
 * instead, and the 600dp reading column (Q18, Q20) centred. Times does both itself, because its sky runs
 * under the status bar and out to both edges.
 */
@Composable
private fun ReadingFrame(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.readingWidth()) { content() }
    }
}
