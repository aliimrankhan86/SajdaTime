package com.sajdatime.app.data

import android.location.Address

/**
 * Turns a geocoder result into the place name the user sees.
 *
 * Both halves of this feature — "find my location" and "search for a city" — share it, so a
 * place found automatically and the same place found by typing are named identically.
 *
 * **Why the town is sometimes dug out of the address line.** A tester in Slough was shown
 * *"Berkshire"*, and the first fix assumed his coarse fix had landed outside the town
 * polygon. Measuring it on 15 Aug 2026 (a probe logging every field of five results at
 * twenty coordinates, HANDOVER §10) showed the real cause is broader and not about coarse
 * location at all: **for reverse geocoding in the United Kingdom, Google's geocoder never
 * fills [Address.getLocality]** — not for the exact centre of Slough, nor Reading,
 * Birmingham or Whitechapel. UK addresses carry a *postal town* ("Slough SL1 1XU"), which
 * Google returns as a component Android's [Address] has no field for. It survives only in
 * the formatted address line, `"5 William St, Slough SL1 1GZ, UK"`, while `subAdminArea`
 * holds the ceremonial county. So the old chain, `locality ?: subLocality ?: subAdminArea`,
 * reached the county for every user in Britain who was not standing in a named district.
 *
 * **And it is not only the UK.** The emulator probe found `locality` present in Karachi,
 * Lahore, Toronto, Cairo, Jakarta, Riyadh, Kuala Lumpur and Lagos, and that was written up
 * as "this only ever changes what the UK sees". Running the app on the owner's Galaxy S23
 * Ultra in Alanya, Türkiye on 15 Aug 2026 disproved it in one reading: `locality=null`,
 * `subLocality=Türkler`, `subAdminArea=Alanya`, `adminArea=Antalya`. So the parse is a
 * general path, not a British special case, and the countries listed above are places the
 * probe happened to sample rather than a guarantee. Treat "`locality` is always there
 * outside the UK" as disproved, not merely unproven.
 *
 * Forward geocoding (typing "Slough") *does* return `locality`, so `CityLookup` was never
 * affected; the same function is kept for both paths anyway.
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
    thoroughfare: String? = null,
    addressLine: String? = null,
    fallback: String = "",
): String {
    val place = locality.orNull()
        // The town before the district: "Bradford" rather than "Manningham", "Slough"
        // rather than "Langley", the same way `locality` beats `subLocality` in Karachi or
        // Toronto. Consistent city-first naming everywhere is what a mosque timetable is
        // titled by, and it is what the owner asked for.
        ?: townFromAddressLine(addressLine, featureName, thoroughfare)
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
    thoroughfare = thoroughfare,
    // Google puts the whole formatted address in line 0; a backend that splits it across
    // lines is joined back into the same shape.
    addressLine = (0..maxAddressLineIndex).mapNotNull { getAddressLine(it) }.joinToString(", "),
    fallback = fallback,
)

/**
 * The postal town, from a formatted address of the shape the geocoder actually returns:
 * `"<premise or street>, <Town> <postcode>, <Country>"`. Measured, not assumed — every UK
 * reverse-geocode result in the 15 Aug 2026 probe had exactly this shape, whether the first
 * part was a house number, a bus stop, a rugby club or a motorway.
 *
 * Deliberately narrow, because a wrong town is worse than a county:
 *  - Needs at least three parts. `"Townhouse Hotel, United Kingdom"` and `"Datchet, UK"`
 *    are two, and from two parts a POI and a town cannot be told apart, so it stays out and
 *    the ordinary chain decides.
 *  - Takes the part before the country, minus any word carrying a digit — the postcode,
 *    whole ("SL1 1XU") or partial ("SL3").
 *  - Refuses the street and the feature name, so a line that happens to end
 *    `"…, High Street, UK"` cannot promote the street.
 *  - Refuses an all-capitals result. `"…, ON M1P 4N6, Canada"` and `"…, CA 94043, USA"`
 *    would leave "ON" and "CA" — a province or state abbreviation, not a town. Those
 *    countries fill `locality`, so this line is rarely reached there, but rural fixes can
 *    reach it and a two-letter code on the home screen would be a new bug.
 *  - Keeps only what precedes a `/`. Turkey writes the pair as `"07410 Alanya/Antalya"` —
 *    district first, then the province — so the slash is the same "town versus county"
 *    boundary this whole function exists to get right, and taking the whole pair would put
 *    an Antalya-sized province on the home screen. Measured on the owner's Galaxy S23 Ultra
 *    in Alanya, 15 Aug 2026: that fix is the one that proved `locality` is null outside the
 *    UK too, so this branch is reached in real use and not merely in theory.
 *
 * ponytail: a positional parse of one provider's format, guarded rather than generalised.
 * The upgrade path, if another geocoder backend formats differently, is to add its shape
 * to `PlaceNameTest` from a real probe and widen this — not to guess.
 */
private fun townFromAddressLine(line: String?, vararg notThese: String?): String? {
    // "،" is the Arabic comma, which Google uses when the geocoder is asked in Arabic.
    val parts = line?.split(',', '،')?.map { it.trim() }?.filter { it.isNotEmpty() } ?: return null
    if (parts.size < 3) return null
    val town = parts[parts.size - 2]
        .substringBefore('/')
        .split(' ')
        .filter { word -> word.none { it.isDigit() } }
        .joinToString(" ")
        .trim()
    val abbreviation = town.all { !it.isLetter() || it.isUpperCase() }
    return town.takeIf {
        it.looksLikeAPlace() && !abbreviation && notThese.none { other -> other.equals(it, ignoreCase = true) }
    }
}

private fun String?.orNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

/**
 * [Address.getFeatureName] is only *usually* a place name. On a precise fix it is just as
 * likely to be a house number or a building, and "37, United Kingdom" is worse than the
 * county the user complained about. Anything without a letter in it is not a town.
 */
private fun String.looksLikeAPlace(): Boolean = any { it.isLetter() }
