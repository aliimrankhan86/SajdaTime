package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Not a check. Puts a fresh emulator past onboarding with London as its place, so the release script can
 * take screenshots of the Times screen, the Ramadan card and the sheets instead of the first run
 * screens. Run by the script with `-e class com.sajdatime.app.ScreenSeedDeviceTest`, never by the normal
 * connected run (which excludes it), and never on the owner's phone.
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
            repository.markDisclaimerSeen()
            // Answer the usage counts question with No, as a person who tapped "No thanks" would have. Without
            // this the question sits on top of every screenshot. Nothing is sent: No only records the answer.
            repository.setAnalyticsEnabled(false)
            assertEquals("London", repository.current().cityName)
            assertTrue(repository.current().analyticsAnswered)
        }
    }
}
