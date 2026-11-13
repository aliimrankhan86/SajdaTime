package com.sajdatime.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sajdatime.app.ui.RamadanState
import com.sajdatime.app.ui.home.RamadanCard
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

/**
 * The Ramadan card's two lines under the tiles, the countdown and "Fast today", sit in the middle of
 * the card (Ali, 12 Nov 2026), left to right and right to left. Measured, not read: each line's middle
 * is the card's middle, and the line is narrower than the card, so a line that merely fills the width
 * and starts at one edge cannot pass.
 *
 * English words at normal text size only: other languages hold by construction (centre has no start or
 * end) and by the right to left runs here, not by a run in each language.
 *
 * London, day 15 of Ramadan 1448, at the three moments the countdown line changes its words: before
 * suhoor ends, during the fast, after iftar.
 */
@RunWith(AndroidJUnit4::class)
class RamadanCardLayoutDeviceTest {

    @get:Rule val compose = createComposeRule()

    private val london = Coordinates(51.5074, -0.1278)
    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)
    private val day = LocalDate.of(2027, 2, 22)
    private fun at(time: String) = LocalDateTime.parse("2027-02-22T$time").atZone(ZoneId.of("Europe/London")).toInstant()

    private fun check(direction: LayoutDirection, time: String) {
        val state = RamadanState.build(london, prefs, PrayerEngine.compute(london, day, prefs))!!
        compose.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                SajdaTimeTheme {
                    RamadanCard(
                        ramadan = state, now = at(time), showDateHint = false,
                        onOpenInfo = {}, onOpenTimetable = {}, onShiftDate = {},
                    )
                }
            }
        }
        val card = compose.onNodeWithTag("ramadan_card").getUnclippedBoundsInRoot()
        val middle = (card.left + card.right) / 2
        for (tag in listOf("ramadan_countdown", "ramadan_fast")) {
            // Finding the tag also guards the modifier order: a tag placed before clearAndSetSemantics is wiped.
            val line = compose.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            val lineMiddle = (line.left + line.right) / 2
            assertTrue("$tag is not in the middle ($direction, $time): $lineMiddle against $middle", abs((lineMiddle - middle).value) <= 1f)
            assertTrue("$tag fills the card, so its place proves nothing ($direction, $time)", line.right - line.left < card.right - card.left - 40.dp)
        }
    }

    @Test fun centred_left_to_right_during_the_fast() = check(LayoutDirection.Ltr, "10:00")
    @Test fun centred_right_to_left_during_the_fast() = check(LayoutDirection.Rtl, "10:00")
    @Test fun centred_left_to_right_before_suhoor_ends() = check(LayoutDirection.Ltr, "03:00")
    @Test fun centred_right_to_left_after_iftar() = check(LayoutDirection.Rtl, "20:00")
}
