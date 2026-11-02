package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerSlot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Not a check. Two seeds for the release script, which runs each by name
 * (`-e class com.sajdatime.app.ScreenSeedDeviceTest#<method>`), never by the normal connected run
 * (which excludes the class) and never on the owner's phone:
 *  - `seed_london_and_skip_the_first_run_screens`: puts a fresh emulator past onboarding with London as its
 *    place, so screenshots show the Times screen, the Ramadan card and the sheets, not the first run screens.
 *  - `seed_store_alert_settings`: sets the alert and adhan choices that store picture 07 shows.
 *
 * It asserts only that what it wrote can be read back.
 */
@RunWith(AndroidJUnit4::class)
class ScreenSeedDeviceTest {

    @Test
    fun seed_london_and_skip_the_first_run_screens() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SettingsRepository(context)
        runBlocking {
            repository.setLocation(Coordinates(51.5074, -0.1278), "London")
            repository.completeOnboarding()
            // Answer the usage counts question with No, as a person who tapped "No thanks" would have. Without
            // this the question sits on top of every screenshot. Nothing is sent: No only records the answer.
            repository.setAnalyticsEnabled(false)
            // The seed leaves the method on Automatic, which would show the loud "Does this match your
            // mosque?" notice; dismissing it gives the quiet door every store picture shows.
            repository.dismissMethodNotice()
            assertEquals("London", repository.current().cityName)
            assertTrue(repository.current().analyticsAnswered)
        }
    }

    /**
     * Exactly the settings behind store picture 07: Fajr alarm, the other four prayers notification, Alarm
     * sound Adhan, prayer reminder 15 minutes before all five. Nothing else is written, except that choosing
     * the built in adhan while the current sound is the phone's also saves that phone sound (PHONE_ALARM_SOUND),
     * as the app itself does.
     */
    @Test
    fun seed_store_alert_settings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = SettingsRepository(context)
        val five = listOf(PrayerSlot.FAJR, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.MAGHRIB, PrayerSlot.ISHA)
        runBlocking {
            five.forEach { repository.setAlert(it, if (it == PrayerSlot.FAJR) AlertStyle.ALARM else AlertStyle.NOTIFICATION) }
            repository.setBuiltInAdhan(BuiltInAdhan.ADHAN)
            repository.setPrayerReminderMinutes(15)
            five.forEach { repository.setPrayerReminder(it, true) }
            val settings = repository.current()
            assertEquals(15, settings.prayerReminderMinutes)
            assertEquals(BuiltInAdhan.ADHAN.uri(context.packageName), settings.alarmSoundUri)
        }
    }
}
