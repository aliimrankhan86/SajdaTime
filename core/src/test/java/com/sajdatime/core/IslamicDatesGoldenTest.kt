package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The eleven dates for 1448 and 1449 against an independent Umm al-Qura source: Aladhan's hToG with
 * calendarMethod=UAQ, fetched 23 Oct 2026 for the six shared and 24 Oct 2026 for the five Shia only
 * (development only; the app calls no network). The Eids also
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
        Triple(1448, IslamicDay.GHADIR, 0) to "2027-05-24",
        Triple(1448, IslamicDay.ARBAEEN, 0) to "2026-08-03",
        Triple(1448, IslamicDay.PROPHET_BIRTH, 0) to "2026-08-30",
        Triple(1448, IslamicDay.IMAM_ALI_BIRTH, 0) to "2026-12-22",
        Triple(1448, IslamicDay.IMAM_MAHDI_BIRTH, 0) to "2027-01-23",
        Triple(1449, IslamicDay.GHADIR, 0) to "2028-05-13",
        Triple(1449, IslamicDay.ARBAEEN, 0) to "2027-07-24",
        Triple(1449, IslamicDay.PROPHET_BIRTH, 0) to "2027-08-19",
        Triple(1449, IslamicDay.IMAM_ALI_BIRTH, 0) to "2027-12-11",
        Triple(1449, IslamicDay.IMAM_MAHDI_BIRTH, 0) to "2028-01-12",
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

    @Test fun `upcoming on 23 Oct 2026 lists the six once for a Sunni user, soonest first, rolling into 1449 for the past ones`() {
        val list = IslamicDates.upcoming(d("2026-10-23"), 0, Sect.SUNNI)
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

    @Test fun `a Shia user on 23 Oct 2026 gets all eleven once, sorted by date`() {
        val list = IslamicDates.upcoming(d("2026-10-23"), 0, Sect.SHIA)
        assertEquals(
            listOf(
                Triple(IslamicDay.IMAM_ALI_BIRTH, 1448, "2026-12-22"),
                Triple(IslamicDay.IMAM_MAHDI_BIRTH, 1448, "2027-01-23"),
                Triple(IslamicDay.RAMADAN_1, 1448, "2027-02-08"),
                Triple(IslamicDay.EID_AL_FITR, 1448, "2027-03-09"),
                Triple(IslamicDay.ARAFAH, 1448, "2027-05-15"),
                Triple(IslamicDay.EID_AL_ADHA, 1448, "2027-05-16"),
                Triple(IslamicDay.GHADIR, 1448, "2027-05-24"),
                Triple(IslamicDay.MUHARRAM_1, 1449, "2027-06-06"),
                Triple(IslamicDay.ASHURA, 1449, "2027-06-15"),
                Triple(IslamicDay.ARBAEEN, 1449, "2027-07-24"),
                Triple(IslamicDay.PROPHET_BIRTH, 1449, "2027-08-19"),
            ),
            list.map { Triple(it.day, it.hijriYear, it.date.toString()) },
        )
    }

    @Test fun `a date that is today is still listed, first`() {
        val list = IslamicDates.upcoming(d("2027-03-09"), 0, Sect.SUNNI)
        assertEquals(IslamicDay.EID_AL_FITR, list.first().day)
        assertEquals(d("2027-03-09"), list.first().date)
    }

    @Test fun `the list agrees with the header's own calendar on every day of a year, for every shift`() {
        var day = d("2026-10-23")
        repeat(400) {
            for (shift in -2..2) {
                for (sect in Sect.entries) for (u in IslamicDates.upcoming(day, shift, sect)) {
                    val h = HijriCalendar.of(u.date, shift)!!
                    assertEquals("$u shift $shift", u.day.month to u.day.day, h.month to h.day)
                    assertTrue(!u.date.isBefore(day))
                }
            }
            day = day.plusDays(1)
        }
    }

    @Test fun `outside the calendar's range there is nothing, not a crash`() =
        assertEquals(emptyList<UpcomingDate>(), IslamicDates.upcoming(d("2200-01-01"), 0, Sect.SHIA))
}
