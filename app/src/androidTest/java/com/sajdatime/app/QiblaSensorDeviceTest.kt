package com.sajdatime.app

import android.app.UiAutomation
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.graphics.Rect
import android.view.Surface
import android.view.accessibility.AccessibilityNodeInfo
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sajdatime.app.ui.SajdaViewModel
import com.sajdatime.app.ui.qibla.KAABA_DISTANCE
import com.sajdatime.app.ui.qibla.KAABA_SIZE
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.core.QiblaEngine
import org.junit.After
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The real app, on the real Qibla screen, against the phone's own compass sensor read a second way.
 *
 * Written 12 Nov 2026, while 1.7.0 was on hold because Ali said the direction "seems a bit off". The dial's
 * drawing is measured by [QiblaDialDeviceTest] with a heading handed to it; this is the other half: the
 * heading the app holds, the sentence it shows and the Kaaba mark it draws (the hand's point) all agree with
 * the rotation vector sensor worked out here from the raw quaternion, with none of the app's sums and none
 * of SensorManager's helpers, and the screen stays upright when the phone is asked to turn.
 *
 * One thing is shared with the app: the magnetic declination comes from Android's `GeomagneticField` in
 * both. It was checked once against NOAA's own calculator (Slough, 12 Nov 2026: Android 1.22, NOAA WMM 2025
 * 1.07, east), and the half degree allowed here while still is less than that declination, so leaving it
 * out or reversing it fails.
 *
 * What it proves: the app reads the sensor and does the sums correctly. What it cannot prove: that the
 * phone's compass itself points to true north. Only a person holding the phone against a second reference
 * can show that.
 *
 * Run on a phone that already has a location: it opens the app as it is and changes nothing. With no
 * location, or no rotation vector sensor, it is skipped. `-e qiblaProbeSeconds 30` sets how long it compares
 * (5 by default, so the emulator suite stays quick). Measured readings are printed in the instrumentation output.
 */
@RunWith(AndroidJUnit4::class)
class QiblaSensorDeviceTest {

    // A plain launch, not a Compose test rule: under the rule the screen only moves when the test pumps its
    // clock, so the tab never finished changing and the dial stood still (the Redmi, 12 Nov 2026). Here the
    // app runs exactly as it does in the hand, and the screen is read the way TalkBack reads it.
    private lateinit var scenario: ActivityScenario<MainActivity>

    private var putRotationBack: () -> Unit = {}

    @Before
    fun open() {
        putRotationBack = DeviceSupport.holdUpright()
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val seconds = InstrumentationRegistry.getArguments().getString("qiblaProbeSeconds")?.toIntOrNull() ?: 5

    @Volatile private var rawAzimuthUp = Double.NaN // heading of the phone's own top edge, magnetic
    @Volatile private var rawAzimuthRight = Double.NaN // heading of its right edge, magnetic
    @Volatile private var sensorAccuracy = -99
    private var listener: SensorEventListener? = null

    @After
    fun putBack() {
        listener?.let { instrumentation.targetContext.getSystemService(SensorManager::class.java).unregisterListener(it) }
        putRotationBack()
        if (::scenario.isInitialized) scenario.close()
    }

    // Include the measured evidence in am instrument output even on phones with logcat disabled.
    private fun report(message: String) {
        instrumentation.sendStatus(2, Bundle().apply { putString("stream", "\n$message\n") })
    }

    private fun activity(): MainActivity {
        lateinit var found: MainActivity
        scenario.onActivity { found = it }
        return found
    }

    private fun waitUntil(what: String, millis: Long = 10_000, done: () -> Boolean) {
        val end = System.currentTimeMillis() + millis
        while (!done()) {
            assertTrue("waited ${millis / 1000}s for $what", System.currentTimeMillis() < end)
            Thread.sleep(100)
        }
    }

    private fun wrap(degrees: Double): Double = ((degrees % 360.0) + 360.0) % 360.0

    private fun apart(a: Double, b: Double): Double = abs(wrap(a - b + 180.0) - 180.0)

    /** The great circle bearing to the Kaaba by unit vectors, not the formula `QiblaEngine` uses. */
    private fun bearingByVectors(latitude: Double, longitude: Double): Double {
        val la = Math.toRadians(latitude)
        val lo = Math.toRadians(longitude)
        val kLa = Math.toRadians(21.4224779)
        val kLo = Math.toRadians(39.8251832)
        val kaaba = doubleArrayOf(cos(kLa) * cos(kLo), cos(kLa) * sin(kLo), sin(kLa))
        val north = doubleArrayOf(-sin(la) * cos(lo), -sin(la) * sin(lo), cos(la))
        val east = doubleArrayOf(-sin(lo), cos(lo), 0.0)
        fun dot(a: DoubleArray, b: DoubleArray) = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]
        return wrap(Math.toDegrees(atan2(dot(kaaba, east), dot(kaaba, north))))
    }

    private fun listen(): Sensor? {
        val manager = instrumentation.targetContext.getSystemService(SensorManager::class.java)
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return null
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()
                val w = if (event.values.size > 3) event.values[3].toDouble() else sqrt(maxOf(0.0, 1 - x * x - y * y - z * z))
                // Where the phone's axes point in the world (east, north), straight from the quaternion.
                rawAzimuthUp = wrap(Math.toDegrees(atan2(2 * (x * y - w * z), 1 - 2 * (x * x + z * z))))
                rawAzimuthRight = wrap(Math.toDegrees(atan2(1 - 2 * (y * y + z * z), 2 * (x * y + w * z))))
                if (sensorAccuracy == -99) sensorAccuracy = event.accuracy
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                sensorAccuracy = accuracy
            }
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        return sensor
    }

    /** Every node on screen, as the accessibility service sees it. */
    private fun nodes(): List<AccessibilityNodeInfo> {
        val found = mutableListOf<AccessibilityNodeInfo>()
        fun walk(node: AccessibilityNodeInfo?) {
            node ?: return
            found += node
            for (child in 0 until node.childCount) walk(node.getChild(child))
        }
        walk(instrumentation.uiAutomation.rootInActiveWindow)
        return found
    }

    private fun screenTexts(): List<String> =
        nodes().flatMap { listOfNotNull(it.text?.toString(), it.contentDescription?.toString()) }

    private fun model(): SajdaViewModel {
        lateinit var found: SajdaViewModel
        scenario.onActivity { found = ViewModelProvider(it)[SajdaViewModel::class.java] }
        return found
    }

    private fun openQibla() {
        waitUntil("the app to load") { !model().state.value.loading }
        val title = activity().getString(R.string.qibla_title)
        waitUntil("the Qibla tab") {
            var tab = nodes().firstOrNull { it.text?.toString() == title }
            while (tab != null && !tab.isClickable) tab = tab.parent
            tab?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
        }
        waitUntil("a heading") { model().state.value.compassHeading != null && !rawAzimuthUp.isNaN() }
        Thread.sleep(2_000) // the app's filter and the dial's 120 ms turn settle
    }

    /**
     * Compares for [forSeconds] with the screen turned to [rotation]. Returns the last heading worked out
     * here, in degrees from true north.
     */
    private fun compare(rotation: Int, bearing: Double, declination: Double, forSeconds: Int): Double {
        val name = if (rotation == Surface.ROTATION_0) "upright" else "on its side"
        var worst = 0.0
        var worstStill = 0.0
        var stillTicks = 0
        var previous = wrap((if (rotation == Surface.ROTATION_0) rawAzimuthUp else rawAzimuthRight) + declination)
        var mine = Double.NaN
        val activity = activity()
        repeat(forSeconds * 5) { tick ->
            Thread.sleep(200)
            // The screen's top edge is the phone's top edge upright, and its right edge when turned 90.
            mine = wrap((if (rotation == Surface.ROTATION_0) rawAzimuthUp else rawAzimuthRight) + declination)
            val app = model().state.value.compassHeading ?: error("the app lost its heading")
            val gap = apart(app, mine)
            // Held still they agree to a fraction of a degree. While the phone is being turned the app
            // trails by its smoothing (about a tenth of a second, `CompassRepository.SMOOTHING`), and the two
            // readings here are taken a moment apart, so a turning phone is allowed a third of a second of
            // its own turn on top. Found on the Redmi when it was picked up mid run: 5.6 apart at 55 a second.
            val turning = apart(mine, previous) / 0.2
            previous = mine
            // Still, half a degree: tight enough that a missing declination (over a degree in the UK) fails.
            val allowed = if (turning < 1.0) 0.5 else 2.0 + turning * 0.33
            if (turning < 1.0) {
                worstStill = maxOf(worstStill, gap)
                stillTicks++
            }
            worst = maxOf(worst, gap)
            if (tick % 5 == 0) {
                report("$name t=${tick / 5}s sensor=${"%.2f".format(mine)} app=${"%.2f".format(app)} " +
                    "gap=${"%.2f".format(gap)} turning=${"%.0f".format(turning)}/s accuracy=${model().state.value.compassAccuracy}/$sensorAccuracy")
            }
            assertTrue("$name: the app says $app and the sensor says $mine, $gap apart, turning $turning a second", gap <= allowed)
        }
        report("$name: worst gap over ${forSeconds}s = ${"%.2f".format(worst)} degrees, " +
            "${"%.2f".format(worstStill)} while still ($stillTicks of ${forSeconds * 5} readings still)")
        assertTrue("$name: the phone was still for only $stillTicks readings; lay it flat and run again", stillTicks >= 10)

        if (model().state.value.compassAccuracy.needsCalibration) {
            val texts = screenTexts()
            assertTrue("the calibration warning is missing: $texts", activity.getString(R.string.qibla_calibrate) in texts)
            assertTrue("untrusted compass claims alignment: $texts", activity.getString(R.string.qibla_facing) !in texts)
            assertTrue("untrusted compass shows a heading: $texts", texts.none { text ->
                (0..359).any { text == activity.getString(R.string.qibla_heading_now, it) }
            })
            return mine
        }

        // The sentence and the "Facing" line are this heading too. Tried for a few seconds, reading the sensor
        // afresh each time, so a phone that is being moved is not judged on a stale reading.
        var texts = emptyList<String>()
        var turn = 0.0
        var sentences = emptyList<String>()
        var facing = emptyList<String>()
        for (attempt in 1..20) {
            mine = wrap((if (rotation == Surface.ROTATION_0) rawAzimuthUp else rawAzimuthRight) + declination)
            turn = wrap(bearing - mine + 180.0) - 180.0
            sentences = (-2..2).map { turn + it }.map {
                val degrees = abs(it).roundToInt()
                when {
                    degrees == 0 -> activity.getString(R.string.qibla_bearing, QiblaEngine.wholeDegrees(bearing))
                    it > 0 -> activity.getString(R.string.qibla_turn_right, degrees)
                    else -> activity.getString(R.string.qibla_turn_left, degrees)
                }
            } + if (abs(turn) <= QiblaEngine.ALIGN_RELEASE_DEGREES + 0.5) listOf(activity.getString(R.string.qibla_facing)) else emptyList()
            facing = (-2..2).flatMap {
                listOf(activity.getString(R.string.qibla_heading_now, wrap(mine + it).roundToInt().mod(360)))
            }
            texts = screenTexts()
            if (texts.any { it in sentences } && texts.any { it in facing }) break
            Thread.sleep(250)
        }
        report("$name: screen says ${texts.filter { t -> '°' in t || t in sentences }.distinct()}; " +
            "sensor heading ${"%.1f".format(mine)}, turn ${"%.1f".format(turn)}")
        assertTrue("$name: no sentence for a turn of $turn in $texts", texts.any { it in sentences })
        assertTrue("$name: no \"Facing\" line for $mine in $texts", texts.any { it in facing })
        return mine
    }

    private fun far(a: Int, b: Int): Boolean =
        abs((a shr 16 and 0xff) - (b shr 16 and 0xff)) + abs((a shr 8 and 0xff) - (b shr 8 and 0xff)) +
            abs((a and 0xff) - (b and 0xff)) > 90

    /** The Kaaba mark on the real dial is drawn at (bearing minus heading) from straight up. */
    private fun measureMark(bearing: Double, declination: Double) {
        // The picture and the reading must be of the same moment, so it is only measured when the phone has
        // not turned half a degree across the capture. Ten tries, then it is logged as not measured.
        var picture: Bitmap? = null
        var heading = Double.NaN
        for (attempt in 1..10) {
            val before = wrap(rawAzimuthUp + declination)
            Thread.sleep(300) // the dial's own 120 ms turn
            val taken = instrumentation.uiAutomation.takeScreenshot()
            heading = wrap(rawAzimuthUp + declination)
            if (apart(before, heading) <= 0.5) {
                picture = taken.copy(Bitmap.Config.ARGB_8888, false)
                break
            }
        }
        assertTrue("the mark could not be measured: the phone was being moved; lay it flat and run again", picture != null)
        picture!!
        val turn = wrap(bearing - heading + 180.0) - 180.0
        // Where the dial is on the whole screen: centred, one Section gap under the "Facing" line, and as
        // wide as `dialMaxWidth` allows (320dp, or 0.36 of the screen's height, or the column if narrower).
        val activity = activity()
        val above = nodes().firstOrNull { it.text?.toString() == activity.getString(R.string.qibla_heading_now, heading.roundToInt().mod(360)) }
            ?: nodes().first { it.text?.toString() in (0..359).map { d -> activity.getString(R.string.qibla_heading_now, d) } }
        val line = Rect().also(above::getBoundsInScreen)
        val density = activity.resources.displayMetrics.density
        val screen = activity.resources.configuration
        val r = minOf(320f, screen.screenHeightDp * 0.36f, screen.screenWidthDp - 40f) * density / 2f
        val cx = picture.width / 2f
        val cy = line.bottom + Space.Section.value * density + r
        // A clean piece of the face: inside the letters, a third of a turn round from the hand.
        val clean = Math.toRadians(turn + 120.0)
        val face = picture.getPixel((cx + 0.38f * r * sin(clean)).toInt(), (cy - 0.38f * r * cos(clean)).toInt())
        val angle = Math.toRadians(turn)
        val wantX = cx + KAABA_DISTANCE * r * sin(angle).toFloat()
        val wantY = cy - KAABA_DISTANCE * r * cos(angle).toFloat() + 0.59f / 24f * KAABA_SIZE * r // its ink sits low
        val half = 0.095f * r
        var sumX = 0.0
        var sumY = 0.0
        var count = 0
        for (py in (wantY - half).toInt()..(wantY + half).toInt()) {
            for (px in (wantX - half).toInt()..(wantX + half).toInt()) {
                if (px in 0 until picture.width && py in 0 until picture.height && far(picture.getPixel(px, py), face)) {
                    sumX += px + 0.5
                    sumY += py + 0.5
                    count++
                }
            }
        }
        assertTrue("no Kaaba mark where a turn of $turn puts it", count > 0)
        val off = sqrt((sumX / count - wantX) * (sumX / count - wantX) + (sumY / count - wantY) * (sumY / count - wantY))
        val twoDegrees = KAABA_DISTANCE * r * sin(Math.toRadians(2.0)) + 1.5
        report("mark: ${"%.1f".format(off)}px from where the sensor puts it (2 degrees is ${"%.1f".format(twoDegrees)}px, dial radius ${r}px)")
        assertTrue("the Kaaba mark is ${off}px from where a turn of $turn puts it", off <= twoDegrees)
    }

    @Test
    fun the_heading_the_sentence_and_the_mark_agree_with_the_raw_sensor_and_the_screen_stays_upright() {
        waitUntil("the app to load") { !model().state.value.loading }
        val settings = model().state.value.settings
        val appBearing = model().state.value.qiblaBearing
        assumeTrue("no location on this device", appBearing != null && !settings.usingDefaultLocation)
        val sensor = listen()
        assumeTrue("no rotation vector sensor", sensor != null)

        val at = settings.coordinates!!
        val bearing = bearingByVectors(at.latitude, at.longitude)
        val declination = GeomagneticField(at.latitude.toFloat(), at.longitude.toFloat(), 0f, System.currentTimeMillis())
            .declination.toDouble()
        report("place=${settings.cityName} bearing app=${"%.3f".format(appBearing)} " +
            "independent=${"%.3f".format(bearing)} declination=${"%.2f".format(declination)} " +
            "sensor=${sensor!!.name} (${sensor.vendor})")
        assertEquals("the bearing to the Kaaba", bearing, appBearing!!, 0.05)

        openQibla()
        compare(Surface.ROTATION_0, bearing, declination, seconds)
        if (model().state.value.compassAccuracy.allowsArrival) {
            measureMark(bearing, declination)
        } else {
            report("mark: not measured without a high-confidence heading")
        }

        // The phone's screen is asked to turn on its side. The Qibla tab holds itself upright (the top mark
        // means the top of the phone), so the screen must stay as it was and the heading with it. A large
        // screen may ignore the tab's request from Android 16: there the heading must move a quarter turn
        // with the screen, which is the phone's right edge worked out here.
        assertEquals("the Qibla tab asks to stay upright", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, activity().requestedOrientation)
        instrumentation.uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90)
        Thread.sleep(3_000)
        val turned = activity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        report("asked to turn on its side: the screen " + if (turned) "turned (large screen)" else "stayed upright")
        compare(if (turned) Surface.ROTATION_90 else Surface.ROTATION_0, bearing, declination, minOf(seconds, 10))
    }

}
