package com.sajdatime.core

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.abs

/**
 * Qibla direction. Pure maths, no Android imports, so an iOS port can lift it directly.
 *
 * The Qibla is the great-circle (shortest path over the sphere) bearing from the user to
 * the Kaaba, not the direction read off a flat map. Treating latitude and longitude as
 * flat x/y coordinates puts London at about 127 degrees against the correct 119, and the
 * error grows with latitude and distance.
 *
 * Bearings are verified against the Aladhan Qibla API in QiblaEngineTest; the two agree
 * to within a hundredth of a degree across ten cities on five continents.
 */
object QiblaEngine {

    /** The Kaaba, Masjid al-Haram, Makkah. */
    val KAABA = Coordinates(latitude = 21.4224779, longitude = 39.8251832)

    /**
     * Initial great-circle bearing from [from] to the Kaaba, in degrees clockwise from
     * **true** north, normalised to 0..360.
     *
     * Callers showing a compass must convert to magnetic north with [trueToMagnetic],
     * because phone magnetometers read magnetic north, not true north.
     */
    fun bearingToKaaba(from: Coordinates): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(KAABA.latitude)
        val deltaLon = Math.toRadians(KAABA.longitude - from.longitude)

        val y = sin(deltaLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        return normalise(Math.toDegrees(atan2(y, x)))
    }

    /**
     * Converts a true-north bearing to a magnetic-north one.
     *
     * [declination] is the local magnetic declination in degrees (east positive), which
     * on Android comes from android.hardware.GeomagneticField — the platform's built-in
     * World Magnetic Model. Declination reaches 20 degrees or more at high latitudes, so
     * skipping this step would point a UK user noticeably off the Kaaba.
     */
    fun trueToMagnetic(trueBearing: Double, declination: Double): Double =
        normalise(trueBearing - declination)

    /**
     * Smallest signed turn from [current] heading to [target], in -180..180.
     * Negative means turn left, positive means turn right.
     */
    fun relativeTurn(current: Double, target: Double): Double {
        val diff = normalise(target - current)
        return if (diff > 180.0) diff - 360.0 else diff
    }

    /** True when the device is pointing close enough to call it aligned. */
    fun isAligned(current: Double, target: Double, toleranceDegrees: Double = 3.0): Boolean =
        abs(relativeTurn(current, target)) <= toleranceDegrees

    /** It takes this much to arrive. */
    const val ALIGN_GRAB_DEGREES = 5.0

    /** And this much to be told you have left, which is deliberately wider. See [staysAligned]. */
    const val ALIGN_RELEASE_DEGREES = 8.0

    /**
     * Arrival, with hysteresis: harder to leave than to enter.
     *
     * A bare threshold is fine for a caption and wrong for a vibration. Arriving is an
     * event — it buzzes the phone and swaps the whole dial — so it must fire when the user
     * turns to face the Kaaba and at no other time. With one threshold, a heading resting
     * anywhere near it re-triggers on every wobble.
     *
     * That is not hypothetical. On the owner's S23 Ultra on 16 Aug 2026, sitting untouched
     * on a desk while the battery finished charging, the fused heading wandered between
     * 144 and 167 degrees against a Qibla of 153. Alignment flipped at least six times in
     * 55 seconds and the vibrator log shows it buzzing each time, for a phone nobody had
     * moved. The compass is otherwise steady — 42 consecutive samples aligned, and 24
     * consecutive at "turn right 6" on a later run — so this is a boundary problem, not a
     * noisy sensor.
     *
     * Hence two thresholds: arrive within [ALIGN_GRAB_DEGREES], and keep saying so until
     * the user is [ALIGN_RELEASE_DEGREES] away. The gap is the wobble the screen absorbs
     * in silence. It is deliberately narrow — it swallows the jitter that sits on the
     * boundary, not a genuine 12-degree excursion, because a user who really has turned
     * away should be told so.
     *
     * Rejected: smoothing the heading harder instead. The filter in CompassRepository
     * already runs at every sensor sample; slowing it enough to hide this would make the
     * needle visibly lag the hand, which costs every user something to fix a boundary that
     * only some users sit on.
     */
    fun staysAligned(wasAligned: Boolean, current: Double, target: Double): Boolean =
        isAligned(
            current,
            target,
            if (wasAligned) ALIGN_RELEASE_DEGREES else ALIGN_GRAB_DEGREES,
        )

    /**
     * Great-circle distance to the Kaaba in kilometres. Shown alongside the compass so
     * the reading has a sanity check the user can recognise.
     */
    fun distanceToKaabaKm(from: Coordinates): Double {
        val lat1 = Math.toRadians(from.latitude)
        val lat2 = Math.toRadians(KAABA.latitude)
        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(KAABA.longitude - from.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
            cos(lat1) * cos(lat2) * sin(deltaLon / 2) * sin(deltaLon / 2)
        return 2 * EARTH_RADIUS_KM * atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    }

    fun normalise(degrees: Double): Double {
        val wrapped = degrees % 360.0
        return if (wrapped < 0) wrapped + 360.0 else wrapped
    }

    private const val EARTH_RADIUS_KM = 6371.0088
}
