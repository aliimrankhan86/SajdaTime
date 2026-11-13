package com.sajdatime.core

import java.time.LocalDate
import kotlin.math.floor

/**
 * What the Timetable sheet lists. Plain rules, so the screen only draws them.
 *
 * Today and the six days after it (Ali, 12 Nov 2026: "the timetable should show only the timing of the
 * next 7 days. The save as pdf should have for the whole month"). Until then it listed this month and the
 * next, about sixty cards, to answer a question most people ask about this week. The month lives in the
 * PDF, which is the form people print and pin up.
 */
object TimetableList {
    const val DAYS = 7

    /** The days the sheet shows, today first. They run across a month end without ceremony. */
    fun days(today: LocalDate): List<LocalDate> = (0 until DAYS).map { today.plusDays(it.toLong()) }
}

/** How many times sit across one day card, so a time is never cut and a word never breaks. */
object TimetableGrid {
    /** 84dp is a column's share at 1.0 text, including its 8dp gap; it grows with the text size. */
    fun columns(widthDp: Float, fontScale: Float): Int =
        floor(widthDp / (84f * fontScale)).toInt().coerceIn(1, 3)
}
