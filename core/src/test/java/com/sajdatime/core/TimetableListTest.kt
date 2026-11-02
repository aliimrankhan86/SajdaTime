package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimetableListTest {

    @Test
    fun `on 31 October the list is October then November`() {
        val months = TimetableList.months(LocalDate.of(2026, 10, 31))
        assertEquals(listOf(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1)), months.map { it.first })
        assertEquals(listOf(31, 30), months.map { it.length })
    }

    @Test
    fun `December runs into January of the next year`() {
        val months = TimetableList.months(LocalDate.of(2026, 12, 15))
        assertEquals(listOf(LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 1)), months.map { it.first })
    }

    @Test
    fun `a leap February has 29 days`() {
        assertEquals(29, TimetableList.months(LocalDate.of(2028, 2, 10)).first().length)
    }

    @Test
    fun `today is the day of the month, so the 1st is 1 and 31 October is 31`() {
        assertEquals(1, TimetableList.todayIndex(LocalDate.of(2026, 10, 1)))
        assertEquals(31, TimetableList.todayIndex(LocalDate.of(2026, 10, 31)))
    }

    @Test
    fun `a count of one gives this month only`() {
        assertEquals(1, TimetableList.months(LocalDate.of(2026, 10, 24), count = 1).size)
    }

    @Test
    fun `columns follow width and text size`() {
        assertEquals(3, TimetableGrid.columns(296f, 1.0f))
        assertEquals(3, TimetableGrid.columns(256f, 1.0f))
        assertEquals(2, TimetableGrid.columns(296f, 1.3f))
        assertEquals(2, TimetableGrid.columns(296f, 1.5f))
        assertEquals(2, TimetableGrid.columns(256f, 1.5f))
        assertEquals(1, TimetableGrid.columns(296f, 2.0f))
        assertEquals(1, TimetableGrid.columns(256f, 2.0f))
    }

    @Test
    fun `columns are never 0 or 4`() {
        for (width in listOf(0f, 100f, 256f, 600f, 2000f)) {
            for (scale in listOf(0.85f, 1.0f, 1.3f, 2.0f)) {
                assertTrue(TimetableGrid.columns(width, scale) in 1..3)
            }
        }
    }
}
