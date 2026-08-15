package com.sajdatime.core

import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Qibla bearings, checked against the Aladhan Qibla API — an independent implementation
 * of the same great-circle calculation. Values are pinned here so the suite stays
 * offline. Regenerate with:
 *   curl "https://api.aladhan.com/v1/qibla/<lat>/<lng>"
 *
 * Tolerance is 0.5 degrees. The two implementations currently agree to about a hundredth
 * of a degree; the margin only absorbs differences in the chosen Kaaba coordinate.
 */
class QiblaEngineTest {

    private fun assertBearing(name: String, from: Coordinates, expected: Double) {
        val actual = QiblaEngine.bearingToKaaba(from)
        val error = abs(QiblaEngine.relativeTurn(actual, expected))
        assertTrue(
            "$name: expected ~%.3f but was %.3f (%.3f degrees off)".format(expected, actual, error),
            error <= 0.5,
        )
    }

    @Test
    fun `bearings match the reference implementation worldwide`() {
        // Spread across both hemispheres and either side of Makkah, including points
        // where the shortest path runs nowhere near the flat-map direction.
        assertBearing("London", Coordinates(51.5074, -0.1278), 118.987)
        assertBearing("Manchester", Coordinates(53.4808, -2.2426), 118.455)
        assertBearing("New York", Coordinates(40.7128, -74.0060), 58.482)
        assertBearing("Jakarta", Coordinates(-6.2088, 106.8456), 295.152)
        assertBearing("Sydney", Coordinates(-33.8688, 151.2093), 277.500)
        assertBearing("Cape Town", Coordinates(-33.9249, 18.4241), 23.353)
        assertBearing("Tokyo", Coordinates(35.6762, 139.6503), 292.999)
        assertBearing("Karachi", Coordinates(24.8607, 67.0011), 267.741)
        assertBearing("Cairo", Coordinates(30.0444, 31.2357), 136.137)
        assertBearing("Toronto", Coordinates(43.6532, -79.3832), 54.581)
    }

    /**
     * Why great-circle rather than flat-map trigonometry. Treating latitude and longitude
     * as if they were flat x/y gives London a bearing of about 127 degrees against the
     * correct 119 — an 8 degree error, roughly the width of the Kaaba's courtyard seen
     * from that distance, and it grows with latitude.
     */
    @Test
    fun `great circle bearing differs from naive flat map bearing`() {
        val london = Coordinates(51.5074, -0.1278)
        val greatCircle = QiblaEngine.bearingToKaaba(london)

        val flatMap = QiblaEngine.normalise(
            Math.toDegrees(
                kotlin.math.atan2(
                    QiblaEngine.KAABA.longitude - london.longitude,
                    QiblaEngine.KAABA.latitude - london.latitude,
                ),
            ),
        )

        val divergence = abs(QiblaEngine.relativeTurn(greatCircle, flatMap))
        assertTrue(
            "Expected a meaningful divergence, got great-circle $greatCircle vs flat $flatMap",
            divergence > 5.0,
        )
    }

    @Test
    fun `standing at the kaaba is degenerate but does not crash`() {
        val bearing = QiblaEngine.bearingToKaaba(QiblaEngine.KAABA)
        assertTrue("Bearing must stay in range, was $bearing", bearing in 0.0..360.0)
    }

    @Test
    fun `declination correction shifts the bearing by exactly the declination`() {
        val trueBearing = 118.99
        // UK declination is roughly 1 degree west, i.e. negative.
        assertEquals(119.99, QiblaEngine.trueToMagnetic(trueBearing, -1.0), 0.001)
        assertEquals(108.99, QiblaEngine.trueToMagnetic(trueBearing, 10.0), 0.001)
    }

    @Test
    fun `relative turn takes the short way round the circle`() {
        assertEquals(10.0, QiblaEngine.relativeTurn(350.0, 0.0), 0.001)
        assertEquals(-10.0, QiblaEngine.relativeTurn(0.0, 350.0), 0.001)
        assertEquals(180.0, abs(QiblaEngine.relativeTurn(0.0, 180.0)), 0.001)
        assertTrue(QiblaEngine.relativeTurn(0.0, 90.0) > 0)
        assertTrue(QiblaEngine.relativeTurn(90.0, 0.0) < 0)
    }

    @Test
    fun `alignment tolerance works across the wrap point`() {
        assertTrue(QiblaEngine.isAligned(current = 359.0, target = 1.0))
        assertTrue(QiblaEngine.isAligned(current = 1.0, target = 359.0))
        assertFalse(QiblaEngine.isAligned(current = 350.0, target = 10.0))
    }

    @Test
    fun `distance to the kaaba matches known values`() {
        // Published great-circle distances, tolerance 25km for coordinate variation.
        assertEquals(4780.0, QiblaEngine.distanceToKaabaKm(Coordinates(51.5074, -0.1278)), 25.0)
        assertEquals(0.0, QiblaEngine.distanceToKaabaKm(QiblaEngine.KAABA), 1.0)
        assertEquals(13240.0, QiblaEngine.distanceToKaabaKm(Coordinates(-33.8688, 151.2093)), 60.0)
    }

    /**
     * The hysteresis is the whole point, so it is pinned rather than left to be tidied
     * into a single tolerance by a later reader who sees two constants and assumes one is
     * redundant. Collapsing them buzzed the owner's phone 26 times in 76 seconds while it
     * lay untouched on a desk — see [QiblaEngine.staysAligned].
     */
    @Test
    fun `arriving takes five degrees but leaving takes eight`() {
        // Coming in: 6 degrees off is not yet arrival, 5 is.
        assertFalse(QiblaEngine.staysAligned(wasAligned = false, current = 159.0, target = 153.0))
        assertTrue(QiblaEngine.staysAligned(wasAligned = false, current = 158.0, target = 153.0))

        // Already there: the same 6 degrees does not throw you out again. This is the exact
        // band the S23 Ultra was wobbling across.
        assertTrue(QiblaEngine.staysAligned(wasAligned = true, current = 159.0, target = 153.0))
        assertTrue(QiblaEngine.staysAligned(wasAligned = true, current = 161.0, target = 153.0))

        // But a real turn away still registers.
        assertFalse(QiblaEngine.staysAligned(wasAligned = true, current = 162.0, target = 153.0))
        assertFalse(QiblaEngine.staysAligned(wasAligned = true, current = 167.0, target = 153.0))
    }

    @Test
    fun `the release angle is wider than the grab angle`() {
        // If these ever meet, the hysteresis is gone and the buzzing is back.
        assertTrue(QiblaEngine.ALIGN_RELEASE_DEGREES > QiblaEngine.ALIGN_GRAB_DEGREES)
    }

    @Test
    fun `hysteresis works across the 360 degree seam`() {
        // The wobble does not care that the Qibla happens to sit near north.
        // 357 -> 2 is a 5 degree turn: inside the grab angle either way.
        assertTrue(QiblaEngine.staysAligned(wasAligned = false, current = 357.0, target = 2.0))
        // 355 -> 2 is 7 degrees: too far to arrive, near enough to stay.
        assertFalse(QiblaEngine.staysAligned(wasAligned = false, current = 355.0, target = 2.0))
        assertTrue(QiblaEngine.staysAligned(wasAligned = true, current = 355.0, target = 2.0))
        // 353 -> 2 is 9 degrees, past the release angle, so it is a genuine turn away.
        assertFalse(QiblaEngine.staysAligned(wasAligned = true, current = 353.0, target = 2.0))
    }

    @Test
    fun `normalise always returns zero to 360`() {
        listOf(-720.0, -181.0, -0.5, 0.0, 359.9, 360.0, 720.5).forEach { input ->
            val result = QiblaEngine.normalise(input)
            assertTrue("normalise($input) = $result out of range", result >= 0.0 && result < 360.0)
        }
    }
}
