package com.sajdatime.app.data

import android.location.Address

/**
 * Turns a geocoder result into the place name the user sees.
 *
 * Both halves of this feature — "find my location" and "search for a city" — used to build
 * this label themselves, from `locality ?: subAdminArea ?: adminArea`, and both were wrong
 * in the same way. A tester on a coarse fix was shown his *county* instead of his town.
 *
 * The cause is that a coarse position is deliberately fuzzed, so it often lands outside any
 * town polygon. When that happens the geocoder returns no [Address.getLocality] at all, and
 * the old chain fell straight past the town-sized fields to the county. Adding the two more
 * specific fields ahead of the county fixes it without touching the common case, where
 * `locality` is present and still wins.
 *
 * The two paths also disagreed with each other: a searched city read "Slough, United
 * Kingdom" while the same place found automatically read "Slough". Same app, same screen,
 * two formats. They share this function now, so they cannot drift apart again.
 *
 * ponytail: plain nullable strings rather than an [Address], so the ordering is covered by
 * an ordinary JVM unit test. `android.location.Address` is a stub outside an emulator and
 * every assertion here would have needed Robolectric to say anything at all.
 */
internal fun placeLabel(
    locality: String?,
    subLocality: String?,
    featureName: String?,
    subAdminArea: String?,
    adminArea: String?,
    countryName: String?,
    fallback: String = "",
): String {
    val place = locality.orNull()
        ?: subLocality.orNull()
        ?: subAdminArea.orNull()
        ?: adminArea.orNull()
        // Last resort only, and it earned that position by failing. Placed third at first,
        // ahead of the county, on the reasoning that it is "more specific". Running it
        // produced "Townhouse Hotel, United Kingdom" — the geocoder had returned a POI,
        // which is worse than the county this whole change exists to remove, and on an app
        // that promises to ask only for an approximate position, naming a *building* reads
        // as though it knows exactly where the user is standing. It stays only for the case
        // where every administrative field is null — mid-ocean, open desert — where a name
        // beats no name at all.
        ?: featureName.orNull()?.takeIf { it.looksLikeAPlace() }
    val country = countryName.orNull()

    return when {
        // "United Kingdom, United Kingdom" — reached when nothing but the country resolved.
        place == null -> country ?: fallback
        country == null || place.equals(country, ignoreCase = true) -> place
        else -> "$place, $country"
    }.ifBlank { fallback }
}

internal fun Address.placeLabel(fallback: String = ""): String = placeLabel(
    locality = locality,
    subLocality = subLocality,
    featureName = featureName,
    subAdminArea = subAdminArea,
    adminArea = adminArea,
    countryName = countryName,
    fallback = fallback,
)

private fun String?.orNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/**
 * [Address.getFeatureName] is only *usually* a place name. On a precise fix it is just as
 * likely to be a house number or a building, and "37, United Kingdom" is worse than the
 * county the user complained about. Anything without a letter in it is not a town.
 */
private fun String.looksLikeAPlace(): Boolean = any { it.isLetter() }
