package com.sajdatime.core

import java.time.LocalDate
import kotlin.math.floor

/** One Gregorian month of the Timetable sheet: its first day and how many days it has. */
data class TimetableMonth(val first: LocalDate, val length: Int)

/** What the Timetable sheet lists (STEP-4-DESIGN §9). Plain rules, so the screen only draws them. */
object TimetableList {
    /** This month, then the next: a sheet opened on the 31st must show tomorrow. */
    fun months(today: LocalDate, count: Int = 2): List<TimetableMonth> =
        (0 until count).map {
            val first = today.withDayOfMonth(1).plusMonths(it.toLong())
            TimetableMonth(first, first.lengthOfMonth())
        }

    /** Index of today's card in a list laid out as [heading, days..., heading, days...]. */
    fun todayIndex(today: LocalDate): Int = today.dayOfMonth
}

/** How many times sit across one day card, so a time is never cut and a word never breaks. */
object TimetableGrid {
    /** 84dp is a column's share at 1.0 text, including its 8dp gap; it grows with the text size. */
    fun columns(widthDp: Float, fontScale: Float): Int =
        floor(widthDp / (84f * fontScale)).toInt().coerceIn(1, 3)
}
