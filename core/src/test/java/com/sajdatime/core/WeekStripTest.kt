package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The week card's seven dates and the Last 30 days grid's week rows (STEP-1-DESIGN 9.4), as pure
 * rules so the locale's first day and the edges are checked without a screen. The 36 or 32dp ring
 * choice is in the app module (WeekRingSizeTest).
 */
class WeekStripTest {

    // 2026-10-21 is a Wednesday.
    private val wed = LocalDate.of(2026, 10, 21)

    @Test
    fun `Monday first week holds today and starts on a Monday`() {
        val week = TrackerLogic.weekOf(wed, DayOfWeek.MONDAY)
        assertEquals(LocalDate.of(2026, 10, 19), week.first())
        assertEquals(LocalDate.of(2026, 10, 25), week.last())
        assertEquals(7, week.size)
    }

    @Test
    fun `Sunday first and Saturday first`() {
        assertEquals(LocalDate.of(2026, 10, 18), TrackerLogic.weekOf(wed, DayOfWeek.SUNDAY).first())
        assertEquals(LocalDate.of(2026, 10, 17), TrackerLogic.weekOf(wed, DayOfWeek.SATURDAY).first())
    }

    @Test
    fun `today on the first day and on the last day of the week`() {
        val monday = LocalDate.of(2026, 10, 19)
        val sunday = LocalDate.of(2026, 10, 25)
        assertEquals(monday, TrackerLogic.weekOf(monday, DayOfWeek.MONDAY).first())
        assertEquals(monday, TrackerLogic.weekOf(sunday, DayOfWeek.MONDAY).first())
        // Sunday is the first day of a Sunday-first week, and Saturday its last.
        assertEquals(sunday, TrackerLogic.weekOf(sunday, DayOfWeek.SUNDAY).first())
        assertEquals(LocalDate.of(2026, 10, 24), TrackerLogic.weekOf(LocalDate.of(2026, 10, 24), DayOfWeek.SUNDAY).last())
        // Saturday-first: Friday is the last day, Saturday the first.
        val friday = LocalDate.of(2026, 10, 23)
        assertEquals(LocalDate.of(2026, 10, 17), TrackerLogic.weekOf(friday, DayOfWeek.SATURDAY).first())
        assertEquals(friday, TrackerLogic.weekOf(friday, DayOfWeek.SATURDAY).last())
    }

    @Test
    fun `a week across a year boundary is still seven consecutive dates`() {
        val week = TrackerLogic.weekOf(LocalDate.of(2027, 1, 1), DayOfWeek.MONDAY)
        assertEquals(LocalDate.of(2026, 12, 28), week.first())
        assertEquals(LocalDate.of(2027, 1, 3), week.last())
    }

    @Test
    fun `grid drops weeks before the start and keeps the week holding it`() {
        // Started Sunday 11 Oct; Monday-first, so its week begins Monday 5 Oct.
        val weeks = TrackerLogic.gridWeeks(wed, LocalDate.of(2026, 10, 11), DayOfWeek.MONDAY)
        assertEquals(listOf(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 19)), weeks)
    }

    @Test
    fun `grid started today is one row, holding today`() {
        assertEquals(listOf(LocalDate.of(2026, 10, 19)), TrackerLogic.gridWeeks(wed, wed, DayOfWeek.MONDAY))
    }

    @Test
    fun `grid with a start long ago shows the 30 day window, the last row holding today`() {
        val weeks = TrackerLogic.gridWeeks(wed, LocalDate.of(2020, 1, 1), DayOfWeek.MONDAY)
        // The window starts Tuesday 22 Sep (30 days ending 21 Oct), in the week of Monday 21 Sep.
        assertEquals(LocalDate.of(2026, 9, 21), weeks.first())
        assertEquals(LocalDate.of(2026, 10, 19), weeks.last())
        assertEquals(5, weeks.size)
        assertEquals(true, weeks.zipWithNext().all { (a, b) -> b == a.plusDays(7) })
    }

    @Test
    fun `grid follows the first day of the locale`() {
        val weeks = TrackerLogic.gridWeeks(wed, LocalDate.of(2020, 1, 1), DayOfWeek.SATURDAY)
        assertEquals(DayOfWeek.SATURDAY, weeks.first().dayOfWeek)
        assertEquals(LocalDate.of(2026, 10, 17), weeks.last())
    }
}
