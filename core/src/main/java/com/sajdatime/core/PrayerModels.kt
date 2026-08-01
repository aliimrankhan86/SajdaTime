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
data class Coordinates(val latitude: Double, val longitude: Double) {
    companion object {
        /**
         * The only way a stored or received pair should become a [Coordinates].
         *
         * Every reader of persisted settings goes through here, because not every writer
         * is trusted. The watch's [SettingsSyncService][com.sajdatime.core.WatchSyncContract]
         * listener has to be `exported` for Play Services to deliver Data Layer events at
         * all, so any app on the same watch can also address it directly and hand over a
         * payload. It can only write, never read — but writing a latitude of 999 into a
         * prayer app means wrong times, and wrong times are the whole harm this app can do.
         * Returning null instead degrades to "no location yet", which every screen already
         * handles because a first run looks the same.
         *
         * It also covers the duller case of a settings file corrupted by a half-finished
         * write, which needs no attacker at all.
         *
         * NaN fails every comparison including its own, so it falls out of the range check
         * rather than needing a line of its own.
         */
        fun orNull(latitude: Double?, longitude: Double?): Coordinates? {
            if (latitude == null || longitude == null) return null
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
            return Coordinates(latitude, longitude)
        }
    }
}

/** The six daily time markers for one calendar day at one location. */
data class DayPrayerTimes(
    val date: LocalDate,
    val times: Map<PrayerSlot, Instant>,
    /**
     * The latitude these times were projected from, or null when they are the user's own
     * astronomy. Set only when the sun refused to rise or set where the user actually is,
     * which in practice means above the polar circles. It carries the *number* rather than
     * a flag because the number is not constant — it depends on the calculation method, and
     * the screen shows it so the user can check it against whatever their mosque follows.
     * See [PrayerEngine.polarReferenceLatitude].
     *
     * The UI must say so when this is set: an approximation the user is not told about is
     * worse than no times at all.
     */
    val approximatedFrom: Double? = null,
) {
    /** True when these times came from somewhere other than where the user is standing. */
    val approximated: Boolean get() = approximatedFrom != null

    operator fun get(slot: PrayerSlot): Instant = times.getValue(slot)

    /** Prayer slots only (no sunrise), in chronological order. */
    val prayersOnly: List<Pair<PrayerSlot, Instant>>
        get() = PrayerSlot.entries.filter { it.isPrayer }.map { it to times.getValue(it) }

    /**
     * Every slot in chronological order, **sunrise included**.
     *
     * Sunrise earns its place here even though it is not a prayer: it is the moment Fajr's
     * window shuts. Any question of the form "which prayer is in right now" has to see it,
     * or it will answer "Fajr" all morning. [PrayerEngine.currentPrayer] relies on that.
     */
    val ordered: List<Pair<PrayerSlot, Instant>>
        get() = PrayerSlot.entries.map { it to times.getValue(it) }
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
