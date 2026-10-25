package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.core.JamaahRule
import com.sajdatime.core.PrayerSlot
import java.time.LocalTime
import java.util.TimeZone
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every jama'ah save stamps the phone's zone (STEP-7-DESIGN §4), so the times hide abroad and come back at home.
 * Mutation proof: drop `zoneId = ...` from either save in `SettingsRepository` and the matching half fails.
 */
@RunWith(AndroidJUnit4::class)
class JamaahZoneStampDeviceTest {

    @Test
    fun both_jamaah_saves_stamp_the_phone_zone() {
        val repository = SettingsRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        val home = TimeZone.getDefault()
        // Each save runs under its own zone, so a save that forgot to stamp keeps the other's zone and fails.
        try {
            runBlocking {
                TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
                repository.setJamaahRule(PrayerSlot.DHUHR, JamaahRule.Fixed(LocalTime.of(13, 30)))
                assertEquals("Asia/Tokyo", repository.current().jamaah.zoneId)
                TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"))
                repository.setJumuah(LocalTime.of(13, 15))
                assertEquals("Pacific/Auckland", repository.current().jamaah.zoneId)
            }
        } finally {
            TimeZone.setDefault(home)
            // Leave the emulator as other tests expect: no jama'ah times, stamped with the phone's own zone.
            runBlocking {
                repository.setJamaahRule(PrayerSlot.DHUHR, null)
                repository.setJumuah(null)
            }
        }
    }
}
