package com.sajdatime.wear

import android.content.Context
import android.text.format.DateFormat
import com.sajdatime.core.AppLocale
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
        // applicationContext for the 12/24 question only: it is the one context left
        // unpinned, so it still answers for the watch rather than for en-GB. See
        // TimeFormat.clock on the phone for what happens if it is asked of a pinned one.
        clock(instant, zone, DateFormat.is24HourFormat(context.applicationContext), AppLocale.of(context))

    /**
     * The formatting itself, with the device preference and the locale passed in so it can
     * be tested. The locale is the app's own language, not the watch's — see AppLocale.kt.
     * The default keeps the existing tests, which have no Context, working unchanged.
     */
    fun clock(
        instant: Instant,
        zone: ZoneId,
        use24Hour: Boolean,
        locale: Locale = Locale.UK,
    ): String {
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return DateTimeFormatter.ofPattern(pattern, locale).format(instant.atZone(zone))
    }

    /**
     * When to ask the system for a new tile: a minute after the prayer starts, so the
     * tile rolls on to the next one. Floored at a minute so an already-passed time
     * cannot ask for an immediate refresh over and over.
     */
    fun freshness(remaining: Duration): Duration =
        remaining.plusMinutes(1).coerceAtLeast(Duration.ofMinutes(1))
}
