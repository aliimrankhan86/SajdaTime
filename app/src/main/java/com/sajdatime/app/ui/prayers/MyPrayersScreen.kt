package com.sajdatime.app.ui.prayers

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.TrackerActions
import com.sajdatime.app.ui.TrackerState
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.BottomBarClearance
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DayRing
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.components.NoticeTone
import com.sajdatime.app.ui.components.RingKind
import com.sajdatime.app.ui.components.ScreenTitle
import com.sajdatime.app.ui.components.WeekdayNames
import com.sajdatime.app.ui.components.Tick
import com.sajdatime.app.ui.home.TrackerHistorySheet
import com.sajdatime.app.ui.settings.SettingsItem
import com.sajdatime.app.ui.settings.SettingsItemRow
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.AppLocale
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TRACKED_SLOTS
import com.sajdatime.core.labelRes
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

/**
 * "My prayers", the fourth tab (STEP-1-DESIGN, 23 Oct 2026): a daily checklist of the five prayers with
 * the tick at the start of each row, one row of buttons, and the week as seven rings in its own card.
 * Off, it is one switch card and a line under it. Nothing animates when the tab opens; the only motion
 * is BUILD-INSTRUCTIONS section 6 rows 1, 2, 3 and 12.
 *
 * [onPause] is the scaffold's, not this screen's: the "Paused from today" message with Undo belongs to
 * the scaffold's one snackbar host, above the tab bar, so the same message works on every tab.
 */
@Composable
fun MyPrayersScreen(
    state: UiState,
    tracker: TrackerActions,
    onPause: () -> Unit,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenTitle(stringResource(R.string.nav_tracker))
        // Nothing is drawn until the settings have loaded, so the switch card never flashes for
        // someone who has tracking on.
        if (!state.loading) {
            // Row 12: switching on crossfades the one row card into the checklist (Standard in, Quick
            // out). The Switch gives its own haptic.
            AnimatedContent(
                targetState = state.settings.trackerEnabled,
                transitionSpec = { fadeIn(Motion.standard()) togetherWith fadeOut(Motion.quick()) },
                label = "tracking on or off",
            ) { on ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    val record = state.tracker
                    if (on && record != null) {
                        Checklist(state, record, tracker, onPause, onUseGps, onSearchCity)
                    } else {
                        OffCard(state, tracker)
                    }
                }
            }
        }
        BottomBarClearance()
    }
}

/** 3b: the switch row in a card, and the one line that says what off means. */
@Composable
private fun OffCard(state: UiState, tracker: TrackerActions) {
    Spacer(Modifier.height(Space.Group))
    Column(Modifier.fillMaxWidth().sajdaSurface(RoundedCornerShape(24.dp))) {
        SettingsItemRow(
            SettingsItem.Toggle(
                key = "tracker",
                icon = Icons.Outlined.CheckCircle,
                title = stringResource(R.string.settings_tracker),
                subtitle = stringResource(R.string.settings_tracker_desc),
                checked = state.settings.trackerEnabled,
                onCheckedChange = tracker::setEnabled,
            ),
        )
    }
    Spacer(Modifier.height(Space.Related))
    Text(
        text = stringResource(R.string.tracker_off_line),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/** 3a: today line, notices, today card, button row, week card. */
@Composable
private fun Checklist(
    state: UiState,
    t: TrackerState,
    tracker: TrackerActions,
    onPause: () -> Unit,
    onUseGps: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    val context = LocalContext.current
    var history by rememberSaveable { mutableStateOf(false) }
    var locationSheet by rememberSaveable { mutableStateOf(false) }
    // No location means two things in code and both read alike (STEP-1-DESIGN 9.1): none stored, or the
    // Makkah fallback. In the fallback state.today holds Makkah's times, so they are ignored here.
    val noLocation = state.settings.coordinates == null || state.settings.usingDefaultLocation
    val scheme = MaterialTheme.colorScheme

    Spacer(Modifier.height(Space.Group))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_today),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        Text(
            text = TimeFormat.date(context, t.today),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(Space.Related))
    if (noLocation) {
        Notice(
            brief = stringResource(R.string.home_no_location),
            actionLabel = stringResource(R.string.action_set_location),
            onAction = { locationSheet = true },
        )
        Spacer(Modifier.height(Space.Group))
    }
    if (t.data.readOnly) {
        Notice(brief = stringResource(R.string.tracker_newer_version), tone = NoticeTone.QUIET, icon = Icons.Outlined.Info)
        Spacer(Modifier.height(Space.Group))
    }

    TodayCard(state, t, tracker, noLocation)
    Spacer(Modifier.height(Space.Group))
    ButtonRow(t, onPause = onPause, onResume = tracker::resume, onHistory = { history = true })
    Spacer(Modifier.height(Space.Section))
    WeekCard(t, showStreak = state.settings.trackerShowStreak)
    Spacer(Modifier.height(Space.Section))

    if (history) {
        TrackerHistorySheet(
            tracker = t,
            today = state.today.takeIf { !noLocation },
            now = state.now,
            actions = tracker,
            onDismiss = { history = false },
        )
    }
    if (locationSheet) {
        // Closes as soon as a location lands, as on Times.
        val opened = remember { state.settings.coordinates to state.settings.cityName }
        val current = state.settings.coordinates to state.settings.cityName
        LaunchedEffect(current) { if (current != opened) locationSheet = false }
        LocationSheet(
            state = state,
            onDismiss = { locationSheet = false },
            onUseGps = onUseGps,
            onSearchCity = onSearchCity,
        )
    }
}

@Composable
private fun TodayCard(state: UiState, t: TrackerState, tracker: TrackerActions, noLocation: Boolean) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val times = state.today?.takeIf { !noLocation && it.date == t.today }?.times.orEmpty()
    val yesterday = t.today.minusDays(1)
    // After midnight, while Isha is still the current prayer, it is still last night's Isha: the card
    // opens with a row that writes to yesterday (tracker plan B4). Never shown without times.
    val lastNight = !noLocation && state.current == PrayerSlot.ISHA &&
        times[PrayerSlot.FAJR]?.let { state.now.isBefore(it) } == true
    val ishaYesterday = if (lastNight) {
        remember(yesterday, state.settings.coordinates, state.settings.calculationPrefs) {
            state.settings.coordinates?.let {
                PrayerEngine.compute(it, yesterday, state.settings.calculationPrefs).times[PrayerSlot.ISHA]
            }
        }
    } else {
        null
    }

    Column(Modifier.fillMaxWidth().sajdaSurface(RoundedCornerShape(24.dp))) {
        if (lastNight) {
            val name = stringResource(R.string.tracker_last_night_isha)
            PrayerCheckRow(
                name = name,
                time = ishaYesterday?.let { TimeFormat.clock(context, it) },
                isNow = false,
            ) {
                // due is forced true: it is a prayer that has begun (STEP-1-DESIGN 9.7).
                Tick(
                    state = t.state(yesterday, PrayerSlot.ISHA, due = true),
                    label = name,
                    readOnly = t.data.readOnly,
                    onChange = { on -> tracker.tick(yesterday, PrayerSlot.ISHA, on) },
                )
            }
            HorizontalDivider(color = scheme.outlineVariant)
        }
        TRACKED_SLOTS.forEachIndexed { index, slot ->
            val at = times[slot]
            val name = stringResource(slot.labelRes)
            PrayerCheckRow(
                name = name,
                time = at?.let { TimeFormat.clock(context, it) },
                // Now = its time has come and no later prayer has. Last night's Isha is not today's Isha.
                isNow = !lastNight && slot == state.current && at != null,
            ) {
                Tick(
                    // No times: nothing is "not due yet", every ring is tickable.
                    state = t.state(t.today, slot, due = at == null || !state.now.isBefore(at)),
                    label = name,
                    readOnly = t.data.readOnly,
                    onChange = { on -> tracker.tick(t.today, slot, on) },
                )
            }
            if (index != TRACKED_SLOTS.lastIndex) HorizontalDivider(color = scheme.outlineVariant)
        }
    }
}

/** One row of the checklist: the tick at the start (48dp target), the name, the Now pill, the time. */
@Composable
private fun PrayerCheckRow(name: String, time: String?, isNow: Boolean, tick: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 8.dp, end = 18.dp),
    ) {
        tick()
        Spacer(Modifier.width(Space.Related))
        // The tick already says which prayer ("Fajr, prayed"); the visible name is not read again.
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
            modifier = Modifier.weight(1f).clearAndSetSemantics {},
        )
        if (isNow) {
            val nowLabel = stringResource(R.string.home_now_marker_a11y)
            Text(
                text = stringResource(R.string.home_now_marker),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(scheme.secondaryContainer)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
                    .semantics { contentDescription = nowLabel },
            )
            Spacer(Modifier.width(10.dp))
        }
        if (time != null) {
            Text(text = time, style = PrayerTimeTextStyle.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize), color = scheme.onSurfaceVariant)
        }
    }
}

/**
 * Pause and Last 30 days under the card, or "Tracking is paused" and Resume (row 3). A FlowRow, so both
 * pairs stack alike by measured width when they would touch, never by a width or font scale number
 * (STEP-1-DESIGN 9.5). Pause is first in reading order, so a stacked pair puts it on top. No haptic on
 * any of the three.
 */
@Composable
private fun ButtonRow(t: TrackerState, onPause: () -> Unit, onResume: () -> Unit, onHistory: () -> Unit) {
    AnimatedContent(
        targetState = t.paused,
        transitionSpec = {
            (fadeIn(Motion.standard()) togetherWith fadeOut(Motion.quick())) using
                SizeTransform(clip = false) { _, _ -> Motion.standard() }
        },
        label = "pause row",
    ) { paused ->
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            // A read only record has no Pause, so the one button that is left sits at the end.
            horizontalArrangement = if (t.data.readOnly && !paused) Arrangement.End else Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Space.Related),
        ) {
            if (paused) {
                Text(
                    text = stringResource(R.string.tracker_paused_line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                CtaButton(onClick = onResume, compact = true) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.Related))
                    Text(stringResource(R.string.action_resume))
                }
            } else {
                if (!t.data.readOnly) {
                    CtaButton(onClick = onPause, compact = true) {
                        Icon(Icons.Rounded.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(Space.Related))
                        Text(stringResource(R.string.action_pause))
                    }
                }
                CtaButton(onClick = onHistory, compact = true) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Space.Related))
                    Text(stringResource(R.string.tracker_open_history))
                }
            }
        }
    }
}

/**
 * The week the way the calendar shows it: the locale's first day, seven rings, today with its halo.
 * Not tappable. Rings are 36dp, or 32dp when seven of them plus 4dp gaps do not fit the card's inner
 * width (under 340dp phones). The row never wraps (STEP-1-DESIGN 9.4).
 */
@Composable
private fun WeekCard(t: TrackerState, showStreak: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val locale = AppLocale.of(LocalContext.current)
    val first = WeekFields.of(locale).firstDayOfWeek
    val start = t.today.with(TemporalAdjusters.previousOrSame(first))
    val days = (0L..6L).map { start.plusDays(it) }
    val labelStyle = MaterialTheme.typography.labelMedium
    val measurer = rememberTextMeasurer()

    Column(Modifier.fillMaxWidth().sajdaSurface(RoundedCornerShape(24.dp)).padding(16.dp)) {
        Text(
            text = stringResource(R.string.tracker_week_title),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
            color = scheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(Space.Related))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val ring = if (maxWidth >= 36.dp * 7 + Space.Tight * 6) 36.dp else 32.dp
            val slot = ring + (maxWidth - ring * 7) / 6
            val style = WeekdayNames.pick(measurer, days.map { it.dayOfWeek }, locale, labelStyle, with(LocalDensity.current) { slot.toPx() })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day -> WeekDay(t, day, ring, style, locale) }
            }
        }
        if (showStreak && t.streak > 0) {
            Spacer(Modifier.height(Space.Paragraph))
            Text(
                text = pluralStringResource(R.plurals.tracker_streak, t.streak, t.streak),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeekDay(t: TrackerState, day: LocalDate, ring: Dp, names: TextStyle, locale: java.util.Locale) {
    val scheme = MaterialTheme.colorScheme
    val isToday = day == t.today
    val before = day.isBefore(t.started)
    val kind = when {
        // A pause still on has no end date, so it would cover every day to come: future first.
        day.isAfter(t.today) -> RingKind.FUTURE
        t.isPaused(day) -> RingKind.PAUSED
        else -> RingKind.SHARE
    }
    val count = t.data.records[day]?.ticked?.size ?: 0
    val full = day.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
    val spoken = when {
        isToday && kind == RingKind.SHARE -> pluralStringResource(R.plurals.tracker_ring_today_a11y, count, count)
        before -> stringResource(R.string.tracker_ring_before_a11y, full)
        kind == RingKind.PAUSED -> stringResource(R.string.tracker_ring_paused_a11y, full)
        kind == RingKind.FUTURE -> stringResource(R.string.tracker_ring_future_a11y, full)
        else -> pluralStringResource(R.plurals.tracker_ring_a11y, count, full, count)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(ring).clearAndSetSemantics { contentDescription = spoken },
    ) {
        // Before tracking began there is no ring, only the name: no empty circle to read as a miss.
        if (before) Spacer(Modifier.size(ring)) else DayRing(ticked = count, kind = kind, size = ring, halo = isToday)
        Spacer(Modifier.height(Space.Tight))
        Text(
            text = day.dayOfWeek.getDisplayName(names, locale),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal),
            color = if (isToday) scheme.primary else scheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.wrapContentWidth(unbounded = true),
        )
    }
}
