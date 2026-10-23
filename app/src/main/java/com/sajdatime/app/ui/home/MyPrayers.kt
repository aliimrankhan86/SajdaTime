package com.sajdatime.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.TrackerActions
import com.sajdatime.app.ui.TrackerState
import com.sajdatime.app.ui.components.DayRing
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.components.NoticeTone
import com.sajdatime.app.ui.components.RingKind
import com.sajdatime.app.ui.prayers.DayName
import com.sajdatime.app.ui.components.Tick
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.AppLocale
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.DayKind
import com.sajdatime.core.TRACKED_SLOTS
import com.sajdatime.core.TrackerLogic
import com.sajdatime.core.labelRes
import java.text.NumberFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * The last 30 days as a calendar grid in weeks of seven (STEP-1-DESIGN 3c), oldest week at the top,
 * today last, with the same first day and day names as the week card, and under it the chosen day's
 * five ticks. Today is chosen when it opens. It replaces 30 day cards (about 3300dp tall).
 *
 *  - Cells outside the 30 days are blank, and a day before tracking began shows its number with no
 *    ring and cannot be chosen: a day number with no ring means "before tracking began" and nothing
 *    else, and back fillable blanks would be a missed prayer counter in another form (Fable, 23 Oct 2026).
 *  - A paused day shows "Paused" in place of the ticks. A day still to come is not in the grid.
 *  - The grid row drops the sheet's side padding so a cell is about 46dp wide on a 320dp phone, over
 *    the 44pt Apple minimum (STEP-1-DESIGN 9.6).
 *
 * After midnight, where Isha can still be the current prayer, that Isha is ticked on the previous
 * day: that day is a cell here, and the My prayers tab has the "Last night's Isha" row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackerHistorySheet(
    tracker: TrackerState,
    /** Null while there are no times to compare with: every tick is then allowed. */
    today: DayPrayerTimes?,
    now: Instant,
    actions: TrackerActions,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val locale = AppLocale.of(context)
    val scheme = MaterialTheme.colorScheme
    val first = WeekFields.of(locale).firstDayOfWeek
    val windowStart = tracker.today.minusDays((TrackerLogic.HISTORY_DAYS - 1).toLong())
    // Weeks wholly before tracking began are dropped (Q4 forbids rows that read as "you missed a lot").
    val weeks = TrackerLogic.gridWeeks(tracker.today, tracker.started, first)
    val weekdays = (0L..6L).map { first.plus(it) }
    var chosenDay by rememberSaveable { mutableLongStateOf(tracker.today.toEpochDay()) }
    val chosen = LocalDate.ofEpochDay(chosenDay)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(horizontal = 24.dp)) {
                ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                    DialogTitle(text = stringResource(R.string.tracker_history_title), onClose = onDismiss)
                }
                if (tracker.data.readOnly) {
                    Spacer(Modifier.height(Space.Group))
                    Notice(brief = stringResource(R.string.tracker_newer_version), tone = NoticeTone.QUIET, icon = Icons.Outlined.Info)
                }
            }
            Spacer(Modifier.height(Space.Group))
            Box(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().selectableGroup()) {
                    Row(Modifier.fillMaxWidth()) {
                        weekdays.forEach { day -> Box(Modifier.weight(1f)) { DayName(day, locale) } }
                    }
                    Spacer(Modifier.height(Space.Tight))
                    weeks.forEach { weekStart ->
                        Row(Modifier.fillMaxWidth()) {
                            (0L..6L).map { weekStart.plusDays(it) }.forEach { date ->
                                DayCell(
                                    tracker = tracker,
                                    date = date,
                                    inWindow = !date.isBefore(windowStart) && !date.isAfter(tracker.today),
                                    selected = date == chosen,
                                    onSelect = { chosenDay = date.toEpochDay() },
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Space.Section))
            ChosenDay(tracker, chosen, today, now, actions)
            Spacer(Modifier.height(Space.Section))
        }
    }
}

/** One cell: a 32dp ring over the day number, 56dp tall, the week card's drawing. */
@Composable
private fun RowScope.DayCell(tracker: TrackerState, date: LocalDate, inWindow: Boolean, selected: Boolean, onSelect: () -> Unit) {
    val cell = Modifier.weight(1f).heightIn(min = 56.dp)
    if (!inWindow) {
        Spacer(cell)
        return
    }
    val context = LocalContext.current
    val locale = AppLocale.of(context)
    val scheme = MaterialTheme.colorScheme
    val isToday = date == tracker.today
    val dayKind = tracker.dayKind(date)
    val before = dayKind == DayKind.BEFORE_START
    val kind = if (dayKind == DayKind.PAUSED) RingKind.PAUSED else RingKind.SHARE
    val count = tracker.data.records[date]?.ticked?.size ?: 0
    val name = TimeFormat.date(context, date)
    val spoken = when {
        before -> stringResource(R.string.tracker_ring_before_a11y, name)
        kind == RingKind.PAUSED -> stringResource(R.string.tracker_ring_paused_a11y, name)
        isToday -> pluralStringResource(R.plurals.tracker_ring_today_a11y, count, count)
        else -> pluralStringResource(R.plurals.tracker_ring_a11y, count, name, count)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = cell
            .selectable(selected = selected, enabled = !before, role = Role.RadioButton, onClick = onSelect)
            .semantics { contentDescription = spoken },
    ) {
        if (before) Box(Modifier.size(32.dp)) else DayRing(ticked = count, kind = kind, size = 32.dp, halo = selected)
        Text(
            text = NumberFormat.getIntegerInstance(locale).format(date.dayOfMonth),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal),
            color = if (isToday) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {},
        )
    }
}

/** The chosen day: its date, then the five names over five ticks (or "Paused"). Past days can be changed. */
@Composable
private fun ChosenDay(tracker: TrackerState, date: LocalDate, today: DayPrayerTimes?, now: Instant, actions: TrackerActions) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val separator = stringResource(R.string.list_separator)
    val dateText = if (date == tracker.today) stringResource(R.string.home_today) else TimeFormat.date(context, date)
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(16.dp))
            .padding(vertical = Space.Related),
    ) {
        Text(dateText, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
        val paused = tracker.isPaused(date)
        // A paused day counts nothing, but what was ticked before the pause is still shown, disabled,
        // under the word "Paused" (Fable, 23 Oct 2026): pausing never hides what someone prayed.
        if (paused) {
            Text(
                text = stringResource(R.string.tracker_paused_day),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = Space.Tight),
            )
        }
        run {
            // The prayer names once, over the five columns, so a row of ticks reads without them.
            Row(Modifier.fillMaxWidth()) {
                TRACKED_SLOTS.forEach { slot ->
                    Text(
                        text = stringResource(slot.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f).clearAndSetSemantics {},
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TRACKED_SLOTS.forEach { slot ->
                    // Today's prayers that have not begun cannot be ticked yet.
                    val due = date != tracker.today ||
                        today?.takeIf { it.date == date }?.times?.get(slot)?.let { !now.isBefore(it) } ?: true
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Tick(
                            state = tracker.state(date, slot, due),
                            label = dateText + separator + stringResource(slot.labelRes),
                            readOnly = tracker.data.readOnly || paused,
                            onChange = { on -> actions.tick(date, slot, on) },
                        )
                    }
                }
            }
        }
    }
}
