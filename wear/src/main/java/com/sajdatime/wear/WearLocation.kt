package com.sajdatime.wear

import com.sajdatime.core.Coordinates
import com.sajdatime.core.QiblaEngine

/**
 * Where the watch's place comes from, as chosen on the wrist.
 *
 * Until 1.5.0 the watch had no say in this at all. Whichever wrote last won: the phone's sync,
 * the watch's own fix on every open, or the Makkah fallback. So a person who had chosen a city on
 * the phone saw it replaced by the watch's own position the next time they raised their wrist, a
 * person with no phone app who had tapped Makkah could never leave it if the watch had no fix, and
 * the watch never said which place its times were for. The owner found this on 22 Oct 2026: "on the
 * wearable the user cant select or change their location".
 *
 * Stored as its name; absent means automatic (see [acceptsPhoneLocation] and [acceptsWatchFix]).
 * Automatic is the default for everyone, including every watch that upgrades. On an upgraded watch the
 * place the phone last sent is recognised in the old keys (WearSettingsStore.phonePlace), so
 * the phone's city is kept from the first open rather than from the phone app's next sync.
 *
 * Rejected, 22 Oct 2026: a city search on the watch. Typing on a watch is hard, and turning a name
 * into coordinates needs a geocoder, which on Wear OS is either missing or answered by Google over
 * the network. That would be a new data flow on the watch, a privacy policy change and a new
 * permission (INTERNET) for a lookup the phone already does better. A city is chosen on the phone and
 * "Same as phone" carries it. An offline city list stays the owner's open decision (HANDOVER 5.21).
 */
enum class LocationSource {
    /** The location the phone app sent, whatever the phone uses (its own fix or a chosen city). */
    PHONE,

    /** The watch's own fix, from its GPS or from the paired phone's location service. */
    WATCH,

    /** Makkah, chosen on purpose. Nothing replaces it until the wearer chooses again. */
    MAKKAH,
}

/**
 * Whether the phone sent a real place, rather than its own Makkah fallback. The phone stores and sends
 * Makkah like any place when it has no location (location denied on the phone, say), and without this a
 * watch with perfectly good GPS would follow the phone to Makkah's times and stop finding itself: the
 * very trap [WearSettingsStore.useMakkahUntilLocated] exists to avoid (independent review, 22 Oct 2026).
 */
fun isRealPlace(coordinates: Coordinates?): Boolean =
    coordinates != null && coordinates != QiblaEngine.KAABA

/**
 * Whether a location the phone sends becomes the one the watch uses. Automatic follows the phone,
 * because the phone is where the place is chosen and two devices from one app showing two sets of
 * times is the worst bug this project has had (HANDOVER §10, "What running both emulators side by
 * side caught"). In automatic, the phone's Makkah fallback is taken only when the watch has no real
 * place of its own, because it is better than nothing and worse than anything else.
 */
fun acceptsPhoneLocation(source: LocationSource?, phonePlace: Coordinates, current: Coordinates?): Boolean =
    when (source) {
        LocationSource.PHONE -> true
        null -> isRealPlace(phonePlace) || !isRealPlace(current)
        else -> false
    }

/**
 * Whether the watch's own fix becomes the one it uses. Automatic takes it only while the phone has
 * never sent a real place, which is a watch used without the phone app, or with a phone that has none.
 */
fun acceptsWatchFix(source: LocationSource?, phoneHasRealPlace: Boolean): Boolean =
    source == LocationSource.WATCH || (source == null && !phoneHasRealPlace)

/**
 * Which choice to show as selected. An explicit choice is shown as it is. Automatic is shown as what
 * it is doing now, so the screen never says "automatic" (a word that explains nothing on a wrist).
 * Null only when the watch has no place at all.
 */
fun shownSource(settings: WearSettings): LocationSource? = settings.locationSource ?: when {
    isRealPlace(settings.phoneCoordinates) -> LocationSource.PHONE
    settings.coordinates == QiblaEngine.KAABA -> LocationSource.MAKKAH
    settings.coordinates != null -> LocationSource.WATCH
    else -> null
}

/** What the place label says, before it is turned into words. */
sealed interface PlaceLabel {
    data object Makkah : PlaceLabel
    data class Named(val name: String) : PlaceLabel
    /** A place with no name: a watch fix (the watch has no geocoder, on purpose) or a phone with none. */
    data object Unnamed : PlaceLabel
}

/**
 * The label for a place. Makkah is recognised by its coordinates rather than its stored name, so it
 * is shown in the watch's language ("Makkah" is what is stored and synced, the same as the phone).
 */
fun placeLabel(coordinates: Coordinates?, name: String): PlaceLabel? = when {
    coordinates == null -> null
    coordinates == QiblaEngine.KAABA -> PlaceLabel.Makkah
    name.isNotBlank() -> PlaceLabel.Named(name)
    else -> PlaceLabel.Unnamed
}
