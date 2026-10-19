package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The dates here come from RAMADAN-LOCATION-SPEC.md section 3 and 9.1, which were computed with
 * JDK 21's Umm al-Qura chronology in the planning session and cross checked against the Aladhan
 * API (8 February 2027 is 01-09-1448 there too).
 *
 * They are pinned rather than recomputed on purpose. A test that derives its expectation from
 * the same HijrahDate call it is testing would pass whatever the calendar said.
 */
class HijriCalendarTest {

    private val ramadan1447 = LocalDate.of(2026, 2, 18)
    private val ramadan1448 = LocalDate.of(2027, 2, 8)

    @Test
    fun `1 Ramadan 1447 is 18 February 2026 and the month has 30 days`() {
        val info = HijriCalendar.ramadan(LocalDate.of(2026, 3, 1), 0)!!
        assertEquals(1447, info.hijriYear)
        assertEquals(ramadan1447, info.firstDay)
        assertEquals(30, info.length)
        assertEquals(LocalDate.of(2026, 3, 19), info.lastDay)
        assertEquals(1, HijriCalendar.of(ramadan1447, 0)!!.day)
        assertEquals(HijriCalendar.RAMADAN, HijriCalendar.of(ramadan1447, 0)!!.month)
    }

    @Test
    fun `1 Ramadan 1448 is 8 February 2027 and the month has 29 days`() {
        val info = HijriCalendar.ramadan(LocalDate.of(2027, 2, 22), 0)!!
        assertEquals(1448, info.hijriYear)
        assertEquals(ramadan1448, info.firstDay)
        assertEquals(29, info.length)
        assertEquals(LocalDate.of(2027, 3, 8), info.lastDay)
        // The same day, asked of the calendar directly.
        val day = HijriCalendar.of(ramadan1448, 0)!!
        assertEquals(1, day.day)
        assertEquals(9, day.month)
        assertEquals(1448, day.year)
        assertEquals(29, day.monthLength)
    }

    @Test
    fun `the eve of Ramadan 1448 is 30 Shaban`() {
        val eve = LocalDate.of(2027, 2, 7)
        val day = HijriCalendar.of(eve, 0)!!
        assertEquals(30, day.day)
        assertEquals(HijriCalendar.SHABAN, day.month)
        assertFalse(HijriCalendar.isRamadan(eve, 0))
        val info = HijriCalendar.ramadan(eve, 0)!!
        assertNull(info.dayNumber)
        assertFalse(info.isRamadan)
        assertEquals(1L, info.daysUntilStart(eve))
        assertEquals(ramadan1448, info.firstDay)
    }

    @Test
    fun `a shift of plus one makes 7 February the first day`() {
        val seventh = LocalDate.of(2027, 2, 7)
        assertTrue(HijriCalendar.isRamadan(seventh, 1))
        val info = HijriCalendar.ramadan(seventh, 1)!!
        assertEquals(seventh, info.firstDay)
        assertEquals(1, info.dayNumber)
        assertEquals(LocalDate.of(2027, 3, 7), info.lastDay)
        // The day before is still Shaban for this user.
        assertFalse(HijriCalendar.isRamadan(seventh.minusDays(1), 1))
    }

    @Test
    fun `a shift of minus one makes 9 February the first day`() {
        val eighth = LocalDate.of(2027, 2, 8)
        val ninth = LocalDate.of(2027, 2, 9)
        // Umm al-Qura's own first day is still Shaban for this user.
        assertFalse(HijriCalendar.isRamadan(eighth, -1))
        assertEquals(30, HijriCalendar.of(eighth, -1)!!.day)
        val info = HijriCalendar.ramadan(eighth, -1)!!
        assertEquals(ninth, info.firstDay)
        assertEquals(1L, info.daysUntilStart(eighth))
        assertTrue(HijriCalendar.isRamadan(ninth, -1))
        assertEquals(1, HijriCalendar.ramadan(ninth, -1)!!.dayNumber)
    }

    @Test
    fun `every offset from minus 2 to plus 2 keeps firstDay plus offset equal to the unshifted first day`() {
        for (offset in -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS..CalculationPrefs.MAX_HIJRI_OFFSET_DAYS) {
            val info = HijriCalendar.ramadan(LocalDate.of(2027, 2, 1), offset)!!
            assertEquals(
                "offset $offset",
                ramadan1448,
                info.firstDay.plusDays(offset.toLong()),
            )
            // And the user's header agrees: day 1 of month 9 lands exactly on firstDay.
            val header = HijriCalendar.of(info.firstDay, offset)!!
            assertEquals("offset $offset", 1, header.day)
            assertEquals("offset $offset", HijriCalendar.RAMADAN, header.month)
        }
    }

    @Test
    fun `inside Ramadan the day number counts from 1 to the length`() {
        for ((first, length, offset) in listOf(
            Triple(ramadan1447, 30, 0),
            Triple(ramadan1448, 29, 0),
            Triple(ramadan1448.minusDays(1), 29, 1),
            Triple(ramadan1448.plusDays(2), 29, -2),
        )) {
            for (i in 0 until length) {
                val date = first.plusDays(i.toLong())
                val info = HijriCalendar.ramadan(date, offset)!!
                assertEquals("$date offset $offset", i + 1, info.dayNumber)
                assertEquals("$date offset $offset", length, info.length)
                assertTrue("$date offset $offset", info.isRamadan)
                assertEquals("$date offset $offset", i + 1 == length, info.isLastDay)
                assertTrue("$date offset $offset", info.contains(date))
                // The card must never disagree with the header above it.
                assertEquals("$date offset $offset", i + 1, HijriCalendar.of(date, offset)!!.day)
            }
            // The day before and the day after fall outside, with no day number.
            assertNull(HijriCalendar.ramadan(first.minusDays(1), offset)!!.dayNumber)
            val after = HijriCalendar.ramadan(first.plusDays(length.toLong()), offset)!!
            assertNull(after.dayNumber)
        }
    }

    @Test
    fun `after Ramadan the next Ramadan is next year`() {
        val shawwal = LocalDate.of(2027, 3, 9)
        assertEquals(10, HijriCalendar.of(shawwal, 0)!!.month)
        assertEquals(2, HijriCalendar.of(LocalDate.of(2027, 3, 10), 0)!!.day)
        val info = HijriCalendar.ramadan(shawwal, 0)!!
        assertEquals(1449, info.hijriYear)
        assertEquals(LocalDate.of(2028, 1, 28), info.firstDay)
        assertEquals(29, info.length)
        assertNull(info.dayNumber)
        // The window of "days until" is large and positive, which is what keeps the card away.
        assertTrue(info.daysUntilStart(shawwal) > 300)
    }

    @Test
    fun `the notice window runs from 3 days before to day 2 and nothing else`() {
        val first = ramadan1448
        fun shows(today: LocalDate) = HijriCalendar.ramadan(today, 0)!!.showsNotice(today)
        assertFalse("7 days before, the old window", shows(first.minusDays(7)))
        assertFalse("4 days before", shows(first.minusDays(4)))
        assertTrue("3 days before", shows(first.minusDays(3)))
        assertTrue("the eve", shows(first.minusDays(1)))
        assertTrue("day 1", shows(first))
        assertTrue("day 2", shows(first.plusDays(1)))
        assertFalse("day 3", shows(first.plusDays(2)))
        assertFalse("mid month", shows(first.plusDays(14)))
        assertFalse("the last day", shows(LocalDate.of(2027, 3, 8)))
        // Nothing lingers after Ramadan, and nothing shows months ahead of the next one.
        assertFalse("1 Shawwal", shows(LocalDate.of(2027, 3, 9)))
        assertFalse("July", shows(LocalDate.of(2027, 7, 1)))
    }

    @Test
    fun `the notice window follows the users shift`() {
        // Offset plus one starts Ramadan on 7 February for this user, so their notice starts a day
        // earlier too. The app must not keep a second, unshifted idea of the start.
        val first = LocalDate.of(2027, 2, 7)
        fun shows(today: LocalDate) = HijriCalendar.ramadan(today, 1)!!.showsNotice(today)
        assertFalse(shows(first.minusDays(4)))
        assertTrue(shows(first.minusDays(3)))
        assertTrue(shows(first.plusDays(1)))
        assertFalse(shows(first.plusDays(2)))
    }

    @Test
    fun `the Settings rows show from 3 days before until the last day, and not the rest of the year`() {
        val first = ramadan1448
        fun shows(today: LocalDate) = HijriCalendar.ramadan(today, 0)!!.showsSettings(today)
        assertFalse("4 days before", shows(first.minusDays(4)))
        assertTrue("3 days before, the day the notice starts", shows(first.minusDays(3)))
        assertTrue("day 1", shows(first))
        assertTrue("day 15", shows(first.plusDays(14)))
        assertTrue("the last day", shows(LocalDate.of(2027, 3, 8)))
        assertFalse("1 Shawwal", shows(LocalDate.of(2027, 3, 9)))
        assertFalse("July", shows(LocalDate.of(2027, 7, 1)))
        assertFalse("October", shows(LocalDate.of(2026, 10, 19)))
        // With a shift the rows move with the user's own first day, as the notice does.
        val shifted = LocalDate.of(2027, 2, 7)
        fun showsShifted(today: LocalDate) = HijriCalendar.ramadan(today, 1)!!.showsSettings(today)
        assertFalse(showsShifted(shifted.minusDays(4)))
        assertTrue(showsShifted(shifted.minusDays(3)))
    }

    @Test
    fun `the timetable is offered from 30 days before until the last day`() {
        fun offered(today: LocalDate) = HijriCalendar.ramadan(today, 0)!!.offersTimetable(today)
        val first = ramadan1448
        assertFalse("31 days before", offered(first.minusDays(31)))
        assertTrue("30 days before", offered(first.minusDays(30)))
        assertTrue("the eve", offered(first.minusDays(1)))
        assertTrue("day 1", offered(first))
        assertTrue("the last day", offered(LocalDate.of(2027, 3, 8)))
        assertFalse("1 Shawwal", offered(LocalDate.of(2027, 3, 9)))
        assertFalse("October", offered(LocalDate.of(2026, 10, 16)))
    }

    @Test
    fun `a date outside the calendar's range gives null, not a throw`() {
        val far = LocalDate.of(2200, 1, 1)
        assertNull(HijriCalendar.of(far, 0))
        assertNull(HijriCalendar.ramadan(far, 0))
        assertFalse(HijriCalendar.isRamadan(far, 0))
        assertNull(HijriCalendar.of(LocalDate.of(1800, 1, 1), 0))
        // The edge of the supported range still works, so the guard is not swallowing real dates.
        assertNotNull(HijriCalendar.of(LocalDate.of(2026, 10, 16), 0))
    }

    @Test
    fun `the engine's Umm al-Qura rule still moves with the shift`() {
        // Makkah, Umm al-Qura. Isha minus Maghrib is 120 minutes inside Ramadan and 90 outside,
        // and which side a date falls on must follow the user's shift. Spec 9.2, offset table.
        val makkah = Coordinates(21.4225, 39.8262)
        fun gap(date: LocalDate, offset: Int): Long {
            val prefs = CalculationPrefs(method = CalcMethod.UMM_AL_QURA, hijriOffsetDays = offset)
            val times = PrayerEngine.compute(makkah, date, prefs)
            return java.time.Duration
                .between(times[PrayerSlot.MAGHRIB], times[PrayerSlot.ISHA])
                .toMinutes()
        }
        // 2027-02-07 is 30 Shaban for offset 0 and 1 Ramadan for offset +1.
        assertEquals(90L, gap(LocalDate.of(2027, 2, 7), 0))
        assertEquals(120L, gap(LocalDate.of(2027, 2, 7), 1))
        assertEquals(120L, gap(LocalDate.of(2027, 2, 8), 0))
        assertEquals(90L, gap(LocalDate.of(2027, 2, 8), -1))
        // 2027-03-08 is the last day for offset 0, and already Shawwal for offset +1.
        assertEquals(120L, gap(LocalDate.of(2027, 3, 8), 0))
        assertEquals(90L, gap(LocalDate.of(2027, 3, 8), 1))
    }
}
