package com.sajdatime.core

import java.time.Instant
import java.time.LocalDate

/**
 * Pure domain model for prayer timing. Deliberately free of Android imports so the
 * same rules can be lifted into an iOS/KMP target later — see docs/ARCHITECTURE.md.
 */

/** School of thought. Drives the default calculation method. */
enum class Sect { SUNNI, SHIA }

/**
 * Sunni madhab. Only the Asr shadow ratio actually differs between them:
 * Hanafi uses 2x object shadow, the other three use 1x.
 */
enum class Madhab { HANAFI, SHAFII, MALIKI, HANBALI }

/**
 * Calculation convention. [AUTO] resolves from the user's sect at compute time,
 * so a user who never opens advanced settings still gets a sane convention.
 */
enum class CalcMethod {
    AUTO,
    MUSLIM_WORLD_LEAGUE,
    EGYPTIAN,
    KARACHI,
    UMM_AL_QURA,
    DUBAI,
    MOON_SIGHTING,
    NORTH_AMERICA,
    KUWAIT,
    QATAR,
    SINGAPORE,
    TURKEY,
    JAFARI,
    TEHRAN,
}

enum class PrayerSlot {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA,
    ;

    /** Sunrise is a time marker, not a prayer — it is never notified. */
    val isPrayer: Boolean get() = this != SUNRISE
}

/** Geographic position. Latitude/longitude in decimal degrees. */
data class Coordinates(val latitude: Double, val longitude: Double)

/** The six daily time markers for one calendar day at one location. */
data class DayPrayerTimes(
    val date: LocalDate,
    val times: Map<PrayerSlot, Instant>,
) {
    operator fun get(slot: PrayerSlot): Instant = times.getValue(slot)

    /** Prayer slots only (no sunrise), in chronological order. */
    val prayersOnly: List<Pair<PrayerSlot, Instant>>
        get() = PrayerSlot.entries.filter { it.isPrayer }.map { it to times.getValue(it) }
}

/** Everything the engine needs to produce times, independent of where it is stored. */
data class CalculationPrefs(
    val sect: Sect = Sect.SUNNI,
    val madhab: Madhab = Madhab.SHAFII,
    val method: CalcMethod = CalcMethod.AUTO,
)

/** The next upcoming prayer, and how long until it starts. */
data class NextPrayer(
    val slot: PrayerSlot,
    val at: Instant,
    /** True when [at] falls on tomorrow rather than today. */
    val isTomorrow: Boolean,
)
