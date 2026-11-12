package com.sajdatime.app

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

    @Test
    fun thirty_three_taps_complete_a_round_and_the_next_tap_starts_another() {
        open()
        count().assertTextEquals("0")
        tap(32)
        count().assertTextEquals("32")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 0))
        tap(1)
        count().assertTextEquals("33")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 1))
        tap(1)
        count().assertTextEquals("1")
        rounds().assertTextEquals(word(R.string.tasbeeh_rounds, 1))
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
    }

    @Test
    fun changing_the_target_starts_again_and_start_again_keeps_the_target() {
        open()
        tap(5)
        compose.onNodeWithTag("tasbeeh_target").performClick()
        count().assertTextEquals("0")
        assertEquals(Tasbeeh(count = 0, target = 99, rounds = 0), TasbeehStore(context).load())
        tap(3)
        compose.onNodeWithTag("tasbeeh_reset").performClick()
        count().assertTextEquals("0")
        assertEquals(Tasbeeh(count = 0, target = 99, rounds = 0), TasbeehStore(context).load())
    }

    @Test
    fun with_no_target_it_keeps_counting_past_one_hundred() {
        TasbeehStore(context).save(Tasbeeh(count = 0, target = 0, rounds = 0))
        open()
        tap(101)
        count().assertTextEquals("101")
    }
}
