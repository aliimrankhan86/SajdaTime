package com.sajdatime.app

import com.sajdatime.app.notify.FastReminderPlan
import com.sajdatime.app.notify.JamaahReminderPlan
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.FastKind
import com.sajdatime.core.FastReason
import com.sajdatime.core.JamaahRule
import com.sajdatime.core.JamaahTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The 1.6.1 jama'ah and fasting reminder planners (ExtraReminderPlans.kt). */
class ExtraReminderPlanTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs()
    private fun d(s: String) = LocalDate.parse(s)
    private fun morning(s: String) = d(s).atStartOfDay(zone).toInstant()

    @Test fun `a jama'ah reminder is N minutes before each jama'ah that is shown`() {
        val j = JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.After(15)))
        val plan = JamaahReminderPlan.plan(london, prefs, j, 10, d("2027-02-04"), morning("2027-02-04"), zone)
        val todays = plan.single { it.date == d("2027-02-04") }
        val asr = PrayerEngine.compute(london, d("2027-02-04"), prefs)[PrayerSlot.ASR]
        assertEquals(asr.plus(Duration.ofMinutes(15)), todays.jamaahAt)
        assertEquals(asr.plus(Duration.ofMinutes(5)), todays.at)
        assertTrue(plan.all { it.at.isAfter(morning("2027-02-04")) })
    }

    @Test fun `no reminder for a jama'ah outside its window, or when off`() {
        val wrong = JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.Fixed(LocalTime.of(3, 0))))
        assertEquals(0, JamaahReminderPlan.plan(london, prefs, wrong, 10, d("2027-02-04"), morning("2027-02-04"), zone).size)
        val ok = JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.After(15)))
        assertEquals(0, JamaahReminderPlan.plan(london, prefs, ok, 0, d("2027-02-04"), morning("2027-02-04"), zone).size)
    }

    @Test fun `a fast reminder rings at Isha the evening before`() {
        // Mon 26 Oct 2026 is a Monday fast; the reminder is Sunday's Isha.
        val plan = FastReminderPlan.plan(london, prefs, Sect.SUNNI, setOf(FastKind.MONDAY_THURSDAY), d("2026-10-25"), morning("2026-10-25"), zone)
        val monday = plan.single { it.fastDate == d("2026-10-26") }
        assertEquals(FastReason.MONDAY, monday.reason)
        assertEquals(PrayerEngine.compute(london, d("2026-10-25"), prefs)[PrayerSlot.ISHA], monday.at)
        assertEquals(PrayerEngine.compute(london, d("2026-10-26"), prefs)[PrayerSlot.FAJR], monday.fajrAt)
        assertEquals(d("2026-10-25"), monday.eve)
    }

    @Test fun `never for a Shia user, whatever is chosen`() =
        assertEquals(0, FastReminderPlan.plan(london, prefs.copy(sect = Sect.SHIA), Sect.SHIA, FastKind.entries.toSet(), d("2027-06-13"), morning("2027-06-13"), zone).size)

    @Test fun `none during Ramadan`() {
        val plan = FastReminderPlan.plan(london, prefs, Sect.SUNNI, FastKind.entries.toSet(), d("2027-02-15"), morning("2027-02-15"), zone)
        assertEquals(0, plan.size)
    }

    @Test fun `an Isha after 23 00 or after midnight moves the reminder to half an hour after Maghrib`() {
        // London in late June: Isha (Muslim World League, 18 degrees) is projected or very late.
        val eve = d("2027-06-20")
        val at = FastReminderPlan.eveningOf(london, prefs, eve, zone)
        val day = PrayerEngine.compute(london, eve, prefs)
        val isha = day[PrayerSlot.ISHA].atZone(zone)
        if (day.approximated || isha.toLocalDate() != eve || isha.toLocalTime() >= LocalTime.of(23, 0)) {
            assertEquals(day[PrayerSlot.MAGHRIB].plus(Duration.ofMinutes(30)), at)
        }
        // Whichever rule applied, the reminder is on the evening before, never after midnight.
        assertEquals(eve, at.atZone(zone).toLocalDate())
    }

    @Test fun `a reminder of the far north still lands on the evening before`() {
        val tromso = Coordinates(69.65, 18.96)
        val oslo = ZoneId.of("Europe/Oslo")
        var eve = d("2027-05-01")
        repeat(120) {
            val at = FastReminderPlan.eveningOf(tromso, prefs, eve, oslo)
            assertEquals("$eve", eve, at.atZone(oslo).toLocalDate())
            eve = eve.plusDays(1)
        }
    }

    @Test fun `the reminder is always after Maghrib, never at iftar`() {
        var eve = d("2026-11-01")
        repeat(365) {
            val at = FastReminderPlan.eveningOf(london, prefs, eve, zone)
            assertTrue("$eve", at.isAfter(PrayerEngine.compute(london, eve, prefs)[PrayerSlot.MAGHRIB]))
            eve = eve.plusDays(1)
        }
    }

    @Test fun `past reminders are dropped`() {
        val late = Instant.parse("2026-10-25T23:59:00Z")
        val plan = FastReminderPlan.plan(london, prefs, Sect.SUNNI, setOf(FastKind.MONDAY_THURSDAY), d("2026-10-25"), late, zone)
        assertTrue(plan.none { it.fastDate == d("2026-10-26") })
    }
}
