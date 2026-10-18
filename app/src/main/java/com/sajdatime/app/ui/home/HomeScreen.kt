package com.sajdatime.app.ui.home

import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.data.placeName
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.components.DialogTitle
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.rounded.Mosque
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
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material.icons.rounded.Brightness5
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sajdatime.app.ui.components.Notice
import androidx.compose.ui.text.font.FontWeight
import com.sajdatime.app.ui.components.BottomBarClearance
import androidx.compose.ui.text.style.TextOverflow
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
import com.sajdatime.app.ui.components.PermissionCard
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.labelRes
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.review.ReviewPrompt
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.rememberRemainingText
import com.sajdatime.app.ui.settings.SettingsChooser
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.heroStyle
import com.sajdatime.app.ui.theme.sajdaSurface
import java.time.Instant
import java.time.ZoneId
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
    onDismissRamadanNotice: (hijriYear: Int) -> Unit,
    onDismissEid: (code: Int) -> Unit,
    onSetRamadanCard: (Boolean) -> Unit,
    /** Opens Settings with the named chooser already showing. See [SettingsChooser]. */
    onOpenSetting: (SettingsChooser) -> Unit,
) {
    var exportSheet by rememberSaveable { mutableStateOf(false) }
    var locationSheet by rememberSaveable { mutableStateOf(false) }
    var explaining by rememberSaveable { mutableStateOf(false) }
    var ramadanInfo by rememberSaveable { mutableStateOf(false) }
    var ramadanTimetable by rememberSaveable { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState()
    // Full height at once: it is a table of thirty rows, and a half open sheet shows five of them.
    val timetableSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Google's rating card, at most at a settled moment (review/ReviewPrompt.kt). Calm means
    // nothing open on top and no warning on screen: a card must not land on a problem. Keyed on
    // all of that, so opening a sheet inside the five seconds cancels the wait.
    val activity = androidx.activity.compose.LocalActivity.current
    val exactWarning = !PrayerAlarmScheduler.canScheduleExact(LocalContext.current) &&
        !state.settings.exactAlarmNoticeDismissed
    val calm = !exportSheet && !locationSheet && !explaining && !ramadanInfo && !ramadanTimetable &&
        state.problem == null && !state.settings.usingDefaultLocation && !exactWarning
    val times = state.today?.times
    LaunchedEffect(calm, times) {
        if (activity == null || times == null) return@LaunchedEffect
        kotlinx.coroutines.delay(5_000)
        ReviewPrompt.onTimesShown(
            activity = activity,
            calmScreen = calm,
            todayTimes = times.values.toList(),
            fajr = times[PrayerSlot.FAJR],
            sunrise = times[PrayerSlot.SUNRISE],
        )
    }

    // The Ramadan option in the export sheet: offered from 30 days before day 1 to the last day,
    // on the user's own calendar (RamadanInfo.offersTimetable), so a family can print it ahead.
    // Keyed on the date and the shift, so it is not recomputed on the clock's every tick.
    val exportToday = state.today?.date
    val exportShift = state.settings.calculationPrefs.hijriOffset
    val ramadanExportYear = remember(exportToday, exportShift) {
        exportToday?.let { day ->
            HijriCalendar.ramadan(day, exportShift)?.takeIf { it.offersTimetable(day) }?.hijriYear
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Order and heights are budgeted so that on a phone at the default font size the
        // whole of this — header, next prayer, all six times, the mosque door — sits above
        // the navigation bar without scrolling. That is why the export action lives on the
        // "Today" line rather than in a 56dp button of its own, and why the far-north
        // notice is the door under the times rather than a card above them (which alone was
        // 185dp and pushed Maghrib and Isha off the screen). The rare banners in between —
        // no location, polar, exact alarms — are allowed to push things down; they are
        // transient and they matter more than the fit.
        //
        // **The budget was set on an emulator and the emulator was the roomy case.** It was
        // measured on 1080x2400 at 420dpi, which is 914dp tall, and passed. The owner's
        // Galaxy S23 Ultra is 1440x3088 at density 3.75 — **823dp**, 91dp less — and there
        // the door was sliced in half by the navigation bar (measured 15 Aug 2026: the
        // content overran the viewport by exactly 137px, 37dp). Logical height does not
        // follow from a screen sounding large; a taller, denser panel can give the app less
        // room than a modest one. If this list grows again, measure on the *short* device,
        // and note that the column stays `verticalScroll` on purpose so that a screen
        // shorter still, or a large font setting, loses nothing — it only has to be reached.
        LocationHeader(state = state, now = state.now, onClick = { locationSheet = true })
        Spacer(Modifier.height(Space.Group))
        NextPrayerCard(state)
        DefaultLocationBanner(state, onFix = { locationSheet = true })
        PolarBanner(state)
        // Below the two banners that say the times may be for the wrong place or approximate,
        // so a card of suhoor and iftar times is never the first thing read about a doubtful
        // number. Present only inside Ramadan and the week before it. During the month the
        // column scrolls on a short phone: for that month this card is the second most read
        // thing on the screen, and the height budget above is spent on it knowingly.
        // The Eid greeting takes the same slot. It cannot meet the Ramadan card: Ramadan has
        // ended on the day before 1 Shawwal, and 10 Dhul Hijjah is months from the next notice.
        EidSection(state = state, onDismiss = onDismissEid)
        RamadanSection(
            state = state,
            onOpenInfo = { ramadanInfo = true },
            onOpenTimetable = { ramadanTimetable = true },
            onShiftDate = { onOpenSetting(SettingsChooser.ADJUSTMENTS) },
            onDismissNotice = onDismissRamadanNotice,
        )
        ExactAlarmBanner(state, onDismiss = onDismissExactAlarmNotice)
        Spacer(Modifier.height(Space.Group))
        TodayTimeline(state, onExport = { exportSheet = true })
        MosqueDifferenceDoor(
            state = state,
            onOpen = { explaining = true },
            onDismissNotice = onDismissMethodNotice,
        )
        // The floating tab bar sits over the bottom of this column (Quiet Glass); this is the
        // room to scroll the mosque button clear of it. Measured, so it grows with the bar.
        Spacer(Modifier.height(8.dp))
        BottomBarClearance()
    }

    if (explaining) {
        MosqueDifferenceDialog(
            onDismiss = { explaining = false },
            onOpen = { chooser ->
                explaining = false
                onOpenSetting(chooser)
            },
        )
    }

    if (ramadanInfo) {
        RamadanInfoDialog(
            onDismiss = { ramadanInfo = false },
            onOpenDateShift = {
                ramadanInfo = false
                onOpenSetting(SettingsChooser.ADJUSTMENTS)
            },
            onHideCard = {
                ramadanInfo = false
                onSetRamadanCard(false)
            },
        )
    }

    if (ramadanTimetable) {
        RamadanTimetableSheet(
            state = state,
            sheetState = timetableSheetState,
            onExport = {
                onExport(PrayerPdfExporter.Range.RAMADAN)
                ramadanTimetable = false
            },
            onShiftDate = {
                ramadanTimetable = false
                onOpenSetting(SettingsChooser.ADJUSTMENTS)
            },
            onDismiss = { ramadanTimetable = false },
        )
    }

    if (exportSheet) {
        ExportSheet(
            sheetState = exportSheetState,
            cityName = state.settings.placeName(LocalContext.current),
            ramadanYear = ramadanExportYear,
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
    val city = state.settings.placeName(LocalContext.current).ifBlank {
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

    // The same large, start aligned title as Qibla and Settings (Quiet Glass, 17 Oct 2026), so
    // the three tabs open the same way. Here the title is the city, because the city is what
    // decides every time on this screen. 4dp in from the column's 16dp, so it sits on the same
    // 20dp line as the other titles.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .semantics { contentDescription = spokenLocation },
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = city,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(2.dp))
                // A name alone reads as a label. The green caret is what says "you can change
                // this", which is the single most common thing a traveller needs.
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Text(
                text = dateLine(now, state.settings.hijriOffsetDays),
                style = MaterialTheme.typography.bodyMedium,
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
    val heroShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // The glossy finish (owner, 18 Oct 2026, matching the widgets): a soft green glow
            // under the light card, then the fixed gradient, a sheen over the top half and a rim
            // that is brightest at the top. Every colour is in HeroStyle and contrast tested.
            .then(
                if (hero.glow == Color.Transparent) {
                    Modifier
                } else {
                    Modifier.shadow(12.dp, heroShape, clip = false, ambientColor = hero.glow, spotColor = hero.glow)
                },
            )
            .sajdaSurface(heroShape, hero.brush)
            .background(Brush.verticalGradient(0f to hero.sheen, 0.5f to Color.Transparent))
            .border(1.dp, Brush.verticalGradient(listOf(hero.rim, hero.rimBottom)), heroShape)
            // 16dp, not 24: part of the height budget in HomeScreen, and the card is the
            // tallest single block on the screen so it is where height is cheapest to find.
            .padding(horizontal = 20.dp, vertical = 16.dp),
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
        // After Isha the next prayer is tomorrow's Fajr, and the widget already says so; the
        // card now does too, on the label line so the 823dp fit is untouched (research F13).
        Text(
            text = if (next.isTomorrow) {
                stringResource(
                    R.string.settings_value_pair,
                    stringResource(R.string.home_next_prayer),
                    stringResource(R.string.widget_tomorrow),
                )
            } else {
                stringResource(R.string.home_next_prayer)
            },
            // Sentence case and medium weight since Quiet Glass (17 Oct 2026), matching the
            // section titles in Settings; the letterspaced capitals read as dated.
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
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

/**
 * The one door every user gets, at the moment they need it — right under the times being
 * doubted, because a user who thinks the app is broken is looking at the times, not at
 * Settings. It names no method and changes no time; the dialog behind it does the
 * explaining and hands over to the chooser that answers.
 *
 * Two forms, one door:
 *
 *  - **Plain**, for everyone: a solid CtaButton (green, white text) with a mosque icon. A
 *    button and not a text link since 15 Aug 2026; outlined until 18 Oct 2026, then tonal for
 *    one day; the owner found both easy to miss and asked for white text on a solid button. It
 *    never goes away, so it must still not nag: no motion, no badge, no colour that changes.
 *  - **Loud**, above 45° of latitude while the method is still AUTO and until dismissed:
 *    the amber notice card. That threshold is where the spread between methods crosses an
 *    hour (78 minutes on Isha in Slough against the mosques' Moonsighting convention).
 *    Until 15 Aug 2026 this was a separate card *above* the times; on a 20:9 phone it was
 *    185dp tall and pushed Maghrib, Isha and every button below the fold, which the owner
 *    reported. It is the same door made loud, and it opens the same explainer — whose
 *    first button is the method chooser — rather than dropping a first-time user straight
 *    into a list of fourteen conventions.
 *
 * Why the plain form is asked of everyone: measured with this app's own engine, the
 * *complaint-sized* gap — ten to twenty minutes on Isha — sits below the 45° line. The
 * default is 10-21 minutes late against ISNA across North America and 6-21 minutes early
 * against Umm al-Qura and Qatar in the Gulf, and none of those users ever sees the loud
 * form. The rest of the world is within a few minutes of its local convention, but is still
 * comparing the app to a mosque board that shows the *congregation*, which is a gap of
 * exactly this size and is supposed to be there.
 *
 * Deliberately not an auto-switch: naming or picking a method for the user would be the app
 * taking a fiqh-adjacent position on their behalf (HANDOVER §11, T1). Asking is not deciding.
 */
@Composable
private fun MosqueDifferenceDoor(state: UiState, onOpen: () -> Unit, onDismissNotice: () -> Unit) {
    if (state.today == null) return
    val latitude = state.settings.coordinates?.latitude
    val loud = !state.settings.methodNoticeDismissed &&
        state.settings.method == CalcMethod.AUTO &&
        latitude != null && abs(latitude) >= METHOD_NOTICE_LATITUDE

    Spacer(Modifier.height(Space.Group))
    if (loud) {
        Notice(
            title = stringResource(R.string.method_notice_title),
            brief = stringResource(R.string.method_notice_body),
            actionLabel = stringResource(R.string.action_see_why),
            onAction = onOpen,
            onDismiss = onDismissNotice,
        )
    } else {
        CtaButton(
            onClick = onOpen,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
        ) {
            Icon(Icons.Rounded.Mosque, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.mosque_diff_link))
        }
    }
}

/**
 * Why the app and the mosque board disagree, in the user's own order of surprise, with the
 * fix for each cause one tap away. The paragraphs are the four causes the project has
 * measured (HANDOVER §10 and §11, "Match your mosque"): the board shows the congregation;
 * Fajr and Isha follow the twilight method; Asr follows the madhab; and a few minutes of
 * mosque precaution are left over. It ends the way the disclaimer does — follow the mosque —
 * because that sentence is the app's whole position and it should be the last thing read.
 *
 * Deliberately not a second disclaimer and not a religious statement about which times are
 * "valid". "Neither is wrong" is as far as it goes.
 */
@Composable
private fun MosqueDifferenceDialog(onDismiss: () -> Unit, onOpen: (SettingsChooser) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { DialogTitle(stringResource(R.string.mosque_diff_title), onDismiss) },
        // Scrollable, like every long dialog here: AlertDialog will not scroll its body for
        // you, and at a raised font size the last paragraph is what falls off the bottom.
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ExplainerParagraph(stringResource(R.string.mosque_diff_congregation), first = true)
                ExplainerParagraph(stringResource(R.string.mosque_diff_twilight))
                ExplainerAction(stringResource(R.string.settings_method)) {
                    onOpen(SettingsChooser.METHOD)
                }
                ExplainerParagraph(stringResource(R.string.mosque_diff_asr))
                ExplainerAction(stringResource(R.string.settings_school)) {
                    onOpen(SettingsChooser.SCHOOL)
                }
                ExplainerParagraph(stringResource(R.string.mosque_diff_minutes))
                ExplainerAction(stringResource(R.string.settings_adjustments)) {
                    onOpen(SettingsChooser.ADJUSTMENTS)
                }
                ExplainerParagraph(stringResource(R.string.mosque_diff_follow))
            }
        },
    )
}

/**
 * A paragraph in an explainer dialog. With [ExplainerAction] it follows the spacing scale
 * (theme/Spacing.kt): a paragraph sits [Space.Group] under whatever came before, and the button
 * that acts on it sits [Space.Related] under it, so each paragraph and its button read as one
 * group. The first paragraph adds nothing, because the dialog already spaces it from the title.
 */
@Composable
internal fun ExplainerParagraph(text: String, first: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = if (first) 0.dp else Space.Group),
    )
}

@Composable
internal fun ExplainerAction(label: String, onClick: () -> Unit) {
    CtaButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Space.Related)
            .heightIn(min = 52.dp),
    ) {
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportSheet(
    sheetState: SheetState,
    cityName: String,
    /** The Hijri year when the Ramadan timetable is on offer today, otherwise null and the option is absent. */
    ramadanYear: Int?,
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

            PrayerPdfExporter.Range.entries
                .filter { it != PrayerPdfExporter.Range.RAMADAN || ramadanYear != null }
                .forEach { range ->
                    ExportOption(range = range, ramadanYear = ramadanYear, onClick = { onPick(range) })
                    HorizontalDivider(
                        Modifier.padding(horizontal = 24.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
        }
    }
}

@Composable
private fun ExportOption(range: PrayerPdfExporter.Range, ramadanYear: Int?, onClick: () -> Unit) {
    val (titleRes, subtitleRes) = when (range) {
        PrayerPdfExporter.Range.TODAY -> R.string.export_today to R.string.export_today_desc
        PrayerPdfExporter.Range.NEXT_7_DAYS -> R.string.export_week to R.string.export_week_desc
        PrayerPdfExporter.Range.THIS_MONTH -> R.string.export_month to R.string.export_month_desc
        PrayerPdfExporter.Range.RAMADAN -> R.string.export_ramadan to R.string.export_ramadan_desc
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(
            text = if (range == PrayerPdfExporter.Range.RAMADAN && ramadanYear != null) {
                stringResource(titleRes, ramadanYear)
            } else {
                stringResource(titleRes)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
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
    Spacer(Modifier.height(Space.Group))
    Notice(
        title = stringResource(R.string.home_default_location_title),
        brief = stringResource(R.string.home_default_location_body),
        actionLabel = stringResource(R.string.action_set_location),
        onAction = onFix,
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
    Spacer(Modifier.height(Space.Group))
    // The alarm sentence is added only when it is both true and about to matter: the user
    // has set at least one alarm, and has not already turned the override on. Without it the
    // only way to learn that today's alarms will not ring is to not be woken by one. With it
    // shown to everyone, most polar users would read a paragraph about a feature they never
    // enabled. See AppSettings.alarmOnApproximateDays.
    val quietened = state.settings.usesAlarm && !state.settings.alarmOnApproximateDays
    // Brief first (where the times come from), the reasoning folded behind it.
    Notice(
        title = stringResource(R.string.polar_notice_title),
        brief = stringResource(R.string.polar_notice_brief, from.toInt()),
        details = stringResource(R.string.polar_notice_body, from.toInt()) +
            if (quietened) "\n\n" + stringResource(R.string.polar_notice_alarm) else "",
    )
}

@Composable
private fun ExactAlarmBanner(state: UiState, onDismiss: () -> Unit) {
    val context = LocalContext.current
    if (PrayerAlarmScheduler.canScheduleExact(context)) return
    if (state.settings.exactAlarmNoticeDismissed) return

    Spacer(Modifier.height(Space.Group))
    PermissionCard(
        title = stringResource(R.string.settings_exact_alarms_title),
        body = stringResource(R.string.settings_exact_alarms_brief),
        details = stringResource(R.string.settings_exact_alarms_desc),
        granted = false,
        onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
        onDismiss = onDismiss,
    )
}

@Composable
private fun TodayTimeline(state: UiState, onExport: () -> Unit) {
    val today = state.today ?: return
    val context = LocalContext.current
    val next = state.next

    // The heading and the export action share one line. The export used to be a 56dp filled
    // button under everything, and on a 20:9 phone it was the thing that fell below the
    // fold; beside the list it exports it costs no height at all and reads as what it is —
    // "save this". A text button with an icon, not an icon alone: icon-only is guesswork.
    // Compact (40dp drawn, 48dp to touch) and 8dp above the list it saves, 16dp under the card:
    // at full size it looked squashed between the two (owner, 18 Oct 2026, screenshot).
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.home_today),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        CtaButton(onClick = onExport, compact = true) {
            Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.Related))
            Text(stringResource(R.string.action_save_pdf))
        }
    }
    Spacer(Modifier.height(Space.Related))

    val scheme = MaterialTheme.colorScheme
    val slots = PrayerSlot.entries.filter { today.times[it] != null }

    // During Ramadan, and only then, Fajr is also where suhoor ends and Maghrib is iftar. The
    // labels come from the same state as the card, so they start with day 1 on the user's own
    // calendar and are gone at midnight into 1 Shawwal with nothing to switch off. They follow
    // the "Ramadan card" setting: someone who is not fasting hid it to stop seeing fasting.
    val inRamadan = state.ramadan?.info?.isRamadan == true && state.settings.ramadanCardEnabled
    val suhoorLabel = stringResource(R.string.ramadan_suhoor_ends)
    val iftarLabel = stringResource(R.string.ramadan_iftar)

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
                note = when {
                    !inRamadan -> null
                    slot == PrayerSlot.FAJR -> suhoorLabel
                    slot == PrayerSlot.MAGHRIB -> iftarLabel
                    else -> null
                },
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
private fun PrayerRow(slot: PrayerSlot, time: String, isNext: Boolean, isCurrent: Boolean, note: String?) {
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
            // One stop for a screen reader: "Fajr, Next, 5:37 am", not three swipes (research D1).
            .semantics(mergeDescendants = true) {}
            .background(background)
            // drawBehind rather than a sibling Box: a full-height stripe next to a row
            // whose height is driven by the text needs intrinsic measurement, and this
            // gets the same pixels without asking the layout to measure twice.
            .drawBehind {
                if (isNext) drawRect(accent, size = Size(barWidth, size.height))
            }
            // Vertical padding plus a floor, not a fixed height: at a large system font
            // size the row has to grow rather than crop the prayer name. 50, not 56, since
            // 15 Aug 2026: six dp a row over six rows is what lets the mosque door under
            // the list clear the navigation bar (see the budget note in HomeScreen). Still
            // above the 48dp touch-target minimum, though these rows are not tappable.
            .heightIn(min = 50.dp)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = slotIcon(slot),
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(if (dimmed) 18.dp else 21.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(slot.labelRes),
                style = if (dimmed) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = contentColor,
            )
            // "Suhoor ends" under Fajr and "Iftar" under Maghrib in Ramadan. A second line and not
            // a pill beside the name: the row already holds the name, a Next or Now marker and the
            // time, and a third item squeezes them at 360dp.
            if (note != null) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.labelMedium,
                    // Both are contrast pairs ColorContrastTest asserts: onPrimaryContainer on the
                    // highlighted row, onSurfaceVariant on the plain and the "Now" rows.
                    color = if (isNext) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
            }
        }
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
    PrayerSlot.FAJR -> Icons.Rounded.Brightness3
    PrayerSlot.SUNRISE -> Icons.Rounded.WbTwilight
    PrayerSlot.DHUHR -> Icons.Rounded.WbSunny
    PrayerSlot.ASR -> Icons.Rounded.Brightness5
    PrayerSlot.MAGHRIB -> Icons.Rounded.Brightness6
    PrayerSlot.ISHA -> Icons.Rounded.NightsStay
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
private fun dateLine(now: Instant, hijriOffsetDays: Int): String {
    val context = LocalContext.current
    // Keyed on the calendar date for the same reason hijriToday is: this header sits
    // beside a live countdown and recomposes about once a second, and building a
    // DateTimeFormatter that often to render a string that changes at midnight is waste.
    val date = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val gregorian = remember(context, date) { TimeFormat.date(context, date) }
    val hijri = hijriToday(now, hijriOffsetDays)
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
private fun hijriToday(now: Instant, offsetDays: Int): String {
    val months = stringArrayResource(R.array.hijri_months)
    // Keyed on the calendar date, not computed once. Left open overnight the header
    // otherwise kept yesterday's Hijri date until the app was killed.
    val date = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val parts = remember(months, date, offsetDays) {
        // The user's own sighting correction. HijriCalendar is the Umm al-Qura calendar,
        // which is calculated and announced from Makkah; a mosque that sighted the moon
        // locally can legitimately be a day either side of it, and the offset is how a
        // user says so. The Ramadan card, the widgets and the engine's Isha rule ask the
        // same object, so none of them can disagree with this line.
        HijriCalendar.of(date, offsetDays)?.let { Triple(it.day, it.month, it.year) }
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
 * Where the mosque door switches to its loud, amber form, in degrees of latitude either
 * side of the equator (`abs`, because latitude bands are symmetric and the Southern
 * Hemisphere is not an afterthought).
 *
 * 45 is not a round number chosen for tidiness — it is where the measured divergence starts.
 * Below it the default (Muslim World League) is within a few minutes of local practice
 * essentially everywhere; above it the gap grows quickly (+26 minutes by March, +49 by June,
 * 78 on Isha in Slough against Moonsighting). Showing the loud form to everyone would hand
 * ~80% of users a question they have no reason to answer. See HANDOVER §10, "Is Muslim
 * World League actually a bad default?".
 */
private const val METHOD_NOTICE_LATITUDE = 45.0
