package com.sajdatime.core

import kotlin.math.roundToInt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MeasurementSystemTest {
    @Test fun milesRegionsUseMiles() {
        for (c in listOf("US", "GB", "LR", "MM", "gb")) assertTrue(c, MeasurementSystem.usesMiles(c))
    }

    @Test fun otherRegionsAndEmptyUseKm() {
        for (c in listOf("PK", "TR", "ID", "SA", "")) assertFalse(c, MeasurementSystem.usesMiles(c))
    }

    @Test fun milesFromKm() {
        assertEquals(2979, MeasurementSystem.milesFromKm(4794.0).roundToInt())
        assertEquals(0.0, MeasurementSystem.milesFromKm(0.0), 0.0)
    }
}
