package com.sajdatime.app

import com.sajdatime.app.ui.qibla.KAABA_DISTANCE
import com.sajdatime.app.ui.qibla.KAABA_SIZE
import com.sajdatime.app.ui.qibla.NEEDLE_HALF_WIDTH
import com.sajdatime.app.ui.qibla.NEEDLE_HUB
import com.sajdatime.app.ui.qibla.NEEDLE_HUB_HOLE
import com.sajdatime.app.ui.qibla.NEEDLE_SHOULDER
import com.sajdatime.app.ui.qibla.NEEDLE_TAIL
import com.sajdatime.app.ui.qibla.NEEDLE_TAIL_HALF_WIDTH
import com.sajdatime.app.ui.qibla.NEEDLE_TIP
import com.sajdatime.app.ui.qibla.needleHalfWidthAt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Holds the Qibla needle's shape to the Kaaba mark's, so the pointer can always be read by eye.
 *
 * Written 11 Nov 2026 after Ali reported, on the day 1.6.0 went live, that the needle had got thicker and the
 * direction "seems a bit off". Nothing in the direction maths had changed; the step 6 teardrop was 0.24 of the
 * radius across with a blunt tip that stopped under a mark only 0.22 across, so nobody could tell whether the
 * two were lined up. These are the numbers that went wrong, written as rules, so the next restyle cannot
 * fatten or shorten the needle again without this test saying so. The pixels themselves are measured on a
 * device by `QiblaDialDeviceTest`.
 *
 * Shares of the dial radius throughout, y down, dial centre at the origin, as in `QiblaScreen.kt`.
 */
class QiblaDialGeometryTest {

    /** The Kaaba's wall is x 5 to 19 of the 24 unit viewport in `ic_kaaba.xml`: 14 of 24 of the mark's box. */
    private val wallHalfWidth = KAABA_SIZE * 14f / 24f / 2f
    private val markNearEdge = KAABA_DISTANCE - KAABA_SIZE / 2f

    @Test
    fun `the point is on the centre line and the blade runs straight to it`() {
        // The path mirrors every x, so a width of nothing at the tip is the whole of "the needle points
        // straight up". A tip a hair to one side is a compass that is always off.
        assertEquals(0f, needleHalfWidthAt(NEEDLE_TIP), 0f)
        assertEquals(NEEDLE_HALF_WIDTH, needleHalfWidthAt(NEEDLE_SHOULDER), 0.0001f)
        // Straight edges: half way from shoulder to tip it is half as wide.
        val half = (NEEDLE_TIP + NEEDLE_SHOULDER) / 2f
        assertEquals(NEEDLE_HALF_WIDTH / 2f, needleHalfWidthAt(half), 0.0001f)
    }

    @Test
    fun `the tip ends inside the kaaba mark and short of the letters`() {
        assertTrue("the tip must reach into the mark, not stop under it", NEEDLE_TIP > markNearEdge + KAABA_SIZE / 4f)
        assertTrue("the tip must not run past the mark's centre", NEEDLE_TIP <= KAABA_DISTANCE)
        // The letters' band starts at 0.695 (0.74 less half of 0.09).
        assertTrue("the tip must stay clear of the cardinal letters", NEEDLE_TIP < 0.74f - 0.09f / 2f)
    }

    @Test
    fun `the needle is a fine point where it meets the mark`() {
        // Where the needle enters the mark it must be a small part of the wall's width, or it cannot be
        // centred by eye. The 1.6.0 teardrop was blunt here (0.01 plus a 0.0125 stroke) under a 0.064 wall.
        val entering = needleHalfWidthAt(markNearEdge)
        assertTrue("needle is $entering wide at the mark's edge, wall is $wallHalfWidth", entering <= wallHalfWidth / 8f)
    }

    @Test
    fun `nothing about the needle is wider than the one people could read`() {
        // 0.075 was the 1.5.0 needle's half width. The fat 1.6.0 teardrop was 0.12.
        assertTrue(NEEDLE_HUB <= 0.075f)
        assertTrue(NEEDLE_HALF_WIDTH < NEEDLE_HUB)
        assertTrue(NEEDLE_TAIL_HALF_WIDTH <= NEEDLE_HALF_WIDTH)
        assertTrue("the hole must leave a ring", NEEDLE_HUB_HOLE < NEEDLE_HUB / 2f)
    }

    @Test
    fun `the blade narrows all the way to the tip and the tail is the short end`() {
        var previous = Float.MAX_VALUE
        var up = NEEDLE_SHOULDER
        while (up <= NEEDLE_TIP) {
            val now = needleHalfWidthAt(up)
            assertTrue("needle widens going up at $up", now <= previous)
            previous = now
            up += 0.01f
        }
        assertTrue("the tail must be clearly shorter than the blade", NEEDLE_TAIL < NEEDLE_TIP / 3f)
    }
}
