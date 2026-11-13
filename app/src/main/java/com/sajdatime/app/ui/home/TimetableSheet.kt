package com.sajdatime.app.ui.home

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.data.placeName
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TimetableGrid
import com.sajdatime.core.TimetableList
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.labelRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Timetable: today and the six days after it as day cards (Ali, 12 Nov 2026; it was this month and
 * the next until then, see [TimetableList]). Save as PDF is the door to the whole month: the caller
 * decides whether that is one tap or the chooser, and the sheet stays open after a save. If today's
 * times vanish while it is open it closes rather than show nothing.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val days by produceState(emptyList<DayPrayerTimes>(), coordinates, prefs, today) {
        value = withContext(Dispatchers.Default) {
            PrayerEngine.computeRange(coordinates, today, TimetableList.DAYS, prefs)
        }
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
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = Space.Section + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                ) {
                    items(days, key = { it.date.toEpochDay() }) { day ->
                        Column(Modifier.padding(bottom = Space.Paragraph)) {
                            DayCard(day = day, isToday = day.date == today, columns = columns)
                        }
                    }
                    days.firstNotNullOfOrNull { it.approximatedFrom }?.let { from ->
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

/**
 * One day: the date, then the six times in a grid of [columns]. Ramadan keeps its table (Ali, 24 Oct
 * 2026, Q8 "Keep the table"), so there is no Ramadan form of this card.
 */
@Composable
internal fun DayCard(day: DayPrayerTimes, isToday: Boolean, columns: Int) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(24.dp)
    val surfaceColour = if (isToday) scheme.primaryContainer else scheme.surface
    val labelColour = if (isToday) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val timeColour = if (isToday) scheme.onPrimaryContainer else scheme.onSurface
    val dateText = TimeFormat.date(context, day.date)
    val slots = PrayerSlot.entries.filter { day.times[it] != null }
    fun clock(slot: PrayerSlot) = day.times[slot]?.let { TimeFormat.clock(context, it) }.orEmpty()
    // ponytail: the sentence has six fixed places, so a slot missing (never today) reads as blank, not skipped.
    val spoken = stringResource(
        R.string.timetable_day_a11y, dateText,
        clock(PrayerSlot.FAJR), clock(PrayerSlot.SUNRISE), clock(PrayerSlot.DHUHR),
        clock(PrayerSlot.ASR), clock(PrayerSlot.MAGHRIB), clock(PrayerSlot.ISHA),
    )
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
                Text(dateText, style = MaterialTheme.typography.titleMedium, color = timeColour)
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
                    val label = stringResource(slot.labelRes)
                    // Sunrise is not a prayer: one step quieter, by weight as well as colour.
                    val textColour = if (sunrise && !isToday) scheme.onSurfaceVariant else timeColour
                    val timeStyle = if (sunrise) MaterialTheme.typography.bodyMedium else PrayerTimeTextStyle.copy(fontSize = 15.sp)
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
    }
}
