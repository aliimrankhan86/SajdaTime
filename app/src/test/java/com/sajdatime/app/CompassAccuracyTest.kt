package com.sajdatime.app

import android.hardware.SensorManager
import com.sajdatime.core.CompassAccuracy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The compass never claims an accuracy no sensor reported, and a magnet chip that asks to be calibrated is
 * believed even when the fused rotation vector says nothing (12 Nov 2026: it was started at "high" and many
 * phones never corrected it, so the figure of eight notice could not show).
 */
class CompassAccuracyTest {

    private val high = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
    private val medium = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
    private val low = SensorManager.SENSOR_STATUS_ACCURACY_LOW
    private val unreliable = SensorManager.SENSOR_STATUS_UNRELIABLE

    private fun accuracy(vector: Int?, magnet: Int?, disturbed: Boolean = false) =
        CompassAccuracy.fromSensorReports(vector, magnet, disturbed)

    @Test
    fun `only trusted readings can give direction or confirm arrival`() {
        for (quality in listOf(CompassAccuracy.LOW, CompassAccuracy.UNKNOWN)) {
            assertTrue(quality.needsCalibration)
            assertFalse(quality.allowsHeading)
            assertFalse(quality.allowsArrival)
        }
        assertTrue(CompassAccuracy.MEDIUM.allowsHeading)
        assertFalse(CompassAccuracy.MEDIUM.allowsArrival)
        assertTrue(CompassAccuracy.HIGH.allowsArrival)
    }

    @Test
    fun `nothing reported is unknown, not high`() {
        assertEquals(CompassAccuracy.UNKNOWN, accuracy(vector = null, magnet = null))
    }

    @Test
    fun `a disturbed magnet chip is believed when the rotation vector is silent or content`() {
        assertEquals(CompassAccuracy.LOW, accuracy(vector = null, magnet = low))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = null, magnet = unreliable))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = unreliable))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = SensorManager.SENSOR_STATUS_NO_CONTACT))
        assertEquals(CompassAccuracy.MEDIUM, accuracy(vector = high, magnet = medium))
    }

    @Test
    fun `the worse of the two wins, whichever it is`() {
        assertEquals(CompassAccuracy.LOW, accuracy(vector = low, magnet = high))
        assertEquals(CompassAccuracy.MEDIUM, accuracy(vector = medium, magnet = null))
        assertEquals(CompassAccuracy.HIGH, accuracy(vector = high, magnet = high))
        assertEquals(CompassAccuracy.HIGH, accuracy(vector = null, magnet = high))
    }

    @Test
    fun `a field of the wrong strength is disturbed, a building's worth of difference is not`() {
        // The owner's desk, 12 Nov 2026: 88 microtesla where Slough has 49.
        assertTrue(CompassAccuracy.fieldLooksDisturbed(measured = 88.3f, expected = 49f))
        assertTrue(CompassAccuracy.fieldLooksDisturbed(measured = 25f, expected = 49f))
        assertFalse(CompassAccuracy.fieldLooksDisturbed(measured = 49f, expected = 49f))
        assertFalse(CompassAccuracy.fieldLooksDisturbed(measured = 58f, expected = 49f))
        assertFalse(CompassAccuracy.fieldLooksDisturbed(measured = 40f, expected = 49f))
        // No model to compare with: say nothing.
        assertFalse(CompassAccuracy.fieldLooksDisturbed(measured = 88f, expected = 0f))
    }

    @Test
    fun `a disturbed field is never called high, and never hides a call to calibrate`() {
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = high, disturbed = true))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = null, magnet = null, disturbed = true))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = low, disturbed = true))
        assertEquals(CompassAccuracy.HIGH, accuracy(vector = high, magnet = high, disturbed = false))
    }
}
