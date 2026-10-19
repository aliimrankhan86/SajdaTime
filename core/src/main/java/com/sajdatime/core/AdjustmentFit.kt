package com.sajdatime.core

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

/**
 * Turns "my mosque's board says 10:15" into the minutes "Match your mosque" needs.
 *
 * Counting taps was the only way in before this: a gap of twenty minutes was twenty presses
 * of a plus button, on a screen the user reaches precisely because they are holding a
 * printed timetable. Reading a time off a board is something everyone can do; working out
 * "that is plus seventeen" is not.
 *
 * Pure, no Android, so the edge cases below are unit tests rather than hopes.
 *
 * What it does **not** do is decide anything about *why* the times differ. A gap past
 * [CalculationPrefs.MAX_ADJUSTMENT_MINUTES] comes back as [Result.TooFar] and the caller
 * explains it: that size of gap is a different calculation method or a board that shows the
 * congregation, never a bigger offset (docs/HANDOVER.md §5.17 and the "Match your mosque"
 * note in §10). Widening the limit would let "Isha begins" be dragged onto the congregation
 * time, and the notification and the Now marker would then state something false.
 */
object AdjustmentFit {

    sealed interface Result {
        /** The correction to store, already inside the legal range, 0 meaning "no change". */
        data class Within(val minutes: Int) : Result

        /** Further from the calculated time than a correction is allowed to go. */
        data object TooFar : Result
    }

    /**
     * @param calculated the prayer time **before** any correction, as the engine produced it.
     *   The caller must pass the uncorrected time: the stored correction is relative to it,
     *   so fitting against an already corrected time would double count.
     * @param target the clock time the user read off their mosque's board.
     */
    fun fit(calculated: Instant, target: LocalTime, zone: ZoneId): Result {
        // Compared at minute resolution, and from the *truncated* calculated time, because
        // that is what the screen shows. A calculated 21:58:50 is displayed as 21:58, so a
        // board reading 22:15 is seventeen minutes away on screen, not sixteen.
        val shown = calculated.atZone(zone).truncatedTo(ChronoUnit.MINUTES)

        // Either half of the day, whichever is nearer. Material's 12 hour dial keeps the a.m.
        // or p.m. the user started in when the hand passes 12 (seen on the emulator, 19 Oct
        // 2026: 11:58 p.m. moved to 12:05 gave 12:05 p.m., noon), so a board time just past 12
        // arrived half a day away and was refused; Dhuhr at 11:58 a.m. moved to 12:05 would
        // arrive as 00:05 the same way. Boards are often 12 hour too, with no a.m. or p.m. A time 12
        // hours out can never be a real correction, so the nearer reading is the only one
        // that could be meant, and the row then shows the minutes it stored.
        val minutes = listOf(target, target.plusHours(12)).map { minutesTo(shown, it, zone) }.minBy { abs(it) }
        return if (minutes in -CalculationPrefs.MAX_ADJUSTMENT_MINUTES..CalculationPrefs.MAX_ADJUSTMENT_MINUTES) {
            Result.Within(minutes)
        } else {
            Result.TooFar
        }
    }

    private fun minutesTo(shown: ZonedDateTime, target: LocalTime, zone: ZoneId): Int {
        var wanted = shown.toLocalDate().atTime(target).atZone(zone)

        // Take the short way round the clock. Isha at 23:58 and a board saying 00:05 is
        // seven minutes later, not 23 hours and 53 minutes earlier. Without this, the one
        // prayer most likely to sit near midnight in a northern summer is the one that
        // could never be matched.
        if (Duration.between(shown, wanted) > HALF_DAY) wanted = wanted.minusDays(1)
        if (Duration.between(shown, wanted) < HALF_DAY.negated()) wanted = wanted.plusDays(1)

        return Duration.between(shown, wanted).toMinutes().toInt()
    }

    private val HALF_DAY: Duration = Duration.ofHours(12)
}
