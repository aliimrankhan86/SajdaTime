package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * "Type the time your mosque shows" must land on exactly the minute the screen will then
 * display, in the cases where an off by one is easy to miss by eye: the seconds the engine
 * carries, midnight, and a clock change.
 */
class AdjustmentFitTest {

    private val london = ZoneId.of("Europe/London")

    private fun at(date: LocalDate, hour: Int, minute: Int, second: Int = 0) =
        date.atTime(hour, minute, second).atZone(london).toInstant()

    private val day = LocalDate.of(2026, 8, 1)

    @Test
    fun `the same minute needs no correction`() {
        assertEquals(
            AdjustmentFit.Result.Within(0),
            AdjustmentFit.fit(at(day, 21, 58), LocalTime.of(21, 58), london),
        )
    }

    @Test
    fun `a later board time is a positive correction and an earlier one is negative`() {
        assertEquals(
            AdjustmentFit.Result.Within(17),
            AdjustmentFit.fit(at(day, 21, 58), LocalTime.of(22, 15), london),
        )
        assertEquals(
            AdjustmentFit.Result.Within(-3),
            AdjustmentFit.fit(at(day, 13, 14), LocalTime.of(13, 11), london),
        )
    }

    @Test
    fun `seconds are ignored because the screen ignores them`() {
        // Calculated 21:58:50 is shown as 21:58, so a board saying 22:15 is 17 minutes away on
        // screen. Rounding to the nearest minute instead would treat the base as 21:59 and
        // store 16. The engine adds that to the exact 21:58:50, giving 22:14:50, which the
        // screen shows as 22:14: one minute short of the board.
        assertEquals(
            AdjustmentFit.Result.Within(17),
            AdjustmentFit.fit(at(day, 21, 58, 50), LocalTime.of(22, 15), london),
        )
    }

    @Test
    fun `the stored correction really does display as the board time`() {
        // The round trip the user depends on: fit, apply the way the engine applies it
        // (added to the exact instant), then format to the minute like the screen does.
        val calculated = at(day, 21, 58, 50)
        val result = AdjustmentFit.fit(calculated, LocalTime.of(22, 15), london)
        val minutes = (result as AdjustmentFit.Result.Within).minutes
        val shown = calculated.plusSeconds(60L * minutes).atZone(london).toLocalTime()
        assertEquals(LocalTime.of(22, 15), shown.withSecond(0).withNano(0))
    }

    @Test
    fun `the limit is inclusive and one minute past it is too far`() {
        val calculated = at(day, 12, 0)
        assertEquals(
            AdjustmentFit.Result.Within(30),
            AdjustmentFit.fit(calculated, LocalTime.of(12, 30), london),
        )
        assertEquals(
            AdjustmentFit.Result.TooFar,
            AdjustmentFit.fit(calculated, LocalTime.of(12, 31), london),
        )
        assertEquals(
            AdjustmentFit.Result.Within(-30),
            AdjustmentFit.fit(calculated, LocalTime.of(11, 30), london),
        )
        assertEquals(
            AdjustmentFit.Result.TooFar,
            AdjustmentFit.fit(calculated, LocalTime.of(11, 29), london),
        )
    }

    @Test
    fun `a gap of an hour is refused rather than clamped`() {
        // Slough on the default method: Isha 78 minutes after the mosques. The answer there is
        // the method, so this must not quietly store +30 and look nearly right.
        assertEquals(
            AdjustmentFit.Result.TooFar,
            AdjustmentFit.fit(at(day, 23, 17), LocalTime.of(21, 58), london),
        )
    }

    @Test
    fun `midnight is crossed the short way round`() {
        assertEquals(
            AdjustmentFit.Result.Within(7),
            AdjustmentFit.fit(at(day, 23, 58), LocalTime.of(0, 5), london),
        )
        assertEquals(
            AdjustmentFit.Result.Within(-7),
            AdjustmentFit.fit(at(day, 0, 5), LocalTime.of(23, 58), london),
        )
    }

    @Test
    fun `a clock change day still counts real minutes`() {
        // Europe/London springs forward at 01:00 on 29 March 2026. Nothing is prayed at that
        // hour, but a calculated Fajr at 04:40 BST and a board saying 04:45 must still be 5.
        val change = LocalDate.of(2026, 3, 29)
        assertEquals(
            AdjustmentFit.Result.Within(5),
            AdjustmentFit.fit(at(change, 4, 40), LocalTime.of(4, 45), london),
        )
    }
}
