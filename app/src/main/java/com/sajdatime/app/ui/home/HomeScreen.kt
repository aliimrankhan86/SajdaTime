package com.sajdatime.app.ui.home

import android.content.Intent
import android.os.Build
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.labelRes
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.rememberRemainingText
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.heroStyle
import com.sajdatime.app.ui.theme.sajdaSurface
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
    var exportSheet by rememberSaveable { mutableStateOf(false) }
    var locationSheet by rememberSaveable { mutableStateOf(false) }
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
        Spacer(Modifier.height(24.dp))
        TodayTimeline(state)
        Spacer(Modifier.height(24.dp))
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = city, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(4.dp))
                // A pin alone reads as a label. The caret is what says "you can change
                // this", which is the single most common thing a traveller needs and was
                // previously discoverable only by guessing the city name was a button.
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
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
    val scheme = MaterialTheme.colorScheme

    // Light draws the design's mint-to-sand gradient here; dark draws a flat container.
    // Both are fixed, which is the point — the old gradient changed with the prayer slot,
    // so the contrast under the countdown was different at Isha than at Fajr and could not
    // be asserted. Every stop of this one is checked in ColorContrastTest.
    val hero = heroStyle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(24.dp), hero.brush)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (next == null) {
            Text(
                text = stringResource(R.string.home_no_location),
                style = MaterialTheme.typography.titleMedium,
                color = hero.prominent,
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        // ponytail: letterspaced, not uppercased in code. The design draws this label in
        // capitals, but String.uppercase() on a translated string is a trap — Turkish
        // dotted/dotless i, and scripts with no case at all. If a language wants capitals
        // its translator writes them into the string.
        Text(
            text = stringResource(R.string.home_next_prayer),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp),
            color = hero.label,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))

        // Name and time share one optical line so the block reads as a single
        // centred unit rather than three stacked fragments.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(next.slot.labelRes),
                style = MaterialTheme.typography.headlineMedium,
                color = hero.prominent,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = TimeFormat.clock(context, next.at),
                style = MaterialTheme.typography.headlineMedium,
                color = hero.secondary,
            )
        }

        Spacer(Modifier.height(8.dp))

        // A per-second live region would interrupt a screen reader constantly, so
        // the digits are hidden and a readable summary is announced instead.
        val spokenCountdown = stringResource(
            R.string.home_countdown_a11y,
            stringResource(next.slot.labelRes),
            rememberRemainingText(state.now, next.at),
        )
        Text(
            text = TimeFormat.countdownClock(state.now, next.at),
            style = MaterialTheme.typography.displayLarge,
            color = hero.prominent,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = spokenCountdown
            },
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.home_until),
            style = MaterialTheme.typography.labelMedium,
            color = hero.secondary,
            textAlign = TextAlign.Center,
        )
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

/**
 * "The system is withholding something you asked for." Amber container, amber icon and
 * amber-on-amber text, so it reads as a warning at a glance rather than as one more grey
 * card in a stack of grey cards — which is what it looked like before, and why the exact
 * alarm notice went unnoticed.
 */
@Composable
private fun NoticeCard(title: String, body: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(16.dp), scheme.tertiaryContainer)
            .border(1.dp, scheme.tertiary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = scheme.tertiary,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onTertiaryContainer,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onTertiaryContainer,
            )
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

    val scheme = MaterialTheme.colorScheme
    val slots = PrayerSlot.entries.filter { today.times[it] != null }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(24.dp)),
    ) {
        slots.forEachIndexed { index, slot ->
            PrayerRow(
                slot = slot,
                time = TimeFormat.clock(context, today.times.getValue(slot)),
                isNext = next != null && slot == next.slot && !next.isTomorrow,
            )
            if (index != slots.lastIndex) {
                HorizontalDivider(color = scheme.outlineVariant)
            }
        }
    }
}

/**
 * One row of the day.
 *
 * Three states, and each is carried by more than colour, because "which prayer is next"
 * is the only question this screen has to answer and a colour wash alone answers it for
 * nobody using a greyscale display or a colour-blind palette:
 *
 *  - next    — highlighted surface, a 3dp accent bar down the leading edge, and a "Next"
 *              pill in words
 *  - sunrise — dimmed and set in smaller type, because it is not a prayer and it should
 *              not compete with the five that are
 *  - the rest — plain
 */
@Composable
private fun PrayerRow(slot: PrayerSlot, time: String, isNext: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val dimmed = !slot.isPrayer && !isNext

    val background = when {
        isNext -> scheme.primaryContainer
        dimmed -> scheme.surfaceContainerLow
        else -> Color.Transparent
    }
    val contentColor = when {
        isNext -> scheme.onSurface
        dimmed -> scheme.onSurfaceVariant
        else -> scheme.onSurface
    }
    val iconColor = if (isNext) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val accent = scheme.primary
    val barWidth = with(LocalDensity.current) { 3.dp.toPx() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            // drawBehind rather than a sibling Box: a full-height stripe next to a row
            // whose height is driven by the text needs intrinsic measurement, and this
            // gets the same pixels without asking the layout to measure twice.
            .drawBehind {
                if (isNext) drawRect(accent, size = Size(barWidth, size.height))
            }
            // Vertical padding plus a floor, not a fixed height: at a large system font
            // size the row has to grow rather than crop the prayer name.
            .heightIn(min = 56.dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = slotIcon(slot),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(if (dimmed) 18.dp else 21.dp),
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(slot.labelRes),
            style = if (dimmed) {
                MaterialTheme.typography.bodyMedium
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = contentColor,
            modifier = Modifier.weight(1f),
        )
        if (isNext) {
            // Not colour alone: the next prayer also carries a text marker.
            Text(
                text = stringResource(R.string.home_next_marker),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accent)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = time,
            style = if (dimmed) PrayerTimeTextStyle.copy(fontSize = 15.sp) else PrayerTimeTextStyle,
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
