package com.sajdatime.wear

import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.WatchSyncContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class WearSettingsTest {

    private val manchester = Coordinates(latitude = 53.4808, longitude = -2.2426)
    private val date = LocalDate.of(2026, 6, 21)

    /**
     * The whole point of the watch app is that it is not a mirror: it recalculates. If the
     * settings that arrive from the phone do not reach the engine intact, the watch shows
     * confidently wrong times with no indication anything is off.
     */
    @Test
    fun `watch settings reach the engine and match a direct calculation`() {
        val watch = WearSettings(
            sect = Sect.SHIA,
            madhab = Madhab.HANAFI,
            method = CalcMethod.JAFARI,
            coordinates = manchester,
            cityName = "Manchester",
        )

        val fromWatch = PrayerEngine.compute(manchester, date, watch.calculationPrefs)
        val direct = PrayerEngine.compute(
            manchester,
            date,
            CalculationPrefs(sect = Sect.SHIA, madhab = Madhab.HANAFI, method = CalcMethod.JAFARI),
        )

        assertEquals(direct, fromWatch)
    }

    @Test
    fun `a watch that has never been paired still calculates with defaults`() {
        val unpaired = WearSettings()
        assertEquals(Sect.SUNNI, unpaired.sect)
        assertEquals(Madhab.SHAFII, unpaired.madhab)
        assertEquals(CalcMethod.AUTO, unpaired.method)
        // No coordinates means the UI shows a prompt rather than guessing a location.
        assertEquals(null, unpaired.coordinates)

        assertNotNull(PrayerEngine.compute(manchester, date, unpaired.calculationPrefs))
    }

    /**
     * Sect drives the Maghrib rule, so this is the setting most likely to be noticed if
     * the sync silently drops it.
     */
    @Test
    fun `sect actually changes what the watch would display`() {
        val sunni = WearSettings(sect = Sect.SUNNI, method = CalcMethod.MUSLIM_WORLD_LEAGUE)
        val shia = WearSettings(sect = Sect.SHIA, method = CalcMethod.JAFARI)

        val sunniTimes = PrayerEngine.compute(manchester, date, sunni.calculationPrefs)
        val shiaTimes = PrayerEngine.compute(manchester, date, shia.calculationPrefs)

        // Jafari holds Maghrib until the redness passes, so it is always later than sunset.
        assertEquals(
            true,
            shiaTimes[PrayerSlot.MAGHRIB].isAfter(sunniTimes[PrayerSlot.MAGHRIB]),
        )
    }

    /**
     * Guards the phone-to-watch wire format. These strings are the only thing holding the
     * two modules together and a rename on one side is otherwise silent.
     */
    @Test
    fun `sync contract keys are stable`() {
        assertEquals("/sajdatime/settings", WatchSyncContract.PATH_SETTINGS)
        assertEquals("sect", WatchSyncContract.KEY_SECT)
        assertEquals("madhab", WatchSyncContract.KEY_MADHAB)
        assertEquals("method", WatchSyncContract.KEY_METHOD)
        assertEquals("latitude", WatchSyncContract.KEY_LATITUDE)
        assertEquals("longitude", WatchSyncContract.KEY_LONGITUDE)
        assertEquals("city", WatchSyncContract.KEY_CITY)
        assertEquals("updated_at", WatchSyncContract.KEY_UPDATED_AT)
    }
}
