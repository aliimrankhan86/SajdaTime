package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Eid by calendar. The dates are pinned from the Umm al-Qura chronology in JDK 21 (the same table
 * HijriCalendarTest pins Ramadan from) and not recomputed, because a test that derives its
 * expectation from the call it is testing passes whatever the calendar says.
 *
 * 9 March 2027 as 1 Shawwal 1448 follows from 1 Ramadan 1448 being 8 February 2027 with 29 days.
 */
class EidTest {

    private data class Expected(val date: LocalDate, val eid: Eid, val hijriYear: Int)

    private val pinned = listOf(
        Expected(LocalDate.of(2026, 3, 20), Eid.FITR, 1447),
        Expected(LocalDate.of(2026, 5, 27), Eid.ADHA, 1447),
        Expected(LocalDate.of(2027, 3, 9), Eid.FITR, 1448),
        Expected(LocalDate.of(2027, 5, 16), Eid.ADHA, 1448),
        Expected(LocalDate.of(2028, 2, 26), Eid.FITR, 1449),
        Expected(LocalDate.of(2028, 5, 5), Eid.ADHA, 1449),
        Expected(LocalDate.of(2029, 2, 14), Eid.FITR, 1450),
        Expected(LocalDate.of(2029, 4, 24), Eid.ADHA, 1450),
    )

    @Test
    fun `Eid al-Fitr 2027 is 9 March and Eid al-Adha 2027 is 16 May`() {
        val fitr = HijriCalendar.eidOn(LocalDate.of(2027, 3, 9), 0)!!
        assertEquals(Eid.FITR, fitr.eid)
        assertEquals(1448, fitr.hijriYear)
        val adha = HijriCalendar.eidOn(LocalDate.of(2027, 5, 16), 0)!!
        assertEquals(Eid.ADHA, adha.eid)
        assertEquals(1448, adha.hijriYear)
    }

    @Test
    fun `Eid al-Fitr 2028 is 26 February and Eid al-Adha 2028 is 5 May`() {
        val fitr = HijriCalendar.eidOn(LocalDate.of(2028, 2, 26), 0)!!
        assertEquals(Eid.FITR, fitr.eid)
        assertEquals(1449, fitr.hijriYear)
        val adha = HijriCalendar.eidOn(LocalDate.of(2028, 5, 5), 0)!!
        assertEquals(Eid.ADHA, adha.eid)
        assertEquals(1449, adha.hijriYear)
    }

    @Test
    fun `every pinned Eid is found on its day and on no neighbouring day`() {
        for (e in pinned) {
            val day = HijriCalendar.eidOn(e.date, 0)
            assertEquals("${e.date}", e.eid, day?.eid)
            assertEquals("${e.date}", e.hijriYear, day?.hijriYear)
            assertNull("the day before ${e.date}", HijriCalendar.eidOn(e.date.minusDays(1), 0))
            assertNull("the day after ${e.date}", HijriCalendar.eidOn(e.date.plusDays(1), 0))
        }
    }

    @Test
    fun `Eid al-Fitr is the day after the last day of Ramadan`() {
        // The two features share one calendar, so the Ramadan card ends on the day the greeting
        // starts and the two are never on screen together.
        for (year in listOf(2026, 2027, 2028, 2029)) {
            val ramadan = HijriCalendar.ramadan(LocalDate.of(year, 1, 1), 0)!!
            val fitr = HijriCalendar.eidOn(ramadan.lastDay.plusDays(1), 0)
            assertEquals("$year", Eid.FITR, fitr?.eid)
            assertFalse("$year", HijriCalendar.isRamadan(ramadan.lastDay.plusDays(1), 0))
        }
    }

    @Test
    fun `a whole calendar year holds exactly the two Eids of 2027 and of 2028`() {
        for ((year, expected) in listOf(
            2027 to listOf(Eid.FITR, Eid.ADHA),
            2028 to listOf(Eid.FITR, Eid.ADHA),
        )) {
            val found = generateSequence(LocalDate.of(year, 1, 1)) { it.plusDays(1) }
                .takeWhile { it.year == year }
                .mapNotNull { HijriCalendar.eidOn(it, 0)?.eid }
                .toList()
            assertEquals("$year", expected, found)
        }
    }

    @Test
    fun `the users date shift moves the greeting with their header`() {
        val umm = LocalDate.of(2027, 3, 9)
        // Plus one: this user's header reads 1 Shawwal a day earlier.
        assertEquals(Eid.FITR, HijriCalendar.eidOn(umm.minusDays(1), 1)?.eid)
        assertNull(HijriCalendar.eidOn(umm, 1))
        // Minus one: a day later.
        assertEquals(Eid.FITR, HijriCalendar.eidOn(umm.plusDays(1), -1)?.eid)
        assertNull(HijriCalendar.eidOn(umm, -1))
    }

    @Test
    fun `every shift from minus 2 to plus 2 lands the greeting on the day the header agrees`() {
        for (offset in -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS..CalculationPrefs.MAX_HIJRI_OFFSET_DAYS) {
            for (e in pinned) {
                val day = e.date.minusDays(offset.toLong())
                val found = HijriCalendar.eidOn(day, offset)
                assertEquals("${e.date} offset $offset", e.eid, found?.eid)
                // The header and the greeting must read the same Hijri day.
                val header = HijriCalendar.of(day, offset)!!
                assertEquals(e.eid.day, header.day)
                assertEquals(e.eid.month, header.month)
            }
        }
    }

    @Test
    fun `the code is year then month, differs for the two Eids and rises with time`() {
        val codes = pinned.map { HijriCalendar.eidOn(it.date, 0)!!.code }
        assertEquals(listOf(144710, 144712, 144810, 144812, 144910, 144912, 145010, 145012), codes)
        assertEquals(codes.sorted(), codes)
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `each Eid is shown until it is closed and once closed it stays closed`() {
        val fitr1448 = HijriCalendar.eidOn(LocalDate.of(2027, 3, 9), 0)!!
        val adha1448 = HijriCalendar.eidOn(LocalDate.of(2027, 5, 16), 0)!!
        val fitr1449 = HijriCalendar.eidOn(LocalDate.of(2028, 2, 26), 0)!!
        assertTrue("never closed", fitr1448.isUnseen(0))
        assertFalse("closed", fitr1448.isUnseen(fitr1448.code))
        // Closing one Eid says nothing about the next one, in the same year or the next.
        assertTrue(adha1448.isUnseen(fitr1448.code))
        assertTrue(fitr1449.isUnseen(adha1448.code))
        // And the other way round: the last closed code is only ever the latest Eid.
        assertFalse(adha1448.isUnseen(adha1448.code))
    }

    @Test
    fun `an ordinary day and a date outside the calendar's range give null, not a throw`() {
        assertNull(HijriCalendar.eidOn(LocalDate.of(2027, 7, 1), 0))
        assertNull(HijriCalendar.eidOn(LocalDate.of(2027, 2, 8), 0)) // 1 Ramadan is not an Eid
        assertNull(HijriCalendar.eidOn(LocalDate.of(2200, 1, 1), 0))
        assertNull(HijriCalendar.eidOn(LocalDate.of(1800, 1, 1), 0))
    }
}
