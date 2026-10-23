package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The six dates for 1448 and 1449 against an independent Umm al-Qura source: Aladhan's hToG with
 * calendarMethod=UAQ, fetched 23 Oct 2026 (development only; the app calls no network). The Eids also
 * agree with EidTest and 1 Ramadan 1448 with RamadanGoldenTest.
 */
class IslamicDatesGoldenTest {

    private fun d(s: String) = LocalDate.parse(s)

    private val aladhan = mapOf(
        Triple(1448, IslamicDay.MUHARRAM_1, 0) to "2026-06-16",
        Triple(1448, IslamicDay.ASHURA, 0) to "2026-06-25",
        Triple(1448, IslamicDay.RAMADAN_1, 0) to "2027-02-08",
        Triple(1448, IslamicDay.EID_AL_FITR, 0) to "2027-03-09",
        Triple(1448, IslamicDay.ARAFAH, 0) to "2027-05-15",
        Triple(1448, IslamicDay.EID_AL_ADHA, 0) to "2027-05-16",
        Triple(1449, IslamicDay.MUHARRAM_1, 0) to "2027-06-06",
        Triple(1449, IslamicDay.ASHURA, 0) to "2027-06-15",
        Triple(1449, IslamicDay.RAMADAN_1, 0) to "2028-01-28",
        Triple(1449, IslamicDay.EID_AL_FITR, 0) to "2028-02-26",
        Triple(1449, IslamicDay.ARAFAH, 0) to "2028-05-04",
        Triple(1449, IslamicDay.EID_AL_ADHA, 0) to "2028-05-05",
    )

    @Test fun `every date for 1448 and 1449 matches Aladhan's Umm al-Qura`() {
        for ((key, expected) in aladhan) {
            assertEquals(key.toString(), d(expected), IslamicDates.gregorian(key.first, key.second, key.third))
        }
    }

    @Test fun `a shift of plus one reads each day one Gregorian day earlier, minus one a day later`() {
        assertEquals(d("2027-02-07"), IslamicDates.gregorian(1448, IslamicDay.RAMADAN_1, 1))
        assertEquals(d("2027-02-09"), IslamicDates.gregorian(1448, IslamicDay.RAMADAN_1, -1))
    }

    @Test fun `upcoming on 23 Oct 2026 lists all six once, soonest first, rolling into 1449 for the past ones`() {
        val list = IslamicDates.upcoming(d("2026-10-23"), 0)
        assertEquals(
            listOf(
                IslamicDay.RAMADAN_1 to "2027-02-08",
                IslamicDay.EID_AL_FITR to "2027-03-09",
                IslamicDay.ARAFAH to "2027-05-15",
                IslamicDay.EID_AL_ADHA to "2027-05-16",
                IslamicDay.MUHARRAM_1 to "2027-06-06",
                IslamicDay.ASHURA to "2027-06-15",
            ),
            list.map { it.day to it.date.toString() },
        )
        assertEquals(listOf(1448, 1448, 1448, 1448, 1449, 1449), list.map { it.hijriYear })
    }

    @Test fun `a date that is today is still listed, first`() {
        val list = IslamicDates.upcoming(d("2027-03-09"), 0)
        assertEquals(IslamicDay.EID_AL_FITR, list.first().day)
        assertEquals(d("2027-03-09"), list.first().date)
    }

    @Test fun `the list agrees with the header's own calendar on every day of a year, for every shift`() {
        var day = d("2026-10-23")
        repeat(400) {
            for (shift in -2..2) {
                for (u in IslamicDates.upcoming(day, shift)) {
                    val h = HijriCalendar.of(u.date, shift)!!
                    assertEquals("$u shift $shift", u.day.month to u.day.day, h.month to h.day)
                    assertTrue(!u.date.isBefore(day))
                }
            }
            day = day.plusDays(1)
        }
    }

    @Test fun `outside the calendar's range there is nothing, not a crash`() =
        assertEquals(emptyList<UpcomingDate>(), IslamicDates.upcoming(d("2200-01-01"), 0))
}
