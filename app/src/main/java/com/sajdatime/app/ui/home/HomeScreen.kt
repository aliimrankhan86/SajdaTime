package com.sajdatime.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Close
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
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.bidiIsolated
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onExport: (PrayerPdfExporter.Range) -> Unit,
    onChangeLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onDismissExactAlarmNotice: () -> Unit,
    onDismissMethodNotice: () -> Unit,
    onOpenMethodSetting: () -> Unit,
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
        PolarBanner(state)
        MethodBanner(state, onOpen = onOpenMethodSetting, onDismiss = onDismissMethodNotice)
        ExactAlarmBanner(state, onDismiss = onDismissExactAlarmNotice)
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
    // Through a resource rather than "$city. $changeLabel": the full stop and the order of
    // the two halves are punctuation decisions, and a translator cannot change either one
    // if they are welded into the code.
    //
    // The city is isolated because it is the one half of this sentence the translator does
    // not own — it comes from the geocoder and stays Latin even in an Arabic build. See
    // core/Bidi.kt.
    val spokenLocation = stringResource(
        R.string.home_location_a11y,
        city.bidiIsolated(),
        stringResource(R.string.action_change_location),
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 10.dp)
            .semantics { contentDescription = spokenLocation },
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
            Text(
                text = dateLine(now),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
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
            // A null `next` means two different things and they need different words: the
            // user has no location, or they have one and its times have not been worked
            // out yet. Only the first is theirs to act on. Telling somebody who set their
            // location months ago to "set your location" is simply false, and on a cold
            // start on a slow device it is on screen long enough to read and believe —
            // observed once on the API 36 emulator, where the card sat there for seconds
            // while DataStore was still loading coordinates that were present all along.
            //
            // So the message is tied to the thing it actually talks about — a missing
            // location — and not to the absence of a calculation. `loading` is still
            // needed alongside it, because until the store emits, `settings` is the
            // default `AppSettings()` and its coordinates are legitimately null.
            if (!state.loading && state.settings.coordinates == null) {
                Text(
                    text = stringResource(R.string.home_no_location),
                    style = MaterialTheme.typography.titleMedium,
                    color = hero.prominent,
                    textAlign = TextAlign.Center,
                )
            }
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
            text = TimeFormat.countdownClock(context, state.now, next.at),
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
                    // isBlank is checked before isolating, so the fallback still fires.
                    cityName.bidiIsolated().ifBlank {
                        stringResource(R.string.location_set_generic)
                    },
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
 *
 * **Closable, and it stays closed.** Telling someone the same thing every time they open
 * the app is nagging, not informing; they have read it, and a warning that cannot be put
 * away teaches people to look past that whole part of the screen — including the Makkah
 * notice sitting directly above it, which they *do* need to act on. Dismissing hides it
 * here only. Settings shows the identical card for as long as the permission is missing,
 * so the fix is always one tap away on the screen whose job is settings.
 */
/**
 * Shown only above the polar circles, where the sun does not rise or set and there is no
 * true Fajr or Isha to calculate.
 *
 * The reference latitude is printed rather than hard-coded into the sentence because it is
 * not a constant: it is 45 degrees under the Islamic Fiqh Council's ruling and 60 under
 * Moonsighting Committee's, so it changes with the method the user picked. See
 * PrayerEngine.polarReferenceLatitude for both sources. Printing the actual number is what
 * lets someone check the app against their mosque instead of taking its word for it.
 *
 * Not dismissible, and deliberately so. Every other notice here describes something the
 * user can fix; this one describes where they live, and it will be just as true tomorrow.
 * Hiding it would leave approximated times looking like measured ones.
 */
@Composable
private fun PolarBanner(state: UiState) {
    val from = state.today?.approximatedFrom ?: return
    Spacer(Modifier.height(12.dp))
    NoticeCard(
        title = stringResource(R.string.polar_notice_title),
        body = stringResource(R.string.polar_notice_body, from.toInt()),
    )
}

/**
 * Offers the calculation-method setting to the users it actually matters to.
 *
 * The complaint behind it: three mosques in the tester's town put Isha **78 minutes**
 * earlier than the app did. The app was not miscalculating — `MOON_SIGHTING` already ships
 * and already matches them to the minute — but onboarding never asks for a method, so a
 * user who does not open Settings can never reach the fix.
 *
 * **Only above [METHOD_NOTICE_LATITUDE].** The worldwide sweep in HANDOVER §10 found the
 * default (Muslim World League) is within about five minutes across South Asia,
 * South-East Asia, the Middle East and Africa, and only diverges nearer the poles, where it
 * reaches +26 minutes in March and +49 in June. Showing this to everyone would hand ~80% of
 * users a question they have no reason to answer. `abs` because latitude bands are
 * symmetric and the Southern Hemisphere is not an afterthought.
 *
 * **Only while the method is still [CalcMethod.AUTO].** A user who has already chosen —
 * even if they chose MWL deliberately — has answered this question, and asking again would
 * second-guess them.
 *
 * It names no method and changes no time. Auto-switching by latitude was considered and
 * rejected: the objection was never that a smart default is wrong, it is that a *silent*
 * one has the app taking a fiqh-adjacent position for the user. A visible prompt is not the
 * same thing. See HANDOVER §11, T1.
 */
@Composable
private fun MethodBanner(state: UiState, onOpen: () -> Unit, onDismiss: () -> Unit) {
    if (state.settings.methodNoticeDismissed) return
    if (state.settings.method != CalcMethod.AUTO) return
    val latitude = state.settings.coordinates?.latitude ?: return
    if (abs(latitude) < METHOD_NOTICE_LATITUDE) return

    Spacer(Modifier.height(12.dp))
    NoticeCard(
        title = stringResource(R.string.method_notice_title),
        body = stringResource(R.string.method_notice_body),
        onClick = onOpen,
        onDismiss = onDismiss,
    )
}

@Composable
private fun ExactAlarmBanner(state: UiState, onDismiss: () -> Unit) {
    val context = LocalContext.current
    if (PrayerAlarmScheduler.canScheduleExact(context)) return
    if (state.settings.exactAlarmNoticeDismissed) return

    Spacer(Modifier.height(12.dp))
    NoticeCard(
        title = stringResource(R.string.settings_exact_alarms_title),
        body = stringResource(R.string.settings_exact_alarms_desc),
        onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
        onDismiss = onDismiss,
    )
}

/**
 * "The system is withholding something you asked for." Amber container, amber icon and
 * amber-on-amber text, so it reads as a warning at a glance rather than as one more grey
 * card in a stack of grey cards — which is what it looked like before, and why the exact
 * alarm notice went unnoticed.
 */
@Composable
private fun NoticeCard(
    title: String,
    body: String,
    // Null when the card only informs. Not every notice has somewhere to go: the polar
    // one states a fact about where the user lives, and a card that looks tappable but
    // does nothing teaches people that tapping cards here is pointless.
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(16.dp), scheme.tertiaryContainer)
            .border(1.dp, scheme.tertiary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
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
        if (onDismiss != null) {
            // Its own clickable inside a clickable row. The row opens the system screen
            // that grants the permission; this closes the card. They must not be the same
            // gesture, and 44dp is the smallest target that is honestly tappable — the
            // 24dp glyph alone would be a trap for anyone with less than perfect aim.
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.notice_dismiss),
                tint = scheme.onTertiaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(role = Role.Button, onClick = onDismiss)
                    .padding(10.dp)
                    .size(24.dp),
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
                isCurrent = slot == state.current,
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
 * Four states, and each is carried by more than colour, because this screen answers two
 * questions — what is coming, and what can I still pray — and a colour wash alone answers
 * neither for anyone on a greyscale display or a colour-blind palette:
 *
 *  - next    — highlighted surface, a 3dp accent bar down the leading edge, and a "Next"
 *              pill in words
 *  - current — a quieter lift off the card and a "Now" pill, deliberately softer than
 *              next: two loud rows next to each other and neither reads as the answer
 *  - sunrise — dimmed and set in smaller type, because it is not a prayer and it should
 *              not compete with the five that are
 *  - the rest — plain
 *
 * `isNext` and `isCurrent` can never both be true for the same row — one time is in the
 * future and the other has passed — so the two treatments cannot stack and the `when`
 * chains below need no tie-break.
 */
@Composable
private fun PrayerRow(slot: PrayerSlot, time: String, isNext: Boolean, isCurrent: Boolean) {
    val scheme = MaterialTheme.colorScheme
    val dimmed = !slot.isPrayer && !isNext

    val background = when {
        isNext -> scheme.primaryContainer
        isCurrent -> scheme.surfaceContainerHigh
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
        if (isCurrent) {
            // Tonal rather than filled, so it reads as a note beside the loud "Next" pill
            // instead of arguing with it. onSecondaryContainer/secondaryContainer is an
            // already-asserted pair in ColorContrastTest, in both themes.
            val nowLabel = stringResource(R.string.home_now_marker_a11y)
            Text(
                text = stringResource(R.string.home_now_marker),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onSecondaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(scheme.secondaryContainer)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
                    // "Now" alone is meaningless read aloud in a list of times.
                    .semantics { contentDescription = nowLabel },
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
 * The header's date line, e.g. "Thu 31 Jul · 16 Safar 1448".
 *
 * Both dates, and no setting to choose between them. This screen used to show the Hijri
 * date alone, which is the one date the phone's own clock cannot give a Muslim user — but
 * it left everyone who reads a Gregorian date with nothing on the screen to anchor to,
 * since the only other reference was the word "Today". Showing both costs one line.
 *
 * A toggle was considered and rejected. `CLAUDE.md` requires this app to work for someone
 * who has never opened Settings, and a preference only helps the people who go looking for
 * it; the ones most likely to be confused by an unfamiliar calendar are exactly the ones
 * who never will. Two dates on one line is not clutter that needs an escape hatch.
 *
 * Falls back to the ordinary date alone if the Hijri date cannot be produced, rather than
 * showing nothing — which is what the old code did on the same failure.
 */
@Composable
private fun dateLine(now: Instant): String {
    val context = LocalContext.current
    // Keyed on the calendar date for the same reason hijriToday is: this header sits
    // beside a live countdown and recomposes about once a second, and building a
    // DateTimeFormatter that often to render a string that changes at midnight is waste.
    val date = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val gregorian = remember(context, date) { TimeFormat.date(context, date) }
    val hijri = hijriToday(now)
    return if (hijri.isBlank()) gregorian
    else stringResource(R.string.home_date_line, gregorian, hijri)
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
    // The month name is isolated and the day and year are not, and that asymmetry is the
    // whole fix — see BidiTest, "the Hijri date reads in the right order". The month is the
    // only strong left-to-right run in "19 Safar 1448"; without a fence the two numbers
    // resolve around it and a right-to-left reader gets 19, 1448, Safar, with the year
    // where the month should be. Wrapping the *whole* date instead, which is the obvious
    // move, is worse: it becomes one foreign block and reads 1448 Safar 19.
    //
    // Measured with java.text.Bidi, not reasoned, and it reproduced the header exactly.
    // Invisible in English, where the paragraph is already left-to-right.
    val name = months.getOrNull(month - 1)?.bidiIsolated() ?: return ""
    return stringResource(R.string.hijri_date_format, day, name, year)
}

/**
 * Where the calculation-method notice starts appearing, in degrees of latitude, either side
 * of the equator.
 *
 * 45 is not a round number chosen for tidiness — it is where the measured divergence starts.
 * Below it the default is within a few minutes of local practice essentially everywhere;
 * above it the gap grows quickly (+26 minutes by March, +49 by June). See HANDOVER §10,
 * "Is Muslim World League actually a bad default?".
 */
private const val METHOD_NOTICE_LATITUDE = 45.0
