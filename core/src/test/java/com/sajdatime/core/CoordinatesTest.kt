package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * [Coordinates.orNull] is the gate on every persisted or received position, so the values
 * it must reject are worth pinning down. The watch's Data Layer listener is an exported
 * component and cannot be anything else; a latitude it accepts is a prayer time the user
 * is shown.
 */
class CoordinatesTest {

    @Test
    fun `accepts real positions, including the extremes`() {
        assertEquals(Coordinates(51.5, -0.6), Coordinates.orNull(51.5, -0.6))
        assertEquals(Coordinates(0.0, 0.0), Coordinates.orNull(0.0, 0.0))
        assertEquals(Coordinates(90.0, 180.0), Coordinates.orNull(90.0, 180.0))
        assertEquals(Coordinates(-90.0, -180.0), Coordinates.orNull(-90.0, -180.0))
    }

    @Test
    fun `rejects anything off the globe`() {
        assertNull(Coordinates.orNull(90.001, 0.0))
        assertNull(Coordinates.orNull(-90.001, 0.0))
        assertNull(Coordinates.orNull(0.0, 180.001))
        assertNull(Coordinates.orNull(0.0, -180.001))
        assertNull(Coordinates.orNull(999.0, 999.0))
    }

    @Test
    fun `rejects NaN and infinity rather than passing them to the engine`() {
        // These reach the range check without failing it in the obvious way: every
        // comparison against NaN is false, so it is rejected by falling out of the range
        // rather than by being caught. Worth a test precisely because it looks accidental.
        assertNull(Coordinates.orNull(Double.NaN, 0.0))
        assertNull(Coordinates.orNull(0.0, Double.NaN))
        assertNull(Coordinates.orNull(Double.POSITIVE_INFINITY, 0.0))
        assertNull(Coordinates.orNull(0.0, Double.NEGATIVE_INFINITY))
    }

    @Test
    fun `a missing half means no location, not half a location`() {
        assertNull(Coordinates.orNull(null, 0.0))
        assertNull(Coordinates.orNull(51.5, null))
        assertNull(Coordinates.orNull(null, null))
    }
}
