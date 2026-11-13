package com.sajdatime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.AppSettings
import com.sajdatime.core.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.qibla.QiblaBody
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.core.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The arrival buzz, counted in the phone's own vibration log: one on turning to face the Kaaba, none while
 * the phone rests there with a compass that wobbles, one more after really turning away and back.
 *
 * Written 12 Nov 2026. The rules are in `QiblaEngine.staysAligned` and `QiblaLocated`, and the wobble is the
 * one measured on the S23 Ultra on 16 Aug 2026 (it buzzed a phone nobody had touched). A phone lying on a
 * desk cannot be turned by a test, so the Qibla screen is given its headings; the vibration is real, and it
 * must be logged as played ("finished"), not as ignored: a phone with media vibration switched off fails
 * here, and should.
 *
 * `-e qiblaStillSeconds 60` sets how long the phone rests on the Kaaba (10 by default).
 */
@RunWith(AndroidJUnit4::class)
class QiblaArrivalBuzzDeviceTest {

    @get:Rule val compose = createComposeRule()

    private val qibla = 118.99
    // Never under 10: no buzz can follow another within 5 seconds whatever happens, so only the rest of the
    // wait proves anything.
    private val still = maxOf(10, InstrumentationRegistry.getArguments().getString("qiblaStillSeconds")?.toIntOrNull() ?: 10)

    /** Every 70 ms vibration of this app the log says was played. A set of lines, since the log is a ring. */
    private fun buzzes(): Set<String> {
        val app = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        return DeviceSupport.shell("dumpsys vibrator_manager").lines()
            .filter { "$app (uid" in it && "original: Step=70ms" in it && "finished" in it }
            .map { it.trim().substringBefore(" |") } // the time it was asked for
            .toSet()
    }

    @Test
    fun one_buzz_on_arrival_none_while_resting_there_and_one_more_after_turning_away_and_back() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assumeTrue("no vibrator on this device", context.getSystemService(android.os.Vibrator::class.java)?.hasVibrator() == true)
        var heading by mutableDoubleStateOf(qibla)
        var accuracy by mutableStateOf(CompassAccuracy.UNKNOWN)
        compose.setContent {
            SajdaTimeTheme {
                Box(Modifier.width(360.dp)) {
                    QiblaBody(
                        state = UiState(
                            loading = false,
                            settings = AppSettings(
                                onboardingComplete = true,
                                coordinates = Coordinates(51.5074, -0.1278),
                                cityName = "London",
                            ),
                            compassHeading = heading,
                            compassAccuracy = accuracy,
                            qiblaBearing = qibla,
                            qiblaDistanceKm = 4788.0,
                        ),
                        onUseGps = {},
                        onSearchCity = {},
                    )
                }
            }
        }
        fun turnTo(degrees: Double) {
            compose.runOnIdle { heading = degrees }
            compose.waitForIdle()
        }
        compose.waitForIdle()
        val before = buzzes()

        // Starting aligned and recovering trust in place are both silent.
        compose.runOnIdle { accuracy = CompassAccuracy.HIGH }
        compose.waitForIdle()
        Thread.sleep(500)
        assertEquals("buzzes at the first trusted heading", 0, (buzzes() - before).size)

        turnTo(qibla - 20.0)
        turnTo(qibla - 2.0)
        Thread.sleep(500)
        assertEquals("buzzes on arriving", 1, (buzzes() - before).size)

        // The original measured wobble is wider than the visual release, but cannot rearm the haptic.
        // Four readings a second, long enough to exceed the five-second quiet period.
        val wobble = listOf(0.0, 6.0, -7.0, 3.0, -6.5, 7.0, -1.0, 5.5)
        repeat(still * 4) {
            turnTo(qibla + wobble[it % wobble.size])
            Thread.sleep(250)
        }
        assertEquals("buzzes while resting on the Kaaba for $still seconds", 1, (buzzes() - before).size)

        turnTo(qibla + 40.0)
        compose.runOnIdle { accuracy = CompassAccuracy.LOW }
        turnTo(qibla)
        compose.runOnIdle { accuracy = CompassAccuracy.HIGH }
        compose.waitForIdle()
        Thread.sleep(500)
        assertEquals("buzzes while untrusted or recovering", 1, (buzzes() - before).size)

        // Really turning away and coming back is a new arrival.
        turnTo(qibla + 40.0)
        Thread.sleep(maxOf(0L, 5_500L - still * 1_000L))
        turnTo(qibla + 1.0)
        Thread.sleep(500)
        assertEquals("buzzes after turning away and back", 2, (buzzes() - before).size)
    }
}
