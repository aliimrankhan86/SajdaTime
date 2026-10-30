package com.sajdatime.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Brightness3
import androidx.compose.material.icons.rounded.Brightness5
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.placeName
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.review.ReviewPrompt
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.Bell
import com.sajdatime.app.ui.components.shrinkToFit
import com.sajdatime.app.ui.components.BottomBarClearance
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.DialogTitle
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.Notice
import com.sajdatime.app.ui.components.PermissionCard
import com.sajdatime.app.ui.components.readingWidth
import com.sajdatime.app.ui.components.rememberRemainingText
import com.sajdatime.app.ui.settings.SettingsChooser
import com.sajdatime.app.ui.theme.Motion
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.theme.heroStyle
import com.sajdatime.app.ui.theme.sajdaSurface
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.JamaahResult
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sky
import com.sajdatime.core.SkyPeriod
import com.sajdatime.core.bidiIsolated
import com.sajdatime.core.labelRes
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
    /** A user's tap on a prayer's bell: the prayer and whether it is now on (MainScaffold shows the message). */
    onBell: (PrayerSlot, Boolean) -> Unit,
    /** The prayer whose bell Undo just restored, which snaps instead of fading. */
    snapBell: PrayerSlot?,
    /** True while the bell's message is up, so the rating card never lands over Undo. */
    bellMessageShowing: Boolean,
    /** The phone is not letting SajdaTime post notifications at all. */
    notificationsBlocked: Boolean,
    onAllowNotifications: () -> Unit,
    onDismissNotificationsNotice: () -> Unit,
) {
    var exportSheet by rememberSaveable { mutableStateOf(false) }
    // One flag for both Timetable views: the month sheet, or the Ramadan table inside Ramadan (see ramadanView).
    var timetable by rememberSaveable { mutableStateOf(false) }
    var locationSheet by rememberSaveable { mutableStateOf(false) }
    var explaining by rememberSaveable { mutableStateOf(false) }
    var ramadanInfo by rememberSaveable { mutableStateOf(false) }
    var islamicDates by rememberSaveable { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState()
    // Full height at once: it is a table of thirty rows, and a half open sheet shows five of them.
    val timetableSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Google's rating card, at most at a settled moment (review/ReviewPrompt.kt). Calm means
    // nothing open on top and no warning on screen: a card must not land on a problem. Keyed on
    // all of that, so opening a sheet inside the five seconds cancels the wait.
    val activity = androidx.activity.compose.LocalActivity.current
    val exactWarning = !PrayerAlarmScheduler.canScheduleExact(LocalContext.current) &&
        !state.settings.exactAlarmNoticeDismissed
    // The card's calm timer lives only here: this screen is not composed while another tab shows,
    // so a tick on My prayers can never start it (sign-off, 23 Oct 2026, SIGNOFF.md:52).
    val notificationsWarning = notificationsBlocked && state.settings.alertFor.isNotEmpty() &&
        !state.settings.notificationsNoticeDismissed
    val calm = !bellMessageShowing && !notificationsWarning && !exportSheet && !timetable && !locationSheet && !explaining && !ramadanInfo && !islamicDates &&
        state.problem == null && !state.settings.usingDefaultLocation && !exactWarning
    // During Ramadan, with the Ramadan card on, the Timetable opens the Ramadan table; otherwise the month
    // view. The same test as the suhoor and iftar labels on the Times rows.
    val ramadanView = state.ramadan?.info?.isRamadan == true && state.settings.ramadanCardEnabled
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

    // Review fix (24 Oct): the Scaffold no longer reserves the top inset, so the scroll would carry the
    // city and card under the clock icons at large text. A fixed strip in the sky's top colour keeps
    // the bar readable; at scroll 0 it matches the sky under it, so it is invisible.
    val skyColours = rememberSkyColours(Sky.periodOf(state.current))
    Box(Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Order and heights are budgeted so that on a phone at the default font size the
        // whole of this — header, next prayer, all six times, the mosque door — sits above
        // the navigation bar without scrolling. That is why the Timetable (with Save as PDF
        // inside it) opens from the "Today" line rather than a 56dp button of its own, and why the far-north
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
        //
        // **Since 1.6 (steps 1 to 4) the budget no longer holds at 823dp, by decision.** At 914dp and taller
        // everything still fits; at 823dp the mosque door is the one thing that scrolls, about 60dp short,
        // and must never need more than one short scroll. BottomBarClearance under it keeps the end of
        // scroll clear of the glass bar. Fable judged it (24 Oct 2026): no honest Space tightening buys
        // 60dp, and moving the door beside Timetable breaks one solid button per line; content passing
        // under a glass bar is how Quiet Glass and iOS tab bars both behave.
        //
        // The sky (1.6, step 5) is a backdrop behind the header and the card, edge to edge and under the
        // status bar; the card itself is unchanged. The side padding and the 600dp reading column live on
        // the inner columns, because the sky has to run out to both edges.
        Box(Modifier.fillMaxWidth().skyBackdrop(skyColours).statusBarsPadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.padding(horizontal = 16.dp).readingWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LocationHeader(state = state, now = state.now, onClick = { locationSheet = true }, onOpenDates = { islamicDates = true })
                // Tight, not Group, since 1.6.0: the date line's 48dp target already leaves about 14dp of
                // its own under the text, so the card still sits a Group's distance from the words.
                Spacer(Modifier.height(Space.Tight))
                NextPrayerCard(state)
            }
        }
        Column(
            Modifier.padding(horizontal = 16.dp).readingWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        DefaultLocationBanner(state, onFix = { locationSheet = true })
        PolarBanner(state)
        // Below the two banners that say the times may be for the wrong place or approximate,
        // so a card of suhoor and iftar times is never the first thing read about a doubtful
        // number. Present only inside Ramadan and the 3 days before it. During the month the
        // column scrolls on a short phone: for that month this card is the second most read
        // thing on the screen, and the height budget above is spent on it knowingly.
        // The Eid greeting takes the same slot. It cannot meet the Ramadan card: Ramadan has
        // ended on the day before 1 Shawwal, and 10 Dhul Hijjah is months from the next notice.
        EidSection(state = state, onDismiss = onDismissEid)
        RamadanSection(
            state = state,
            onOpenInfo = { ramadanInfo = true },
            onOpenTimetable = { timetable = true },
            onShiftDate = { onOpenSetting(SettingsChooser.ADJUSTMENTS) },
            onDismissNotice = onDismissRamadanNotice,
        )
        ExactAlarmBanner(state, onDismiss = onDismissExactAlarmNotice)
        NotificationsBanner(visible = notificationsWarning, onAllow = onAllowNotifications, onDismiss = onDismissNotificationsNotice)
        Spacer(Modifier.height(Space.Group))
        TodayTimeline(
            state,
            onTimetable = { timetable = true },
            ramadanView = ramadanView,
            onBell = onBell,
            snapBell = snapBell,
            notificationsBlocked = notificationsBlocked,
        )
        MosqueDifferenceDoor(
            state = state,
            onOpen = { explaining = true },
            onDismissNotice = onDismissMethodNotice,
        )
        // The floating tab bar sits over the bottom of this column (Quiet Glass); this is the
        // room to scroll the mosque button clear of it. Measured, so it grows with the bar.
        Spacer(Modifier.height(Space.Related))
        BottomBarClearance()
        }
    }
        Box(
            Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)
                .drawBehind { drawRect(skyColours().top) },
        )
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

    if (timetable) {
        if (ramadanView) {
            RamadanTimetableSheet(
                state = state,
                sheetState = timetableSheetState,
                // The chooser, not the Ramadan PDF straight away: this table is now the only door to
                // Save as PDF in Ramadan, and Today, 7 days and the month must stay reachable (Q7; Ali,
                // 24 Oct 2026, "Show the choices"). The chooser offers Ramadan too in Ramadan.
                onExport = { exportSheet = true },
                onShiftDate = {
                    timetable = false
                    onOpenSetting(SettingsChooser.ADJUSTMENTS)
                },
                onDismiss = { timetable = false },
            )
        } else {
            // The chooser below opens over this sheet and the sheet stays after a save.
            TimetableSheet(
                state = state,
                sheetState = timetableSheetState,
                onExport = { exportSheet = true },
                onDismiss = { timetable = false },
            )
        }
    }

    if (islamicDates) {
        IslamicDatesSheet(
            today = state.today?.date ?: state.now.atZone(ZoneId.systemDefault()).toLocalDate(),
            offsetDays = state.settings.calculationPrefs.hijriOffset,
            sect = state.settings.sect,
            onShiftDate = {
                islamicDates = false
                onOpenSetting(SettingsChooser.ADJUSTMENTS)
            },
            onDismiss = { islamicDates = false },
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
internal fun LocationHeader(state: UiState, now: Instant, onClick: () -> Unit, onOpenDates: () -> Unit) {
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
    //
    // Two targets since 1.6.0, not one: the city opens the location sheet as before, and the date
    // line under it opens the Islamic dates sheet (sign-off, 23 Oct 2026: the Hijri half in primary,
    // green meaning tappable, and no extra caret). The 6dp the whole row used to have above and
    // below is split between them, so the header is no taller than the date line's own target.
    Column(Modifier.fillMaxWidth().padding(top = Space.Tight)) {
        // The sky mark (step 5) sits at the end of this row as a sibling of the clickable city, never inside
        // it: clickable merges its descendants, so a mark inside would be read as part of "London, Change
        // location" and a tap on it would open the sheet. It hides when the name needs the room (Sky.markFits).
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val measurer = rememberTextMeasurer()
            val style = MaterialTheme.typography.headlineMedium
            val nameWidth = remember(city, style, density) { measurer.measure(city, style, maxLines = 1).size.width.toFloat() }
            val today = state.today
            val showMark = today != null &&
                Sky.markFits(nameWidth, constraints.maxWidth.toFloat(), density.density, density.fontScale)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = !showMark)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(role = Role.Button, onClick = onClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .semantics { contentDescription = spokenLocation },
                ) {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        // A long town name, or one in a tall script, shrinks before it is cut short.
                        autoSize = shrinkToFit(MaterialTheme.typography.headlineMedium.fontSize),
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
                if (showMark && today != null) {
                    val context = LocalContext.current
                    val period = Sky.periodOf(state.current)
                    // Once a minute, not on the clock's every second tick.
                    val minute = now.epochSecond / 60
                    val progress = remember(minute, today) { Sky.progress(now, today.prayersOnly) }
                    val periodName = stringResource(
                        when (period) {
                            SkyPeriod.DAWN -> R.string.sky_dawn
                            SkyPeriod.DAY -> R.string.sky_day
                            SkyPeriod.EVENING -> R.string.sky_evening
                            SkyPeriod.NIGHT -> R.string.sky_night
                        },
                    )
                    val end = Sky.endOf(period, today, now, state.next?.takeIf { it.slot == PrayerSlot.FAJR }?.at)
                    val spoken = end?.let { stringResource(R.string.sky_now_a11y, periodName, TimeFormat.clock(context, it)) } ?: periodName
                    Spacer(Modifier.width(8.dp))
                    SkyMark(period = period, progress = progress, spoken = spoken)
                }
            }
        }
        DateLine(now, state.settings.hijriOffsetDays, onOpenDates)
    }
}

/**
 * The header's date line, e.g. "Thu 31 Jul · 16 Safar 1448", and the door to the Islamic dates sheet.
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
 * showing nothing — and then it is not tappable, because the sheet would have nothing to list.
 */
@Composable
private fun DateLine(now: Instant, hijriOffsetDays: Int, onOpenDates: () -> Unit) {
    val context = LocalContext.current
    // Keyed on the calendar date for the same reason hijriToday is: this header sits
    // beside a live countdown and recomposes about once a second, and building a
    // DateTimeFormatter that often to render a string that changes at midnight is waste.
    val date = now.atZone(ZoneId.systemDefault()).toLocalDate()
    val gregorian = remember(context, date) { TimeFormat.date(context, date) }
    val hijri = hijriToday(now, hijriOffsetDays)
    val scheme = MaterialTheme.colorScheme
    if (hijri.isBlank()) {
        Text(
            text = gregorian,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        return
    }
    val line = stringResource(R.string.home_date_line, gregorian, hijri)
    val spoken = stringResource(R.string.dates_header_a11y, hijri)
    val styled = remember(line, hijri, scheme.primary) {
        androidx.compose.ui.text.buildAnnotatedString {
            append(line)
            val at = line.indexOf(hijri)
            if (at >= 0) addStyle(androidx.compose.ui.text.SpanStyle(color = scheme.primary), at, at + hijri.length)
        }
    }
    Text(
        text = styled,
        style = MaterialTheme.typography.bodyMedium,
        color = scheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onOpenDates)
            .semantics { contentDescription = spoken }
            // 48dp, the touch target minimum, with the text centred in it.
            .heightIn(min = 48.dp)
            .wrapContentHeight()
            .padding(horizontal = 4.dp),
    )
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
        Spacer(Modifier.height(Space.Related))

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

        Spacer(Modifier.height(Space.Related))

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
        Spacer(Modifier.height(Space.Tight))
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
                // 1.6.0: the congregation time itself, the most direct answer to "the board says 1:30".
                ExplainerAction(stringResource(R.string.mosque_diff_add_jamaah)) {
                    onOpen(SettingsChooser.JAMAAH)
                }
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
        Column(Modifier.padding(bottom = Space.Section)) {
            Text(
                text = stringResource(R.string.export_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .semantics { heading() },
            )
            Spacer(Modifier.height(Space.Tight))
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
            Spacer(Modifier.height(Space.Group))

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
    val show = !PrayerAlarmScheduler.canScheduleExact(context) && !state.settings.exactAlarmNoticeDismissed

    // No entrance; closing fades and folds away so the cards below do not jump (24 Oct 2026).
    AnimatedVisibility(show, enter = EnterTransition.None, exit = fadeOut(Motion.quick()) + shrinkVertically(Motion.quick())) {
        Column {
            Spacer(Modifier.height(Space.Group))
            PermissionCard(
                title = stringResource(R.string.settings_exact_alarms_title),
                body = stringResource(R.string.settings_exact_alarms_brief),
                granted = false,
                onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
                onDismiss = onDismiss,
            )
        }
    }
}

/**
 * Shown while the phone blocks notifications and at least one alert is on (Ali, 24 Oct 2026): without it a
 * user who refused them at setup sees five bells and never learns why nothing arrives. Closable and it stays
 * closed, like the exact alarm notice; a bell turned on while still blocked brings it back.
 */
@Composable
private fun NotificationsBanner(visible: Boolean, onAllow: () -> Unit, onDismiss: () -> Unit) {
    AnimatedVisibility(visible, enter = EnterTransition.None, exit = fadeOut(Motion.quick()) + shrinkVertically(Motion.quick())) {
        Column {
            Spacer(Modifier.height(Space.Group))
            PermissionCard(
                title = stringResource(R.string.notifications_off_title),
                body = stringResource(R.string.notifications_off_brief),
                granted = false,
                onClick = onAllow,
                onDismiss = onDismiss,
                icon = Icons.Rounded.NotificationsOff,
            )
        }
    }
}

@Composable
internal fun TodayTimeline(
    state: UiState,
    onTimetable: () -> Unit,
    ramadanView: Boolean,
    onBell: (PrayerSlot, Boolean) -> Unit,
    snapBell: PrayerSlot?,
    notificationsBlocked: Boolean,
) {
    val today = state.today ?: return
    val context = LocalContext.current
    val next = state.next

    // The heading and the Timetable button share one line (it was Save as PDF until 1.6.0; the PDF now
    // lives in the Timetable's header). It used to be a 56dp filled
    // button under everything, and on a 20:9 phone it was the thing that fell below the
    // fold; beside the list it exports it costs no height at all and reads as what it is —
    // "save this". A text button with an icon, not an icon alone: icon-only is guesswork.
    // Compact (40dp drawn, 48dp to touch) and 8dp above the list it saves, 16dp under the card:
    // at full size it looked squashed between the two (owner, 18 Oct 2026, screenshot).
    // At 1.5 text and above the button drops under the heading: at 320dp and 2.0 it squeezed "Today" until it
    // broke mid word. The same rule as the prayer rows below and step 2's dates rows.
    val timetableButton: @Composable () -> Unit = {
        CtaButton(onClick = onTimetable, compact = true) {
            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Space.Related))
            Text(stringResource(R.string.action_timetable))
        }
    }
    if (LocalDensity.current.fontScale >= 1.5f) {
        // Full width so both start at the leading edge; the screen's column centres its children.
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.home_today),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(Space.Related))
            timetableButton()
        }
    } else {
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
            timetableButton()
        }
    }
    Spacer(Modifier.height(Space.Related))

    val scheme = MaterialTheme.colorScheme
    val slots = PrayerSlot.entries.filter { today.times[it] != null }

    // During Ramadan, and only then, Fajr is also where suhoor ends and Maghrib is iftar. The
    // labels come from the same state as the card, so they start with day 1 on the user's own
    // calendar and are gone at midnight into 1 Shawwal with nothing to switch off. They follow
    // the "Ramadan card" setting: someone who is not fasting hid it to stop seeing fasting.
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
                note = rowNote(
                    ramadan = when {
                        !ramadanView -> null
                        slot == PrayerSlot.FAJR -> suhoorLabel
                        slot == PrayerSlot.MAGHRIB -> iftarLabel
                        else -> null
                    },
                    jamaah = state.jamaah[slot],
                ),
                trailing = {
                    if (slot.isPrayer) {
                        // A crossed bell keeps the style it had while it fades, so an alarm does not flip to a bell.
                        val style = state.settings.alertFor[slot]
                        Bell(
                            on = style != null,
                            alarm = (style ?: state.settings.lastAlertStyle[slot]) == AlertStyle.ALARM,
                            blocked = notificationsBlocked,
                            prayerName = stringResource(slot.labelRes),
                            onNext = next != null && slot == next.slot && !next.isTomorrow,
                            snap = snapBell == slot,
                            onChange = { on -> onBell(slot, on) },
                        )
                    } else {
                        // Sunrise has no bell; the space keeps its time in line with the others.
                        Spacer(Modifier.width(48.dp))
                    }
                },
            )
            if (index != slots.lastIndex) {
                HorizontalDivider(color = scheme.outlineVariant)
            }
        }
    }
}

/**
 * The second line under a prayer: the Ramadan word, the jama'ah time, or both on one line
 * ("Suhoor ends · Jama'ah 5:45 am"), never two lines (UX.md). A jama'ah time outside its prayer's
 * window is never shown as a time: the line says "Check your jama'ah time" in the same height.
 */
@Composable
private fun rowNote(ramadan: String?, jamaah: JamaahResult?): String? {
    val context = LocalContext.current
    val jamaahText = when (jamaah) {
        null -> null
        is JamaahResult.InWindow -> stringResource(
            if (jamaah.isJumuah) R.string.jumuah_line else R.string.jamaah_line,
            // A non breaking space in the time only, so "pm" never wraps alone onto its own line
            // (not in TimeFormat: it feeds the PDF, the widgets and the card).
            TimeFormat.clock(context, jamaah.at).replace(' ', '\u00A0'),
        )
        JamaahResult.Before, is JamaahResult.After -> stringResource(R.string.jamaah_check)
    }
    return when {
        ramadan != null && jamaahText != null -> stringResource(R.string.settings_value_pair, ramadan, jamaahText)
        else -> ramadan ?: jamaahText
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
private fun PrayerRow(
    slot: PrayerSlot,
    time: String,
    isNext: Boolean,
    isCurrent: Boolean,
    note: String?,
    /** The bell, after the time. Its 48dp target takes the row's end padding. */
    trailing: @Composable () -> Unit,
) {
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
    // The fill and the bar move to the next row over Motion.standard when a prayer passes (motion row 8,
    // step 5), read only inside drawBehind so nothing recomposes per frame. On open they start at the target.
    val fill by animateColorAsState(background, Motion.standard(), label = "rowFill")
    val bar by animateColorAsState(if (isNext) accent else Color.Transparent, Motion.standard(), label = "rowBar")

    // At 1.5 text and above the pill and time drop under the name, as step 2's dates rows do: at 320dp and 2.0
    // the bell's 48dp squeezed the name until "Fajr" broke one letter a line (STEP-3-DESIGN §7). The bell
    // stays at the end either way. Below 1.5 the row is unchanged.
    val stacked = LocalDensity.current.fontScale >= 1.5f
    val markersAndTime: @Composable () -> Unit = {
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

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // One stop for a screen reader: "Fajr, Next, 5:37 am", not three swipes (research D1).
            .semantics(mergeDescendants = true) {}
            // drawBehind rather than a sibling Box: a full-height stripe next to a row
            // whose height is driven by the text needs intrinsic measurement, and this
            // gets the same pixels without asking the layout to measure twice.
            .drawBehind {
                drawRect(fill)
                drawRect(bar, size = Size(barWidth, size.height))
            }
            // Vertical padding plus a floor, not a fixed height: at a large system font
            // size the row has to grow rather than crop the prayer name. 50, not 56, since
            // 15 Aug 2026: six dp a row over six rows is what lets the mosque door under
            // the list clear the navigation bar (see the budget note in HomeScreen). Still
            // above the 48dp touch-target minimum, though these rows are not tappable.
            .heightIn(min = 50.dp)
            // A second line (Ramadan or jama'ah) fits inside the 50dp floor (24 name + 18 line + 2 padding
            // = 44dp), so five jama'ah lines add 0dp a row at 1.0 (STEP-7-DESIGN section 3).
            // The bell's 48dp target overlaps the 18dp end padding, so the time ends where it did, and it
            // fills the row's 50dp floor: 12dp above and below a 48dp target made every row 72dp and pushed
            // Isha under the tab bar on the 823dp frame (the tick, e98b2a6).
            .padding(start = 18.dp, end = 6.dp)
            .padding(vertical = 1.dp),
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
            if (stacked) {
                Row(verticalAlignment = Alignment.CenterVertically) { markersAndTime() }
            }
        }
        if (!stacked) {
            // A gap so a long jama'ah note never touches the Next or Now pill (Fable, step 7 pictures).
            // The note wraps one line earlier at 360dp instead, which STEP-7-DESIGN §3 allows.
            Spacer(Modifier.width(8.dp))
            markersAndTime()
        }
        trailing()
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
