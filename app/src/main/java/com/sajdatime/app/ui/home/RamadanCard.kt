package com.sajdatime.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.rounded.Brightness3
import com.sajdatime.app.ui.components.NoticeTone
import com.sajdatime.app.ui.components.Notice
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.RamadanState
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.rememberRemainingText
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.sajdaSurface
import java.time.Instant
import kotlin.math.abs

/**
 * What the Times screen shows about Ramadan today, if anything: the card inside Ramadan, a quiet
 * notice in the week before it, or nothing at all.
 *
 * All the timing lives in [com.sajdatime.core.RamadanInfo] (`isRamadan`, `showsNotice`) and the
 * state is null outside those windows, so this function only decides between the two shapes and
 * honours the two things the user can say: hide the card ("Ramadan card" in Settings, also in the
 * info dialog, because not everyone fasts and a daily fasting card must be removable) and close
 * the notice for this Hijri year.
 *
 * It sits below the rare banners on purpose. The Makkah fallback banner says the times are for the
 * wrong place and the polar banner says they are approximate, and a Ramadan card that appeared
 * above either would be the first thing read and the last thing to be doubted.
 */
@Composable
internal fun RamadanSection(
    state: UiState,
    onOpenInfo: () -> Unit,
    onOpenTimetable: () -> Unit,
    onShiftDate: () -> Unit,
    onDismissNotice: (hijriYear: Int) -> Unit,
) {
    val ramadan = state.ramadan ?: return
    val settings = state.settings
    if (!settings.ramadanCardEnabled) return

    val info = ramadan.info
    val dismissed = settings.ramadanNoticeDismissedYear == info.hijriYear
    val noticeDay = info.showsNotice(ramadan.today.date) && !dismissed

    Spacer(Modifier.height(12.dp))
    if (info.isRamadan) {
        RamadanCard(
            ramadan = ramadan,
            now = state.now,
            // Days 1 and 2 carry the date question inside the card, so the notice and the card
            // are never two Ramadan items stacked on the same screen.
            showDateHint = noticeDay,
            onOpenInfo = onOpenInfo,
            onOpenTimetable = onOpenTimetable,
            onShiftDate = onShiftDate,
        )
    } else if (noticeDay) {
        RamadanNotice(
            ramadan = ramadan,
            onShiftDate = onShiftDate,
            onDismiss = { onDismissNotice(info.hijriYear) },
        )
    }
}

/**
 * Suhoor ends and iftar for this place, the countdown to whichever is next, the length of the fast
 * and how it differs from yesterday, and tomorrow's two times.
 *
 * Suhoor ends at Fajr and iftar is at Maghrib because those are the two times the app already
 * calculates and labels. The words say "as calculated" in the info dialog and nothing here adds an
 * imsak margin (docs/HANDOVER.md A11). The fast is described, never judged: no "valid", "must" or
 * "reward".
 */
@Composable
private fun RamadanCard(
    ramadan: RamadanState,
    now: Instant,
    showDateHint: Boolean,
    onOpenInfo: () -> Unit,
    onOpenTimetable: () -> Unit,
    onShiftDate: () -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val phase = ramadan.phase(now)
    val info = ramadan.info

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.ramadan_card_label),
                // Letterspaced like "Next prayer" above it, not uppercased in code: see the note
                // in NextPrayerCard about Turkish dotted and dotless i.
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
                color = scheme.onSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Text(
                text = stringResource(R.string.ramadan_day_of, info.dayNumber ?: 1, info.length),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeColumn(
                label = stringResource(R.string.ramadan_suhoor_ends),
                time = TimeFormat.clock(context, ramadan.suhoorEnds),
                isNext = phase == RamadanState.Phase.BEFORE_SUHOOR_END,
                modifier = Modifier.weight(1f),
            )
            TimeColumn(
                label = stringResource(R.string.ramadan_iftar),
                time = TimeFormat.clock(context, ramadan.iftar),
                isNext = phase == RamadanState.Phase.FASTING,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))

        Countdown(ramadan = ramadan, now = now, phase = phase)

        Spacer(Modifier.height(6.dp))
        FastLine(ramadan = ramadan)

        if (ramadan.tomorrowIsRamadan) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.ramadan_tomorrow_line,
                    TimeFormat.clock(context, ramadan.tomorrowSuhoorEnds),
                    TimeFormat.clock(context, ramadan.tomorrowIftar),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }

        if (ramadan.today.approximated) {
            // The polar banner above names the latitude. This line keeps the marking on the card
            // itself, so a card that is looked at on its own is never taken for measured times.
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.notif_approximate),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
        }

        if (showDateHint) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.ramadan_date_differs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onShiftDate) {
                    Text(stringResource(R.string.settings_hijri_offset_label))
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // The whole month, on screen. A text button with words, not an icon: it is the one
            // action on the card and the thing a person comes to it for in the second week.
            TextButton(onClick = onOpenTimetable) {
                Text(stringResource(R.string.ramadan_open_timetable))
            }
            // 48dp target: this is the only way to the explanation, and it is small.
            IconButton(onClick = onOpenInfo, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.ramadan_info_a11y),
                    tint = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimeColumn(label: String, time: String, isNext: Boolean, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isNext) scheme.primaryContainer else scheme.surfaceContainerLow)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNext) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (isNext) {
                // Not colour alone: the next time also carries the same "Next" word the list uses.
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_next_marker),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(scheme.primary)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = time,
            style = PrayerTimeTextStyle,
            color = scheme.onSurface,
        )
    }
}

/**
 * The line that says what is coming. Before suhoor ends and during the fast it ticks, so its digits
 * are hidden from a screen reader and a spoken summary is announced instead, as the next prayer card
 * does. After iftar it is a plain sentence: tomorrow's suhoor, or on the last day the end of the
 * month, in words that leave Eid to the mosque.
 */
@Composable
private fun Countdown(ramadan: RamadanState, now: Instant, phase: RamadanState.Phase) {
    val context = LocalContext.current
    val primary = MaterialTheme.colorScheme.primary
    val style = MaterialTheme.typography.bodyLarge

    when (phase) {
        RamadanState.Phase.BEFORE_SUHOOR_END, RamadanState.Phase.FASTING -> {
            val target = if (phase == RamadanState.Phase.BEFORE_SUHOOR_END) ramadan.suhoorEnds else ramadan.iftar
            val resource = if (phase == RamadanState.Phase.BEFORE_SUHOOR_END) {
                R.string.ramadan_countdown_suhoor
            } else {
                R.string.ramadan_countdown_iftar
            }
            val spoken = stringResource(resource, rememberRemainingText(now, target))
            Text(
                text = stringResource(resource, TimeFormat.countdownClock(context, now, target)),
                style = style,
                color = primary,
                modifier = Modifier.clearAndSetSemantics { contentDescription = spoken },
            )
        }

        RamadanState.Phase.AFTER_IFTAR -> {
            val text = if (ramadan.tomorrowIsRamadan) {
                stringResource(
                    R.string.ramadan_next_suhoor_tomorrow,
                    TimeFormat.clock(context, ramadan.tomorrowSuhoorEnds),
                )
            } else {
                stringResource(R.string.ramadan_month_ends_today)
            }
            Text(text = text, style = style, color = primary)
        }
    }
}

/** "Fast today 12h 18m, 4 minutes longer than yesterday". Joined by a resource: punctuation is a translator's call. */
@Composable
private fun FastLine(ramadan: RamadanState) {
    val context = LocalContext.current
    val change = ramadan.fastChangeMinutes.toInt()
    val changeText = when {
        change > 0 -> pluralStringResource(R.plurals.ramadan_fast_change_longer, change, change)
        change < 0 -> pluralStringResource(R.plurals.ramadan_fast_change_shorter, abs(change), abs(change))
        else -> stringResource(R.string.ramadan_fast_change_same)
    }
    Text(
        text = stringResource(
            R.string.ramadan_fast_line,
            stringResource(
                R.string.ramadan_fast_length,
                TimeFormat.remaining(context, ramadan.suhoorEnds, ramadan.iftar),
            ),
            changeText,
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * The week before day 1, one quiet card: when Ramadan begins on the calendar the app is set to, and
 * that mosques announce their own start. Neutral, not amber. It carries no warning, and the other
 * notices on this screen use amber for "something is wrong", which this is not.
 *
 * Tapping it goes to the Islamic date shift, the one thing a person who follows local sighting can
 * do about it. Closing it hides it for this Hijri year and it comes back next year without a reset.
 */
@Composable
private fun RamadanNotice(ramadan: RamadanState, onShiftDate: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    // A quiet Notice (17 Oct 2026): the date and one line up front, the moon sighting reasoning
    // folded, and the date shift as the action rather than the whole card.
    Notice(
        tone = NoticeTone.QUIET,
        icon = Icons.Rounded.Brightness3,
        title = stringResource(R.string.ramadan_eve_title_before, TimeFormat.date(context, ramadan.info.firstDay)),
        brief = stringResource(R.string.ramadan_eve_brief),
        details = stringResource(R.string.ramadan_eve_body),
        actionLabel = stringResource(R.string.settings_hijri_offset_label),
        onAction = onShiftDate,
        onDismiss = onDismiss,
    )
}

/**
 * What the card does and does not decide, in four short paragraphs that end the way the disclaimer
 * does: follow your mosque. Then the two things a person might want to do about it: shift the
 * Islamic date, or hide the card.
 *
 * Deliberately not a second disclaimer and not a ruling on whose timetable is "valid".
 */
@Composable
internal fun RamadanInfoDialog(
    onDismiss: () -> Unit,
    onOpenDateShift: () -> Unit,
    onHideCard: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { Text(stringResource(R.string.ramadan_info_title)) },
        // Scrollable, like every long dialog here: AlertDialog will not scroll its body for you.
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ExplainerParagraph(stringResource(R.string.ramadan_info_calc))
                ExplainerParagraph(stringResource(R.string.ramadan_info_margin))
                ExplainerParagraph(stringResource(R.string.ramadan_info_mosque))
                ExplainerParagraph(stringResource(R.string.ramadan_info_date))
                ExplainerAction(stringResource(R.string.settings_hijri_offset), onOpenDateShift)
                ExplainerAction(stringResource(R.string.ramadan_info_hide), onHideCard)
                Text(
                    text = stringResource(R.string.ramadan_info_hide_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        },
    )
}
