package com.sajdatime.core

import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/** What the "time left" line counts to. It is always a time the Times list already shows. */
enum class WindowTarget { SUNRISE, ASR, MAGHRIB, ISHA, DHUHR, MIDNIGHT, FAJR }

/**
 * "Until [target] · 2h 14m" on the Now row (1.7.0). Deliberately NOT the end of a prayer's window: the app
 * does not model that (madhab differences, Jafari combining; HANDOVER 5.8a, 5.39). It only counts to the next
 * time on the list. After Isha that is Islamic midnight while the night row shows and midnight is ahead,
 * otherwise Fajr. Ali's call whether Isha should always count to Fajr (alternative A2 in DESIGN-A-B-C-F).
 */
data class CurrentWindow(val current: PrayerSlot, val target: WindowTarget, val at: Instant) {
    fun remaining(now: Instant): Duration = Duration.between(now, at)

    companion object {
        /** Null when no prayer is the current one (before Fajr with no yesterday, or sunrise to Dhuhr). */
        fun at(
            coordinates: Coordinates,
            prefs: CalculationPrefs,
            now: Instant,
            zone: ZoneId,
            showNight: Boolean,
        ): CurrentWindow? {
            val current = PrayerEngine.currentPrayer(coordinates, prefs, now, zone) ?: return null
            val today = now.atZone(zone).toLocalDate()
            // The day the current prayer belongs to: at 00:30 Isha is still yesterday's.
            val day = (-1L..0L)
                .map { PrayerEngine.compute(coordinates, today.plusDays(it), prefs) }
                .last { d -> d.times.any { (slot, at) -> slot == current && at <= now } }
            val direct = when (current) {
                PrayerSlot.FAJR -> WindowTarget.SUNRISE to day[PrayerSlot.SUNRISE]
                PrayerSlot.DHUHR -> WindowTarget.ASR to day[PrayerSlot.ASR]
                PrayerSlot.ASR -> WindowTarget.MAGHRIB to day[PrayerSlot.MAGHRIB]
                PrayerSlot.MAGHRIB -> WindowTarget.ISHA to day[PrayerSlot.ISHA]
                else -> null
            }
            if (direct != null) return CurrentWindow(current, direct.first, direct.second)
            if (current != PrayerSlot.ISHA) return null
            val fajr = PrayerEngine.compute(coordinates, day.date.plusDays(1), prefs)[PrayerSlot.FAJR]
            val night = day.sunset?.let { Night.between(it, fajr) }
            return if (showNight && night != null && now < night.midnight) {
                CurrentWindow(current, WindowTarget.MIDNIGHT, night.midnight)
            } else {
                CurrentWindow(current, WindowTarget.FAJR, fajr)
            }
        }
    }
}
