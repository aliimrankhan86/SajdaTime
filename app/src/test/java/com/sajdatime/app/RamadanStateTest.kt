package com.sajdatime.app

import com.sajdatime.app.ui.RamadanState
import com.sajdatime.app.ui.RamadanState.Phase
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The numbers on the Ramadan card, from the real engine. Expected values are the golden tables in
 * docs/plans/2026-10-widget-ramadan-tracker/RAMADAN-LOCATION-SPEC.md section 9.2, which
 * RamadanGoldenTest in :core also pins, so this file tests the arithmetic on top of them and not
 * the astronomy under them.
 */
class RamadanStateTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val londonZone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)

    private fun build(date: String, where: Coordinates = london, p: CalculationPrefs = prefs): RamadanState? {
        val day = LocalDate.parse(date)
        return RamadanState.build(where, p, PrayerEngine.compute(where, day, p))
    }

    private fun at(date: String, time: String) =
        LocalDateTime.parse("${date}T$time").atZone(londonZone).toInstant()

    @Test
    fun `London on day 15 has a 12h18m fast, 4 minutes longer than yesterday`() {
        val state = build("2027-02-22")!!
        assertEquals(15, state.info.dayNumber)
        assertEquals(Duration.ofMinutes(12 * 60 + 18), state.fastLength)
        assertEquals(4L, state.fastChangeMinutes)
        // The change is the difference of two engine calls, not just the table: 21 February is 12h14m.
        val yesterday = PrayerEngine.compute(london, LocalDate.parse("2027-02-21"), prefs)
        val yesterdayFast = Duration.between(
            yesterday[com.sajdatime.core.PrayerSlot.FAJR],
            yesterday[com.sajdatime.core.PrayerSlot.MAGHRIB],
        )
        assertEquals(Duration.ofMinutes(12 * 60 + 14), yesterdayFast)
    }

    @Test
    fun `suhoor end and iftar are the Fajr and Maghrib rows of the same day, to the instant`() {
        val state = build("2027-02-22")!!
        assertEquals(state.today[com.sajdatime.core.PrayerSlot.FAJR], state.suhoorEnds)
        assertEquals(state.today[com.sajdatime.core.PrayerSlot.MAGHRIB], state.iftar)
        // And the printed values are the pinned ones: 05:10 and 17:28 at London in February.
        assertEquals(at("2027-02-22", "05:10"), state.suhoorEnds)
        assertEquals(at("2027-02-22", "17:28"), state.iftar)
        assertEquals(at("2027-02-23", "05:08"), state.tomorrowSuhoorEnds)
        assertEquals(at("2027-02-23", "17:30"), state.tomorrowIftar)
    }

    @Test
    fun `the phase follows the clock through the day`() {
        val state = build("2027-02-22")!!
        assertEquals(Phase.BEFORE_SUHOOR_END, state.phase(at("2027-02-22", "04:00")))
        assertEquals(Phase.BEFORE_SUHOOR_END, state.phase(at("2027-02-22", "05:09")))
        assertEquals(Phase.FASTING, state.phase(at("2027-02-22", "05:10")))
        assertEquals(Phase.FASTING, state.phase(at("2027-02-22", "12:00")))
        assertEquals(Phase.FASTING, state.phase(at("2027-02-22", "17:27")))
        assertEquals(Phase.AFTER_IFTAR, state.phase(at("2027-02-22", "17:28")))
        assertEquals(Phase.AFTER_IFTAR, state.phase(at("2027-02-22", "20:00")))
    }

    @Test
    fun `tomorrow is a fasting day on day 15 and not on the last day`() {
        assertTrue(build("2027-02-22")!!.tomorrowIsRamadan)
        assertTrue(build("2027-03-07")!!.tomorrowIsRamadan)
        val last = build("2027-03-08")!!
        assertTrue(last.info.isLastDay)
        assertFalse(last.tomorrowIsRamadan)
    }

    @Test
    fun `Sydney's fast gets shorter day by day`() {
        val sydney = Coordinates(-33.8688, 151.2093)
        val state = build("2027-02-22", sydney)!!
        assertTrue("${state.fastChangeMinutes}", state.fastChangeMinutes in -5L..-1L)
    }

    @Test
    fun `Tromso's change is several minutes a day and nothing clips it`() {
        val tromso = Coordinates(69.6496, 18.9560)
        val state = build("2027-02-22", tromso)!!
        assertTrue("${state.fastChangeMinutes}", state.fastChangeMinutes in 6L..12L)
        assertFalse(state.today.approximated)
    }

    @Test
    fun `a daylight saving day shows the true change, plus 5 and not plus 65`() {
        // Ramadan is over by 28 March 2027, so build the state directly. The arithmetic does not
        // care what the calendar says, and the instant based change is the thing under test.
        val date = LocalDate.parse("2027-03-28")
        val info = HijriCalendar.ramadan(LocalDate.parse("2027-02-22"), 0)!!
        val state = RamadanState(
            info = info,
            today = PrayerEngine.compute(london, date, prefs),
            yesterday = PrayerEngine.compute(london, date.minusDays(1), prefs),
            tomorrow = PrayerEngine.compute(london, date.plusDays(1), prefs),
        )
        assertEquals(5L, state.fastChangeMinutes)
    }

    @Test
    fun `the state exists from 3 days before to the last day and not otherwise`() {
        // 3 days since 19 Oct 2026 (the owner's change; it was 7).
        assertNull("19 days before", build("2027-01-20"))
        assertNull("7 days before, the old window", build("2027-02-01"))
        assertNull("4 days before", build("2027-02-04"))
        val eve = build("2027-02-05")
        assertNotNull("3 days before", eve)
        assertFalse(eve!!.info.isRamadan)
        assertNotNull("the eve", build("2027-02-07"))
        assertEquals(1, build("2027-02-08")!!.info.dayNumber)
        assertEquals(3, build("2027-02-10")!!.info.dayNumber)
        assertEquals(29, build("2027-03-08")!!.info.dayNumber)
        assertNull("1 Shawwal", build("2027-03-09"))
        assertNull("July", build("2027-07-01"))
    }

    @Test
    fun `the users date shift moves the window and the day number`() {
        val shifted = prefs.copy(hijriOffsetDays = 1)
        // Plus one makes 7 February day 1, so 4 February is the first day of the notice.
        assertNotNull(build("2027-02-04", p = shifted))
        assertNull(build("2027-02-03", p = shifted))
        assertEquals(1, build("2027-02-07", p = shifted)!!.info.dayNumber)
        // The last day is a day earlier too, and 8 March is already Shawwal for this user.
        assertEquals(29, build("2027-03-07", p = shifted)!!.info.dayNumber)
        assertNull(build("2027-03-08", p = shifted))
    }

    @Test
    fun `yesterday and tomorrow are the neighbouring calendar days`() {
        val state = build("2027-02-22")!!
        assertEquals(LocalDate.parse("2027-02-21"), state.yesterday.date)
        assertEquals(LocalDate.parse("2027-02-23"), state.tomorrow.date)
        assertEquals(LocalDate.parse("2027-02-22"), state.today.date)
    }

    @Test
    fun `Match your mosque minutes reach the card`() {
        // The card shows the adjusted times, like the list above it.
        val adjusted = prefs.copy(adjustments = mapOf(com.sajdatime.core.PrayerSlot.MAGHRIB to 3))
        val plain = build("2027-02-22")!!
        val moved = build("2027-02-22", p = adjusted)!!
        assertEquals(plain.iftar.plusSeconds(180), moved.iftar)
        assertEquals(plain.fastLength.plusMinutes(3), moved.fastLength)
    }
}
