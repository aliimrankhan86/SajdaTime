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
import com.sajdatime.app.ui.qibla.TOP_MARK_BASE
import com.sajdatime.app.ui.qibla.TOP_MARK_HALF_WIDTH
import com.sajdatime.app.ui.qibla.TOP_MARK_TIP
import com.sajdatime.app.ui.qibla.continuousTurn
import com.sajdatime.app.ui.qibla.handRotation
import com.sajdatime.app.ui.qibla.needleHalfWidthAt
import com.sajdatime.core.QiblaEngine
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

    /** Degrees apart on a circle, 0 to 180. */
    private fun apart(a: Double, b: Double): Double = kotlin.math.abs((((a - b) % 360.0) + 540.0) % 360.0 - 180.0)

    @Test
    fun `the hand stands at the bearing minus the heading`() {
        // Ali, 12 Nov 2026, on his Redmi: "Facing 80", "Turn right 39", and the hand pointing straight up.
        // He followed the hand. The hand is the Kaaba now, and this is the sum that says so. The dial is
        // turned by minus the heading, so `handRotation` is given that, as `CompassDial` gives it.
        val slough = 118.5
        for (heading in listOf(0.0, 80.0, 118.5, 200.0, 300.0, 359.0)) {
            val hand = handRotation(-heading.toFloat(), slough).toDouble()
            assertEquals("heading $heading", 0.0, apart(hand, slough - heading), 0.001)
            // The same number the sentence prints ("Turn right 39"), so the hand and the words agree.
            assertEquals("heading $heading", 0.0, apart(hand, QiblaEngine.relativeTurn(heading, slough)), 0.001)
        }
        assertEquals("80 from north, Kaaba at 118.5: 38.5 right of straight up", 38.5f, handRotation(-80f, slough), 0.001f)
        assertEquals("facing it: straight up", 0f, handRotation(-118.5f, slough), 0.001f)
    }

    @Test
    fun `the hand is right across north, either way round`() {
        // Heading 350 with the Kaaba at 10 is 20 to the right, not 340 to the left, and the other way about.
        assertEquals(0.0, apart(handRotation(-350f, 10.0).toDouble(), 20.0), 0.001)
        assertEquals(0.0, apart(handRotation(-10f, 350.0).toDouble(), -20.0), 0.001)
        assertEquals(0.0, apart(handRotation(-359.5f, 0.5).toDouble(), 1.0), 0.001)
    }

    @Test
    fun `without a compass the hand stands at the bearing from north`() {
        // No heading: the ring is not turned (north up), so the hand is at the bearing itself.
        assertEquals(118.5f, handRotation(0f, 118.5), 0.001f)
    }

    @Test
    fun `the top mark is small, on the centre line, and clear of the kaaba when the hand is under it`() {
        assertTrue("it points in from the rim", TOP_MARK_TIP < TOP_MARK_BASE && TOP_MARK_BASE <= 1f)
        // The Kaaba's ink reaches 0.816 at a cardinal bearing (see KAABA_DISTANCE): facing the Kaaba the two
        // must not touch, or the mark hides the very moment it is there for.
        assertTrue("clear of the Kaaba mark", TOP_MARK_TIP > 0.816f + 0.03f)
        assertTrue("quieter than the hand", TOP_MARK_BASE - TOP_MARK_TIP < NEEDLE_TIP / 4f && TOP_MARK_HALF_WIDTH < 0.07f)
    }

    @Test
    fun `the ring never spins a whole turn when the heading crosses north`() {
        // Heading 359.9 then 0.1: the ring's turn goes from -359.9 on to -360.1, two tenths, not back to
        // -0.1, which is 359.8 the other way and a full spin on screen.
        assertEquals(-360.1f, continuousTurn(-359.9f, -0.1f), 0.001f)
        assertEquals(-359.9f, continuousTurn(-360.1f, -359.9f), 0.001f)
        // Several times round, it keeps going the short way and still means the same angle.
        var turn = 0f
        for (heading in listOf(90f, 180f, 270f, 359f, 1f, 90f, 180f, 270f, 359f, 1f)) {
            val next = continuousTurn(turn, -heading)
            assertTrue("heading $heading moved the ring ${next - turn}", kotlin.math.abs(next - turn) <= 180f)
            assertEquals(0.0, apart(next.toDouble(), -heading.toDouble()), 0.01)
            turn = next
        }
        assertEquals(-721f, turn, 0.01f)
    }
}
