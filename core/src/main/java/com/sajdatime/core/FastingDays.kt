package com.sajdatime.core

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Sunnah fast reminders (1.6.1, signed off with conditions on 23 Oct 2026; PLAN.md section 0 and
 * SIGNOFF.md). Offered to Sunni users only: Monday and Thursday fasts and the White Days are Sunni
 * practice, and for Shia users fasting on Ashura is discouraged, so a Shia user must never get one.
 *
 * Every exclusion lives here, in one function, so it is all tested in FastingDaysTest.
 */
enum class FastKind { MONDAY_THURSDAY, WHITE_DAYS, SPECIAL }

/** Why a day is offered as a fast. The notification words come from this. */
enum class FastReason { MONDAY, THURSDAY, WHITE_DAY, MUHARRAM_9, ASHURA, ARAFAH, SHAWWAL_SIX }

object FastingDays {

    /**
     * Why [date] is a Sunnah fast for someone who chose [kinds], on the calendar shifted by
     * [offsetDays], or null. Never on a day in Ramadan, never on Eid, never on the days of Tashreeq.
     *
     * Moon-sighting buffer: Umm al-Qura can be a day ahead of a community that sights locally, so the
     * days next to the forbidden ones are skipped too. No reminder on 1 or 2 Shawwal (2 Shawwal on this
     * calendar can be Eid for them), so the six days of Shawwal are announced for 3 Shawwal. No reminder
     * on 9 to 14 Dhul Hijjah except Arafah itself, whose notice says "on this calendar, follow your
     * mosque" (14 Dhul Hijjah here is a day of Tashreeq for a community a day behind). No reminder on
     * the last two days of Sha'ban, which can be 1 Ramadan for them, or the day of doubt.
     *
     * Fridays are never offered for being Friday. A White Day that falls on a Friday is still offered,
     * as a White Day.
     */
    fun fastOn(date: LocalDate, offsetDays: Int, kinds: Set<FastKind>): FastReason? {
        if (kinds.isEmpty()) return null
        val h = HijriCalendar.of(date, offsetDays) ?: return null
        when {
            h.month == HijriCalendar.RAMADAN -> return null
            h.month == SHAWWAL && h.day <= 2 -> return null
            h.month == DHUL_HIJJAH && h.day in 10..14 -> return null
            h.month == HijriCalendar.SHABAN && h.day >= h.monthLength - 1 -> return null
        }
        if (FastKind.SPECIAL in kinds) {
            when {
                h.month == MUHARRAM && h.day == 9 -> return FastReason.MUHARRAM_9
                h.month == MUHARRAM && h.day == 10 -> return FastReason.ASHURA
                h.month == DHUL_HIJJAH && h.day == 9 -> return FastReason.ARAFAH
                h.month == SHAWWAL && h.day == 3 -> return FastReason.SHAWWAL_SIX
            }
        }
        if (FastKind.WHITE_DAYS in kinds && h.day in 13..15) return FastReason.WHITE_DAY
        if (FastKind.MONDAY_THURSDAY in kinds) {
            when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> return FastReason.MONDAY
                DayOfWeek.THURSDAY -> return FastReason.THURSDAY
                else -> Unit
            }
        }
        return null
    }

    private const val MUHARRAM = 1
    private const val SHAWWAL = 10
    private const val DHUL_HIJJAH = 12
}
