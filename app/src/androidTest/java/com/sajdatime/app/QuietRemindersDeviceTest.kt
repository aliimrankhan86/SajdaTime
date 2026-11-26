package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.AdhkarReminderPlan
import com.sajdatime.app.notify.FridayReminderPlan
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The 1.8.0 adhkar and Friday reminders as the AlarmManager really holds them (`dumpsys alarm`): each under its
 * own action, at the minute the plan says, and gone once switched off. The JVM tests prove the plans and the
 * receiver's rules; only the device shows the registration. Puts the settings back the way it found them.
 */
@RunWith(AndroidJUnit4::class)
class QuietRemindersDeviceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = SettingsRepository(context)
    private val london = Coordinates(51.5074, -0.1278)
    private var adhkarBefore = false
    private var fridayBefore = false

    @Before
    fun remember() = runBlocking {
        repository.setLocation(london, "London")
        val s = repository.current()
        adhkarBefore = s.adhkarReminder
        fridayBefore = s.fridayReminder
    }

    @After
    fun restore() = runBlocking {
        repository.setAdhkarReminder(adhkarBefore)
        repository.setFridayReminder(fridayBefore)
        PrayerAlarmScheduler.reschedule(context, repository.current())
    }

    /** Reschedules until [action]'s alarms number [count] (the app's own reschedules can race, see SuhoorReminderDeviceTest). */
    private fun settled(action: String, count: Int): List<LocalDateTime> {
        var found = DeviceSupport.alarmTimes(context.packageName, action)
        repeat(20) {
            if (found.size == count) return found
            Thread.sleep(500)
            runBlocking { PrayerAlarmScheduler.reschedule(context, repository.current()) }
            found = DeviceSupport.alarmTimes(context.packageName, action)
        }
        return found
    }

    private fun minutes(list: List<Instant>, zone: ZoneId) = list.map { it.atZone(zone).toLocalDateTime().withNano(0) }.sorted()

    @Test
    fun adhkar_reminders_are_laid_down_at_the_planned_minutes_and_removed_when_off() {
        val zone = ZoneId.systemDefault()
        runBlocking { repository.setAdhkarReminder(true) }
        val s = runBlocking { repository.current() }
        val expected = AdhkarReminderPlan.plan(london, s.calculationPrefs, true, LocalDate.now(zone), Instant.now())
        val found = settled(PrayerAlarmScheduler.ACTION_ADHKAR_REMINDER, expected.size)
        assertEquals("adhkar alarms are not the planned minutes", minutes(expected.map { it.at }, zone), found.sorted())

        runBlocking { repository.setAdhkarReminder(false) }
        assertEquals("switching it off left an adhkar alarm behind", 0, settled(PrayerAlarmScheduler.ACTION_ADHKAR_REMINDER, 0).size)
    }

    @Test
    fun the_friday_reminder_is_laid_down_an_hour_before_and_removed_when_off() {
        val zone = ZoneId.systemDefault()
        runBlocking { repository.setFridayReminder(true) }
        val s = runBlocking { repository.current() }
        val expected = FridayReminderPlan.plan(london, s.calculationPrefs, s.sect, true, s.jamaah, LocalDate.now(zone), Instant.now(), zone)
        assumeTrue("No Friday from today to two days ahead, so nothing to lay down", expected.isNotEmpty())
        val found = settled(PrayerAlarmScheduler.ACTION_FRIDAY_REMINDER, expected.size)
        assertEquals("the Friday alarm is not the planned minute", minutes(expected.map { it.at }, zone), found.sorted())

        runBlocking { repository.setFridayReminder(false) }
        assertEquals("switching it off left a Friday alarm behind", 0, settled(PrayerAlarmScheduler.ACTION_FRIDAY_REMINDER, 0).size)
    }
}
