package com.sajdatime.app.data

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import androidx.core.content.ContextCompat
import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** How much the compass can be trusted right now. */
enum class CompassAccuracy {
    /** Sensors report a reliable field. */
    HIGH,

    /** Usable, but the reading may wander by several degrees. */
    MEDIUM,

    /** Needs a figure-of-eight recalibration before the direction means anything. */
    LOW,

    /** No usable compass hardware, or the sensor has not reported yet. */
    UNAVAILABLE,
}

data class CompassReading(
    /** Device heading in degrees clockwise from **true** north. */
    val trueHeading: Double,
    val accuracy: CompassAccuracy,
)

/**
 * Device heading, corrected to true north.
 *
 * ponytail: TYPE_ROTATION_VECTOR rather than fusing raw accelerometer and magnetometer
 * by hand. It is the platform's own sensor fusion, it is already low-pass filtered, and
 * it is markedly steadier than the manual approach. The accelerometer/magnetometer pair
 * is kept only as a fallback for devices without a rotation vector.
 */
class CompassRepository(private val context: Context) {

    private val sensorManager: SensorManager? =
        ContextCompat.getSystemService(context, SensorManager::class.java)

    fun hasCompass(): Boolean {
        val manager = sensorManager ?: return false
        return manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null ||
            (
                manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null &&
                    manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
                )
    }

    /**
     * Emits the current heading while collected, and stops the moment collection ends.
     *
     * [at] supplies the magnetic declination for the user's position. Without it the
     * needle would point at magnetic north, which is up to about 20 degrees away from
     * true north in the UK and far worse near the poles.
     */
    fun headings(at: Coordinates): Flow<CompassReading> = callbackFlow {
        val manager = sensorManager
        if (manager == null) {
            trySend(CompassReading(0.0, CompassAccuracy.UNAVAILABLE))
            awaitClose { }
            return@callbackFlow
        }

        val declination = declinationAt(at)
        val rotationVector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationVector == null && (accelerometer == null || magnetometer == null)) {
            trySend(CompassReading(0.0, CompassAccuracy.UNAVAILABLE))
            awaitClose { }
            return@callbackFlow
        }

        val rotation = FloatArray(9)
        val remapped = FloatArray(9)
        val orientation = FloatArray(3)
        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var haveGravity = false
        var haveGeomagnetic = false
        var smoothed: Double? = null

        // Seeded HIGH rather than UNAVAILABLE. We only reach this line once a real sensor
        // has been found, and many devices report a fused rotation vector without ever
        // calling onAccuracyChanged. Starting at UNAVAILABLE made those devices show
        // "this phone has no compass" directly beneath a perfectly working compass.
        // A genuinely uncalibrated device is told to us via onAccuracyChanged(LOW).
        var accuracy = CompassAccuracy.HIGH

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val azimuthDegrees: Double = when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotation, event.values)
                        azimuthOf(rotation, remapped, orientation)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(gravity, endIndex = 3)
                        haveGravity = true
                        if (!haveGeomagnetic) return
                        legacyAzimuth(rotation, remapped, orientation, gravity, geomagnetic) ?: return
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        event.values.copyInto(geomagnetic, endIndex = 3)
                        haveGeomagnetic = true
                        if (!haveGravity) return
                        legacyAzimuth(rotation, remapped, orientation, gravity, geomagnetic) ?: return
                    }

                    else -> return
                }

                // Circular low-pass filter. Averaging degrees directly would swing wildly
                // across the 359 -> 0 boundary, so the smoothing is done on the unit
                // vector and converted back.
                val target = QiblaEngine.normalise(azimuthDegrees)
                val heading = smoothed?.let { previous -> smoothAngle(previous, target) } ?: target
                smoothed = heading

                trySend(
                    CompassReading(
                        trueHeading = QiblaEngine.normalise(heading + declination),
                        accuracy = accuracy,
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
                accuracy = when (value) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW,
                    SensorManager.SENSOR_STATUS_UNRELIABLE,
                    -> CompassAccuracy.LOW

                    else -> CompassAccuracy.UNAVAILABLE
                }
            }
        }

        if (rotationVector != null) {
            manager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_GAME)
        } else {
            manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            manager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_GAME)
        }

        awaitClose { manager.unregisterListener(listener) }
    }

    /**
     * Magnetic declination at the user's position, from Android's built-in World
     * Magnetic Model. East is positive.
     */
    fun declinationAt(at: Coordinates): Double = runCatching {
        GeomagneticField(
            at.latitude.toFloat(),
            at.longitude.toFloat(),
            0f,
            System.currentTimeMillis(),
        ).declination.toDouble()
    }.getOrDefault(0.0)

    private fun legacyAzimuth(
        rotation: FloatArray,
        remapped: FloatArray,
        orientation: FloatArray,
        gravity: FloatArray,
        geomagnetic: FloatArray,
    ): Double? {
        if (!SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)) return null
        return azimuthOf(rotation, remapped, orientation)
    }

    /**
     * Azimuth from a rotation matrix, corrected for how the screen is currently turned.
     *
     * Sensor readings are always in the device's *natural* orientation, which is portrait
     * on a phone and often landscape on a tablet. getOrientation on the raw matrix
     * therefore reports a heading that is 90, 180 or 270 degrees out as soon as the
     * screen rotates — so a user who turned their phone sideways was being pointed a
     * quarter turn away from the Kaaba with no indication anything was wrong.
     *
     * Read per event rather than cached: the screen can rotate while the Qibla screen is
     * open, and a stale value would be exactly the bug this fixes.
     */
    private fun azimuthOf(
        rotation: FloatArray,
        remapped: FloatArray,
        orientation: FloatArray,
    ): Double {
        val (axisX, axisY) = when (displayRotation()) {
            Surface.ROTATION_90 -> SensorManager.AXIS_Y to SensorManager.AXIS_MINUS_X
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_X to SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> SensorManager.AXIS_MINUS_Y to SensorManager.AXIS_X
            else -> SensorManager.AXIS_X to SensorManager.AXIS_Y
        }
        val matrix = if (SensorManager.remapCoordinateSystem(rotation, axisX, axisY, remapped)) {
            remapped
        } else {
            rotation
        }
        SensorManager.getOrientation(matrix, orientation)
        return Math.toDegrees(orientation[0].toDouble())
    }

    /**
     * ponytail: DisplayManager rather than WindowManager.getDefaultDisplay. This repository
     * holds an application context, and Context.getDisplay throws on one from API 30;
     * DisplayManager is the API that is legal from a non-visual context and is not
     * deprecated.
     */
    private fun displayRotation(): Int =
        ContextCompat.getSystemService(context, DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
            ?.rotation
            ?: Surface.ROTATION_0

    private fun smoothAngle(previous: Double, target: Double): Double {
        val previousRad = Math.toRadians(previous)
        val targetRad = Math.toRadians(target)
        val x = previousRad.let { cos(it) } * (1 - SMOOTHING) + cos(targetRad) * SMOOTHING
        val y = previousRad.let { sin(it) } * (1 - SMOOTHING) + sin(targetRad) * SMOOTHING
        return QiblaEngine.normalise(Math.toDegrees(atan2(y, x)))
    }

    private companion object {
        /**
         * ponytail: single fixed smoothing factor, not an adaptive filter. Higher values
         * track faster but jitter more; 0.18 is steady in the hand without feeling laggy.
         * Raise it if the needle feels sluggish on a specific device.
         */
        const val SMOOTHING = 0.18
    }
}
