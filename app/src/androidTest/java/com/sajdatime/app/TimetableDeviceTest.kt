package com.sajdatime.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.home.TimetableSheet
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * The Timetable sheet as it is drawn: today first, the six days after it, nothing before and nothing
 * after, and Save as PDF handed to the caller (Ali, 12 Nov 2026: "today should be the first row, previous
 * days don't matter. From today to next 1 week. For the whole month save as pdf is there"). The rule is
 * `TimetableListTest` in `:core`; this shows the sheet follows it. A month end is inside the week on
 * purpose (28 Oct), so the cards are seen to run on into November without a heading.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class TimetableDeviceTest {

    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val today = LocalDate.of(2026, 10, 28)
    private val london = Coordinates(51.5074, -0.1278)
    private var exports = 0

    private fun open() {
        val settings = AppSettings(onboardingComplete = true, coordinates = london, cityName = "London")
        val state = UiState(
            loading = false,
            settings = settings,
            today = PrayerEngine.compute(london, today, settings.calculationPrefs),
        )
        compose.setContent {
            SajdaTimeTheme {
                TimetableSheet(
                    state = state,
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    onExport = { exports++ },
                    onDismiss = {},
                )
            }
        }
        compose.waitUntil(20_000) { compose.onAllNodesWithText(date(0)).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun date(plus: Long) = TimeFormat.date(context, today.plusDays(plus))

    private fun listed(plus: Long): Boolean = try {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(date(plus)))
        true
    } catch (notThere: AssertionError) {
        false
    }

    @Test
    fun today_is_first_and_the_week_ends_six_days_later() {
        open()
        // Today's card is on screen as the sheet opens, with nothing to scroll past.
        compose.onNodeWithText(date(0)).assertExists()
        for (day in 1L..6L) assertEquals("day $day is listed", true, listed(day))
        assertFalse("an eighth day is listed", listed(7))
        assertFalse("yesterday is listed", listed(-1))
        // No month heading: the name of a month standing alone is nowhere.
        compose.onAllNodesWithText("October 2026").assertCountEquals(0)
        compose.onAllNodesWithText("November 2026").assertCountEquals(0)
    }

    @Test
    fun save_as_pdf_is_handed_to_the_caller_once() {
        open()
        compose.onNodeWithText(context.getString(R.string.action_save_pdf)).performClick()
        compose.waitForIdle()
        assertEquals(1, exports)
    }
}
