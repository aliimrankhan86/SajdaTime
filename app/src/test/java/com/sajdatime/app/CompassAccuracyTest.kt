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
    fun `the Redmi's field that pointed up instead of down is caught though its strength passed`() {
        // Redmi Note 13 Pro on its desk, 13 Nov 2026, lying flat and face up: raw field and identity rotation
        // (flat, so device axes are the world axes up to heading, which dip does not depend on).
        val flat = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
        val measured = CompassAccuracy.fieldDipDegrees(flat, -36.7f, -48.2f, 20.0f)
        assertEquals(-18.3, measured, 0.5)
        assertFalse(CompassAccuracy.fieldLooksDisturbed(63.8f, 49f))
        assertTrue(CompassAccuracy.dipLooksWrong(measured, expected = 67.0, wasWrong = false))
        // An honest UK field flat on a table: 19 horizontal, 45 down (z is up, so down is negative z).
        val honest = CompassAccuracy.fieldDipDegrees(flat, 0f, 19f, -45f)
        assertEquals(67.1, honest, 0.5)
        assertFalse(CompassAccuracy.dipLooksWrong(honest, expected = 67.0, wasWrong = false))
        // A building's few degrees pass; the edge does not flicker.
        assertFalse(CompassAccuracy.dipLooksWrong(55.0, 67.0, wasWrong = false))
        assertTrue(CompassAccuracy.dipLooksWrong(46.0, 67.0, wasWrong = false))
        assertTrue(CompassAccuracy.dipLooksWrong(52.0, 67.0, wasWrong = true))
        assertFalse(CompassAccuracy.dipLooksWrong(56.0, 67.0, wasWrong = true))
    }

    @Test
    fun `a disturbed field is never called high, and never hides a call to calibrate`() {
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = high, disturbed = true))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = null, magnet = null, disturbed = true))
        assertEquals(CompassAccuracy.LOW, accuracy(vector = high, magnet = low, disturbed = true))
        assertEquals(CompassAccuracy.HIGH, accuracy(vector = high, magnet = high, disturbed = false))
    }
}
