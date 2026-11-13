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
import com.sajdatime.core.CompassAccuracy
import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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

        val magneticModel = magneticModelAt(at)
        val declination = magneticModel?.declination?.toDouble() ?: 0.0
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

        // Fused rotation vectors can be silent about accuracy. Wait for the first magnet reading
        // when the hardware has one, then use the worse report from the two sensors.
        var vectorAccuracy: Int? = null
        var magnetAccuracy: Int? = null
        val expectedField = (magneticModel?.fieldStrength ?: 0f) / 1000f
        var field: Float? = null
        // The field's direction against Earth's: a magnet nearby shows here when the strength looks normal.
        val expectedDip = magneticModel?.inclination?.toDouble()
        val rawField = FloatArray(3)
        var haveRawField = false
        var haveRotation = false
        var dipWrong = false

        fun publish() {
            val heading = smoothed ?: return
            val waitingForMagnet = magnetometer != null && field == null
            val accuracy = if (waitingForMagnet) {
                CompassAccuracy.UNKNOWN
            } else {
                CompassAccuracy.fromSensorReports(
                    vectorAccuracy,
                    magnetAccuracy,
                    disturbed = field?.let { CompassAccuracy.fieldLooksDisturbed(it, expectedField) } == true || dipWrong,
                )
            }
            trySend(CompassReading(QiblaEngine.normalise(heading + declination), accuracy))
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    magnetAccuracy = event.accuracy
                    val (x, y, z) = event.values
                    val now = kotlin.math.sqrt(x * x + y * y + z * z)
                    field = field?.let { it * 0.9f + now * 0.1f } ?: now
                    for (i in 0..2) rawField[i] = if (haveRawField) rawField[i] * 0.9f + event.values[i] * 0.1f else event.values[i]
                    haveRawField = true
                    if (expectedDip != null && haveRotation) {
                        dipWrong = CompassAccuracy.dipLooksWrong(
                            CompassAccuracy.fieldDipDegrees(rotation, rawField[0], rawField[1], rawField[2]),
                            expectedDip,
                            dipWrong,
                        )
                    }
                }
                val azimuthDegrees: Double = when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotation, event.values)
                        haveRotation = true
                        azimuthOf(rotation, remapped, orientation)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        event.values.copyInto(gravity, endIndex = 3)
                        haveGravity = true
                        if (!haveGeomagnetic) return
                        legacyAzimuth(rotation, remapped, orientation, gravity, geomagnetic)?.also { haveRotation = true } ?: return
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        // With a rotation vector the magnet chip is listened to for its accuracy only.
                        if (rotationVector != null) {
                            publish()
                            return
                        }
                        event.values.copyInto(geomagnetic, endIndex = 3)
                        haveGeomagnetic = true
                        if (!haveGravity) return
                        legacyAzimuth(rotation, remapped, orientation, gravity, geomagnetic)?.also { haveRotation = true } ?: return
                    }

                    else -> return
                }

                // Follow the shortest arc, including when crossing north.
                val target = QiblaEngine.normalise(azimuthDegrees)
                val heading = smoothed?.let { previous -> QiblaEngine.smoothHeading(previous, target, SMOOTHING) } ?: target
                smoothed = heading

                publish()
            }

            override fun onAccuracyChanged(sensor: Sensor?, value: Int) {
                when (sensor?.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> vectorAccuracy = value
                    Sensor.TYPE_MAGNETIC_FIELD -> magnetAccuracy = value
                    else -> return
                }
                publish()
            }
        }

        if (rotationVector != null) {
            manager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_GAME)
            // Slowly: only its accuracy is wanted (see onSensorChanged).
            magnetometer?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
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
    fun declinationAt(at: Coordinates): Double = magneticModelAt(at)?.declination?.toDouble() ?: 0.0

    private fun magneticModelAt(at: Coordinates): GeomagneticField? = runCatching {
        GeomagneticField(at.latitude.toFloat(), at.longitude.toFloat(), 0f, System.currentTimeMillis())
    }.getOrNull()

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

    private companion object {
        /**
         * ponytail: single fixed smoothing factor, not an adaptive filter. Higher values
         * track faster but jitter more; 0.18 is steady in the hand without feeling laggy.
         * Raise it if the needle feels sluggish on a specific device.
         */
        const val SMOOTHING = 0.18
    }
}
