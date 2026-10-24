package com.sajdatime.core

import java.time.Instant

/** The four skies behind the top of Times (1.6, step 5). Four fixed skies, never a continuous gradient. */
enum class SkyPeriod { DAWN, DAY, EVENING, NIGHT }

/** Which sky it is, where the day stands, and whether the mark fits. Plain Kotlin: carries to iPhone. */
object Sky {
    /** The sky for the prayer whose window is open, or Day when none is (sunrise to Dhuhr, or no times at all). */
    fun periodOf(current: PrayerSlot?): SkyPeriod = when (current) {
        PrayerSlot.FAJR -> SkyPeriod.DAWN
        null, PrayerSlot.SUNRISE, PrayerSlot.DHUHR -> SkyPeriod.DAY
        PrayerSlot.ASR, PrayerSlot.MAGHRIB -> SkyPeriod.EVENING
        PrayerSlot.ISHA -> SkyPeriod.NIGHT
    }

    /**
     * When this period ends, for the spoken "Dawn, until 7:40 am". Night before Fajr ends at today's Fajr; night
     * after Isha ends at [nextFajr], tomorrow's, which the caller has from NextPrayer; null if it has not.
     */
    fun endOf(period: SkyPeriod, today: DayPrayerTimes, now: Instant, nextFajr: Instant?): Instant? = when (period) {
        SkyPeriod.DAWN -> today[PrayerSlot.SUNRISE]
        SkyPeriod.DAY -> today[PrayerSlot.ASR]
        SkyPeriod.EVENING -> today[PrayerSlot.ISHA]
        SkyPeriod.NIGHT -> if (now < today[PrayerSlot.FAJR]) today[PrayerSlot.FAJR] else nextFajr
    }

    /**
     * 0 before Fajr, 1 after Isha, else (segment + fraction) / 4 over the five prayers in [prayers]
     * (DayPrayerTimes.prayersOnly, never ordered, which has sunrise). Evenly spaced by prayer, not by clock:
     * Fajr to Dhuhr is six hours and Maghrib to Isha ninety minutes, and a clock-spaced arc would bunch.
     */
    fun progress(now: Instant, prayers: List<Pair<PrayerSlot, Instant>>): Float {
        require(prayers.size == 5 && prayers.none { it.first == PrayerSlot.SUNRISE }) {
            "Sky.progress takes the five prayers (prayersOnly), not ordered"
        }
        if (now <= prayers.first().second) return 0f
        if (now >= prayers.last().second) return 1f
        val i = prayers.indexOfLast { it.second <= now }
        val start = prayers[i].second.toEpochMilli()
        val end = prayers[i + 1].second.toEpochMilli()
        val fraction = if (end > start) (now.toEpochMilli() - start).toFloat() / (end - start) else 0f
        return (i + fraction) / 4f
    }

    /** Room for the mark beside the city: row width less 8dp row padding, 30dp caret, 8dp gap and the 104dp mark. */
    fun markFits(nameWidthPx: Float, rowWidthPx: Float, density: Float, fontScale: Float): Boolean =
        fontScale < 2.0f && nameWidthPx <= rowWidthPx - 150f * density
}
