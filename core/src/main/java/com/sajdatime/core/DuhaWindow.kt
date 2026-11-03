package com.sajdatime.core

import java.time.Duration
import java.time.Instant

/**
 * The Duha window (1.7.0, optional row, off by default, Sunni only). One window, no school table: sources differ
 * by minutes, not by school. minutes approved by Ali 3 Nov 2026 after research: start 20 minutes after
 * sunrise (cautious Hanafi figure), end 10 minutes before Dhuhr (Ibn Uthaymin). Sources agree on the shape, not the
 * exact minutes, so Settings says "about". Rejected: 15 before Dhuhr (no source quotes it).
 * Names no ruling and says nothing about "forbidden" times.
 */
data class DuhaWindow(val start: Instant, val end: Instant) {
    companion object {
        const val START_AFTER_SUNRISE_MINUTES = 20L
        const val END_BEFORE_DHUHR_MINUTES = 10L

        /** Cut to the minute in the safe direction (later start, earlier end), like night times. Null if it cannot be trusted. */
        fun of(sunrise: Instant?, dhuhr: Instant?, approximated: Boolean = false): DuhaWindow? {
            if (sunrise == null || dhuhr == null || approximated) return null
            val start = sunrise.plus(Duration.ofMinutes(START_AFTER_SUNRISE_MINUTES)).ceilToMinute()
            val end = dhuhr.minus(Duration.ofMinutes(END_BEFORE_DHUHR_MINUTES)).floorToMinute()
            return if (end.isAfter(start)) DuhaWindow(start, end) else null
        }

        private fun Instant.floorToMinute(): Instant = Instant.ofEpochSecond(epochSecond - Math.floorMod(epochSecond, 60L))
        private fun Instant.ceilToMinute(): Instant =
            if (epochSecond % 60 == 0L && nano == 0) this else floorToMinute().plusSeconds(60)
    }
}
