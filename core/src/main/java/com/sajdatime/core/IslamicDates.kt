package com.sajdatime.core

import java.time.LocalDate
import java.time.chrono.HijrahDate

/**
 * The year's key dates for the Islamic dates sheet (1.6.0).
 *
 * Six dated days, all of them days both Sunni and Shia Muslims mark, named plainly and with no ruling
 * in any name (sign-off, 23 Oct 2026). Ranges such as Laylat al-Qadr and the first ten days of Dhul
 * Hijjah are left out because they are not dates, and contested observances (Mawlid, Isra and Mi'raj,
 * 15 Sha'ban) because listing one is taking a side (decision D4, default no). 1 Muharram is shown as
 * the Hijri date itself rather than "Islamic New Year", which reads as a celebration to Shia users at
 * the start of Muharram.
 */
enum class IslamicDay(val month: Int, val day: Int) {
    MUHARRAM_1(1, 1),
    ASHURA(1, 10),
    RAMADAN_1(HijriCalendar.RAMADAN, 1),
    EID_AL_FITR(10, 1),
    ARAFAH(12, 9),
    EID_AL_ADHA(12, 10),
}

/** One coming date: which day, its Hijri year, and the Gregorian date the user's header reads it on. */
data class UpcomingDate(val day: IslamicDay, val hijriYear: Int, val date: LocalDate)

object IslamicDates {

    /**
     * Each of the six days once, the next time it falls on or after [today], soonest first. Built on
     * the same calendar and the same shift as the Home header ([HijriCalendar]), so the sheet can never
     * name a different day from the header. Empty only outside the calendar's range (1300 to 1600 AH).
     */
    fun upcoming(today: LocalDate, offsetDays: Int): List<UpcomingDate> {
        val year = HijriCalendar.of(today, offsetDays)?.year ?: return emptyList()
        return IslamicDay.entries.mapNotNull { d ->
            listOf(year, year + 1).firstNotNullOfOrNull { y ->
                gregorian(y, d, offsetDays)?.takeIf { !it.isBefore(today) }?.let { UpcomingDate(d, y, it) }
            }
        }.sortedBy { it.date }
    }

    /**
     * The Gregorian date on which a user shifted by [offsetDays] reads [day] of [hijriYear]. The header
     * shows `HijrahDate.from(g + offset)`, so this is the inverse: minus the offset, as in
     * [HijriCalendar.ramadan].
     */
    fun gregorian(hijriYear: Int, day: IslamicDay, offsetDays: Int): LocalDate? = runCatching {
        LocalDate.from(HijrahDate.of(hijriYear, day.month, day.day)).minusDays(offsetDays.toLong())
    }.getOrNull()
}
