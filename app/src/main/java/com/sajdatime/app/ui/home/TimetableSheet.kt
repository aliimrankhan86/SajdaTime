package com.sajdatime.app.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.data.placeName
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.RamadanTimetable
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.AppLocale
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TimetableGrid
import com.sajdatime.core.TimetableList
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

/**
 * The Timetable (STEP-4-DESIGN): this month and the next as day cards, opened on today's card.
 * Save as PDF opens the existing chooser over this sheet (the caller owns it), and the sheet stays
 * open after a save. If today's times vanish while it is open it closes rather than show nothing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun TimetableSheet(
    state: UiState,
    sheetState: SheetState,
    onExport: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val today = state.today?.date
    val coordinates = state.settings.coordinates
    if (today == null || coordinates == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val prefs = state.settings.calculationPrefs
    val months = remember(today) { TimetableList.months(today) }
    val rows by produceState(emptyList<List<DayPrayerTimes>>(), coordinates, prefs, today) {
        value = withContext(Dispatchers.Default) {
            months.map { PrayerEngine.computeRange(coordinates, it.first, it.length, prefs) }
        }
    }
    val listState = rememberLazyListState()
    // Opens on today once, when the rows first arrive.
    LaunchedEffect(rows.isNotEmpty()) {
        if (rows.isNotEmpty()) listState.scrollToItem(TimetableList.todayIndex(today))
    }
    val locale = AppLocale.of(context)
    val monthFormat = remember(locale) {
        DateTimeFormatter.ofPattern(DateFormat.getBestDateTimePattern(locale, "LLLLyyyy"), locale)
    }
    val fontScale = LocalDensity.current.fontScale

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth()) {
            TimetableHeader(
                title = stringResource(R.string.timetable_title),
                place = state.settings.placeName(context),
                onExport = onExport,
                onClose = onDismiss,
            )
            BoxWithConstraints(Modifier.weight(1f, fill = false)) {
                // 32 for the list's sides and 32 for the card's.
                val columns = TimetableGrid.columns((maxWidth - 64.dp).value, fontScale)
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = Space.Section + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                ) {
                    months.forEachIndexed { index, month ->
                        stickyHeader(key = "month$index") {
                            MonthHeading(monthFormat.format(month.first), first = index == 0)
                        }
                        items(rows.getOrNull(index).orEmpty(), key = { it.date.toEpochDay() }) { day ->
                            Column(Modifier.padding(bottom = Space.Paragraph)) {
                                DayCard(day = day, isToday = day.date == today, columns = columns)
                            }
                        }
                    }
                    rows.flatten().firstNotNullOfOrNull { it.approximatedFrom }?.let { from ->
                        item(key = "footer") {
                            Text(
                                text = stringResource(R.string.pdf_approximate_note, from.toInt()),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Title with the close X, then the place with Save as PDF beside it. Shared with the Ramadan table so
 * both doors open the same header. At 1.5 text and above the button drops under the place, full
 * width: the Today line's rule.
 */
@Composable
internal fun TimetableHeader(title: String, place: String, onExport: () -> Unit, onClose: () -> Unit) {
    val placeText: @Composable (Modifier) -> Unit = { modifier ->
        Column(modifier) {
            Text(
                // isBlank is checked before isolating, so the fallback still fires.
                text = place.bidiIsolated().ifBlank { stringResource(R.string.pdf_place_unknown) },
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.pdf_area_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    val button: @Composable (Modifier) -> Unit = { modifier ->
        CtaButton(onClick = onExport, compact = true, modifier = modifier) {
            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.Related))
            Text(stringResource(R.string.action_save_pdf))
        }
    }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        ProvideTextStyle(MaterialTheme.typography.headlineSmall) { DialogTitle(text = title, onClose = onClose) }
        Spacer(Modifier.height(Space.Tight))
        if (LocalDensity.current.fontScale >= 1.5f) {
            placeText(Modifier.fillMaxWidth())
            Spacer(Modifier.height(Space.Related))
            button(Modifier.fillMaxWidth())
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                placeText(Modifier.weight(1f))
                button(Modifier)
            }
        }
        Spacer(Modifier.height(Space.Group))
    }
}

/** Pinned while its days scroll under it; drawn on the sheet's own colour so nothing shows through. */
@Composable
private fun MonthHeading(name: String, first: Boolean) {
    val spoken = stringResource(R.string.timetable_month_a11y, name)
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(top = if (first) 0.dp else Space.Section, bottom = Space.Related)
            .padding(horizontal = 8.dp)
            .semantics { heading(); contentDescription = spoken },
    )
}

/**
 * One day: the date, then the six times in a grid of [columns]. [ramadanDay] is the Q8 prototype
 * (STEP-4-DESIGN §6): the same card with the day number, Suhoor ends and Iftar picked out, and the
 * length of the fast. Not wired into the shipped flow.
 */
@Composable
internal fun DayCard(day: DayPrayerTimes, isToday: Boolean, columns: Int, ramadanDay: Int? = null) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val surfaceColour = if (isToday) scheme.primaryContainer else scheme.surface
    val labelColour = if (isToday) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val timeColour = if (isToday) scheme.onPrimaryContainer else scheme.onSurface
    val emphasis = if (isToday) scheme.onPrimaryContainer else scheme.primary
    val dateText = TimeFormat.date(context, day.date)
    val slots = PrayerSlot.entries.filter { day.times[it] != null }
    fun clock(slot: PrayerSlot) = day.times[slot]?.let { TimeFormat.clock(context, it) }.orEmpty()
    val fast = if (ramadanDay != null) {
        TimeFormat.remaining(context, day.times.getValue(PrayerSlot.FAJR), day.times.getValue(PrayerSlot.MAGHRIB))
    } else {
        ""
    }
    // ponytail: the sentence has six fixed places, so a slot missing (never today) reads as blank, not skipped.
    val spoken = if (ramadanDay != null) {
        stringResource(R.string.ramadan_row_a11y, ramadanDay, dateText, clock(PrayerSlot.FAJR), clock(PrayerSlot.MAGHRIB), fast)
    } else {
        stringResource(
            R.string.timetable_day_a11y, dateText,
            clock(PrayerSlot.FAJR), clock(PrayerSlot.SUNRISE), clock(PrayerSlot.DHUHR),
            clock(PrayerSlot.ASR), clock(PrayerSlot.MAGHRIB), clock(PrayerSlot.ISHA),
        )
    }
    val announced = if (isToday) stringResource(R.string.timetable_today_a11y, spoken) else spoken
    val approx = stringResource(R.string.ramadan_approx_marker)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(shape, color = surfaceColour)
            .semantics(mergeDescendants = true) { contentDescription = announced }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                if (ramadanDay != null) {
                    Text(stringResource(R.string.timetable_ramadan_day, ramadanDay), style = MaterialTheme.typography.titleMedium, color = timeColour)
                    Spacer(Modifier.width(Space.Related))
                    Text(dateText, style = MaterialTheme.typography.bodyMedium, color = labelColour)
                } else {
                    Text(dateText, style = MaterialTheme.typography.titleMedium, color = timeColour)
                }
                if (day.approximated) {
                    Spacer(Modifier.width(Space.Related))
                    Text(approx, style = MaterialTheme.typography.labelSmall, color = labelColour)
                }
            }
            if (isToday) {
                // Drawn as the Next pill on the Times screen is.
                Text(
                    text = stringResource(R.string.home_today),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(scheme.primary)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                )
            }
        }
        Spacer(Modifier.height(Space.Related))
        slots.chunked(columns).forEachIndexed { rowIndex, rowSlots ->
            if (rowIndex > 0) Spacer(Modifier.height(Space.Related))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowSlots.forEach { slot ->
                    val sunrise = slot == PrayerSlot.SUNRISE
                    val picked = ramadanDay != null && (slot == PrayerSlot.FAJR || slot == PrayerSlot.MAGHRIB)
                    val label = when {
                        ramadanDay != null && slot == PrayerSlot.FAJR -> stringResource(R.string.ramadan_suhoor_ends)
                        ramadanDay != null && slot == PrayerSlot.MAGHRIB -> stringResource(R.string.ramadan_iftar)
                        else -> stringResource(slot.labelRes)
                    }
                    // Sunrise is not a prayer: one step quieter, by weight as well as colour.
                    val textColour = when {
                        sunrise && !isToday -> scheme.onSurfaceVariant
                        picked -> emphasis
                        else -> timeColour
                    }
                    val timeStyle = when {
                        sunrise -> MaterialTheme.typography.bodyMedium
                        picked -> PrayerTimeTextStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        else -> PrayerTimeTextStyle.copy(fontSize = 15.sp)
                    }
                    val time = clock(slot)
                    if (columns == 1) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = if (sunrise && !isToday) scheme.onSurfaceVariant else labelColour, modifier = Modifier.weight(1f))
                            Text(time, style = timeStyle, color = textColour)
                        }
                    } else {
                        Column(Modifier.weight(1f)) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = labelColour)
                            Spacer(Modifier.height(Space.Tight))
                            Text(time, style = timeStyle, color = textColour)
                        }
                    }
                }
                // A short last row keeps its cells the same width as the rows above.
                repeat(columns - rowSlots.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        if (ramadanDay != null) {
            Spacer(Modifier.height(Space.Tight))
            Text(stringResource(R.string.timetable_fast, fast), style = MaterialTheme.typography.labelMedium, color = labelColour)
        }
    }
}

/**
 * Q8 prototype only (STEP-4-DESIGN §6): the Ramadan month as [DayCard]s with the table's footer,
 * for side by side pictures. Nothing wires it; drop it into the Ramadan branch temporarily to see it.
 * ponytail: it opens at the top, not on today; the pictures compare card against table, not scroll.
 */
@Composable
internal fun RamadanCardsPreviewList(state: UiState, onShiftDate: () -> Unit) {
    val info = state.ramadan?.info ?: return
    val coordinates = state.settings.coordinates ?: return
    val prefs = state.settings.calculationPrefs
    val rows by produceState(emptyList<com.sajdatime.app.ui.RamadanRow>(), coordinates, prefs, info) {
        value = withContext(Dispatchers.Default) { RamadanTimetable.rows(coordinates, prefs, info) }
    }
    val today = state.today?.date
    BoxWithConstraints {
        val columns = TimetableGrid.columns((maxWidth - 64.dp).value, LocalDensity.current.fontScale)
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = Space.Section)) {
            items(rows, key = { it.dayNumber }) { row ->
                Column(Modifier.padding(bottom = Space.Paragraph)) {
                    DayCard(day = row.times, isToday = row.date == today, columns = columns, ramadanDay = row.dayNumber)
                }
            }
            item(key = "footer") { Footer(rows = rows, onShiftDate = onShiftDate) }
        }
    }
}
