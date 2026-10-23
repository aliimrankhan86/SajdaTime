package com.sajdatime.wear

import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rules for which place the watch uses. Before 1.5.0 there were none: the last writer won, so
 * the watch's own fix replaced a city chosen on the phone every time the app opened, and nothing on
 * the watch could choose or even show the place (owner's report, 22 Oct 2026).
 */
class WearLocationTest {

    private val manchester = Coordinates(53.4808, -2.2426)
    private val lahore = Coordinates(31.5204, 74.3587)

    @Test
    fun `automatic follows the phone once it has sent a place`() {
        assertTrue(acceptsPhoneLocation(null, lahore, current = manchester))
        assertTrue(acceptsPhoneLocation(null, lahore, current = null))
        // The bug: on open, the watch's own fix used to overwrite the phone's chosen city.
        assertFalse(acceptsWatchFix(null, phoneHasRealPlace = true))
    }

    /**
     * The phone sends its own Makkah fallback like any place (location off on the phone, say). Followed
     * blindly it would pin a watch with working GPS to Makkah's times: the review's finding, 22 Oct 2026.
     */
    @Test
    fun `the phone's Makkah fallback does not pin a watch that can find itself`() {
        assertFalse(isRealPlace(QiblaEngine.KAABA))
        assertFalse(isRealPlace(null))
        assertTrue(isRealPlace(lahore))
        // A watch with its own place keeps it, and goes on taking its own fixes.
        assertFalse(acceptsPhoneLocation(null, QiblaEngine.KAABA, current = manchester))
        assertTrue(acceptsWatchFix(null, phoneHasRealPlace = isRealPlace(QiblaEngine.KAABA)))
        // A watch with nothing, or with Makkah already, takes it: better than nothing.
        assertTrue(acceptsPhoneLocation(null, QiblaEngine.KAABA, current = null))
        assertTrue(acceptsPhoneLocation(null, QiblaEngine.KAABA, current = QiblaEngine.KAABA))
        // Shown as what is in use, not as "Same as phone".
        assertEquals(
            LocationSource.WATCH,
            shownSource(WearSettings(coordinates = manchester, phoneCoordinates = QiblaEngine.KAABA)),
        )
        // Chosen on purpose, the phone's place is followed whatever it is.
        assertTrue(acceptsPhoneLocation(LocationSource.PHONE, QiblaEngine.KAABA, current = manchester))
    }

    @Test
    fun `automatic uses the watch's own fix while the phone has sent nothing`() {
        // A watch used without the phone app must still find its place by itself.
        assertTrue(acceptsWatchFix(null, phoneHasRealPlace = false))
    }

    @Test
    fun `choosing this watch keeps the phone's place out`() {
        assertFalse(acceptsPhoneLocation(LocationSource.WATCH, lahore, current = manchester))
        assertFalse(acceptsPhoneLocation(LocationSource.WATCH, lahore, current = null))
        assertTrue(acceptsWatchFix(LocationSource.WATCH, phoneHasRealPlace = true))
        assertTrue(acceptsWatchFix(LocationSource.WATCH, phoneHasRealPlace = false))
    }

    @Test
    fun `choosing the phone keeps the watch's fix out`() {
        assertTrue(acceptsPhoneLocation(LocationSource.PHONE, lahore, current = manchester))
        assertFalse(acceptsWatchFix(LocationSource.PHONE, phoneHasRealPlace = true))
        assertFalse(acceptsWatchFix(LocationSource.PHONE, phoneHasRealPlace = false))
    }

    @Test
    fun `choosing Makkah is kept until the wearer chooses again`() {
        assertFalse(acceptsPhoneLocation(LocationSource.MAKKAH, lahore, current = QiblaEngine.KAABA))
        assertFalse(acceptsWatchFix(LocationSource.MAKKAH, phoneHasRealPlace = false))
        assertFalse(acceptsWatchFix(LocationSource.MAKKAH, phoneHasRealPlace = true))
    }

    @Test
    fun `an explicit choice is shown as chosen`() {
        val settings = WearSettings(coordinates = manchester, phoneCoordinates = lahore)
        for (source in LocationSource.entries) {
            assertEquals(source, shownSource(settings.copy(locationSource = source)))
        }
    }

    @Test
    fun `automatic is shown as what it is doing`() {
        assertEquals(
            LocationSource.PHONE,
            shownSource(WearSettings(coordinates = lahore, phoneCoordinates = lahore)),
        )
        assertEquals(LocationSource.WATCH, shownSource(WearSettings(coordinates = manchester)))
        // The "No location yet" fallback: Makkah until the watch finds itself.
        assertEquals(
            LocationSource.MAKKAH,
            shownSource(WearSettings(coordinates = QiblaEngine.KAABA, cityName = "Makkah")),
        )
        assertNull(shownSource(WearSettings()))
    }

    @Test
    fun `the place label names the place or says what kind it is`() {
        assertNull(placeLabel(null, "Manchester"))
        assertEquals(PlaceLabel.Named("Manchester"), placeLabel(manchester, "Manchester"))
        // A watch fix has no name: the watch has no geocoder, on purpose.
        assertEquals(PlaceLabel.Unnamed, placeLabel(manchester, ""))
        assertEquals(PlaceLabel.Unnamed, placeLabel(manchester, "  "))
        // Makkah by its coordinates, so it is shown in the watch's language whatever was stored.
        assertEquals(PlaceLabel.Makkah, placeLabel(QiblaEngine.KAABA, "Makkah"))
        assertEquals(PlaceLabel.Makkah, placeLabel(QiblaEngine.KAABA, ""))
    }
}
