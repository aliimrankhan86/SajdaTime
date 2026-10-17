package com.sajdatime.app

import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.pdf.PrayerPdfExporter.Range
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.LocalDate

/**
 * The days a PDF covers and what the file is called, without a Canvas. The Ramadan dates are the
 * pinned ones from HijriCalendarTest: 1 Ramadan 1448 is 8 February 2027 with 29 days.
 */
class RamadanPdfRangeTest {

    private val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)

    @Test
    fun `Ramadan is the whole month, from day 1, inside it`() {
        val span = PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2027, 2, 22), prefs)
        assertEquals(LocalDate.of(2027, 2, 8), span.start)
        assertEquals(29, span.days)
        assertEquals(1448, span.ramadan!!.hijriYear)
    }

    @Test
    fun `before the month the timetable is the coming Ramadan, so a family can print it ahead`() {
        val span = PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2027, 1, 12), prefs)
        assertEquals(LocalDate.of(2027, 2, 8), span.start)
        assertEquals(29, span.days)
    }

    @Test
    fun `the users date shift moves the month`() {
        val plusOne = PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2027, 2, 22), prefs.copy(hijriOffsetDays = 1))
        assertEquals(LocalDate.of(2027, 2, 7), plusOne.start)
        assertEquals(29, plusOne.days)
        val minusOne = PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2027, 2, 22), prefs.copy(hijriOffsetDays = -1))
        assertEquals(LocalDate.of(2027, 2, 9), minusOne.start)
    }

    @Test
    fun `a 30 day Ramadan has 30 rows`() {
        val span = PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2026, 3, 1), prefs)
        assertEquals(LocalDate.of(2026, 2, 18), span.start)
        assertEquals(30, span.days)
    }

    @Test
    fun `the other ranges are unchanged and carry no Ramadan`() {
        val today = LocalDate.of(2027, 2, 22)
        val one = PrayerPdfExporter.spanOf(Range.TODAY, today, prefs)
        assertEquals(today to 1, one.start to one.days)
        assertNull(one.ramadan)
        val week = PrayerPdfExporter.spanOf(Range.NEXT_7_DAYS, today, prefs)
        assertEquals(today to 7, week.start to week.days)
        val month = PrayerPdfExporter.spanOf(Range.THIS_MONTH, today, prefs)
        assertEquals(LocalDate.of(2027, 2, 1), month.start)
        assertEquals(28, month.days)
        assertNull(month.ramadan)
    }

    @Test
    fun `a date outside the calendar's range fails loudly for the caller to report, not silently`() {
        try {
            PrayerPdfExporter.spanOf(Range.RAMADAN, LocalDate.of(2200, 1, 1), prefs)
            fail("expected an exception")
        } catch (expected: IllegalStateException) {
            assertNotNull(expected.message)
        }
    }

    @Test
    fun `file names are ASCII and sortable`() {
        val today = LocalDate.of(2027, 2, 22)
        fun name(range: Range) = PrayerPdfExporter.fileNameFor(range, PrayerPdfExporter.spanOf(range, today, prefs))
        assertEquals("PrayerTimes_Ramadan1448.pdf", name(Range.RAMADAN))
        assertEquals("PrayerTimes_February2027.pdf", name(Range.THIS_MONTH))
        assertEquals("PrayerTimes_22Feb2027.pdf", name(Range.TODAY))
        assertEquals("PrayerTimes_22Feb2027.pdf", name(Range.NEXT_7_DAYS))
    }

    @Test
    fun `both column sets fill the width between the margins exactly`() {
        // A4 is 595 wide and the margin is 36 either side, so the rules and the table are 523.
        assertEquals(523f, PrayerPdfExporter.ORDINARY_WIDTHS.sum(), 0.001f)
        assertEquals(523f, PrayerPdfExporter.RAMADAN_WIDTHS.sum(), 0.001f)
        assertEquals(8, PrayerPdfExporter.ORDINARY_WIDTHS.size)
        assertEquals(9, PrayerPdfExporter.RAMADAN_WIDTHS.size)
        assertTrue(PrayerPdfExporter.RAMADAN_WIDTHS.drop(3).all { it >= 57f })
    }
}
