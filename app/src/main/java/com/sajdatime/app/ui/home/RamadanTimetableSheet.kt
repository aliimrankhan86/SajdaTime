package com.sajdatime.app.ui.home

import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.data.placeName
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.RamadanRow
import com.sajdatime.app.ui.RamadanTimetable
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.core.bidiIsolated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The whole month at a glance for the user's own place: day number, date, when suhoor ends, iftar
 * and the length of the fast, today's row marked. Opened from the Ramadan card and from nowhere
 * else (the PDF is the way to it before the month starts).
 *
 * The rows are the engine's own days (see [RamadanTimetable]) and are computed off the main thread
 * the moment the sheet opens, so the card carries nothing for the 11 months the sheet is closed. If
 * the user's calendar rolls into Shawwal while it is open the sheet closes itself rather than
 * showing a month that has ended.
 *
 * Today's row is marked by a filled background and bold type, not by colour alone. Each row reads
 * as one sentence to a screen reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RamadanTimetableSheet(
    state: UiState,
    sheetState: SheetState,
    onExport: () -> Unit,
    onShiftDate: () -> Unit,
    onDismiss: () -> Unit,
) {
    val info = state.ramadan?.info
    val coordinates = state.settings.coordinates
    if (info == null || coordinates == null) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }
    val prefs = state.settings.calculationPrefs
    val rows by produceState(emptyList<RamadanRow>(), coordinates, prefs, info) {
        value = withContext(Dispatchers.Default) { RamadanTimetable.rows(coordinates, prefs, info) }
    }
    val todayDate = state.today?.date
    val todayIndex = rows.indexOfFirst { it.date == todayDate }
    val listState = rememberLazyListState()
    // Opens with today near the top, once, when the rows first arrive.
    LaunchedEffect(rows.isNotEmpty()) {
        if (rows.isNotEmpty() && todayIndex > 0) listState.scrollToItem((todayIndex - 2).coerceAtLeast(0))
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 24.dp, end = 12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ramadan_timetable_title, info.hijriYear),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        // isBlank is checked before isolating, so the fallback still fires.
                        text = state.settings.placeName(LocalContext.current).bidiIsolated().ifBlank {
                            stringResource(R.string.pdf_place_unknown)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.pdf_area_label),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                CtaButton(onClick = onExport) {
                    Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.action_save_pdf))
                }
            }
            Spacer(Modifier.height(12.dp))
            HeaderRow()
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(rows, key = { it.dayNumber }) { row ->
                    TimetableRow(row = row, isToday = row.date == todayDate)
                }
                item(key = "footer") {
                    Footer(rows = rows, onShiftDate = onShiftDate)
                }
            }
        }
    }
}

// Column geometry shared by the header and the rows so they cannot drift apart. The two time
// columns share what is left. At 360dp wide the sheet leaves each about 78dp, which holds
// "12:15 pm" at 15sp; at a large font the cell wraps instead of clipping.
private val DayWidth = 28.dp
private val DateWidth = 84.dp
private val FastWidth = 60.dp

@Composable
private fun HeaderRow() {
    val style = MaterialTheme.typography.labelMedium
    val colour = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(stringResource(R.string.ramadan_col_day), style = style, color = colour, modifier = Modifier.width(DayWidth))
        Text(stringResource(R.string.pdf_column_date), style = style, color = colour, modifier = Modifier.width(DateWidth))
        Text(
            stringResource(R.string.ramadan_suhoor_ends),
            style = style,
            color = colour,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
        Text(
            stringResource(R.string.ramadan_iftar),
            style = style,
            color = colour,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
        Text(stringResource(R.string.ramadan_col_fast), style = style, color = colour, modifier = Modifier.width(FastWidth))
    }
}

@Composable
private fun TimetableRow(row: RamadanRow, isToday: Boolean) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme
    val colour = if (isToday) scheme.onPrimaryContainer else scheme.onSurface
    val weight = if (isToday) FontWeight.Bold else null
    val date = TimeFormat.date(context, row.date)
    val suhoor = TimeFormat.clock(context, row.suhoorEnds)
    val iftar = TimeFormat.clock(context, row.iftar)
    val fast = TimeFormat.remaining(context, row.suhoorEnds, row.iftar)
    val spoken = stringResource(R.string.ramadan_row_a11y, row.dayNumber, date, suhoor, iftar, fast)
    val body = MaterialTheme.typography.bodyMedium.let { if (weight != null) it.copy(fontWeight = weight) else it }
    val timeStyle = PrayerTimeTextStyle.copy(fontSize = 15.sp).let { if (weight != null) it.copy(fontWeight = weight) else it }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isToday) scheme.primaryContainer else Color.Transparent)
            .semantics(mergeDescendants = true) { contentDescription = spoken }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(row.dayNumber.toString(), style = body, color = colour, modifier = Modifier.width(DayWidth))
        Text(date, style = body, color = colour, modifier = Modifier.width(DateWidth))
        TimeCell(suhoor, row.approximated, timeStyle, colour, Modifier.weight(1f))
        TimeCell(iftar, row.approximated, timeStyle, colour, Modifier.weight(1f))
        Text(
            fast,
            style = body,
            color = if (isToday) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
            modifier = Modifier.width(FastWidth),
        )
    }
}

/** A time, and under it "approx." on a projected polar day, so no row presents a guess as a measurement. */
@Composable
private fun TimeCell(
    time: String,
    approximated: Boolean,
    style: TextStyle,
    colour: Color,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(time, style = style, color = colour)
        if (approximated) {
            Text(
                text = stringResource(R.string.ramadan_approx_marker),
                style = MaterialTheme.typography.labelSmall,
                color = colour,
            )
        }
    }
}

@Composable
private fun Footer(rows: List<RamadanRow>, onShiftDate: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = stringResource(R.string.pdf_ramadan_note),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant,
        )
        rows.firstNotNullOfOrNull { it.times.approximatedFrom }?.let { from ->
            Text(
                text = stringResource(R.string.pdf_approximate_note, from.toInt()),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        CtaButton(onClick = onShiftDate) {
            Text(stringResource(R.string.settings_hijri_offset_label))
        }
    }
}
