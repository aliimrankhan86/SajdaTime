package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.decodeSlots
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.PrayerReminderPlan
import com.sajdatime.app.notify.shouldPostPrayerReminder
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * The "prayer in N minutes" reminder (1.5.0): off by default, per prayer, never late, and alarm codes
 * that cannot collide with any other family.
 */
class PrayerReminderTest {

    private val asr = Instant.parse("2027-01-10T14:00:00Z")
    private val maghrib = Instant.parse("2027-01-10T16:10:00Z")
    private val upcoming = listOf(PrayerSlot.ASR to asr, PrayerSlot.MAGHRIB to maghrib)
    private val all = PrayerSlot.entries.filter { it.isPrayer }.toSet()

    @Test
    fun `off by default, with all five ticked for when it is turned on`() {
        val defaults = AppSettings()
        assertEquals(0, defaults.prayerReminderMinutes)
        assertEquals(all, defaults.prayerReminderFor)
        assertEquals(0, AppSettings.PRAYER_REMINDER_CHOICES.first())
    }

    @Test
    fun `off plans nothing`() {
        assertTrue(PrayerReminderPlan.plan(upcoming, 0, all, asr.minusSeconds(3600)).isEmpty())
    }

    @Test
    fun `plans N minutes before each chosen prayer only`() {
        val planned = PrayerReminderPlan.plan(upcoming, 10, setOf(PrayerSlot.MAGHRIB), asr.minusSeconds(3600))
        assertEquals(listOf(PrayerSlot.MAGHRIB), planned.map { it.slot })
        assertEquals(maghrib.minusSeconds(600), planned.single().at)
        assertEquals(maghrib, planned.single().prayerAt)
    }

    @Test
    fun `a reminder whose moment has passed is skipped even though the prayer is ahead`() {
        // Three minutes before Asr, a 10 minute reminder is already late: none for Asr, Maghrib's stays.
        val planned = PrayerReminderPlan.plan(upcoming, 10, all, asr.minusSeconds(180))
        assertEquals(listOf(PrayerSlot.MAGHRIB), planned.map { it.slot })
    }

    @Test
    fun `on arrival it is shown only when still on, still ticked and still ahead of the prayer`() {
        val on = AppSettings(prayerReminderMinutes = 10)
        val at = asr.minusSeconds(600)
        assertTrue(shouldPostPrayerReminder(on, PrayerSlot.ASR, asr, at))
        assertTrue(shouldPostPrayerReminder(on, PrayerSlot.ASR, asr, at.plusSeconds(240)))
        assertFalse(shouldPostPrayerReminder(AppSettings(), PrayerSlot.ASR, asr, at))
        assertFalse(shouldPostPrayerReminder(on.copy(prayerReminderFor = setOf(PrayerSlot.FAJR)), PrayerSlot.ASR, asr, at))
        // More than five minutes late.
        assertFalse(shouldPostPrayerReminder(on, PrayerSlot.ASR, asr, at.plusSeconds(301)))
        // Less than a minute before the prayer, even inside the grace of a 5 minute reminder.
        assertFalse(shouldPostPrayerReminder(on.copy(prayerReminderMinutes = 5), PrayerSlot.ASR, asr, asr.minusSeconds(30)))
    }

    @Test
    fun `stored prayers decode, absent meaning all five and blank meaning none`() {
        assertEquals(all, decodeSlots(null))
        assertEquals(emptySet<PrayerSlot>(), decodeSlots(""))
        assertEquals(setOf(PrayerSlot.FAJR, PrayerSlot.ASR), decodeSlots("FAJR,ASR,SUNRISE,NOT_A_PRAYER"))
    }

    @Test
    fun `reminder codes sit in their own range on every day of a leap year`() {
        var date = LocalDate.parse("2028-01-01")
        val codes = mutableSetOf<Int>()
        while (date.year == 2028) {
            for (slot in all) {
                val code = PrayerAlarmScheduler.prayerReminderRequestCode(slot, date)
                assertTrue("$slot $date $code", code in 30_010..33_669)
                assertTrue(codes.add(code))
            }
            date = date.plusDays(1)
        }
        assertTrue(PrayerAlarmScheduler.ACTION_PRAYER_REMINDER != PrayerAlarmScheduler.ACTION_PRAYER)
        assertTrue(PrayerAlarmScheduler.ACTION_PRAYER_REMINDER != PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
    }
}
