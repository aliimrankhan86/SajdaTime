package com.sajdatime.app.widget

import com.sajdatime.app.data.AppSettings
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import java.time.Instant
import java.time.ZoneId

/**
 * Everything the two home screen widgets draw, computed once per refresh. Pure: no Android, so the
 * rules are tested on the JVM and the renderer only has to turn this into views.
 *
 * The widgets never calculate anything a screen does not. The times are [PrayerEngine.compute] with the
 * same [AppSettings] the Times screen uses, the next prayer is [PrayerEngine.nextPrayer], and the
 * highlight rule is the one the Times screen applies to its own rows ([isHighlighted]). No place name is
 * carried at all: a home screen is seen by other people.
 *
 * Only two things on a widget can go stale if every refresh is missed: which row is highlighted and the
 * countdown. The clock times are for the calendar day and stay right.
 */
data class WidgetState(
    /** False on a first run or after settings were cleared: the widget then says how to fix it. */
    val hasLocation: Boolean,
    val today: DayPrayerTimes?,
    val next: NextPrayer?,
    /** True when the next prayer's own day was projected from another latitude (polar places). */
    val nextIsApproximate: Boolean,
    /**
     * Today is a day of Ramadan on the calendar the person uses, so the Fajr and Maghrib rows of the
     * Today widget carry "Suhoor ends" and "Iftar". Already false when the person has switched the
     * Ramadan card off, because that switch hides every Ramadan label, widgets included.
     */
    val ramadanToday: Boolean,
    /**
     * The next prayer's own day is a day of Ramadan, which is what decides its label. It differs from
     * [ramadanToday] on the eve, when tomorrow's Fajr is next and is already the end of the first
     * suhoor, and on the last day after Maghrib, when tomorrow's Fajr is Shawwal's and carries nothing.
     */
    val ramadanOnNextDay: Boolean,
    /**
     * Whether the countdown may be drawn. It is shown only when the refresh alarm is exact, because
     * then it cannot be left counting past zero for long. See PLAN.md section 4.3.
     */
    val countdownAllowed: Boolean,
) {
    /** The Times screen's rule: the next prayer, and only if it is still today. Sunrise is never next. */
    fun isHighlighted(slot: PrayerSlot): Boolean = next != null && slot == next.slot && !next.isTomorrow

    /** "Suhoor ends" for a Fajr and "Iftar" for a Maghrib on a Ramadan day, nothing otherwise. */
    fun ramadanWordFor(slot: PrayerSlot, ramadanOnThatDay: Boolean): RamadanWord? =
        if (!ramadanOnThatDay) {
            null
        } else {
            when (slot) {
                PrayerSlot.FAJR -> RamadanWord.SUHOOR_ENDS
                PrayerSlot.MAGHRIB -> RamadanWord.IFTAR
                else -> null
            }
        }

    enum class RamadanWord { SUHOOR_ENDS, IFTAR }

    companion object {
        fun build(
            settings: AppSettings,
            now: Instant,
            zone: ZoneId,
            canScheduleExact: Boolean,
        ): WidgetState {
            val coordinates = settings.coordinates
                ?: return WidgetState(
                    hasLocation = false,
                    today = null,
                    next = null,
                    nextIsApproximate = false,
                    ramadanToday = false,
                    ramadanOnNextDay = false,
                    countdownAllowed = canScheduleExact,
                )
            val prefs = settings.calculationPrefs
            val todayDate = now.atZone(zone).toLocalDate()
            val today = PrayerEngine.compute(coordinates, todayDate, prefs)
            val next = PrayerEngine.nextPrayer(coordinates, prefs, now, zone)
            val nextDate = next.at.atZone(zone).toLocalDate()
            val labels = settings.ramadanCardEnabled
            return WidgetState(
                hasLocation = true,
                today = today,
                next = next,
                nextIsApproximate = PrayerEngine.compute(coordinates, nextDate, prefs).approximated,
                ramadanToday = labels && HijriCalendar.isRamadan(todayDate, prefs.hijriOffset),
                ramadanOnNextDay = labels && HijriCalendar.isRamadan(nextDate, prefs.hijriOffset),
                countdownAllowed = canScheduleExact,
            )
        }
    }
}
