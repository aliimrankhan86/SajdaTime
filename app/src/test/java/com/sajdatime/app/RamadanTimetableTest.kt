package com.sajdatime.app

import com.sajdatime.app.ui.RamadanTimetable
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/** The rows behind the Ramadan timetable sheet. Expected values are the London table in RAMADAN-LOCATION-SPEC 9.2. */
class RamadanTimetableTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)
    private val info = HijriCalendar.ramadan(LocalDate.of(2027, 2, 22), 0)!!
    private val rows = RamadanTimetable.rows(london, prefs, info)

    private fun at(date: String, time: String) = LocalDateTime.parse("${date}T$time").atZone(zone).toInstant()

    @Test
    fun `one row per day, day 1 first and the dates consecutive`() {
        assertEquals(info.length, rows.size)
        assertEquals(29, rows.size)
        assertEquals(LocalDate.of(2027, 2, 8), rows.first().date)
        assertEquals(LocalDate.of(2027, 3, 8), rows.last().date)
        assertEquals((1..29).toList(), rows.map { it.dayNumber })
        rows.zipWithNext().forEach { (a, b) -> assertEquals(a.date.plusDays(1), b.date) }
    }

    @Test
    fun `suhoor ends and iftar are the engine's Fajr and Maghrib for that date, to the instant`() {
        for (row in rows) {
            val engine = PrayerEngine.compute(london, row.date, prefs)
            assertEquals("${row.date}", engine[PrayerSlot.FAJR], row.suhoorEnds)
            assertEquals("${row.date}", engine[PrayerSlot.MAGHRIB], row.iftar)
        }
    }

    @Test
    fun `London rows match the pinned table on day 1, day 15 and day 29`() {
        fun row(day: Int) = rows[day - 1]
        assertEquals(at("2027-02-08", "05:34"), row(1).suhoorEnds)
        assertEquals(at("2027-02-08", "17:02"), row(1).iftar)
        assertEquals(Duration.ofMinutes(11 * 60 + 28), row(1).fastLength)
        assertEquals(at("2027-02-22", "05:10"), row(15).suhoorEnds)
        assertEquals(at("2027-02-22", "17:28"), row(15).iftar)
        assertEquals(Duration.ofMinutes(12 * 60 + 18), row(15).fastLength)
        assertEquals(at("2027-03-08", "04:40"), row(29).suhoorEnds)
        assertEquals(at("2027-03-08", "17:53"), row(29).iftar)
        assertEquals(Duration.ofMinutes(13 * 60 + 13), row(29).fastLength)
    }

    @Test
    fun `the fast grows through the month at London in February and March`() {
        assertTrue(rows.zipWithNext().all { (a, b) -> b.fastLength > a.fastLength })
    }

    @Test
    fun `an ordinary place has no projected rows`() {
        assertFalse(rows.any { it.approximated })
    }

    @Test
    fun `the users shift and adjustments reach every row`() {
        val shifted = prefs.copy(hijriOffsetDays = 1)
        val shiftedInfo = HijriCalendar.ramadan(LocalDate.of(2027, 2, 22), 1)!!
        val shiftedRows = RamadanTimetable.rows(london, shifted, shiftedInfo)
        assertEquals(LocalDate.of(2027, 2, 7), shiftedRows.first().date)
        assertEquals(LocalDate.of(2027, 3, 7), shiftedRows.last().date)

        val adjusted = prefs.copy(adjustments = mapOf(PrayerSlot.MAGHRIB to 3))
        val adjustedRows = RamadanTimetable.rows(london, adjusted, info)
        rows.zip(adjustedRows).forEach { (plain, moved) ->
            assertEquals(plain.iftar.plusSeconds(180), moved.iftar)
        }
    }

    @Test
    fun `Tromso is flagged as projected only if the engine says so`() {
        val tromso = Coordinates(69.6496, 18.9560)
        val tromsoRows = RamadanTimetable.rows(tromso, prefs, info)
        for (row in tromsoRows) {
            assertEquals(row.times.approximated, row.approximated)
        }
    }
}
