package com.sajdatime.wear

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
 */
internal object TileFormat {

    /** Never announces a negative countdown, which can happen if a prayer starts mid-render. */
    fun humanise(duration: Duration): String {
        val safe = if (duration.isNegative) Duration.ZERO else duration
        val hours = safe.toHours()
        val minutes = safe.toMinutes() % 60
        return if (hours > 0) "in ${hours}h ${minutes}m" else "in ${minutes}m"
    }

    fun clock(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()).format(instant.atZone(zone))

    /**
     * When to ask the system for a new tile: a minute after the prayer starts, so the
     * tile rolls on to the next one. Floored at a minute so an already-passed time
     * cannot ask for an immediate refresh over and over.
     */
    fun freshness(remaining: Duration): Duration =
        remaining.plusMinutes(1).coerceAtLeast(Duration.ofMinutes(1))
}
