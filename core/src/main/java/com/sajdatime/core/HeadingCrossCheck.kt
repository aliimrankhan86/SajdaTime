package com.sajdatime.core

import kotlin.math.abs
import kotlin.math.atan2

/**
 * Two ways of reading the same compass, held against each other while the phone is still.
 *
 * The fused rotation vector keeps its own long-lived idea of north (gyroscope plus magnet). A heading worked
 * out directly from the magnet and gravity has no memory. On the S23 Ultra, 13 Nov 2026, with the phone
 * lying still in clean air, the first said 131 degrees and the second 163 and neither moved for over a
 * minute, restarting the app changed nothing. Which one was right could not be told from the phone, so the
 * app must not claim a direction from either: a disagreement of this size is a reason to ask for calibration.
 *
 * Judged only when the fused heading has not moved for [STILL_MS], so turning to find the Qibla never trips
 * it (the smoothed magnet heading lags a fast turn). The verdict is kept until the next still moment.
 * Enters at [ENTER_DEGREES], leaves at [EXIT_DEGREES]. Plain Kotlin, no Android types, for an iPhone version.
 */
class HeadingCrossCheck {
    private var rawSmoothed: Double? = null
    private var anchor: Double? = null
    private var anchorMs = 0L
    var disagrees: Boolean = false
        private set

    /** Both headings are degrees clockwise from the same north (magnetic), and unmapped for screen rotation. */
    fun update(fused: Double, rawMagnet: Double, nowMs: Long): Boolean {
        rawSmoothed = rawSmoothed?.let { QiblaEngine.smoothHeading(it, rawMagnet, RAW_SMOOTHING) } ?: rawMagnet
        val held = anchor
        if (held == null || abs(QiblaEngine.relativeTurn(held, fused)) > STILL_DEGREES) {
            anchor = fused
            anchorMs = nowMs
            return disagrees
        }
        if (nowMs - anchorMs >= STILL_MS) {
            val gap = abs(QiblaEngine.relativeTurn(fused, rawSmoothed ?: rawMagnet))
            disagrees = gap > if (disagrees) EXIT_DEGREES else ENTER_DEGREES
        }
        return disagrees
    }

    companion object {
        const val STILL_MS = 1_500L
        const val STILL_DEGREES = 3.0
        const val ENTER_DEGREES = 20.0
        const val EXIT_DEGREES = 12.0
        private const val RAW_SMOOTHING = 0.2

        /**
         * Heading of the device's top edge from the magnet alone, using only the fused matrix's gravity
         * (its row 3, device to world, row-major as Android gives it) for tilt. Degrees clockwise from magnetic north.
         */
        fun magnetHeading(rotation: FloatArray, x: Float, y: Float, z: Float): Double {
            val ux = rotation[6].toDouble()
            val uy = rotation[7].toDouble()
            val uz = rotation[8].toDouble()
            val ex = y * uz - z * uy
            val ey = z * ux - x * uz
            val ez = x * uy - y * ux
            val ny = uz * ex - ux * ez
            return QiblaEngine.normalise(Math.toDegrees(atan2(ey, ny)))
        }
    }
}
