package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.AdhkarReminderPlan
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.shouldPostAdhkarReminder
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

/** The 1.8.0 morning and evening adhkar reminder (ExtraReminderPlans.kt, AdhkarReminderPlan). */
class AdhkarReminderPlanTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs()
    private fun d(s: String) = LocalDate.parse(s)
    private fun morning(date: LocalDate) = date.atStartOfDay(zone).toInstant()
    private fun min(n: Long) = Duration.ofMinutes(n)

    @Test fun `on an ordinary day it is Fajr plus 30 and Asr plus 30`() {
        val date = d("2027-02-04")
        val day = PrayerEngine.compute(london, date, prefs)
        val today = AdhkarReminderPlan.plan(london, prefs, true, date, morning(date)).filter { it.date == date }
        assertEquals(2, today.size)
        assertEquals(day[PrayerSlot.FAJR].plus(min(30)), today.single { !it.evening }.at)
        assertEquals(day[PrayerSlot.SUNRISE], today.single { !it.evening }.endsAt)
        assertEquals(day[PrayerSlot.ASR].plus(min(30)), today.single { it.evening }.at)
        assertEquals(day[PrayerSlot.MAGHRIB], today.single { it.evening }.endsAt)
    }

    @Test fun `mosque adjustments move it, the sunrise cap holds, and a window too short gets none`() {
        // Near the equator Fajr is about 69 minutes before sunrise. Fajr 30 later and sunrise 20
        // earlier leave about 19, so Fajr + 30 is past sunrise - 10 and the cap decides.
        val singapore = Coordinates(1.3521, 103.8198)
        val sg = ZoneId.of("Asia/Singapore")
        val date = d("2027-03-01")
        val start = date.atStartOfDay(sg).toInstant()
        val adjusted = prefs.copy(adjustments = mapOf(PrayerSlot.FAJR to 30, PrayerSlot.SUNRISE to -20))
        val day = PrayerEngine.compute(singapore, date, adjusted)
        val m = AdhkarReminderPlan.plan(singapore, adjusted, true, date, start).single { it.date == date && !it.evening }
        assertEquals(day[PrayerSlot.SUNRISE].minus(min(10)), m.at)
        assertTrue(m.at.isAfter(day[PrayerSlot.FAJR]))
        // The adjusted Fajr is the one used: unadjusted it is 30 minutes earlier.
        assertEquals(PrayerEngine.compute(singapore, date, prefs)[PrayerSlot.FAJR].plus(min(30)), day[PrayerSlot.FAJR])

        // Sunrise 30 earlier leaves 10 minutes or less: sunrise - 10 is not after Fajr, so no morning reminder.
        val tight = prefs.copy(adjustments = mapOf(PrayerSlot.FAJR to 30, PrayerSlot.SUNRISE to -30))
        val tightDay = PrayerEngine.compute(singapore, date, tight)
        assertTrue(Duration.between(tightDay[PrayerSlot.FAJR], tightDay[PrayerSlot.SUNRISE]) <= min(10))
        assertTrue(AdhkarReminderPlan.plan(singapore, tight, true, date, start).none { it.date == date && !it.evening })
    }

    @Test fun `every reminder of a year sits inside its window, after the adhan, here and in the far north`() {
        val places = listOf(london to zone, Coordinates(69.65, 18.96) to ZoneId.of("Europe/Oslo"))
        for ((place, tz) in places) {
            var date = d("2027-01-01")
            var capped = 0
            repeat(365) {
                val day = PrayerEngine.compute(place, date, prefs)
                AdhkarReminderPlan.plan(place, prefs, true, date, date.atStartOfDay(tz).toInstant())
                    .filter { it.date == date }
                    .forEach { r ->
                        val start = day[if (r.evening) PrayerSlot.ASR else PrayerSlot.FAJR]
                        val end = day[if (r.evening) PrayerSlot.MAGHRIB else PrayerSlot.SUNRISE]
                        val latest = end.minus(min(if (r.evening) 15 else 10))
                        assertTrue("$place $date $r", r.at.isAfter(start))
                        assertTrue("$place $date $r", !r.at.isAfter(latest))
                        assertEquals(minOf(start.plus(min(30)), latest), r.at)
                        assertEquals(end, r.endsAt)
                        if (r.at == latest) capped++
                    }
                date = date.plusDays(1)
            }
            println("$place: capped $capped times in 2027")
        }
    }

    @Test fun `off is none, and a reminder already past is dropped`() {
        val date = d("2027-02-04")
        assertEquals(0, AdhkarReminderPlan.plan(london, prefs, false, date, morning(date)).size)
        val afterAsr = PrayerEngine.compute(london, date, prefs)[PrayerSlot.ASR].plus(min(31))
        assertTrue(AdhkarReminderPlan.plan(london, prefs, true, date, afterAsr).none { it.date == date })
        // Today to two days ahead, two a day: the window cancelAll walks.
        assertEquals(6, AdhkarReminderPlan.plan(london, prefs, true, date, morning(date)).size)
    }

    @Test fun `the receiver drops one switched off since, or arriving after its window closed`() {
        val date = d("2027-02-04")
        val sunrise = PrayerEngine.compute(london, date, prefs)[PrayerSlot.SUNRISE]
        val on = AppSettings(adhkarReminder = true)
        assertTrue(shouldPostAdhkarReminder(on, sunrise, sunrise.minus(min(5))))
        assertFalse(shouldPostAdhkarReminder(on, sunrise, sunrise))
        assertFalse(shouldPostAdhkarReminder(AppSettings(), sunrise, sunrise.minus(min(5))))
    }

    @Test fun `request codes run 60002 to 60733, unique in a leap year and above every other band`() {
        val codes = generateSequence(d("2028-01-01")) { it.plusDays(1) }.takeWhile { it.year == 2028 }
            .flatMap { listOf(PrayerAlarmScheduler.adhkarRequestCode(it, false), PrayerAlarmScheduler.adhkarRequestCode(it, true)) }
            .toList()
        assertEquals(codes.size, codes.toSet().size)
        assertEquals(60_002, codes.min())
        assertEquals(60_733, codes.max())
        assertTrue(codes.min() > PrayerAlarmScheduler.fastRequestCode(d("2028-12-31")))
    }
}
