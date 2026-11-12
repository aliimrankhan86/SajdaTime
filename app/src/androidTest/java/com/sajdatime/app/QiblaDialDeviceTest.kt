package com.sajdatime.app

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.qibla.KAABA_DISTANCE
import com.sajdatime.app.ui.qibla.KAABA_SIZE
import com.sajdatime.app.ui.qibla.QiblaBody
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.core.Coordinates
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Measures the Qibla dial's own pixels: the needle is on the dial's centre line, and the Kaaba mark is
 * drawn where the bearing says, to within a pixel and a half.
 *
 * Written 11 Nov 2026, after Ali reported the direction "seems a bit off" on the day 1.6.0 went live. The
 * emulator's compass cannot be turned (HANDOVER section 15, lesson 182), so the screen is given a heading and
 * a Qibla bearing directly and the picture is measured, in light and dark, at 2.0 text and right to left
 * (the dial must never mirror: a mirrored compass points people the wrong way).
 *
 * What it cannot show: whether a real phone's compass reports the right heading. That is the sensor, not
 * the drawing, and only a phone in the hand can show it.
 */
@RunWith(AndroidJUnit4::class)
class QiblaDialDeviceTest {

    @get:Rule val compose = createComposeRule()

    /** London: the bearing the app works out for it. Clear of every cardinal letter. */
    private val qibla = 118.99

    private fun show(
        heading: Double,
        theme: ThemeChoice = ThemeChoice.LIGHT,
        fontScale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
    ) = compose.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(density.density, fontScale = fontScale),
            LocalLayoutDirection provides direction,
        ) {
            SajdaTimeTheme(choice = theme) {
                Box(Modifier.width(360.dp).background(MaterialTheme.colorScheme.background)) {
                    QiblaBody(
                        state = UiState(
                            loading = false,
                            settings = AppSettings(
                                onboardingComplete = true,
                                coordinates = Coordinates(51.5074, -0.1278),
                                cityName = "London",
                            ),
                            compassHeading = heading,
                            compassAccuracy = CompassAccuracy.HIGH,
                            qiblaBearing = qibla,
                            qiblaDistanceKm = 4788.0,
                        ),
                        onUseGps = {},
                        onSearchCity = {},
                    )
                }
            }
        }
    }

    private fun dial(): Bitmap {
        compose.waitForIdle()
        return compose.onNodeWithTag("qibla_dial", useUnmergedTree = true).captureToImage().asAndroidBitmap()
            .copy(Bitmap.Config.ARGB_8888, false)
    }

    private fun far(a: Int, b: Int): Boolean =
        abs((a shr 16 and 0xff) - (b shr 16 and 0xff)) + abs((a shr 8 and 0xff) - (b shr 8 and 0xff)) +
            abs((a and 0xff) - (b and 0xff)) > 90

    /** The centre of everything that is not the dial's face inside the box centred on ([x], [y]). */
    private fun centreOfInk(picture: Bitmap, x: Float, y: Float, half: Float, face: Int): Pair<Float, Float> {
        var sumX = 0.0
        var sumY = 0.0
        var count = 0
        for (py in (y - half).toInt()..(y + half).toInt()) {
            for (px in (x - half).toInt()..(x + half).toInt()) {
                if (far(picture.getPixel(px, py), face)) {
                    sumX += px + 0.5
                    sumY += py + 0.5
                    count++
                }
            }
        }
        assertTrue("nothing drawn at ($x, $y)", count > 0)
        return (sumX / count).toFloat() to (sumY / count).toFloat()
    }

    /** The middle of the run of pixels that are not the face on row [y], looking [reach] either side of [x]. */
    private fun middleOfRun(picture: Bitmap, x: Float, y: Float, reach: Float, face: Int): Pair<Float, Int> {
        var first = -1
        var last = -1
        for (px in (x - reach).toInt()..(x + reach).toInt()) {
            if (far(picture.getPixel(px, y.toInt()), face)) {
                if (first < 0) first = px
                last = px
            }
        }
        assertTrue("nothing drawn on row $y", first >= 0)
        return (first + last + 1) / 2f to (last - first + 1)
    }

    private fun check(heading: Double, theme: ThemeChoice, fontScale: Float, direction: LayoutDirection) {
        show(heading, theme, fontScale, direction)
        val picture = dial()
        val cx = picture.width / 2f
        val cy = picture.height / 2f
        val r = minOf(picture.width, picture.height) / 2f
        val label = "heading $heading, $theme, text $fontScale, $direction"

        // A clean piece of the face: lower left, inside the letters, away from the needle and the mark
        // (the mark is in the upper half for every heading this test uses).
        val face = picture.getPixel((cx - 0.38f * r).toInt(), (cy + 0.38f * r).toInt())

        // The needle, at three heights: each time the middle of it is the dial's centre line.
        for (share in listOf(0.05f, 0.25f, 0.45f)) {
            val (middle, width) = middleOfRun(picture, cx, cy - share * r, 0.2f * r, face)
            assertTrue("$label: needle is ${middle - cx}px off the centre line at $share", abs(middle - cx) <= 1f)
            assertTrue("$label: needle is ${width}px wide at $share, dial radius ${r}px", width <= 0.16f * r)
        }

        // The Kaaba mark, where the bearing puts it: the dial is turned by minus the heading, so the mark
        // is (Qibla minus heading) clockwise from straight up, at KAABA_DISTANCE of the radius.
        val angle = Math.toRadians(qibla - heading)
        val wantX = cx + KAABA_DISTANCE * r * sin(angle).toFloat()
        val wantY = cy - KAABA_DISTANCE * r * cos(angle).toFloat()
        if (abs(qibla - heading) > 12.0) {
            // Clear of the needle: measure the whole mark. The box holds all of its ink (0.078 of the radius
            // either side, 0.086 below) and stops short of the tick ring.
            val (gotX, gotY) = centreOfInk(picture, wantX, wantY, 0.095f * r, face)
            // The artwork is centred left to right in its box. Top to bottom its ink (the wall, y 3.5 to 20
            // of 24, and the plinth under it) balances 0.59 of a unit below the middle of the box.
            val inkY = wantY + 0.59f / 24f * KAABA_SIZE * r
            assertTrue("$label: Kaaba is ${gotX - wantX}px sideways from its bearing", abs(gotX - wantX) <= 1.5f)
            assertTrue("$label: Kaaba is ${gotY - inkY}px up or down from its bearing", abs(gotY - inkY) <= 1.5f)
        } else {
            // On or near the needle, which runs into it from below: measure the wall on the row through the
            // mark's centre, above the needle's tip.
            val (middle, _) = middleOfRun(picture, wantX, wantY, KAABA_SIZE * r * 0.6f, face)
            assertTrue("$label: Kaaba is ${middle - wantX}px sideways from its bearing", abs(middle - wantX) <= 1.5f)
        }
    }

    @Test
    fun facing_the_kaaba_the_needle_runs_into_the_middle_of_the_mark() {
        check(heading = qibla, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun facing_the_kaaba_in_dark() {
        check(heading = qibla, theme = ThemeChoice.DARK, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun three_degrees_off_shows_as_three_degrees_off() {
        // Inside the 5 degree arrival band, so the sentence says "facing"; the picture must still show the
        // true offset and not snap the mark onto the needle.
        check(heading = qibla - 3.0, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun three_degrees_off_the_other_way() {
        check(heading = qibla + 3.0, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun the_mark_is_at_its_bearing_to_the_right() {
        // "Turn right 58", the store picture. (Every heading here keeps the mark in the upper half, so the
        // face sample in the lower left is clean.)
        check(heading = qibla - 58.0, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun the_mark_is_at_its_bearing_to_the_left() {
        check(heading = qibla + 40.0, theme = ThemeChoice.DARK, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun the_dial_is_the_same_at_double_text() {
        check(heading = qibla - 20.0, theme = ThemeChoice.LIGHT, fontScale = 2f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun the_dial_does_not_mirror_right_to_left() {
        // Turn right 25: the mark must be to the RIGHT of the needle in Arabic and Urdu too.
        check(heading = qibla - 25.0, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Rtl)
    }
}
