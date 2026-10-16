package com.sajdatime.app.ui

import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.RamadanInfo
import java.time.Duration
import java.time.Instant

/**
 * Everything the Ramadan card and the notice need, worked out once per recalculation.
 *
 * Present only while the user's calendar is inside Ramadan or inside the notice window
 * ([RamadanInfo.showsNotice]). Outside those it is null and the Times screen carries no trace of
 * Ramadan at all, which is the point: a screen about a month that is not happening is noise.
 *
 * Pure, no Android, so the same numbers are checked on the JVM and on the emulator.
 *
 * Suhoor ends at Fajr and iftar is at Maghrib because those are the two times the app already
 * calculates and labels, and the wording says "as calculated". Nothing here adds an imsak margin
 * (docs/HANDOVER.md A11). The times are the adjusted ones, so "Match your mosque" minutes apply.
 */
data class RamadanState(
    val info: RamadanInfo,
    val today: DayPrayerTimes,
    val yesterday: DayPrayerTimes,
    val tomorrow: DayPrayerTimes,
) {
    val suhoorEnds: Instant get() = today[PrayerSlot.FAJR]

    val iftar: Instant get() = today[PrayerSlot.MAGHRIB]

    val fastLength: Duration get() = Duration.between(suhoorEnds, iftar)

    /**
     * Today's fast minus yesterday's, in whole minutes. Worked out from instants rather than
     * clock readings, so a daylight saving change day shows the true change (London, 28 March
     * 2027: plus 5, not plus 65). Positive in the northern spring, negative in the southern.
     */
    val fastChangeMinutes: Long
        get() = fastLength.toMinutes() -
            Duration.between(yesterday[PrayerSlot.FAJR], yesterday[PrayerSlot.MAGHRIB]).toMinutes()

    val tomorrowSuhoorEnds: Instant get() = tomorrow[PrayerSlot.FAJR]

    val tomorrowIftar: Instant get() = tomorrow[PrayerSlot.MAGHRIB]

    /** Whether tomorrow is still a fasting day on this user's calendar. */
    val tomorrowIsRamadan: Boolean get() = info.contains(tomorrow.date)

    enum class Phase { BEFORE_SUHOOR_END, FASTING, AFTER_IFTAR }

    fun phase(now: Instant): Phase = when {
        now.isBefore(suhoorEnds) -> Phase.BEFORE_SUHOOR_END
        now.isBefore(iftar) -> Phase.FASTING
        else -> Phase.AFTER_IFTAR
    }

    companion object {
        /**
         * Null when there is nothing to show: the user's calendar is neither inside Ramadan nor
         * inside the notice window, or the date is outside the calendar's range.
         *
         * [today] is passed in rather than recomputed so the card can never show a different
         * Fajr or Maghrib from the list on the same screen. Yesterday and tomorrow are two more
         * engine calls, which is cheap next to the one the screen already makes.
         */
        fun build(
            coordinates: Coordinates,
            prefs: CalculationPrefs,
            today: DayPrayerTimes,
        ): RamadanState? {
            val info = HijriCalendar.ramadan(today.date, prefs.hijriOffset) ?: return null
            if (!info.isRamadan && !info.showsNotice(today.date)) return null
            return RamadanState(
                info = info,
                today = today,
                yesterday = PrayerEngine.compute(coordinates, today.date.minusDays(1), prefs),
                tomorrow = PrayerEngine.compute(coordinates, today.date.plusDays(1), prefs),
            )
        }
    }
}
