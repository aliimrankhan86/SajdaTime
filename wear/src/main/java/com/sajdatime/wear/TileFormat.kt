package com.sajdatime.wear

import android.content.Context
import android.text.format.DateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Text and timing helpers for the next-prayer tile.
 *
 * Pulled out of [NextPrayerTileService] because these are the two places the tile can be
 * wrong in a way nobody notices: a countdown that reads oddly at the boundaries, and a
 * refresh interval that either never updates or spins.
 *
 * The wording now comes from string resources so the watch can be translated, which means
 * the public entry points need a Context. Every decision worth testing is kept in a pure
 * function below the resource lookup, so the unit tests still run on a plain JVM with no
 * Robolectric and no device.
 */
internal object TileFormat {

    /**
     * Hours and minutes left, never negative — a tile can be rendered a fraction of a
     * second after the prayer it is counting down to, and "in -1m" is worse than nothing.
     */
    fun remaining(duration: Duration): Pair<Long, Long> {
        val safe = if (duration.isNegative) Duration.ZERO else duration
        return safe.toHours() to (safe.toMinutes() % 60)
    }

    fun humanise(context: Context, duration: Duration): String {
        val (hours, minutes) = remaining(duration)
        return if (hours > 0) {
            context.getString(R.string.wear_in_h_m, hours, minutes)
        } else {
            context.getString(R.string.wear_in_m, minutes)
        }
    }

    /** Clock time, honouring the watch's own 12/24-hour setting rather than assuming 24. */
    fun clock(context: Context, instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        clock(instant, zone, DateFormat.is24HourFormat(context))

    /** The formatting itself, with the device preference passed in so it can be tested. */
    fun clock(instant: Instant, zone: ZoneId, use24Hour: Boolean): String {
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(instant.atZone(zone))
    }

    /**
     * When to ask the system for a new tile: a minute after the prayer starts, so the
     * tile rolls on to the next one. Floored at a minute so an already-passed time
     * cannot ask for an immediate refresh over and over.
     */
    fun freshness(remaining: Duration): Duration =
        remaining.plusMinutes(1).coerceAtLeast(Duration.ofMinutes(1))
}
