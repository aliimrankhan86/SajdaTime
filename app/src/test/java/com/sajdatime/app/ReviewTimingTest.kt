package com.sajdatime.app

import com.sajdatime.app.review.ReviewTiming
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReviewTimingTest {

    private fun due(today: Long = 100, from: Long = 86, open: Int = 3, asks: Int = 0, last: Long = -1000) =
        ReviewTiming.isDue(today, from, open, asks, last)

    @Test fun `due once kept two weeks and opened three days`() = assertTrue(due())
    @Test fun `not before two weeks`() = assertFalse(due(from = 87))
    @Test fun `not before three open days`() = assertFalse(due(open = 2))
    @Test fun `never a fourth time`() = assertFalse(due(asks = 3))
    @Test fun `not within four months of the last ask`() {
        assertFalse(due(last = 100 - 119))
        assertTrue(due(asks = 1, last = 100 - 120))
    }

    private val day = Instant.parse("2026-10-17T00:00:00Z")
    private fun at(h: Int, m: Int = 0) = day.plusSeconds(h * 3600L + m * 60L)
    private val fajr = at(5)
    private val sunrise = at(6, 30)
    private val times = listOf(fajr, at(12, 30), at(15, 30), at(18), at(19, 30))
    private fun calm(now: Instant) = ReviewTiming.isCalmMoment(now, times, fajr, sunrise)

    @Test fun `calm mid morning`() = assertTrue(calm(at(10)))
    @Test fun `not in the hour before Fajr or until sunrise`() {
        assertFalse(calm(at(4, 0)))
        assertFalse(calm(at(6, 29)))
        assertTrue(calm(at(7, 0)))
    }
    @Test fun `not 20 minutes before or 30 after a prayer`() {
        assertFalse(calm(at(12, 10)))
        assertFalse(calm(at(12, 59)))
        assertTrue(calm(at(13, 0)))
        assertTrue(calm(at(12, 9)))
    }
    @Test fun `never without times`() = assertFalse(ReviewTiming.isCalmMoment(at(10), emptyList(), null, null))
}
