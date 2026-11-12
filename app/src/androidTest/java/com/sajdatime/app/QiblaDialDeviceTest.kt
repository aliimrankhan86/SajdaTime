package com.sajdatime.app

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.data.AppSettings
import com.sajdatime.core.CompassAccuracy
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.qibla.KAABA_DISTANCE
import com.sajdatime.app.ui.qibla.KAABA_SIZE
import com.sajdatime.app.ui.qibla.QiblaBody
import com.sajdatime.app.ui.theme.SajdaTimeTheme
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.roundToInt
import org.junit.runner.RunWith
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Measures the Qibla dial's own pixels: the hand lies on the line to the Kaaba (the bearing minus the
 * heading, clockwise from straight up), the Kaaba mark is on that same line to within a pixel and a half,
 * and the fixed top mark is dead centre.
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

    /** The heading on screen; a sweep moves it after [show] has drawn the screen once. */
    private val liveHeading = mutableDoubleStateOf(0.0)

    private fun show(
        heading: Double,
        bearing: Double = qibla,
        theme: ThemeChoice = ThemeChoice.LIGHT,
        fontScale: Float = 1f,
        direction: LayoutDirection = LayoutDirection.Ltr,
        accuracy: CompassAccuracy = CompassAccuracy.HIGH,
    ) {
        liveHeading.doubleValue = heading
        compose.setContent {
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
                                compassHeading = liveHeading.doubleValue,
                                compassAccuracy = accuracy,
                                qiblaBearing = bearing,
                                qiblaDistanceKm = 4788.0,
                            ),
                            onUseGps = {},
                            onSearchCity = {},
                        )
                    }
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

    /**
     * Cuts across the hand's line [along] shares of the radius out from the centre, at [turn] degrees
     * clockwise from straight up, looking [reach] pixels either side. Returns how far the middle of what is
     * drawn there is from the line (plus is clockwise) and how wide it is, in pixels.
     */
    private fun across(picture: Bitmap, r: Float, turn: Double, along: Float, reach: Float, face: Int): Pair<Float, Float> {
        val a = Math.toRadians(turn)
        val x0 = picture.width / 2f + along * r * sin(a).toFloat()
        val y0 = picture.height / 2f - along * r * cos(a).toFloat()
        var first = Float.NaN
        var last = Float.NaN
        var t = -reach
        while (t <= reach) {
            val px = (x0 + t * cos(a).toFloat()).toInt()
            val py = (y0 + t * sin(a).toFloat()).toInt()
            if (far(picture.getPixel(px, py), face)) {
                if (first.isNaN()) first = t
                last = t
            }
            t += 0.25f
        }
        assertTrue("nothing drawn $along of the way out at $turn degrees", !first.isNaN())
        return (first + last) / 2f to (last - first)
    }

    private fun check(
        heading: Double,
        theme: ThemeChoice,
        fontScale: Float,
        direction: LayoutDirection,
        bearing: Double = qibla,
    ) {
        show(heading, bearing, theme, fontScale, direction)
        val picture = dial()
        val cx = picture.width / 2f
        val cy = picture.height / 2f
        val r = minOf(picture.width, picture.height) / 2f
        val label = "heading $heading, $theme, text $fontScale, $direction"
        // Where the hand and the Kaaba must be: the bearing minus the heading, clockwise from straight up.
        val turn = bearing - heading

        // A clean piece of the face: inside the letters, a third of a turn round from the hand.
        val clean = Math.toRadians(turn + 120.0)
        val face = picture.getPixel((cx + 0.38f * r * sin(clean)).toInt(), (cy - 0.38f * r * cos(clean)).toInt())

        // The hand, cut across at two places along that line: each time its middle is on the line. This is
        // the check that would have caught 12 Nov 2026, when the hand stood straight up whatever the turn.
        for (share in listOf(0.25f, 0.45f)) {
            val (off, width) = across(picture, r, turn, share, 0.2f * r, face)
            assertTrue("$label: the hand is ${off}px off the line to the Kaaba at $share", abs(off) <= 1.25f)
            assertTrue("$label: the hand is ${width}px wide at $share, dial radius ${r}px", width <= 0.16f * r)
        }
        // And it has a direction: nothing of it that far out on the opposite side (the tail is short).
        val behind = Math.toRadians(turn + 180.0)
        assertTrue(
            "$label: the hand points away from the Kaaba",
            !far(picture.getPixel((cx + 0.45f * r * sin(behind)).toInt(), (cy - 0.45f * r * cos(behind)).toInt()), face),
        )

        // The Kaaba mark, on the same line at KAABA_DISTANCE of the radius. The box holds all of its ink
        // (0.078 of the radius either side, 0.086 below) and stops short of the tick ring; the hand's point
        // inside it is a hair of green that moves the middle by a third of a pixel at most.
        val angle = Math.toRadians(turn)
        val wantX = cx + KAABA_DISTANCE * r * sin(angle).toFloat()
        val wantY = cy - KAABA_DISTANCE * r * cos(angle).toFloat()
        val (gotX, gotY) = centreOfInk(picture, wantX, wantY, 0.095f * r, face)
        // The artwork is centred left to right in its box. Top to bottom its ink (the wall, y 3.5 to 20
        // of 24, and the plinth under it) balances 0.59 of a unit below the middle of the box.
        val inkY = wantY + 0.59f / 24f * KAABA_SIZE * r
        assertTrue("$label: Kaaba is ${gotX - wantX}px sideways from its bearing", abs(gotX - wantX) <= 1.5f)
        assertTrue("$label: Kaaba is ${gotY - inkY}px up or down from its bearing", abs(gotY - inkY) <= 1.5f)

        // The fixed mark for "the top of your phone": on the bezel, dead centre at the top, whatever the turn.
        val bezel = picture.getPixel((cx + 0.955f * r * sin(Math.toRadians(30.0))).toInt(), (cy - 0.955f * r * cos(Math.toRadians(30.0))).toInt())
        val (off, width) = across(picture, r, 0.0, 0.955f, 0.12f * r, bezel)
        assertTrue("$label: the top mark is ${off}px off centre", abs(off) <= 1.25f)
        assertTrue("$label: the top mark is ${width}px wide, dial radius ${r}px", width <= 0.1f * r)
    }

    @Test
    fun at_heading_80_in_slough_the_hand_and_the_kaaba_are_38_and_a_half_degrees_right_of_up() {
        // Ali's Redmi, 12 Nov 2026: "Facing 80", "Turn right 39", and a hand that pointed straight up.
        check(heading = 80.0, bearing = 118.5, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_turn_right, 39)).assertExists()
    }

    @Test
    fun at_heading_118_and_a_half_in_slough_the_hand_stands_straight_up_and_the_screen_says_facing() {
        check(heading = 118.5, bearing = 118.5, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_facing)).assertExists()
    }

    @Test
    fun low_accuracy_hides_the_heading_and_aligned_dial_even_at_the_bearing() {
        show(heading = qibla, accuracy = CompassAccuracy.LOW)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_calibrate)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_facing)).assertDoesNotExist()
        compose.onNodeWithText(context.getString(R.string.qibla_heading_now, 119)).assertDoesNotExist()
        compose.onNodeWithTag("qibla_dial", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun a_heading_that_rounds_onto_the_bearing_says_facing_rather_than_a_one_degree_turn() {
        // Facing prints 119 while the raw heading is 118.6 and the raw turn is 0.4 degrees.
        show(heading = qibla + 0.4)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_facing)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_turn_left, 0)).assertDoesNotExist()
    }

    private fun shown(text: String) = compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun at_medium_accuracy_every_heading_prints_a_turn_that_adds_up_and_the_bearing_only_over_itself() {
        // A bearing of exactly 118.5 is where rounding bites (Slough itself is 118.48 and prints 118): 118.5
        // prints as 119, so a turn rounded from the raw numbers
        // (the code before f13497b) said "Turn left 82" beside "Facing 200" and, at 118.3, printed the
        // bearing "119° from north" as the headline over "Facing 118°". Medium accuracy has no arrival
        // state, so every heading round the circle shows the plain instruction. Fails on the old code.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bearing = 118.5
        val shownBearing = QiblaEngine.wholeDegrees(bearing)
        show(heading = 0.2, bearing = bearing, accuracy = CompassAccuracy.MEDIUM)
        for (step in 0 until 720) {
            val heading = step / 2.0 + 0.2
            liveHeading.doubleValue = heading
            compose.waitForIdle()
            val facing = QiblaEngine.wholeDegrees(heading)
            val turn = QiblaEngine.relativeTurn(facing.toDouble(), shownBearing.toDouble()).roundToInt()
            val headline = when {
                turn == 0 -> context.getString(R.string.qibla_bearing, shownBearing)
                turn > 0 -> context.getString(R.string.qibla_turn_right, turn)
                else -> context.getString(R.string.qibla_turn_left, -turn)
            }
            assertTrue("heading $heading: expected \"$headline\"", shown(headline))
            assertTrue("heading $heading: expected Facing $facing", shown(context.getString(R.string.qibla_heading_now, facing)))
            if (turn != 0) {
                assertTrue("heading $heading: the bearing is the headline over Facing $facing",
                    !shown(context.getString(R.string.qibla_bearing, shownBearing)))
            }
            assertTrue("heading $heading: facing the Kaaba claimed at medium", !shown(context.getString(R.string.qibla_facing)))
        }
    }

    @Test
    fun at_high_accuracy_near_the_kaaba_it_is_facing_or_a_turn_that_adds_up_never_the_bearing() {
        // Walk upwards from 5 degrees short of a 118.5 bearing to 5 past it. Arrival is 2 degrees in and 4 out,
        // so the screen says facing from exactly 116.5 to 122.5 and nowhere else; elsewhere the turn printed
        // must be the printed bearing minus the printed facing (116.4 printed "Turn right 2" beside "Facing 116"
        // before f13497b); the Facing line is always there and the bearing is never the headline.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bearing = 118.5
        val shownBearing = QiblaEngine.wholeDegrees(bearing)
        show(heading = bearing - 5.0, bearing = bearing)
        for (step in 0..100) {
            val heading = bearing - 5.0 + step / 10.0
            liveHeading.doubleValue = heading
            compose.waitForIdle()
            val facingKaaba = shown(context.getString(R.string.qibla_facing))
            val expectFacing = heading >= bearing - 2.0 && heading <= bearing + 4.0
            assertEquals("heading $heading: facing the Kaaba", expectFacing, facingKaaba)
            assertTrue("heading $heading: expected Facing line",
                shown(context.getString(R.string.qibla_heading_now, QiblaEngine.wholeDegrees(heading))))
            if (!facingKaaba) {
                val turn = QiblaEngine.relativeTurn(QiblaEngine.wholeDegrees(heading).toDouble(), shownBearing.toDouble()).roundToInt()
                val text = if (turn > 0) context.getString(R.string.qibla_turn_right, turn) else context.getString(R.string.qibla_turn_left, -turn)
                assertTrue("heading $heading: expected \"$text\"", turn != 0 && shown(text))
            }
            assertTrue("heading $heading: the bearing is the headline at high accuracy",
                !shown(context.getString(R.string.qibla_bearing, shownBearing)))
        }
    }

    @Test
    fun medium_accuracy_keeps_the_direction_with_a_calibration_hint() {
        show(heading = qibla - 30.0, accuracy = CompassAccuracy.MEDIUM)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_accuracy_medium)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_turn_right, 30)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_facing)).assertDoesNotExist()
    }

    @Test
    fun the_hand_swings_left_when_the_kaaba_is_to_the_left() {
        check(heading = 200.0, bearing = 118.5, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun the_hand_points_back_over_the_shoulder_when_the_kaaba_is_behind() {
        check(heading = 320.0, bearing = 118.5, theme = ThemeChoice.DARK, fontScale = 1f, direction = LayoutDirection.Ltr)
    }

    @Test
    fun just_short_of_north_reads_facing_0_never_360() {
        show(heading = 359.7)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_heading_now, 0)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_heading_now, 360)).assertDoesNotExist()
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
        check(heading = qibla - 3.0, theme = ThemeChoice.LIGHT, fontScale = 1f, direction = LayoutDirection.Ltr)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.onNodeWithText(context.getString(R.string.qibla_turn_right, 3)).assertExists()
        compose.onNodeWithText(context.getString(R.string.qibla_facing)).assertDoesNotExist()
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
