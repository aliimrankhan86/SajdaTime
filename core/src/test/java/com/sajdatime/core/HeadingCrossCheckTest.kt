package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadingCrossCheckTest {
    /** Feeds one still second and a half of samples every 200 ms. */
    private fun still(check: HeadingCrossCheck, fused: Double, raw: Double, from: Long, ms: Long = 2_000): Long {
        var t = from
        while (t < from + ms) { check.update(fused, raw, t); t += 200 }
        return t
    }

    @Test
    fun `the S23 Ultra's 131 against 163 on a still phone is a disagreement`() {
        val check = HeadingCrossCheck()
        still(check, fused = 131.3, raw = 163.4, from = 0)
        assertTrue(check.disagrees)
    }

    @Test
    fun `agreement within a few degrees is fine, and the Redmi's 1_8 degrees passes`() {
        val check = HeadingCrossCheck()
        still(check, fused = 143.8, raw = 142.0, from = 0)
        assertFalse(check.disagrees)
    }

    @Test
    fun `nothing is judged until the phone has been still for a moment`() {
        val check = HeadingCrossCheck()
        still(check, fused = 131.3, raw = 163.4, from = 0, ms = 1_000)
        assertFalse(check.disagrees)
    }

    @Test
    fun `turning to find the Qibla never trips it, a lagging magnet heading included`() {
        val check = HeadingCrossCheck()
        var t = 0L
        // a steady 30 degrees a second turn with the raw heading a second behind
        for (i in 0..40) { val fused = 100.0 + i * 6.0; check.update(fused, fused - 30.0, t); t += 200 }
        assertFalse(check.disagrees)
    }

    @Test
    fun `the verdict holds through a turn and clears at the next still agreement, with hysteresis`() {
        val check = HeadingCrossCheck()
        var t = still(check, 131.3, 163.4, 0)
        assertTrue(check.disagrees)
        t = still(check, 200.0, 172.0, t) // 28 apart still
        assertTrue(check.disagrees)
        t = still(check, 200.0, 186.0, t, 6_000) // 14 apart: inside 12..20, stays
        assertTrue(check.disagrees)
        still(check, 200.0, 195.0, t, 6_000) // 5 apart
        assertFalse(check.disagrees)
    }

    @Test
    fun `works across north`() {
        val check = HeadingCrossCheck()
        still(check, fused = 358.0, raw = 3.0, from = 0)
        assertFalse(check.disagrees)
    }

    @Test
    fun `magnet heading from a flat phone matches the hand calculation`() {
        val flat = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        // field toward device +Y and down: the top edge faces magnetic north
        assertEquals(0.0, HeadingCrossCheck.magnetHeading(flat, 0f, 19f, -45f), 0.01)
        // field toward device +X: north is to the right of the top edge, so the top edge faces west (270)
        assertEquals(270.0, HeadingCrossCheck.magnetHeading(flat, 19f, 0f, -45f), 0.01)
    }
}
