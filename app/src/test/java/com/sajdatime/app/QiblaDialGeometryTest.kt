package com.sajdatime.app

import com.sajdatime.app.ui.qibla.KAABA_DISTANCE
import com.sajdatime.app.ui.qibla.KAABA_SIZE
import com.sajdatime.app.ui.qibla.NEEDLE_END_CENTRE
import com.sajdatime.app.ui.qibla.NEEDLE_END_RADIUS
import com.sajdatime.app.ui.qibla.NEEDLE_FLANK
import com.sajdatime.app.ui.qibla.NEEDLE_TIP
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

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

    /** Half the needle's width at height [y] (negative is up), by bisection along the flank's cubic. */
    private fun halfWidthAt(y: Float): Float {
        val f = NEEDLE_FLANK
        fun at(t: Float, i: Int): Float {
            val u = 1 - t
            return u * u * u * f[i] + 3 * u * u * t * f[i + 2] + 3 * u * t * t * f[i + 4] + t * t * t * f[i + 6]
        }
        var lo = 0f
        var hi = 1f
        repeat(40) {
            val mid = (lo + hi) / 2
            if (at(mid, 1) < y) lo = mid else hi = mid
        }
        return abs(at(lo, 0))
    }

    @Test
    fun the_tip_is_on_the_centre_line_and_the_flanks_mirror_each_other() {
        // The right flank is drawn as the left one with x negated, so the tip being at x 0 is the whole
        // of "the needle points straight up". A tip a hair to one side is a compass that is always off.
        assertEquals(0f, NEEDLE_FLANK[0], 0f)
        assertEquals(-NEEDLE_TIP, NEEDLE_FLANK[1], 0f)
        // Lands on the round end's widest point, tangent to it (the last control point is straight above).
        assertEquals(-NEEDLE_END_RADIUS, NEEDLE_FLANK[6], 0f)
        assertEquals(NEEDLE_END_CENTRE, NEEDLE_FLANK[7], 0f)
        assertEquals(NEEDLE_FLANK[6], NEEDLE_FLANK[4], 0f)
    }

    @Test
    fun the_tip_ends_inside_the_kaaba_mark_and_short_of_the_letters() {
        assertTrue("the tip must reach into the mark, not stop under it", NEEDLE_TIP > markNearEdge + KAABA_SIZE / 4f)
        assertTrue("the tip must not run past the mark's centre", NEEDLE_TIP <= KAABA_DISTANCE)
        // The letters' band starts at 0.695 (0.74 less half of 0.09).
        assertTrue("the tip must stay clear of the cardinal letters", NEEDLE_TIP < 0.74f - 0.09f / 2f)
    }

    @Test
    fun the_needle_is_a_fine_point_where_it_meets_the_mark() {
        // Where the needle enters the mark it must be a small part of the wall's width, or it cannot be
        // centred by eye. The 1.6.0 teardrop was blunt here (0.01 plus a 0.0125 stroke) under a 0.064 wall.
        val entering = halfWidthAt(-markNearEdge)
        assertTrue("needle is $entering wide at the mark's edge, wall is $wallHalfWidth", entering <= wallHalfWidth / 8f)
    }

    @Test
    fun the_needle_is_no_wider_than_the_one_people_could_read() {
        // 0.075 was the 1.5.0 needle's half width. The fat 1.6.0 teardrop was 0.12.
        assertTrue(NEEDLE_END_RADIUS <= 0.075f)
        var widest = 0f
        var y = -NEEDLE_TIP
        while (y <= NEEDLE_END_CENTRE) {
            widest = maxOf(widest, halfWidthAt(y))
            y += 0.01f
        }
        assertTrue("flank bulges to $widest", widest <= NEEDLE_END_RADIUS + 0.0005f)
    }

    @Test
    fun the_flank_narrows_all_the_way_to_the_tip() {
        var previous = Float.MAX_VALUE
        var y = NEEDLE_END_CENTRE
        while (y >= -NEEDLE_TIP + 0.005f) {
            val now = halfWidthAt(y)
            assertTrue("needle widens going up at $y", now <= previous + 0.0005f)
            previous = now
            y -= 0.01f
        }
    }
}
