package com.sajdatime.app.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.core.PrayerSlot
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.UiState
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
    onOpenSettings: () -> Unit,
    onExport: (PrayerPdfExporter.Range) -> Unit,
) {
    var exportSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.settings.cityName.ifBlank {
                                stringResource(R.string.location_set_generic)
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = hijriToday(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.title_settings),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            if (state.today != null) {
                ExtendedFloatingActionButton(
                    onClick = { exportSheet = true },
                    icon = {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                    },
                    text = { Text(stringResource(R.string.action_export_pdf)) },
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                // Keep the last row clear of the floating action button.
                .padding(bottom = 96.dp),
        ) {
            NextPrayerCard(state)
            ExactAlarmBanner()
            Spacer(Modifier.height(24.dp))
            TodayTimeline(state)
        }
    }

    if (exportSheet) {
        ModalBottomSheet(
            onDismissRequest = { exportSheet = false },
            sheetState = sheetState,
        ) {
            Column(Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.export_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .semantics { heading() },
                )
                PrayerPdfExporter.Range.entries.forEach { range ->
                    ListItem(
                        headlineContent = { Text(exportRangeLabel(range)) },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable(role = Role.Button) {
                                onExport(range)
                                exportSheet = false
                            },
                    )
                }
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
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = next.slot.label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.semantics { heading() },
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = TimeFormat.clock(context, next.at),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(20.dp))
                // A per-second live region would interrupt a screen reader constantly,
                // so the digits are hidden and a readable summary is announced instead.
                val spokenCountdown = stringResource(
                    R.string.home_countdown_a11y,
                    next.slot.label,
                    rememberRemainingText(state.now, next.at),
                )
                Text(
                    text = TimeFormat.countdownClock(state.now, next.at),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription = spokenCountdown
                    },
                )
            }
        }
    }
}

/**
 * Shown only when the OS is withholding exact alarms. Prayer alerts still arrive, but
 * possibly a few minutes late — the user deserves to know that rather than quietly
 * receiving a late Fajr, so this states the consequence and links to the fix.
 */
@Composable
private fun ExactAlarmBanner() {
    val context = LocalContext.current
    if (PrayerAlarmScheduler.canScheduleExact(context)) return

    Spacer(Modifier.height(16.dp))
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    runCatching {
                        context.startActivity(
                            Intent(SystemSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                .setData(Uri.parse("package:${context.packageName}")),
                        )
                    }
                }
            },
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
                Text(
                    text = stringResource(R.string.settings_exact_alarms_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.settings_exact_alarms_desc),
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
    val currentSlot = state.next?.slot

    Text(
        text = stringResource(R.string.home_today),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
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
                val isNext = slot == currentSlot && state.next?.isTomorrow == false
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
            .padding(horizontal = 12.dp)
            .height(52.dp),
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

@Composable
private fun exportRangeLabel(range: PrayerPdfExporter.Range): String = stringResource(
    when (range) {
        PrayerPdfExporter.Range.TODAY -> R.string.export_today
        PrayerPdfExporter.Range.NEXT_7_DAYS -> R.string.export_week
        PrayerPdfExporter.Range.THIS_MONTH -> R.string.export_month
    },
)

/**
 * Hijri date for the top bar, e.g. "16 Safar 1448".
 *
 * Month names come from a string array rather than DateTimeFormatter: the Islamic
 * chronology has no month-name data on several Android versions, which renders the
 * month as a bare number. Returns an empty string if the chronology is unavailable
 * at all, in which case the top bar simply shows the city.
 */
@Composable
private fun hijriToday(): String {
    val months = stringArrayResource(R.array.hijri_months)
    val parts = remember(months) {
        runCatching {
            val today = HijrahDate.from(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate())
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
