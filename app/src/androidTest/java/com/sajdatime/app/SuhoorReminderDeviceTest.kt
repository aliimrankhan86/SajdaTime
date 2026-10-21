package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.SuhoorReminderPlan
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerSlot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * The suhoor reminder as the AlarmManager really holds it, read from `dumpsys alarm`, with the prayer
 * alarms beside it. The JVM tests prove the plan and the receiver's rules. Only the device can show that
 * the reminder is registered under its own action, at the minute the plan says, that switching it off
 * removes it, and that none of this moves a prayer alarm by a second.
 *
 * Meant to run with the clock inside Ramadan 1448 (the release script sets 21 February 2027, 23:00, and
 * passes `requireRamadanClock`). On any other day it skips rather than fail, because outside Ramadan
 * there is nothing for the reminder to do, which is itself the thing the JVM tests pin.
 *
 * Puts the settings back the way it found them.
 */
@RunWith(AndroidJUnit4::class)
class SuhoorReminderDeviceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = SettingsRepository(context)
    private val london = Coordinates(51.5074, -0.1278)
    private val prayers = PrayerSlot.entries.filter { it.isPrayer }

    private var minutesBefore = 0
    private var alertsBefore: Map<PrayerSlot, AlertStyle> = emptyMap()

    @Before
    fun remember() {
        runBlocking {
            repository.setLocation(london, "London")
            val s = repository.current()
            minutesBefore = s.suhoorReminderMinutes
            alertsBefore = s.alertFor
            // Prayer alarms beside the reminder, so "none of them moved" has something to compare.
            prayers.forEach { repository.setAlert(it, AlertStyle.NOTIFICATION) }
        }
    }

    @After
    fun restore() {
        runBlocking {
            repository.setSuhoorReminderMinutes(minutesBefore)
            prayers.forEach { repository.setAlert(it, alertsBefore[it]) }
            PrayerAlarmScheduler.reschedule(context, repository.current())
        }
    }

    private fun reschedule() = runBlocking { PrayerAlarmScheduler.reschedule(context, repository.current()) }

    /**
     * Reschedules until the reminder alarms number [count], for up to 10 seconds, and returns what is there.
     *
     * Since the reminder is on by default (1.5.0, 21 Oct 2026) the app's own reschedules can race this test:
     * the release script moves the clock into Ramadan, which fires the overdue prayer alarms, the time change
     * receiver and the daily worker, each rescheduling on its own thread with the settings it read a moment
     * earlier (60 minutes). One that read 60 before this test wrote 0 can lay a reminder down after the
     * test's cancel; release-1.5.0.sh hit exactly that on 21 Oct 2026. Every reschedule after the write reads
     * the new value, so rescheduling again settles it. Not a weaker test: a real "off leaves a reminder"
     * fault survives every reschedule and still fails. In the app the same race is harmless, because the
     * receiver checks the setting again when the reminder fires (shouldPostReminder).
     */
    /**
     * The prayer alarms once the app has stopped rewriting them: reschedule and read until two readings in a
     * row agree (up to 10 seconds), and, given [expected], until they equal it. The second release-1.5.0.sh run
     * of 21 Oct 2026 read 3 of 22 Feb's 5 prayer alarms while one of the app's own reschedules was between its
     * cancel and its writes, then all 5 a moment later. Concurrent reschedules always end with the full set
     * (same request codes, the last write wins), so only a reading taken mid-flight is wrong. A suhoor reminder
     * that really moved a prayer alarm does so on every reschedule and still fails.
     */
    private fun settledPrayers(pkg: String, expected: List<java.time.LocalDateTime>? = null): List<java.time.LocalDateTime> {
        var last = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_PRAYER)
        repeat(20) {
            Thread.sleep(500)
            reschedule()
            val now = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_PRAYER)
            if (now == last && now.isNotEmpty() && (expected == null || now == expected)) return now
            last = now
        }
        return last
    }

    private fun settledReminders(pkg: String, count: Int): List<java.time.LocalDateTime> {
        var found = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
        repeat(20) {
            if (found.size == count) return found
            Thread.sleep(500)
            reschedule()
            found = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
        }
        return found
    }

    @Test
    fun the_reminder_is_one_alarm_at_the_planned_minute_and_leaves_every_prayer_alarm_alone() {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = LocalDate.now(zone)
        val settings = runBlocking { repository.current() }
        val expected = SuhoorReminderPlan.plan(london, settings.calculationPrefs, 30, today, now)
        if (DeviceSupport.requireRamadanClock) {
            assertTrue("The clock was meant to be inside Ramadan 1448, so a reminder should be planned", expected.isNotEmpty())
        } else {
            assumeTrue("The clock is not inside Ramadan 1448, so there is nothing to schedule", expected.isNotEmpty())
        }

        val pkg = context.packageName
        runBlocking { repository.setSuhoorReminderMinutes(0) }
        reschedule()
        val prayersBefore = settledPrayers(pkg)
        assertTrue("no prayer alarms to compare against", prayersBefore.isNotEmpty())
        assertEquals("a reminder exists while it is switched off", 0, settledReminders(pkg, 0).size)

        runBlocking { repository.setSuhoorReminderMinutes(30) }
        reschedule()
        val reminders = settledReminders(pkg, expected.size)
        assertEquals("one reminder for each planned day", expected.size, reminders.size)
        assertEquals(
            "the reminder is not 30 minutes before Fajr",
            expected.map { it.at.atZone(zone).toLocalDateTime().withNano(0) },
            reminders,
        )
        assertEquals("a prayer alarm moved or went", prayersBefore, settledPrayers(pkg, prayersBefore))

        runBlocking { repository.setSuhoorReminderMinutes(0) }
        reschedule()
        assertEquals("switching it off left a reminder behind", 0, settledReminders(pkg, 0).size)
        assertEquals("a prayer alarm moved or went", prayersBefore, settledPrayers(pkg, prayersBefore))
    }
}
