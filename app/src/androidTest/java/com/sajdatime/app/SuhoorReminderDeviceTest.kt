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
        val prayersBefore = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_PRAYER)
        assertTrue("no prayer alarms to compare against", prayersBefore.isNotEmpty())
        assertEquals("a reminder exists while it is switched off", 0, DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER).size)

        runBlocking { repository.setSuhoorReminderMinutes(30) }
        reschedule()
        val reminders = DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
        assertEquals("one reminder for each planned day", expected.size, reminders.size)
        assertEquals(
            "the reminder is not 30 minutes before Fajr",
            expected.map { it.at.atZone(zone).toLocalDateTime().withNano(0) },
            reminders,
        )
        assertEquals("a prayer alarm moved or went", prayersBefore, DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_PRAYER))

        runBlocking { repository.setSuhoorReminderMinutes(0) }
        reschedule()
        assertEquals("switching it off left a reminder behind", 0, DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER).size)
        assertEquals("a prayer alarm moved or went", prayersBefore, DeviceSupport.alarmTimes(pkg, PrayerAlarmScheduler.ACTION_PRAYER))
    }
}
