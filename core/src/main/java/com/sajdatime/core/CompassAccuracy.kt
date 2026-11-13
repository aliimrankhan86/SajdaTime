package com.sajdatime.core

/** Confidence in the current compass direction, shared by the phone and watch. */
enum class CompassAccuracy {
    UNAVAILABLE,
    UNKNOWN,
    LOW,
    MEDIUM,
    HIGH;

    val needsCalibration: Boolean get() = this == UNKNOWN || this == LOW
    val allowsHeading: Boolean get() = this == MEDIUM || this == HIGH
    val allowsArrival: Boolean get() = this == HIGH

    companion object {
        /**
         * The worse sensor report wins. Android reports 3 (high), 2 (medium), 1 (low),
         * 0 (unreliable) or -1 (no contact). A null report means a sensor has not spoken.
         */
        fun fromSensorReports(vector: Int?, magnet: Int?, disturbed: Boolean): CompassAccuracy {
            if (disturbed) return LOW
            val report = if (vector == null) magnet else if (magnet == null) vector else minOf(vector, magnet)
            return when (report) {
                null -> UNKNOWN
                3 -> HIGH
                2 -> MEDIUM
                else -> LOW
            }
        }

        /** A large difference from the local Earth field can invalidate even a high sensor report. */
        fun fieldLooksDisturbed(measured: Float, expected: Float): Boolean =
            expected > 0f && kotlin.math.abs(measured - expected) > expected * 0.35f

        /**
         * How far the measured field points below the horizon, in degrees (down is positive), from the
         * device-to-world rotation matrix (row-major, as Android's SensorManager gives it) and the raw
         * field. Plain maths so an iPhone version can reuse it.
         */
        fun fieldDipDegrees(rotation: FloatArray, x: Float, y: Float, z: Float): Double {
            val east = rotation[0] * x + rotation[1] * y + rotation[2] * z
            val north = rotation[3] * x + rotation[4] * y + rotation[5] * z
            val up = rotation[6] * x + rotation[7] * y + rotation[8] * z
            return Math.toDegrees(kotlin.math.atan2(-up.toDouble(), kotlin.math.hypot(east.toDouble(), north.toDouble())))
        }

        /**
         * The strength check cannot see a magnet beside the phone that happens to leave the total near
         * normal. The Redmi on 13 Nov 2026 read 63.8 microtesla (30% over, passed) while its field pointed
         * 17 degrees UP where Slough's points 67 down. Earth's dip is known from the same World Magnetic
         * Model as the declination, so a field pointing the wrong way is a field that is not Earth's.
         * Enters at 20 degrees off, leaves at 12, so a reading on the edge does not flicker.
         */
        fun dipLooksWrong(measured: Double, expected: Double, wasWrong: Boolean): Boolean =
            kotlin.math.abs(measured - expected) > if (wasWrong) 12.0 else 20.0
    }
}
