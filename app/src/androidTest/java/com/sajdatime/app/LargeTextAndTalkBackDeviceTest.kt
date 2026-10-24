package com.sajdatime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.hasNoClickAction
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sajdatime.app.ui.Destination
import com.sajdatime.app.ui.home.DayCard
import com.sajdatime.app.ui.home.LocationHeader
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.ui.qibla.QiblaBody
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import com.sajdatime.core.NextPrayer
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TimetableGrid
import java.time.Instant
import java.time.LocalDate
import com.sajdatime.app.ui.GlassTabBar
import com.sajdatime.app.ui.components.splitsAWord
import com.sajdatime.app.ui.settings.SettingsItem
import com.sajdatime.app.ui.settings.SettingsList
import com.sajdatime.app.ui.settings.SettingsSection
import com.sajdatime.app.ui.settings.SettingsSectionId
import com.sajdatime.app.ui.settings.StepperRow
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.app.ui.theme.ThemeChoice
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

/**
 * Two faults found on the emulator on 19 Oct 2026 that reading the code had missed: words split across
 * lines at the largest text size, and Match your mosque's plus and minus saying nothing to TalkBack.
 * Each check fails if its fix is taken out (HANDOVER §15 lessons 159 and 162).
 */
class LargeTextAndTalkBackDeviceTest {
    @get:Rule val compose = createComposeRule()

    /** The app's words in the language the test device is in, so the checks are not English only. */
    private fun word(id: Int, vararg args: Any): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    /** [content] 320dp wide at [scale] times the text size; 2.0 as `wm density 540` and `font_scale 2.0` showed it. */
    private fun atText(scale: Float, width: Dp = 320.dp, content: @Composable () -> Unit) = compose.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = scale)) {
            SajdaTimeTheme { Box(Modifier.width(width)) { content() } }
        }
    }

    private fun layoutOf(text: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        compose.onNode(hasText(text)).fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single()
    }

    private fun assertWhole(text: String) {
        val layout = layoutOf(text)
        val ends = (0 until layout.lineCount - 1).map { layout.getLineEnd(it) }
        assertFalse("\"$text\" is split inside a word, lines end at $ends", splitsAWord(text, ends))
    }

    @Test
    fun the_tab_labels_never_split_a_word_at_the_largest_text() {
        atText(2f) { GlassTabBar(current = Destination.SETTINGS, onSelect = {}) }
        assertWhole(word(R.string.nav_settings))
        // The fourth tab (1.6.0): its label must be there and whole at the same text size.
        assertWhole(word(R.string.nav_tracker))
    }

    /**
     * At the phone's Small text size the label keeps its own size: it once grew to 1.18 times it
     * (review, 19 Oct 2026). Checked by width, because the layout reports the style it was given,
     * not the size auto size chose.
     */
    @Test
    fun at_small_text_the_tab_labels_keep_their_own_size() {
        val label = word(R.string.nav_settings)
        var expected = 0f
        atText(0.85f) {
            val style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                .copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            expected = androidx.compose.ui.text.rememberTextMeasurer().measure(label, style).size.width.toFloat()
            GlassTabBar(current = Destination.SETTINGS, onSelect = {})
        }
        val layout = layoutOf(label)
        assertEquals("width of \"$label\" at Small text", expected, layout.getLineRight(0) - layout.getLineLeft(0), 2f)
    }

    @Test
    fun the_theme_options_never_split_a_word_at_the_largest_text() {
        atText(2f) {
            SettingsList(
                title = "Settings",
                banners = {},
                sections = listOf(
                    SettingsSection(SettingsSectionId.APPEARANCE, "Appearance", listOf(SettingsItem.Theme("Theme", ThemeChoice.SYSTEM) {})),
                ),
            )
        }
        assertWhole(word(R.string.theme_system))
    }

    @Test
    fun match_your_mosque_tells_a_screen_reader_the_new_value() {
        compose.setContent {
            SajdaTimeTheme {
                var minutes by remember { mutableIntStateOf(0) }
                StepperRow(
                    label = "Fajr",
                    value = if (minutes == 0) stringResource(R.string.adjustment_none) else stringResource(R.string.adjustment_minutes, minutes),
                    canDecrease = true,
                    canIncrease = true,
                    onDecrease = { minutes-- },
                    onIncrease = { minutes++ },
                )
            }
        }
        val separator = word(R.string.list_separator)
        compose.onNodeWithContentDescription("Fajr" + separator + word(R.string.settings_adjustments_none))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onNodeWithContentDescription(word(R.string.action_increase, "Fajr")).performClick()
        compose.onNodeWithContentDescription("Fajr" + separator + word(R.string.adjustment_minutes, 1)).assertExists()
    }

    private val sampleDay = DayPrayerTimes(
        date = LocalDate.of(2026, 10, 24),
        times = PrayerSlot.entries.mapIndexed { i, slot -> slot to Instant.parse("2026-10-24T0${i + 3}:15:00Z") }.toMap(),
    )

    /** At 2.0 on 320dp the card is one column: its spoken text starts with "Today," and names all six times. */
    @Test
    fun a_timetable_card_reads_as_one_sentence_with_all_six_times_at_the_largest_text() {
        atText(2f) { DayCard(day = sampleDay, isToday = true, columns = TimetableGrid.columns(256f, 2f)) }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val date = TimeFormat.date(context, sampleDay.date)
        val times = PrayerSlot.entries.map { TimeFormat.clock(context, sampleDay.times.getValue(it)) }
        val spoken = word(
            R.string.timetable_today_a11y,
            word(R.string.timetable_day_a11y, date, times[0], times[1], times[2], times[3], times[4], times[5]),
        )
        compose.onNodeWithContentDescription(spoken).assertExists()
        assertWhole(date)
    }

    /**
     * The Timetable header's Save as PDF and its X keep a 48dp target (the drawn button is 40dp,
     * compact), composed through the real header so a change there fails here. ponytail: the Today
     * line's Timetable button is private inside the Times screen; it is the same compact CtaButton.
     */
    @Test
    fun the_timetable_header_buttons_have_48dp_targets() {
        atText(1f) {
            com.sajdatime.app.ui.home.TimetableHeader(title = "Timetable", place = "London", onExport = {}, onClose = {})
        }
        // The touch bounds, not the drawn ones: the compact button draws at 40dp and Compose widens only
        // where a tap lands to 48dp, so assertHeightIsAtLeast measured 40 (first device run, 24 Oct 2026).
        val touch = compose.onNode(hasText(word(R.string.action_save_pdf)) and hasClickAction())
            .fetchSemanticsNode().touchBoundsInRoot
        with(compose.density) {
            assertTrue("touch height ${touch.height.toDp()}", touch.height.toDp() >= 47.5.dp)
            assertTrue("touch width ${touch.width.toDp()}", touch.width.toDp() >= 47.5.dp)
        }
    }

    /** A morning in London for the sky mark: Fajr's window is open, so the sky is Dawn and ends at sunrise. */
    private fun dawnState(city: String): UiState {
        val now = Instant.parse("2026-10-24T03:30:00Z")
        return UiState(
            loading = false,
            settings = AppSettings(cityName = city),
            today = sampleDay,
            next = NextPrayer(PrayerSlot.DHUHR, sampleDay.getValue(PrayerSlot.DHUHR), false),
            current = PrayerSlot.FAJR,
            now = now,
        )
    }

    private fun DayPrayerTimes.getValue(slot: PrayerSlot) = this[slot]

    private fun hasDescriptionStartingWith(prefix: String) = SemanticsMatcher("description starts with $prefix") {
        it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { d -> d.startsWith(prefix) } == true
    }

    /** At 2.0 on 320dp the name wins: the sky mark is not drawn and the city is whole (STEP-5-DESIGN section 4). */
    @Test
    fun the_sky_mark_hides_at_the_largest_text_and_the_city_stays_whole() {
        atText(2f) { LocationHeader(state = dawnState("London"), now = dawnState("London").now, onClick = {}, onOpenDates = {}) }
        compose.onNode(hasDescriptionStartingWith(word(R.string.sky_dawn))).assertDoesNotExist()
        assertWhole("London")
    }

    /**
     * At 1.0 on 360dp the mark is its own node: spoken as the period and when it ends, with no click, apart
     * from "London, Change location". The city row keeps 48dp across and the date line 48dp down.
     */
    @Test
    fun the_sky_mark_is_its_own_spoken_node_and_the_targets_hold() {
        val state = dawnState("London")
        atText(1f, width = 360.dp) { LocationHeader(state = state, now = state.now, onClick = {}, onOpenDates = {}) }
        val sunrise = TimeFormat.clock(InstrumentationRegistry.getInstrumentation().targetContext, sampleDay.getValue(PrayerSlot.SUNRISE))
        val mark = compose.onNode(hasDescriptionStartingWith(word(R.string.sky_dawn)))
        mark.assertExists().assert(hasNoClickAction())
        val description = mark.fetchSemanticsNode().config[SemanticsProperties.ContentDescription].single()
        assertEquals(word(R.string.sky_now_a11y, word(R.string.sky_dawn), sunrise), description)
        // Found by what they say, not by position, so a new clickable in the header cannot shift them (review, 24 Oct).
        compose.onNodeWithContentDescription(word(R.string.action_change_location), substring = true)
            .assert(hasClickAction()).assertWidthIsAtLeast(48.dp)
        compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.dates_header_a11y).substringBefore("%1\$s").trim(), substring = true)
            .assert(hasClickAction()).assertHeightIsAtLeast(48.dp)
    }

    /** Facing 60 degrees with the Kaaba at 118: a turn of 58 to the right, compass working, in London. */
    private fun qiblaState(settings: AppSettings = AppSettings(cityName = "London"), bearing: Double = 118.0, km: Double = 4794.0, heading: Double = 60.0) =
        UiState(
            loading = false,
            settings = settings,
            qiblaBearing = bearing,
            qiblaDistanceKm = km,
            compassHeading = heading,
            compassAccuracy = CompassAccuracy.HIGH,
        )

    /**
     * Qibla (STEP-6-DESIGN section 9): Details has a 48dp target and says collapsed, then expanded and shows the
     * bearing line after a tap; the direction is a polite live region; the dial and key say nothing, so the
     * direction line is the only node with a spoken description.
     */
    @Test
    fun qibla_details_is_a_48dp_disclosure_and_the_direction_is_the_spoken_dial() {
        atText(1f, width = 360.dp) { QiblaBody(qiblaState(), onUseGps = {}, onSearchCity = {}) }
        val details = compose.onNode(hasText(word(R.string.notice_show_details)) and hasClickAction())
        val touch = details.fetchSemanticsNode().touchBoundsInRoot
        with(compose.density) {
            assertTrue("touch height ${touch.height.toDp()}", touch.height.toDp() >= 47.5.dp)
            assertTrue("touch width ${touch.width.toDp()}", touch.width.toDp() >= 47.5.dp)
        }
        details.assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, word(R.string.state_collapsed)))
        compose.onNode(hasText(word(R.string.qibla_bearing, 118))).assertDoesNotExist()
        details.performClick()
        compose.onNode(hasText(word(R.string.notice_hide_details)) and hasClickAction())
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, word(R.string.state_expanded)))
        compose.onNode(hasText(word(R.string.qibla_bearing, 118))).assertExists()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription)).assertCountEquals(1)
    }

    /** At 2.0 on 320dp the key wraps instead of clipping: both labels are drawn whole, inside the width. */
    @Test
    fun the_qibla_key_stays_inside_the_screen_at_the_largest_text() {
        atText(2f) { QiblaBody(qiblaState(), onUseGps = {}, onSearchCity = {}) }
        val width = with(compose.density) { 320.dp.toPx() }
        for (label in listOf(word(R.string.qibla_legend_kaaba), word(R.string.qibla_legend_facing))) {
            // The key clears its semantics, so its words are only in the unmerged tree.
            val node = compose.onNode(hasText(label), useUnmergedTree = true)
            node.assertIsDisplayed()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            assertTrue("\"$label\" spans ${bounds.left}..${bounds.right} of $width", bounds.left >= 0f && bounds.right <= width + 1f)
        }
    }

    /**
     * The Makkah fallback is no location on Qibla: the bearing from Makkah to Makkah is 0 and the distance 0 km,
     * which would say "You are now facing the Kaaba" whenever the phone points north (seeded here, heading 0). Mutation proof: drop
     * `|| usingDefaultLocation` from the guard and this fails.
     */
    @Test
    fun the_makkah_fallback_on_qibla_shows_the_set_location_notice_and_no_numbers() {
        val fallback = AppSettings(usingDefaultLocation = true, cityName = "Makkah")
        atText(1f, width = 360.dp) { QiblaBody(qiblaState(fallback, bearing = 0.0, km = 0.0, heading = 0.0), onUseGps = {}, onSearchCity = {}) }
        compose.onNode(hasText(word(R.string.qibla_needs_location))).assertExists()
        compose.onAllNodes(hasClickAction()).assertCountEquals(1)
        compose.onNode(hasText(word(R.string.qibla_bearing, 0)), useUnmergedTree = true).assertDoesNotExist()
        compose.onNode(hasText(word(R.string.qibla_facing)), useUnmergedTree = true).assertDoesNotExist()
    }
}
