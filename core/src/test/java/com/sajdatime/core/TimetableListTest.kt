package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimetableListTest {

    @Test
    fun `the sheet lists today and the six days after it`() {
        val days = TimetableList.days(LocalDate.of(2026, 11, 12))
        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 11, 12), days.first())
        assertEquals(LocalDate.of(2026, 11, 18), days.last())
    }

    @Test
    fun `the week runs across a month end and a year end`() {
        assertEquals(LocalDate.of(2026, 11, 2), TimetableList.days(LocalDate.of(2026, 10, 27)).last())
        assertEquals(LocalDate.of(2027, 1, 5), TimetableList.days(LocalDate.of(2026, 12, 30)).last())
    }

    @Test
    fun `the week runs across a leap day`() {
        assertEquals(LocalDate.of(2028, 3, 3), TimetableList.days(LocalDate.of(2028, 2, 26)).last())
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
