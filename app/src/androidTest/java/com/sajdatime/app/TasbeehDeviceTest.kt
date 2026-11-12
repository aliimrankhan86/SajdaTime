package com.sajdatime.app

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.TasbeehStore
import com.sajdatime.app.ui.prayers.TasbeehSheet
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.core.Tasbeeh
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The tasbeeh as a person uses it: the real sheet, real taps, the real saved numbers (12 Nov 2026).
 * The counting rule itself is `TasbeehTest` in `:core`; this shows the screen is wired to it and that
 * what is on screen is what is saved. Not shown here: the vibration, which an emulator cannot feel.
 */
@RunWith(AndroidJUnit4::class)
class TasbeehDeviceTest {

    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun word(id: Int, vararg args: Any): String = context.getString(id, *args)

    @Before
    fun start_from_nothing() = TasbeehStore(context).save(Tasbeeh())

    private fun open() = compose.setContent { SajdaTimeTheme { TasbeehSheet(onDismiss = {}) } }
    private fun tap(times: Int) = repeat(times) { compose.onNodeWithTag("tasbeeh_tap").performClick() }
    private fun count() = compose.onNodeWithTag("tasbeeh_count")
    private fun rounds() = compose.onNodeWithTag("tasbeeh_rounds")
    private fun total() = compose.onNodeWithTag("tasbeeh_total")

    @Test
    fun thirty_three_taps_complete_a_round_and_the_next_tap_starts_another() {
        open()
        count().assertTextEquals("0")
        tap(32)
        count().assertTextEquals("32")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 0))
        total().assertTextEquals(word(R.string.tasbeeh_total, 32L))
        tap(1)
        // The round is counted, and saved, on the tap that reaches the target, not the one after.
        count().assertTextEquals("33")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 1))
        total().assertTextEquals(word(R.string.tasbeeh_total, 33L))
        assertEquals(Tasbeeh(count = 33, target = 33, rounds = 1), TasbeehStore(context).load())
        tap(1)
        count().assertTextEquals("1")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 1))
        total().assertTextEquals(word(R.string.tasbeeh_total, 34L))
        assertEquals(Tasbeeh(count = 1, target = 33, rounds = 1), TasbeehStore(context).load())
    }

    @Test
    fun the_count_is_saved_on_every_tap_and_comes_back() {
        open()
        tap(7)
        assertEquals(Tasbeeh(count = 7, target = 33, rounds = 0), TasbeehStore(context).load())
    }

    @Test
    fun a_saved_count_is_what_the_sheet_opens_on() {
        TasbeehStore(context).save(Tasbeeh(count = 12, target = 99, rounds = 2))
        open()
        count().assertTextEquals("12")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 2))
        total().assertTextEquals(word(R.string.tasbeeh_total, 210L))
    }

    @Test
    fun choosing_another_target_starts_again_and_start_again_keeps_the_target() {
        open()
        tap(5)
        compose.onNodeWithTag("tasbeeh_target_99").performClick()
        count().assertTextEquals("0")
        assertEquals(Tasbeeh(count = 0, target = 99, rounds = 0), TasbeehStore(context).load())
        tap(3)
        compose.onNodeWithTag("tasbeeh_reset").performClick()
        count().assertTextEquals("0")
        assertEquals(Tasbeeh(count = 0, target = 99, rounds = 0), TasbeehStore(context).load())
    }

    @Test
    fun choosing_the_target_already_in_use_keeps_the_count() {
        open()
        tap(4)
        compose.onNodeWithTag("tasbeeh_target_33").performClick()
        count().assertTextEquals("4")
    }

    @Test
    fun with_no_target_it_keeps_counting_past_one_hundred() {
        TasbeehStore(context).save(Tasbeeh(count = 0, target = 0, rounds = 0))
        open()
        tap(101)
        count().assertTextEquals("101")
    }

    /**
     * Ali on his Redmi, 12 Nov 2026: "Tasbeeh misses fast taps on my phone". The button counted when the
     * finger left it, so a tap whose finger slid a little (the page under it scrolls and takes the
     * touch) or whose release came after the next finger had landed was dropped: this test gave 100 on
     * that code. 200 beads the way a fast thumb, or two thumbs, really tap: 40 clean, 40 with the finger
     * sliding 24dp down, 40 sliding up, and 40 pairs where the second finger lands before the first
     * lifts. Every finger down is one bead, and a finger sliding on the button never closes the sheet.
     */
    @Test
    fun two_hundred_fast_taps_some_overlapping_and_some_sliding_count_exactly_two_hundred() {
        TasbeehStore(context).save(Tasbeeh(count = 0, target = 0, rounds = 0))
        var dismissed = 0
        compose.setContent { SajdaTimeTheme { TasbeehSheet(onDismiss = { dismissed++ }) } }
        repeat(40) {
            compose.onNodeWithTag("tasbeeh_tap").performTouchInput {
                val slide = 24.dp.toPx()
                // a clean tap, down and up in the same instant
                down(0, center)
                up(0)
                // the finger slides down, then up, before it lifts
                down(0, center)
                moveBy(0, Offset(0f, slide))
                up(0)
                down(0, center)
                moveBy(0, Offset(0f, -slide))
                up(0)
                // two fingers: the second lands before the first lifts
                down(0, Offset(centerX - width / 4f, centerY))
                down(1, Offset(centerX + width / 4f, centerY))
                up(0)
                up(1)
            }
        }
        count().assertTextEquals("200")
        assertEquals(Tasbeeh(count = 200, target = 0, rounds = 0), TasbeehStore(context).load())
        assertEquals(0, dismissed)
    }

    /**
     * The touch right after a slide, before the screen has drawn three more frames. A sheet that had
     * followed the sliding finger would still be springing back and would take this touch as its own.
     */
    @Test
    fun the_tap_straight_after_a_sliding_tap_still_counts() {
        open()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("tasbeeh_tap").performTouchInput {
            down(0, center)
            moveBy(0, Offset(0f, 40.dp.toPx()))
            up(0)
        }
        repeat(3) { compose.mainClock.advanceTimeByFrame() }
        compose.onNodeWithTag("tasbeeh_tap").performTouchInput {
            down(0, center)
            up(0)
        }
        compose.mainClock.autoAdvance = true
        count().assertTextEquals("2")
    }

    /** Fast taps straight through the target, with no pause there: 100 beads at 33 are 3 rounds and 1. */
    @Test
    fun fast_taps_run_through_the_target_and_the_total_keeps_climbing() {
        open()
        repeat(100) {
            compose.onNodeWithTag("tasbeeh_tap").performTouchInput {
                down(0, center)
                up(0)
            }
        }
        count().assertTextEquals("1")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 3))
        total().assertTextEquals(word(R.string.tasbeeh_total, 100L))
        assertEquals(Tasbeeh(count = 1, target = 33, rounds = 3), TasbeehStore(context).load())
    }

    /** TalkBack and Switch Access press the button by its click action, with no finger: still one bead. */
    @Test
    fun the_click_action_a_screen_reader_uses_counts_one() {
        open()
        compose.onNodeWithTag("tasbeeh_tap").performSemanticsAction(SemanticsActions.OnClick)
        count().assertTextEquals("1")
        tap(1)
        count().assertTextEquals("2")
    }
}
