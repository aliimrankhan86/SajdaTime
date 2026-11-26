package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.FridayReminderPlan
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.shouldPostFridayReminder
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.JamaahTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The 1.8.0 Friday reminder with Surah Al-Kahf (ExtraReminderPlans.kt, FridayReminderPlan). */
class FridayReminderPlanTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs()
    private val friday = LocalDate.parse("2027-02-05")
    private val thursday = friday.minusDays(1)
    private fun startOf(d: LocalDate, z: ZoneId = zone) = d.atStartOfDay(z).toInstant()
    private fun min(n: Long) = Duration.ofMinutes(n)
    private fun jumuah(h: Int, m: Int, zoneId: String = zone.id) = JamaahTimes(jumuah = LocalTime.of(h, m), zoneId = zoneId)

    @Test fun `with no Jumu'ah time it is an hour before Dhuhr, on Fridays only`() {
        val plan = FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), thursday, startOf(thursday), zone)
        val r = plan.single()
        assertEquals(friday, r.date)
        val dhuhr = PrayerEngine.compute(london, friday, prefs)[PrayerSlot.DHUHR]
        assertEquals(dhuhr.minus(min(60)), r.at)
        assertEquals(dhuhr, r.dhuhrAt)
        assertNull(r.jumuahAt)
        // A week with no Friday inside the three days planned gets nothing.
        assertEquals(0, FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), friday.plusDays(1), startOf(friday.plusDays(1)), zone).size)
    }

    @Test fun `with a Jumu'ah time it is an hour before that`() {
        val r = FridayReminderPlan.plan(london, prefs, true, jumuah(13, 15), friday, startOf(friday), zone).single()
        assertEquals(friday.atTime(13, 15).atZone(zone).toInstant(), r.jumuahAt)
        assertEquals(friday.atTime(12, 15).atZone(zone).toInstant(), r.at)
    }

    @Test fun `a Jumu'ah time outside Dhuhr's window, or typed in another time zone, falls back to Dhuhr`() {
        val dhuhr = PrayerEngine.compute(london, friday, prefs)[PrayerSlot.DHUHR]
        for (j in listOf(jumuah(11, 0), jumuah(13, 15, "Asia/Karachi"))) {
            val r = FridayReminderPlan.plan(london, prefs, true, j, friday, startOf(friday), zone).single()
            assertNull("$j", r.jumuahAt)
            assertEquals("$j", dhuhr.minus(min(60)), r.at)
        }
    }

    @Test fun `mosque adjustments move Dhuhr and the reminder with it`() {
        val adjusted = prefs.copy(adjustments = mapOf(PrayerSlot.DHUHR to 10))
        val plain = FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), friday, startOf(friday), zone).single()
        val moved = FridayReminderPlan.plan(london, adjusted, true, JamaahTimes(), friday, startOf(friday), zone).single()
        assertEquals(plain.at.plus(min(10)), moved.at)
    }

    @Test fun `Friday is the phone's own Friday, here and on the far side of the date line`() {
        val auckland = ZoneId.of("Pacific/Auckland")
        val akl = Coordinates(-36.85, 174.76)
        val r = FridayReminderPlan.plan(akl, prefs, true, JamaahTimes(), thursday, startOf(thursday, auckland), auckland).single()
        assertEquals(DayOfWeek.FRIDAY, r.at.atZone(auckland).dayOfWeek)
        assertEquals(friday, r.at.atZone(auckland).toLocalDate())
    }

    @Test fun `the Jumu'ah line is for a Sunni user with a Jumu'ah time, the Dhuhr line for everyone else`() {
        val set = FridayReminderPlan.plan(london, prefs, true, jumuah(13, 15), friday, startOf(friday), zone).single()
        assertEquals(set.jumuahAt, set.jumuahLineFor(Sect.SUNNI))
        assertNull(set.jumuahLineFor(Sect.SHIA))
        val unset = FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), friday, startOf(friday), zone).single()
        assertNull(unset.jumuahLineFor(Sect.SUNNI))
    }

    @Test fun `a heads up before Dhuhr within 15 minutes of it is the one dropped`() {
        val r = FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), friday, startOf(friday), zone)
        assertTrue(FridayReminderPlan.overlaps(r.single().at, r))
        assertTrue(FridayReminderPlan.overlaps(r.single().at.plus(min(15)), r))
        assertTrue(FridayReminderPlan.overlaps(r.single().at.minus(min(15)), r))
        assertFalse(FridayReminderPlan.overlaps(r.single().at.plus(min(16)), r))
        assertFalse(FridayReminderPlan.overlaps(r.single().at, emptyList()))
    }

    @Test fun `off is none, and one already past is dropped`() {
        assertEquals(0, FridayReminderPlan.plan(london, prefs, false, JamaahTimes(), friday, startOf(friday), zone).size)
        val dhuhr = PrayerEngine.compute(london, friday, prefs)[PrayerSlot.DHUHR]
        assertEquals(0, FridayReminderPlan.plan(london, prefs, true, JamaahTimes(), friday, dhuhr.minus(min(59)), zone).size)
    }

    @Test fun `the receiver drops one switched off, not on Friday, or once the time it names has begun`() {
        val dhuhr = PrayerEngine.compute(london, friday, prefs)[PrayerSlot.DHUHR]
        val on = AppSettings(fridayReminder = true)
        assertTrue(shouldPostFridayReminder(on, dhuhr, dhuhr.minus(min(60)), zone))
        assertFalse(shouldPostFridayReminder(AppSettings(), dhuhr, dhuhr.minus(min(60)), zone))
        assertFalse(shouldPostFridayReminder(on, dhuhr, dhuhr, zone))
        val thursdayDhuhr = dhuhr.minus(Duration.ofDays(1))
        assertFalse(shouldPostFridayReminder(on, thursdayDhuhr, thursdayDhuhr.minus(min(60)), zone))
    }

    @Test fun `request codes run 70001 to 70366, unique in a leap year and above the adhkar band`() {
        val codes = generateSequence(LocalDate.parse("2028-01-01")) { it.plusDays(1) }.takeWhile { it.year == 2028 }
            .map { PrayerAlarmScheduler.fridayRequestCode(it) }.toList()
        assertEquals(codes.size, codes.toSet().size)
        assertEquals(70_001, codes.min())
        assertEquals(70_366, codes.max())
        assertTrue(codes.min() > PrayerAlarmScheduler.adhkarRequestCode(LocalDate.parse("2028-12-31"), true))
    }
}
