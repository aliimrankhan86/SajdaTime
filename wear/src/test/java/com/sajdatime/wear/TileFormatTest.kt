package com.sajdatime.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class TileFormatTest {

    @Test
    fun `countdown under an hour omits the hours part`() {
        assertEquals("in 42m", TileFormat.humanise(Duration.ofMinutes(42)))
    }

    @Test
    fun `countdown over an hour shows hours and minutes`() {
        assertEquals("in 3h 7m", TileFormat.humanise(Duration.ofMinutes(187)))
    }

    @Test
    fun `an exact hour still reads sensibly`() {
        assertEquals("in 2h 0m", TileFormat.humanise(Duration.ofHours(2)))
    }

    /**
     * A tile can be rendered a fraction of a second after the prayer it is counting down
     * to. Reading "in -1m" would be worse than reading nothing.
     */
    @Test
    fun `a prayer that has just passed never counts down negatively`() {
        assertEquals("in 0m", TileFormat.humanise(Duration.ofSeconds(-30)))
        assertEquals("in 0m", TileFormat.humanise(Duration.ofMinutes(-90)))
    }

    @Test
    fun `refresh is scheduled just after the prayer starts`() {
        assertEquals(
            Duration.ofMinutes(21),
            TileFormat.freshness(Duration.ofMinutes(20)),
        )
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
        assertEquals("19:42", TileFormat.clock(instant, ZoneId.of("Europe/London")))
    }
}
