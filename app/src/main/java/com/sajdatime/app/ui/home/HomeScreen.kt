package com.sajdatime.app.ui.home

import android.content.Intent
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.background
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.rememberRemainingText
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import java.time.Instant
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onExport: (PrayerPdfExporter.Range) -> Unit,
    onChangeLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    var exportSheet by remember { mutableStateOf(false) }
    var locationSheet by remember { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LocationHeader(state = state, now = state.now, onClick = { locationSheet = true })
        Spacer(Modifier.height(12.dp))
        NextPrayerCard(state)
        DefaultLocationBanner(state, onFix = { locationSheet = true })
        ExactAlarmBanner()
        Spacer(Modifier.height(20.dp))
        TodayTimeline(state)
        Spacer(Modifier.height(20.dp))
        ExportButton(enabled = state.today != null, onClick = { exportSheet = true })
    }

    if (exportSheet) {
        ExportSheet(
            sheetState = exportSheetState,
            cityName = state.settings.cityName,
            onDismiss = { exportSheet = false },
            onPick = { range ->
                onExport(range)
                exportSheet = false
            },
        )
    }

    if (locationSheet) {
        // Close as soon as a location actually lands. Leaving the sheet up after a
        // successful search hides the very times the user just changed.
        val opened = remember { state.settings.coordinates to state.settings.cityName }
        val current = state.settings.coordinates to state.settings.cityName
        LaunchedEffect(current) {
            if (current != opened) locationSheet = false
        }

        LocationSheet(
            state = state,
            onDismiss = { locationSheet = false },
            onUseGps = onChangeLocation,
            onSearchCity = onSearchCity,
        )
    }
}

/**
 * The city is a button, not a label. Changing location is the most common thing a
 * traveller needs, and burying it in Settings makes them hunt for it.
 */
@Composable
private fun LocationHeader(state: UiState, now: Instant, onClick: () -> Unit) {
    val city = state.settings.cityName.ifBlank {
        stringResource(R.string.location_set_generic)
    }
    val changeLabel = stringResource(R.string.action_change_location)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp)
            .semantics { contentDescription = "$city. $changeLabel" },
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = city, style = MaterialTheme.typography.titleMedium)
            val hijri = hijriToday(now)
            if (hijri.isNotBlank()) {
                Text(
                    text = hijri,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NextPrayerCard(state: UiState) {
    val next = state.next
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient(next?.slot)),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (next == null) {
                    Text(
                        text = stringResource(R.string.home_no_location),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    )
                    return@Column
                }

                Text(
                    text = stringResource(R.string.home_next_prayer),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))

                // Name and time share one optical line so the block reads as a single
                // centred unit rather than three stacked fragments.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = next.slot.label,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = TimeFormat.clock(context, next.at),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                    )
                }

                Spacer(Modifier.height(18.dp))

                // A per-second live region would interrupt a screen reader constantly, so
                // the digits are hidden and a readable summary is announced instead.
                val spokenCountdown = stringResource(
                    R.string.home_countdown_a11y,
                    next.slot.label,
                    rememberRemainingText(state.now, next.at),
                )
                Text(
                    text = TimeFormat.countdownClock(state.now, next.at),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = spokenCountdown
                    },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_until),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun ExportButton(enabled: Boolean, onClick: () -> Unit) {
    // A plain full-width button rather than a floating one: it never covers a prayer
    // time, and the label can say what actually happens.
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
    ) {
        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.action_save_timetable))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportSheet(
    sheetState: SheetState,
    cityName: String,
    onDismiss: () -> Unit,
    onPick: (PrayerPdfExporter.Range) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Text(
                text = stringResource(R.string.export_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .semantics { heading() },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    R.string.export_sheet_body,
                    cityName.ifBlank { stringResource(R.string.location_set_generic) },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(16.dp))

            PrayerPdfExporter.Range.entries.forEach { range ->
                ExportOption(range = range, onClick = { onPick(range) })
                HorizontalDivider(
                    Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun ExportOption(range: PrayerPdfExporter.Range, onClick: () -> Unit) {
    val (titleRes, subtitleRes) = when (range) {
        PrayerPdfExporter.Range.TODAY -> R.string.export_today to R.string.export_today_desc
        PrayerPdfExporter.Range.NEXT_7_DAYS -> R.string.export_week to R.string.export_week_desc
        PrayerPdfExporter.Range.THIS_MONTH -> R.string.export_month to R.string.export_month_desc
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(text = stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(subtitleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Says plainly that the times on screen are not for where the user actually is. */
@Composable
private fun DefaultLocationBanner(state: UiState, onFix: () -> Unit) {
    if (!state.settings.usingDefaultLocation) return
    Spacer(Modifier.height(12.dp))
    NoticeCard(
        title = stringResource(R.string.home_default_location_title),
        body = stringResource(R.string.home_default_location_body),
        onClick = onFix,
    )
}

/**
 * Shown only when the OS is withholding exact alarms. Prayer alerts still arrive, but
 * possibly a few minutes late, and the user deserves to know that rather than quietly
 * receiving a late Fajr.
 */
@Composable
private fun ExactAlarmBanner() {
    val context = LocalContext.current
    if (PrayerAlarmScheduler.canScheduleExact(context)) return

    Spacer(Modifier.height(12.dp))
    NoticeCard(
        title = stringResource(R.string.settings_exact_alarms_title),
        body = stringResource(R.string.settings_exact_alarms_desc),
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runCatching {
                    context.startActivity(
                        Intent(SystemSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            .setData("package:${context.packageName}".toUri()),
                    )
                }
            }
        },
    )
}

@Composable
private fun NoticeCard(title: String, body: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TodayTimeline(state: UiState) {
    val today = state.today ?: return
    val context = LocalContext.current
    val next = state.next

    Text(
        text = stringResource(R.string.home_today),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .semantics { heading() },
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            PrayerSlot.entries.forEach { slot ->
                val at = today.times[slot] ?: return@forEach
                val isNext = next != null && slot == next.slot && !next.isTomorrow
                PrayerRow(
                    slot = slot,
                    time = TimeFormat.clock(context, at),
                    isNext = isNext,
                )
            }
        }
    }
}

@Composable
private fun PrayerRow(slot: PrayerSlot, time: String, isNext: Boolean) {
    val background = if (isNext) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val contentColor = if (isNext) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .background(background, RoundedCornerShape(12.dp))
            // Vertical padding, not a fixed height: at a large system font size the row
            // has to grow rather than crop the prayer name.
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .heightIn(min = 52.dp),
    ) {
        Icon(
            imageVector = slotIcon(slot),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = slot.label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (isNext) {
            // Not colour alone: the next prayer also carries a text marker.
            Text(
                text = stringResource(R.string.home_next_marker),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = time,
            style = PrayerTimeTextStyle,
            color = contentColor,
        )
    }
}

private fun slotIcon(slot: PrayerSlot): ImageVector = when (slot) {
    PrayerSlot.FAJR -> Icons.Filled.Brightness3
    PrayerSlot.SUNRISE -> Icons.Filled.WbTwilight
    PrayerSlot.DHUHR -> Icons.Filled.WbSunny
    PrayerSlot.ASR -> Icons.Filled.Brightness5
    PrayerSlot.MAGHRIB -> Icons.Filled.Brightness6
    PrayerSlot.ISHA -> Icons.Filled.NightsStay
}

/**
 * The hero gradient shifts with the time of day so the card feels like the sky outside:
 * cool before dawn, warm at Maghrib, deep at Isha.
 */
@Composable
private fun heroGradient(slot: PrayerSlot?): Brush {
    val scheme = MaterialTheme.colorScheme
    val end = when (slot) {
        PrayerSlot.MAGHRIB, PrayerSlot.ISHA -> scheme.tertiary.copy(alpha = 0.28f)
        else -> scheme.primary.copy(alpha = 0.22f)
    }
    return Brush.verticalGradient(listOf(scheme.primaryContainer, end))
}

/**
 * Hijri date, e.g. "16 Safar 1448".
 *
 * Month names come from a string array rather than DateTimeFormatter: the Islamic
 * chronology has no month-name data on several Android versions, which renders the
 * month as a bare number.
 */
@Composable
private fun hijriToday(now: Instant): String {
    val months = stringArrayResource(R.array.hijri_months)
    // Keyed on the calendar date, not computed once. Left open overnight the header
    // otherwise kept yesterday's Hijri date until the app was killed.
    val date = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val parts = remember(months, date) {
        runCatching {
            val today = HijrahDate.from(date)
            Triple(
                today.get(ChronoField.DAY_OF_MONTH),
                today.get(ChronoField.MONTH_OF_YEAR),
                today.get(ChronoField.YEAR),
            )
        }.getOrNull()
    } ?: return ""

    val (day, month, year) = parts
    val name = months.getOrNull(month - 1) ?: return ""
    return stringResource(R.string.hijri_date_format, day, name, year)
}
