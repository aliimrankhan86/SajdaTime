package com.sajdatime.core
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

/** The watch marks the current prayer (1.7.0). Before today's Fajr nothing is marked, even though the engine answers yesterday's Isha. */
class CurrentForDisplayTest {
    private val fajr = Instant.parse("2026-11-04T05:05:00Z")

    @Test
    fun `before today's Fajr nothing is marked`() {
        assertNull(currentForDisplay(PrayerSlot.ISHA, Instant.parse("2026-11-04T04:00:00Z"), fajr))
    }

    @Test
    fun `from Fajr on the engine's answer is marked`() {
        assertEquals(PrayerSlot.FAJR, currentForDisplay(PrayerSlot.FAJR, fajr, fajr))
        assertEquals(PrayerSlot.ASR, currentForDisplay(PrayerSlot.ASR, Instant.parse("2026-11-04T14:30:00Z"), fajr))
    }

    @Test
    fun `no current prayer stays none`() {
        assertNull(currentForDisplay(null, Instant.parse("2026-11-04T14:30:00Z"), fajr))
    }
}
