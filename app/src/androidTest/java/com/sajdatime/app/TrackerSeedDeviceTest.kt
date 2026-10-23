package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.data.TrackerData
import com.sajdatime.app.data.TrackerRepository
import com.sajdatime.core.DayRecord
import com.sajdatime.core.PauseRange
import com.sajdatime.core.TRACKED_SLOTS
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneId

/**
 * Not a check. Gives the My prayers tab the same fixed record every time, relative to today, so its
 * pictures can be taken and compared. Run by hand with
 * `-e class com.sajdatime.app.TrackerSeedDeviceTest#<method>`, excluded from the normal connected run
 * the way [ScreenSeedDeviceTest] is (the release script's `notClass` list), and never on the owner's
 * phone. Run [ScreenSeedDeviceTest] first so the app is past onboarding with a place.
 *
 * The record, as days before today: 6 days ago five ticked, 5 four, 4 five, 3 paused, 2 three,
 * yesterday two, today Fajr only. Show streak is off. It uses the real repositories.
 */
@RunWith(AndroidJUnit4::class)
class TrackerSeedDeviceTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    /** Tracking on, the fixed record written, streak off. Starts from an empty record. */
    private suspend fun seed(today: LocalDate): TrackerRepository {
        val settings = SettingsRepository(context)
        val tracker = TrackerRepository(context)
        tracker.deleteAll()
        settings.setTrackerEnabled(true)
        settings.setTrackerShowStreak(false)
        val counts = mapOf(6L to 5, 5L to 4, 4L to 5, 2L to 3, 1L to 2)
        val records = counts.mapKeys { today.minusDays(it.key) }.mapValues { DayRecord(TRACKED_SLOTS.take(it.value).toSet()) } +
            (today to DayRecord(setOf(TRACKED_SLOTS.first())))
        // A one day pause in the middle of the week: pause() can only start today or yesterday, so the
        // range is merged in, as loading a saved file would.
        val paused = today.minusDays(3)
        tracker.merge(TrackerData(records = records, pauses = listOf(PauseRange(paused, paused))))
        return tracker
    }

    private fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

    @Test
    fun seed_a_week_of_ticks_with_today_started() = runBlocking {
        val today = today()
        val tracker = seed(today)
        val data = tracker.current()
        assertEquals(setOf(TRACKED_SLOTS.first()), data.records.getValue(today).ticked)
        assertEquals(5, data.records.getValue(today.minusDays(6)).ticked.size)
        assertTrue(data.pauses.single().contains(today.minusDays(3)))
        assertFalse(SettingsRepository(context).current().trackerShowStreak)
    }

    @Test
    fun seed_then_pause_from_today() = runBlocking {
        val today = today()
        val tracker = seed(today)
        tracker.pause(today)
        assertTrue(tracker.current().pauses.any { it.contains(today) && it.end == null })
    }

    @Test
    fun clear_the_record_and_switch_tracking_off() = runBlocking {
        TrackerRepository(context).deleteAll()
        val settings = SettingsRepository(context)
        settings.setTrackerEnabled(false)
        assertTrue(TrackerRepository(context).current().isEmpty)
        assertFalse(settings.current().trackerEnabled)
    }
}
