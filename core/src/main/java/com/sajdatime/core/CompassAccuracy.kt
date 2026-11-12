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
    }
}
