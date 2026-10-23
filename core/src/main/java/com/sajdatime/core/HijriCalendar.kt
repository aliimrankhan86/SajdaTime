package com.sajdatime.core

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/** One day on the Umm al-Qura calendar as the user sees it (after their shift). */
data class HijriDay(val day: Int, val month: Int, val year: Int, val monthLength: Int)

/**
 * The Ramadan that is current, or the next one, for a user whose calendar is shifted by
 * [CalculationPrefs.hijriOffsetDays]. Dates are Gregorian and already shifted, so
 * [firstDay] is the calendar date on which this user's header reads 1 Ramadan.
 */
data class RamadanInfo(
    val hijriYear: Int,
    val firstDay: LocalDate,
    val lastDay: LocalDate,
    val length: Int,
    /** 1 to [length] while today is inside Ramadan, otherwise null. */
    val dayNumber: Int?,
) {
    val isRamadan: Boolean get() = dayNumber != null

    val isLastDay: Boolean get() = dayNumber == length

    /** Positive before Ramadan, 0 on the first day, negative inside. */
    fun daysUntilStart(today: LocalDate): Long = ChronoUnit.DAYS.between(today, firstDay)

    fun contains(date: LocalDate): Boolean = !date.isBefore(firstDay) && !date.isAfter(lastDay)

    /**
     * Whether the quiet "Ramadan begins on ..." notice belongs on the Times screen today: from
     * [NOTICE_LEAD_DAYS] days before day 1 until day [NOTICE_LAST_DAY_NUMBER] of the month.
     *
     * Three days, the owner's choice on 19 Oct 2026 ("start 1 or 2 days before ... or it could
     * confuse the users"). It was a week until then. Three, not his one or two, on Claude's advice
     * and his yes: the notice still has to leave two evenings to set the suhoor reminder before the
     * first suhoor and to check the date with the mosque, which often starts a day later than this
     * calendar. One day before is too late for that on a phone opened once that week. The Settings
     * rows for Ramadan appear on the same day ([showsSettings]); the timetable stays at 30 days.
     */
    fun showsNotice(today: LocalDate): Boolean =
        daysUntilStart(today) in -(NOTICE_LAST_DAY_NUMBER - 1L)..NOTICE_LEAD_DAYS

    /**
     * Whether Settings shows the Ramadan rows ("Ramadan card", "Suhoor reminder"): from the day the
     * notice starts until the last day of Ramadan. Hidden the rest of the year so a Ramadan setting
     * in, say, August does not confuse anyone (owner, 19 Oct 2026). A choice made is kept while the
     * rows are hidden and is there again next year; the suhoor reminder only ever fires in Ramadan.
     */
    fun showsSettings(today: LocalDate): Boolean =
        contains(today) || daysUntilStart(today) in 1L..NOTICE_LEAD_DAYS

    /**
     * Whether the Ramadan timetable is offered in the export sheet: from [EXPORT_LEAD_DAYS] days
     * before day 1 until the last day. Earlier than the notice on purpose. Printing a timetable is
     * something people go looking for (a large London mosque publishes its own two weeks ahead),
     * so it sits behind a tap and pushes nothing at anyone.
     */
    fun offersTimetable(today: LocalDate): Boolean =
        isRamadan || daysUntilStart(today) in 1L..EXPORT_LEAD_DAYS

    companion object {
        const val NOTICE_LEAD_DAYS = 3L
        const val NOTICE_LAST_DAY_NUMBER = 2
        const val EXPORT_LEAD_DAYS = 30L
    }
}

/** The two Eids, by their place on the Hijri calendar: 1 Shawwal and 10 Dhul Hijjah. */
enum class Eid(val month: Int, val day: Int) {
    FITR(month = 10, day = 1),
    ADHA(month = 12, day = 10),
}

/**
 * An Eid that falls on today's date on the user's calendar.
 *
 * Found by calendar, not listed by year, so 2027 and 2028 need no entries and neither does any
 * later year (until 1600 AH, where the Umm al-Qura table ends and [HijriCalendar.eidOn] answers
 * null). The user's date shift applies, so the greeting arrives on the day their header reads
 * 1 Shawwal or 10 Dhul Hijjah, whichever day that is.
 */
data class EidDay(val eid: Eid, val hijriYear: Int) {
    /**
     * What the app stores to show each Eid once, for example 144810 for Eid al-Fitr 1448. Year
     * first, so a later Eid always has a larger code than an earlier one, and the month in the last
     * two digits so the two Eids of one year differ.
     */
    val code: Int get() = hijriYear * 100 + eid.month

    /** False once the user has closed this very Eid. [lastDismissedCode] is 0 when they never have. */
    fun isUnseen(lastDismissedCode: Int): Boolean = code != lastDismissedCode
}

/**
 * The one place that turns a Gregorian date into "what Hijri day is it for this user".
 *
 * Two private copies of this used to exist, one in the engine's Umm al-Qura Isha rule and one
 * in the Home header. They agreed only because nobody had changed either. The Ramadan card,
 * the widgets, the suhoor reminder planner and the timetable PDF all need the same answer, and
 * five readers of one fact must not have five implementations of it, or the card will one day
 * say Day 3 under a header that says 2 Ramadan.
 *
 * No Android imports, so it ports to the KMP target as is (docs/ARCHITECTURE.md).
 */
object HijriCalendar {
    const val RAMADAN = 9
    const val SHABAN = 8

    /**
     * The offset is applied as `date.plusDays(offset)` exactly as the two private copies did, so a
     * stored shift keeps its meaning. [HijrahDate] is the Umm al-Qura calendar and covers 1300 to
     * 1600 AH. Outside that `HijrahDate.from` throws, hence null rather than a crash in a widget
     * or a broadcast receiver, which is where an uncaught exception would be least visible.
     */
    fun of(date: LocalDate, offsetDays: Int): HijriDay? = runCatching {
        val h = HijrahDate.from(date.plusDays(offsetDays.toLong()))
        HijriDay(
            day = h.get(ChronoField.DAY_OF_MONTH),
            month = h.get(ChronoField.MONTH_OF_YEAR),
            year = h.get(ChronoField.YEAR),
            monthLength = h.lengthOfMonth(),
        )
    }.getOrNull()

    fun isRamadan(date: LocalDate, offsetDays: Int): Boolean =
        of(date, offsetDays)?.month == RAMADAN

    /**
     * Which Eid, if any, falls on [date] for a user whose calendar is shifted by [offsetDays].
     * Built on [of], so it can never disagree with the date in the Home header.
     */
    fun eidOn(date: LocalDate, offsetDays: Int): EidDay? {
        val h = of(date, offsetDays) ?: return null
        val eid = Eid.entries.firstOrNull { it.month == h.month && it.day == h.day } ?: return null
        return EidDay(eid, h.year)
    }

    /**
     * The current Ramadan if [today] is inside it, otherwise the next one. Null only outside the
     * calendar's range of 1300 to 1600 AH.
     */
    fun ramadan(today: LocalDate, offsetDays: Int): RamadanInfo? {
        val h = of(today, offsetDays) ?: return null
        val year = if (h.month <= RAMADAN) h.year else h.year + 1
        return runCatching {
            val first = HijrahDate.of(year, RAMADAN, 1)
            val length = first.lengthOfMonth()
            // The user's header shows HijrahDate.from(g + offset), so the Gregorian day on which
            // it shows 1 Ramadan is the inverse of that, which is minus the offset.
            val firstDay = LocalDate.from(first).minusDays(offsetDays.toLong())
            RamadanInfo(
                hijriYear = year,
                firstDay = firstDay,
                lastDay = firstDay.plusDays(length - 1L),
                length = length,
                dayNumber = if (h.month == RAMADAN) h.day else null,
            )
        }.getOrNull()
    }
}
