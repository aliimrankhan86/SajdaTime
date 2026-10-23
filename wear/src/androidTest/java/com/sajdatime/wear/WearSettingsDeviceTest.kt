package com.sajdatime.wear

import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The watch's place rules (HANDOVER §5.27) through the real DataStore, on a watch. WearLocationTest
 * covers the rules; this covers the store that applies them, which no emulator can reach by tapping,
 * because "Same as phone" needs a phone sync and an emulator pair cannot sync.
 *
 * Clears and writes the app's own settings, so never on a real watch. Run on an emulator with
 * `adb shell am instrument -w -e class com.sajdatime.wear.WearSettingsDeviceTest
 * com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`; tools/wear-verify.sh does.
 */
@RunWith(AndroidJUnit4::class)
class WearSettingsDeviceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val store = WearSettingsStore(context)
    private val manchester = Coordinates(53.4808, -2.2426)
    private val lahore = Coordinates(31.5204, 74.3587)
    private val karachi = Coordinates(24.8607, 67.0011)

    /** Every test starts as a fresh install. */
    @Before
    fun clear() = runBlocking<Unit> { context.dataStore.edit { it.clear() } }

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
        assertFalse(store.adoptWatchFix(manchester))
        assertEquals(karachi, now().coordinates)

        // Makkah stays Makkah through a phone sync and a watch fix.
        store.chooseLocation(LocationSource.MAKKAH)
        phoneSends(lahore, "Lahore")
        assertFalse(store.adoptWatchFix(manchester))
        assertEquals(QiblaEngine.KAABA, now().coordinates)
        assertEquals("Makkah", now().cityName)
        assertEquals("Lahore", now().phoneCityName)
    }

    /** The phone's own Makkah fallback (location off on the phone) must not pin a watch that can find itself. */
    @Test
    fun thePhonesMakkahFallbackDoesNotPinAWatchThatCanFindItself() = runBlocking {
        phoneSends(QiblaEngine.KAABA, "Makkah")
        assertTrue(store.adoptWatchFix(manchester))
        assertEquals(manchester, now().coordinates)
        // And a later real place from the phone still wins, as automatic promises.
        phoneSends(lahore, "Lahore")
        assertEquals(lahore, now().coordinates)
    }

    /**
     * A watch updated from 1.2.0 has the phone's city only in the old keys. Its first fix must not replace
     * it (the bug the owner reported, 22 Oct 2026), and "Same as phone" must know which place it means.
     */
    @Test
    fun aWatchUpdatedFrom120KeepsThePhonesCity() = runBlocking {
        context.dataStore.edit {
            it[WearSettingsStore.Keys.LATITUDE] = lahore.latitude
            it[WearSettingsStore.Keys.LONGITUDE] = lahore.longitude
            it[WearSettingsStore.Keys.CITY] = "Lahore"
        }
        assertFalse(store.adoptWatchFix(manchester))
        assertEquals("Lahore", now().cityName)
        assertEquals("Lahore", now().phoneCityName)
    }
}
