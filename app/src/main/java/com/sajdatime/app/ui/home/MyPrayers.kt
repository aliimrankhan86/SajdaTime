package com.sajdatime.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.TrackerActions
import com.sajdatime.app.ui.TrackerState
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.components.NoticeTone
import com.sajdatime.app.ui.components.Tick
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.TRACKED_SLOTS
import com.sajdatime.core.TrackerLogic
import com.sajdatime.core.labelRes
import java.time.Instant

/**
 * The one line "My prayers" adds under the times: the streak, if the person switched it on and it is
 * above zero, and the way into the last 30 days. While paused it says so, with Resume, in the same
 * height, so a pause is never forgotten (Ali, 16 Oct 2026). Under the list rather than above it, so
 * the six rows stay where every user knows them (sign-off, 23 Oct 2026).
 *
 * When a streak ends the number simply goes. There is no message, no zero and no "you broke it".
 */
@Composable
internal fun TrackerLine(tracker: TrackerState, showStreak: Boolean, actions: TrackerActions, onOpenHistory: () -> Unit) {
    Spacer(Modifier.height(Space.Related))
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(start = 4.dp)) {
        val text = when {
            tracker.paused -> stringResource(R.string.tracker_paused_line)
            showStreak && tracker.streak > 0 -> pluralStringResource(R.plurals.tracker_streak, tracker.streak, tracker.streak)
            else -> null
        }
        Text(
            text = text.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        if (tracker.paused) {
            CtaButton(onClick = actions::resume, compact = true) { Text(stringResource(R.string.action_resume)) }
        } else {
            CtaButton(onClick = onOpenHistory, compact = true) { Text(stringResource(R.string.tracker_open_history)) }
        }
    }
}

/**
 * The last 30 days, newest first. Each day is its date over five ticks (date on its own line, so the
 * ticks keep their 48dp targets on a 320dp phone and at large text). Past days can be changed. A
 * paused day says "Paused" and nothing else. No year grid and no totals: a grid of blanks is a
 * missed-prayer counter in another form (sign-off, 23 Oct 2026).
 *
 * After midnight, where Isha can still be the current prayer, that Isha is ticked here on the
 * previous day's row: the Times screen ticks the calendar day it is showing (tracker plan B4).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TrackerHistorySheet(
    tracker: TrackerState,
    today: DayPrayerTimes?,
    now: Instant,
    actions: TrackerActions,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val separator = stringResource(R.string.list_separator)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.padding(horizontal = 16.dp)) {
            item {
                Text(
                    text = stringResource(R.string.tracker_history_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 8.dp).semantics { heading() },
                )
                if (tracker.data.readOnly) {
                    Spacer(Modifier.height(Space.Group))
                    Notice(brief = stringResource(R.string.tracker_newer_version), tone = NoticeTone.QUIET)
                }
                Spacer(Modifier.height(Space.Group))
                // The prayer names once, over the five columns, so a row of ticks reads without them.
                Row(Modifier.fillMaxWidth()) {
                    TRACKED_SLOTS.forEach { slot ->
                        Text(
                            text = stringResource(slot.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(Modifier.height(Space.Tight))
            }
            items(TrackerLogic.historyDates(tracker.today)) { date ->
                val dateText = if (date == tracker.today) stringResource(R.string.home_today) else TimeFormat.date(context, date)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = Space.Tight)
                        .sajdaSurface(RoundedCornerShape(16.dp))
                        .padding(vertical = Space.Related),
                ) {
                    Text(dateText, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
                    if (tracker.isPaused(date)) {
                        Text(
                            text = stringResource(R.string.tracker_paused_day),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp).heightIn(min = 48.dp).padding(top = Space.Paragraph),
                        )
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            TRACKED_SLOTS.forEach { slot ->
                                // Today's prayers that have not begun cannot be ticked yet.
                                val due = date != tracker.today ||
                                    today?.takeIf { it.date == date }?.times?.get(slot)?.let { !now.isBefore(it) } ?: true
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                    Tick(
                                        state = tracker.state(date, slot, due),
                                        label = dateText + separator + stringResource(slot.labelRes),
                                        readOnly = tracker.data.readOnly,
                                        onChange = { on -> actions.tick(date, slot, on) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(Space.Section)) }
        }
    }
}
