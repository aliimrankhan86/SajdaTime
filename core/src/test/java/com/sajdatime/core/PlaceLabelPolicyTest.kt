package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceLabelPolicyTest {

    private val slough = Coordinates(51.5105, -0.5950)

    @Test
    fun `first ever fix asks for a label`() {
        assertTrue(PlaceLabelPolicy.needsNewLabel(null, "", slough))
        assertTrue(PlaceLabelPolicy.needsNewLabel(null, "Slough", slough))
    }

    @Test
    fun `a stored position with no label asks again`() {
        // The geocoder came back empty last time (offline, or no backend). Try again.
        assertTrue(PlaceLabelPolicy.needsNewLabel(slough, "", slough))
        assertTrue(PlaceLabelPolicy.needsNewLabel(slough, "   ", slough))
    }

    @Test
    fun `staying put keeps the label and asks nothing`() {
        assertFalse(PlaceLabelPolicy.needsNewLabel(slough, "Slough", slough))
        // About 1 km north: still the same town label.
        assertFalse(PlaceLabelPolicy.needsNewLabel(slough, "Slough", Coordinates(51.5195, -0.5950)))
    }

    @Test
    fun `moving 5 km or more asks for a new label`() {
        // 0.05 degrees of latitude is about 5.56 km.
        assertTrue(PlaceLabelPolicy.needsNewLabel(slough, "Slough", Coordinates(51.5605, -0.5950)))
        // London to Slough is about 30 km.
        assertTrue(PlaceLabelPolicy.needsNewLabel(Coordinates(51.5074, -0.1278), "London", slough))
    }

    @Test
    fun `distance helper agrees with the known London to Slough gap`() {
        assertEquals(
            30.0,
            QiblaEngine.distanceKm(Coordinates(51.5074, -0.1278), slough),
            3.0,
        )
        assertEquals(0.0, QiblaEngine.distanceKm(slough, slough), 0.0001)
    }

    @Test
    fun `distance to the Kaaba still delegates to the same maths`() {
        val london = Coordinates(51.5074, -0.1278)
        assertEquals(
            QiblaEngine.distanceKm(london, QiblaEngine.KAABA),
            QiblaEngine.distanceToKaabaKm(london),
            0.0,
        )
    }
}
