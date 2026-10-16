package com.sajdatime.core

/**
 * When the app may ask Android's geocoder for a town name.
 *
 * The prayer times and the Qibla never need a town name. It is a label on the Home screen
 * and in the PDF, and the lookup behind it is not free: Android's geocoder may be answered
 * over the network (on a phone with Google Play services it usually is), so each call hands
 * the phone's approximate position to the geocoding provider. The app used to make that
 * call on every foreground visit. This keeps the label we already have until the phone has
 * moved far enough that the label could be wrong, so a person who stays in one town asks
 * once, not every time they open the app.
 *
 * 5 km is about the size of the "coarse" fix the app already settles for, and a town label
 * does not change over a distance smaller than that in any place a prayer app is used.
 *
 * Known and accepted: the comparison is against the last stored position, not the position
 * the label was fetched at, so someone who moves in hops under 5 km, opening the app between
 * each, could keep a label a little longer than 5 km from where it was fetched. The label
 * is cosmetic and the stored coordinates (which drive the times) are always the fresh ones.
 *
 * Pure Kotlin so it is unit tested without a device. See PlaceLabelPolicyTest.
 */
object PlaceLabelPolicy {

    const val RELABEL_KM = 5.0

    fun needsNewLabel(
        previous: Coordinates?,
        previousLabel: String,
        current: Coordinates,
    ): Boolean =
        previous == null ||
            previousLabel.isBlank() ||
            QiblaEngine.distanceKm(previous, current) >= RELABEL_KM
}
