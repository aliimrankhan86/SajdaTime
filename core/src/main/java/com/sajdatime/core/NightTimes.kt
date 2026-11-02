package com.sajdatime.core

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * Islamic midnight and the start of the last third of the night (1.6.0, "Night times"). Plain calculated
 * facts: this names no ruling and no act of worship.
 *
 * [approximated] is true when the sunset or Fajr behind them was projected from a lower latitude (the same
 * condition as [DayPrayerTimes.approximated], which is what Isha is flagged by).
 */
data class NightTimes(
    val midnight: Instant,
    val lastThirdStart: Instant,
    val approximated: Boolean,
)

object Night {
    /**
     * The night is astronomical sunset to the next Fajr (Tahajjud ends at Fajr), for every user, Sunni and Shia:
     * Ali's decision, 2 Nov 2026. It matches Aladhan midnightMode=1 (Jafari). Aladhan's default measures to
     * sunrise instead and differs by about an hour. Sunset, not Maghrib: the Jafari Maghrib offset would move it for Shia users only, and the
     * two sects must see the same night. Null if Fajr is not after sunset (a day that cannot be trusted).
     * Midnight is sunset plus half the night; the last third starts at sunset plus two thirds.
     */
    fun between(sunset: Instant, fajr: Instant, approximated: Boolean = false): NightTimes? {
        if (!fajr.isAfter(sunset)) return null
        val night = Duration.between(sunset, fajr).toMillis()
        return NightTimes(
            midnight = sunset.plusMillis(night / 2),
            lastThirdStart = sunset.plusMillis(night * 2 / 3),
            approximated = approximated,
        )
    }

    /**
     * The night that is on now: before today's Fajr, yesterday's sunset to today's Fajr; otherwise today's
     * sunset to tomorrow's Fajr (the same rule as [Sky.endOf]). Fajr is the one the screen shows, with the
     * user's corrections, so the night ends where the list says Fajr begins.
     */
    fun at(coordinates: Coordinates, prefs: CalculationPrefs, now: Instant, zone: ZoneId): NightTimes? {
        val date = now.atZone(zone).toLocalDate()
        val today = PrayerEngine.compute(coordinates, date, prefs)
        val (start, end) = if (now < today[PrayerSlot.FAJR]) {
            PrayerEngine.compute(coordinates, date.minusDays(1), prefs) to today
        } else {
            today to PrayerEngine.compute(coordinates, date.plusDays(1), prefs)
        }
        val sunset = start.sunset ?: return null
        return between(sunset, end[PrayerSlot.FAJR], start.approximated || end.approximated)
    }
}
