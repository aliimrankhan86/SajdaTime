package com.sajdatime.app

import com.sajdatime.app.notify.SuhoorReminderPlan
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Which suhoor reminders get laid down. Ramadan 1448 in London runs 8 Feb to 8 Mar 2027 (29 days,
 * 1 Shawwal on 9 Mar), so every date below is a known edge of the real month. The expected times are
 * recomputed from the engine, never typed in, so the reminder can only ever disagree with Fajr by
 * the number of minutes the user chose.
 */
class SuhoorReminderPlanTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)

    private fun now(date: String, time: String): Instant =
        LocalDateTime.parse("${date}T$time").atZone(zone).toInstant()

    private fun fajr(date: String, p: CalculationPrefs = prefs): Instant =
        PrayerEngine.compute(london, LocalDate.parse(date), p)[PrayerSlot.FAJR]

    private fun plan(today: String, time: String, minutes: Int, p: CalculationPrefs = prefs) =
        SuhoorReminderPlan.plan(london, p, minutes, LocalDate.parse(today), now(today, time))

    @Test
    fun `off plans nothing, even in the middle of Ramadan`() {
        assertTrue(plan("2027-02-20", "01:00", 0).isEmpty())
        assertTrue(plan("2027-02-20", "01:00", -15).isEmpty())
    }

    @Test
    fun `outside Ramadan plans nothing`() {
        assertTrue(plan("2027-01-20", "01:00", 30).isEmpty())
        assertTrue(plan("2027-06-01", "01:00", 30).isEmpty())
    }

    @Test
    fun `inside Ramadan, before today's reminder, plans today and tomorrow`() {
        val planned = plan("2027-02-15", "00:30", 30)
        assertEquals(listOf(LocalDate.parse("2027-02-15"), LocalDate.parse("2027-02-16")), planned.map { it.date })
        for (r in planned) {
            assertEquals(fajr(r.date.toString()), r.fajrAt)
            assertEquals(r.fajrAt.minusSeconds(30 * 60), r.at)
        }
    }

    @Test
    fun `once today's reminder time has passed it is not planned again`() {
        // 20 minutes before Fajr: a 30 minute reminder was 10 minutes ago, so only tomorrow's is left.
        val fajrToday = fajr("2027-02-15")
        val planned = SuhoorReminderPlan.plan(
            london, prefs, 30, LocalDate.parse("2027-02-15"), fajrToday.minusSeconds(20 * 60),
        )
        assertEquals(listOf(LocalDate.parse("2027-02-16")), planned.map { it.date })
    }

    @Test
    fun `before Fajr but after a long reminder time still plans the shorter one`() {
        // Same moment, 20 minutes before Fajr: a 15 minute reminder is still ahead, a 30 minute one is not.
        val moment = fajr("2027-02-15").minusSeconds(20 * 60)
        val today = LocalDate.parse("2027-02-15")
        assertEquals(today, SuhoorReminderPlan.plan(london, prefs, 15, today, moment).first().date)
        assertEquals(
            LocalDate.parse("2027-02-16"),
            SuhoorReminderPlan.plan(london, prefs, 30, today, moment).first().date,
        )
    }

    @Test
    fun `a reminder exactly at now is already past and is not planned`() {
        val today = LocalDate.parse("2027-02-15")
        val moment = fajr("2027-02-15").minusSeconds(30 * 60)
        assertEquals(
            listOf(LocalDate.parse("2027-02-16")),
            SuhoorReminderPlan.plan(london, prefs, 30, today, moment).map { it.date },
        )
    }

    @Test
    fun `the eve of Ramadan plans day 1, and the day itself is not Ramadan yet`() {
        val planned = plan("2027-02-07", "20:00", 30)
        assertEquals(listOf(LocalDate.parse("2027-02-08")), planned.map { it.date })
    }

    @Test
    fun `the last day plans itself and never the first of Shawwal`() {
        val planned = plan("2027-03-08", "01:00", 30)
        assertEquals(listOf(LocalDate.parse("2027-03-08")), planned.map { it.date })
    }

    @Test
    fun `after the last Fajr nothing is left, and tomorrow is Shawwal`() {
        assertTrue(plan("2027-03-08", "12:00", 30).isEmpty())
    }

    @Test
    fun `the Islamic date shift moves the month, minus one starts it a day later`() {
        val shifted = prefs.copy(hijriOffsetDays = -1)
        // 8 Feb is the eve under a minus one shift, so nothing today, day 1 tomorrow.
        assertEquals(
            listOf(LocalDate.parse("2027-02-09")),
            plan("2027-02-08", "00:30", 30, shifted).map { it.date },
        )
        // And the month now ends a day later: 9 Mar is its last day.
        assertEquals(
            listOf(LocalDate.parse("2027-03-09")),
            plan("2027-03-09", "01:00", 30, shifted).map { it.date },
        )
    }

    @Test
    fun `a plus one shift starts the month a day earlier`() {
        val shifted = prefs.copy(hijriOffsetDays = 1)
        assertEquals(
            listOf(LocalDate.parse("2027-02-07")),
            plan("2027-02-06", "20:00", 30, shifted).map { it.date },
        )
    }

    @Test
    fun `the reminder is always strictly before Fajr and never on top of the Fajr alert`() {
        for (minutes in listOf(15, 30, 45, 60)) {
            for (r in plan("2027-02-20", "00:30", minutes)) {
                assertTrue("$minutes min on ${r.date}", r.at.isBefore(r.fajrAt))
                assertNotEquals(r.fajrAt, r.at)
                assertEquals(minutes * 60L, r.fajrAt.epochSecond - r.at.epochSecond)
            }
        }
    }

    @Test
    fun `the planned Fajr is the engine's, to the instant, for the user's own prefs`() {
        val custom = prefs.copy(hijriOffsetDays = 1)
        for (r in plan("2027-02-20", "00:30", 45, custom)) {
            assertEquals(fajr(r.date.toString(), custom), r.fajrAt)
        }
    }
}
