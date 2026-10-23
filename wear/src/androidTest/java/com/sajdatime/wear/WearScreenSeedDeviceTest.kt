package com.sajdatime.wear

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Not a check. Hands the watch the place a paired phone would send (Manchester, as in the store
 * screenshots), so the times list and the Location screen can be captured as a paired watch shows them;
 * an emulator pair cannot sync. Skipped unless asked for, so a plain run of the watch's device tests
 * never writes it. Clear the app first (`adb shell pm clear com.sajdatime.app`), or a watch left on Makkah
 * by WearSettingsDeviceTest keeps Makkah:
 * `adb shell am instrument -w -e class com.sajdatime.wear.WearScreenSeedDeviceTest -e seed true
 * com.sajdatime.app.test/androidx.test.runner.AndroidJUnitRunner`. Never on a real watch.
 */
@RunWith(AndroidJUnit4::class)
class WearScreenSeedDeviceTest {

    @Test
    fun seedThePhonesPlace() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("seed") == "true")
        val store = WearSettingsStore(InstrumentationRegistry.getInstrumentation().targetContext)
        store.applyFromPhone("SUNNI", "SHAFII", "AUTO", 53.4808, -2.2426, "Manchester")
        assertEquals("Manchester", store.settings.first().cityName)
    }
}
