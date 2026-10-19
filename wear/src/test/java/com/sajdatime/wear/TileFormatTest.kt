package com.sajdatime.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TileFormatTest {

    // The wording lives in strings.xml now, so these assert the numbers the wording is
    // built from. Same coverage, still no device needed.

    @Test
    fun `countdown under an hour has no hours part`() {
        assertEquals(0L to 42L, TileFormat.remaining(Duration.ofMinutes(42)))
    }

    @Test
    fun `countdown over an hour splits into hours and minutes`() {
        assertEquals(3L to 7L, TileFormat.remaining(Duration.ofMinutes(187)))
    }

    @Test
    fun `an exact hour still reads sensibly`() {
        assertEquals(2L to 0L, TileFormat.remaining(Duration.ofHours(2)))
    }

    /**
     * A tile can be rendered a fraction of a second after the prayer it is counting down
     * to. Reading "in -1m" would be worse than reading nothing.
     */
    @Test
    fun `a prayer that has just passed never counts down negatively`() {
        assertEquals(0L to 0L, TileFormat.remaining(Duration.ofSeconds(-30)))
        assertEquals(0L to 0L, TileFormat.remaining(Duration.ofMinutes(-90)))
    }

    @Test
    fun `refresh is scheduled just after the prayer starts`() {
        assertEquals(
            Duration.ofMinutes(21),
            TileFormat.freshness(Duration.ofMinutes(20)),
        )
    }

    @Test
    fun `a long wait refreshes when the hour of countdown runs out`() {
        assertEquals(Duration.ofMinutes(60), TileFormat.freshness(Duration.ofHours(5)))
    }

    /**
     * The timeline floors like the watch app and the phone: each entry's minutes are what is
     * left a second after it starts, and still a second before the next one starts.
     */
    @Test
    fun `each countdown entry shows the floored minutes for its whole window`() {
        val at = Instant.parse("2026-10-19T12:45:00Z")
        for (now in listOf("2026-10-19T12:04:30Z", "2026-10-19T12:04:00Z", "2026-10-19T12:44:59Z")) {
            val steps = TileFormat.countdownSteps(Instant.parse(now), at)
            steps.forEachIndexed { i, (start, shown) ->
                val end = steps.getOrNull(i + 1)?.first ?: at
                for (moment in listOf(start.plusSeconds(1), end.minusSeconds(1)).filter { !it.isBefore(start) }) {
                    assertEquals("$now, entry $i at $moment", Duration.between(moment, at).toMinutes(), shown.toMinutes())
                }
            }
        }
    }

    @Test
    fun `the countdown steps fall on the minute boundaries before the prayer`() {
        val now = Instant.parse("2026-10-19T12:04:30Z")
        val at = Instant.parse("2026-10-19T12:45:00Z")
        val steps = TileFormat.countdownSteps(now, at)
        assertEquals(now to Duration.ofMinutes(40), steps.first())
        assertEquals(Instant.parse("2026-10-19T12:05:00Z") to Duration.ofMinutes(39), steps[1])
        assertEquals(Instant.parse("2026-10-19T12:44:00Z") to Duration.ZERO, steps.last())
        assertEquals(41, steps.size)
    }

    @Test
    fun `a long wait gets an hour of steps and a passed prayer gets one`() {
        val now = Instant.parse("2026-10-19T01:00:30Z")
        val long = TileFormat.countdownSteps(now, now.plus(Duration.ofHours(5)))
        assertEquals(TileFormat.TIMELINE_MINUTES + 1, long.size)
        // The last entry starts no earlier than the hourly refresh, so no minute is left stale before it.
        assertTrue(!long.last().first.isBefore(now.plus(TileFormat.freshness(Duration.ofHours(5)))))
        assertEquals(listOf(now to Duration.ZERO), TileFormat.countdownSteps(now, now.minusSeconds(5)))
    }

    /**
     * The one that matters: a stale or passed time must not ask the system to refresh
     * immediately, or the tile re-renders in a loop and drains the watch.
     */
    @Test
    fun `a passed prayer still asks for at least a minute before refreshing`() {
        val cases = listOf(
            Duration.ofSeconds(-1),
            Duration.ofSeconds(-30),
            Duration.ofMinutes(-5),
            Duration.ofHours(-9),
            Duration.ZERO,
        )
        for (remaining in cases) {
            val refresh = TileFormat.freshness(remaining)
            assertTrue(
                "freshness for $remaining was $refresh, which would spin",
                refresh >= Duration.ofMinutes(1),
            )
        }
    }

    @Test
    fun `clock renders 24 hour time in the given zone`() {
        // 2026-06-21T18:42:00Z, read in London where the offset is +1 in June.
        val instant = Instant.parse("2026-06-21T18:42:00Z")
        assertEquals(
            "19:42",
            TileFormat.clock(instant, ZoneId.of("Europe/London"), use24Hour = true),
        )
    }

    /**
     * The tile used to hardcode 24-hour time, so a watch set to 12-hour showed "19:42"
     * where every other app on it showed "7:42 pm".
     *
     * The meridiem marker itself is not asserted: it is "PM" under en-US and "pm" under
     * en-GB, so pinning the text would only test which locale the build machine happens
     * to run in. What matters is that the hour rolls back to 7 and that the two settings
     * genuinely differ.
     */
    @Test
    fun `clock honours a 12 hour device`() {
        val instant = Instant.parse("2026-06-21T18:42:00Z")
        val london = ZoneId.of("Europe/London")
        val twelveHour = TileFormat.clock(instant, london, use24Hour = false)

        assertTrue("expected a 12-hour reading, got $twelveHour", twelveHour.startsWith("7:42"))
        assertNotEquals(twelveHour, TileFormat.clock(instant, london, use24Hour = true))
    }
}
