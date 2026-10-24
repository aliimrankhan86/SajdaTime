package com.sajdatime.core

import java.time.LocalDate
import java.time.chrono.HijrahDate

/**
 * The year's key dates for the Islamic dates sheet (1.6.0).
 *
 * Six dated days shared by everyone, named plainly and with no ruling in any name (sign-off, 23 Oct
 * 2026). Mawlid, Isra and Mi'raj and 15 Sha'ban are not in the shared list, and 12 Rabi al-Awwal is
 * out for everyone (Ali, Q20). Ranges such as Laylat al-Qadr and the first ten days of Dhul Hijjah are
 * left out because they are not dates. 1 Muharram is shown as the Hijri date itself rather than
 * "Islamic New Year", which reads as a celebration to Shia users at the start of Muharram.
 *
 * Five more days are marked [shia] and shown only when the stored sect is Shia (Ali, Q21). They merge
 * into the same list in date order with no heading, so the screen never prints the user's sect. The
 * three Ramadan nights are left out of 1.6 (Ali, 23 Oct 2026) because a Hijri night starts at the
 * previous sunset, so the date alone would name the wrong evening.
 */
enum class IslamicDay(val month: Int, val day: Int, val shia: Boolean = false) {
    MUHARRAM_1(1, 1),
    ASHURA(1, 10),
    RAMADAN_1(HijriCalendar.RAMADAN, 1),
    EID_AL_FITR(10, 1),
    ARAFAH(12, 9),
    EID_AL_ADHA(12, 10),

    /** Eid al-Ghadir, 18 Dhul Hijjah: https://imam-us.org/congratulations-on-the-joyous-event-of-eid-al-ghadir-1447 */
    GHADIR(12, 18, true),

    /** Arbaeen, 20 Safar: https://imam-us.org/clarification-regarding-the-determination-of-arbaeen */
    ARBAEEN(2, 20, true),

    /** The Prophet's birth, 17 Rabi al-Awwal: https://imam-us.org/birth-anniversary-of-prophet-muhammad-pbuhhp-imam-jafar-al-sadiq-p-1446-a-h */
    PROPHET_BIRTH(3, 17, true),

    /** Imam Ali's birth, 13 Rajab: https://imam-us.org/birth-anniversary-of-imam-ali-ibn-abi-talib-p-1444-a-h */
    IMAM_ALI_BIRTH(7, 13, true),

    /** Imam al-Mahdi's birth, 15 Sha'ban: https://imam-us.org/birth-anniversary-of-imam-muhammad-al-mahdi-p-1446-a-h */
    IMAM_MAHDI_BIRTH(8, 15, true),
}

/** One coming date: which day, its Hijri year, and the Gregorian date the user's header reads it on. */
data class UpcomingDate(val day: IslamicDay, val hijriYear: Int, val date: LocalDate)

object IslamicDates {

    /**
     * Each day once (the six shared ones, plus the five Shia only ones when [sect] is Shia), the next
     * time it falls on or after [today], soonest first. Built on
     * the same calendar and the same shift as the Home header ([HijriCalendar]), so the sheet can never
     * name a different day from the header. Empty only outside the calendar's range (1300 to 1600 AH).
     */
    fun upcoming(today: LocalDate, offsetDays: Int, sect: Sect): List<UpcomingDate> {
        val year = HijriCalendar.of(today, offsetDays)?.year ?: return emptyList()
        return IslamicDay.entries.filter { !it.shia || sect == Sect.SHIA }.mapNotNull { d ->
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
