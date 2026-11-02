package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The Sunnah fast rules (FastingDays.kt). Dates are Umm al-Qura 1448, pinned by IslamicDatesGoldenTest:
 * 1 Ramadan = Mon 8 Feb 2027, 1 Shawwal = Tue 9 Mar 2027, 9 Dhul Hijjah = Sat 15 May 2027, 10 Muharram
 * 1449 = Tue 15 Jun 2027.
 */
class FastingDaysTest {

    private val all = FastKind.entries.toSet()
    private fun d(s: String) = LocalDate.parse(s)
    private fun on(s: String, kinds: Set<FastKind> = all, shift: Int = 0) = FastingDays.fastOn(d(s), shift, kinds)

    @Test fun `nothing chosen, nothing offered`() = assertNull(on("2026-10-26", emptySet()))

    @Test fun `Monday and Thursday`() {
        assertEquals(FastReason.MONDAY, on("2026-10-26", setOf(FastKind.MONDAY_THURSDAY)))
        assertEquals(FastReason.THURSDAY, on("2026-10-29", setOf(FastKind.MONDAY_THURSDAY)))
        assertNull(on("2026-10-27", setOf(FastKind.MONDAY_THURSDAY)))
    }

    @Test fun `never a day of Ramadan, the whole month, every shift`() {
        var day = d("2027-02-04")
        repeat(40) {
            for (shift in -2..2) {
                val h = HijriCalendar.of(day, shift)!!
                if (h.month == HijriCalendar.RAMADAN) assertNull("$day shift $shift", FastingDays.fastOn(day, shift, all))
            }
            day = day.plusDays(1)
        }
    }

    @Test fun `never Eid, Tashreeq or the buffer days, on any shift`() {
        var day = d("2026-06-01")
        repeat(800) {
            for (shift in -2..2) {
                val h = HijriCalendar.of(day, shift)!!
                val forbidden = (h.month == 10 && h.day <= 2) || (h.month == 12 && h.day in 10..14) ||
                    (h.month == 8 && h.day >= h.monthLength - 1) || h.month == 9
                if (forbidden) assertNull("$day = ${h.day}/${h.month} shift $shift", FastingDays.fastOn(day, shift, all))
            }
            day = day.plusDays(1)
        }
    }

    @Test fun `13 Dhul Hijjah is a White Day by number and is still never offered`() {
        // 13 Dhul Hijjah 1448 = 19 May 2027.
        assertEquals(13, HijriCalendar.of(d("2027-05-19"), 0)!!.day)
        assertNull(on("2027-05-19", setOf(FastKind.WHITE_DAYS)))
    }

    @Test fun `14 Dhul Hijjah, a Tashreeq day for a community a day behind, is skipped, and 15th is a White Day again`() {
        assertEquals(14, HijriCalendar.of(d("2027-05-20"), 0)!!.day)
        assertNull(on("2027-05-20"))
        assertEquals(FastReason.WHITE_DAY, on("2027-05-21", setOf(FastKind.WHITE_DAYS)))
    }

    @Test fun `Arafah is offered, and only with special days chosen`() {
        assertEquals(FastReason.ARAFAH, on("2027-05-15"))
        // 15 May 2027 is a Saturday, so nothing else claims it.
        assertNull(on("2027-05-15", setOf(FastKind.MONDAY_THURSDAY, FastKind.WHITE_DAYS)))
    }

    @Test fun `the six days of Shawwal are announced for 3 Shawwal, not 2`() {
        assertNull(on("2027-03-10"))
        assertEquals(FastReason.SHAWWAL_SIX, on("2027-03-11"))
    }

    @Test fun `Ashura and the day before it`() {
        assertEquals(FastReason.ASHURA, on("2027-06-15"))
        assertEquals(FastReason.MUHARRAM_9, on("2027-06-14"))
    }

    @Test fun `the last two days of Sha'ban are skipped, the day of doubt among them`() {
        assertNull(on("2027-02-07"))
        assertNull(on("2027-02-06"))
    }

    @Test fun `White Days on a Friday say White Day, and no Friday is offered for being Friday`() {
        var day = d("2026-06-01")
        var fridayWhite = 0
        repeat(800) {
            val r = FastingDays.fastOn(day, 0, all)
            if (day.dayOfWeek == DayOfWeek.FRIDAY && r != null) {
                assertTrue("$day is $r", r == FastReason.WHITE_DAY || r in setOf(FastReason.ARAFAH, FastReason.ASHURA, FastReason.MUHARRAM_9, FastReason.SHAWWAL_SIX))
                if (r == FastReason.WHITE_DAY) fridayWhite++
            }
            day = day.plusDays(1)
        }
        assertTrue("some White Day fell on a Friday in two years", fridayWhite > 0)
    }

    @Test fun `the date shift moves the White Days with the header`() {
        // With a shift of +1 the header reads one Hijri day later, so the 13th comes a day earlier.
        var day = d("2026-11-01")
        repeat(60) {
            val h = HijriCalendar.of(day, 1)!!
            val r = FastingDays.fastOn(day, 1, setOf(FastKind.WHITE_DAYS))
            assertEquals("$day", h.day in 13..15 && h.month !in setOf(8, 9, 12), r == FastReason.WHITE_DAY)
            day = day.plusDays(1)
        }
    }

    @Test fun `out of the calendar's range, nothing`() = assertNull(on("2200-01-05"))
}
