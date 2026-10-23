package com.sajdatime.wear

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The watch's place rules (HANDOVER §5.27) through the real DataStore, on a watch. WearLocationTest
 * covers the rules; this covers the store that applies them, which no emulator can reach by tapping,
 * because "Same as phone" needs a phone sync and an emulator pair cannot sync.
 *
 * Writes the app's own settings, so clear them first:
 * `adb shell pm clear com.sajdatime.app`, then
 * `adb shell am instrument -w com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`.
 */
@RunWith(AndroidJUnit4::class)
class WearSettingsDeviceTest {

    private val store = WearSettingsStore(InstrumentationRegistry.getInstrumentation().targetContext)
    private val manchester = Coordinates(53.4808, -2.2426)
    private val lahore = Coordinates(31.5204, 74.3587)
    private val karachi = Coordinates(24.8607, 67.0011)

    private fun now() = runBlocking { store.settings.first() }

    private fun phoneSends(place: Coordinates, city: String) = runBlocking {
        store.applyFromPhone(null, null, null, place.latitude, place.longitude, city)
    }

    @Test
    fun theWatchFollowsThePhoneUnlessTheWearerChoosesOtherwise() = runBlocking {
        // A watch with no phone yet uses its own fix.
        assertTrue(store.adoptWatchFix(manchester))
        assertEquals(manchester, now().coordinates)

        // Automatic: the phone's place wins, and the watch's next fix no longer replaces it.
        phoneSends(lahore, "Lahore")
        assertEquals(lahore, now().coordinates)
        assertEquals("Lahore", now().cityName)
        assertFalse(store.adoptWatchFix(manchester))
        assertEquals("Lahore", now().cityName)

        // This watch: its fix is used, and a later phone sync is kept aside, not applied.
        store.chooseLocation(LocationSource.WATCH)
        assertTrue(store.adoptWatchFix(manchester))
        phoneSends(karachi, "Karachi")
        assertEquals(manchester, now().coordinates)
        assertEquals("Karachi", now().phoneCityName)

        // Same as phone: back to the phone's latest place at once.
        store.chooseLocation(LocationSource.PHONE)
        assertEquals(karachi, now().coordinates)
        assertEquals("Karachi", now().cityName)

        // Makkah stays Makkah through a phone sync and a watch fix.
        store.chooseLocation(LocationSource.MAKKAH)
        phoneSends(lahore, "Lahore")
        assertFalse(store.adoptWatchFix(manchester))
        assertEquals(QiblaEngine.KAABA, now().coordinates)
        assertEquals("Makkah", now().cityName)
        assertEquals("Lahore", now().phoneCityName)
    }
}
