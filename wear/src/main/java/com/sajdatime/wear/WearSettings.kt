package com.sajdatime.wear

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.AdjustmentCodec
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.Sect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// An unreadable settings file becomes an empty one, so the watch shows "set up on your phone"
// again instead of crashing on open and at every tile refresh. The phone re-sends everything
// on its next sync. Same reasoning and same handler as the phone: see SettingsRepository.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sajdatime_wear",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

/**
 * The watch's own copy of the settings that affect calculation.
 *
 * Kept local so the watch works with the phone out of range. [SettingsSyncService] writes the
 * phone's settings here whenever it sends an update; the place follows the rules in WearLocation.kt.
 */
data class WearSettings(
    val sect: Sect = Sect.SUNNI,
    val madhab: Madhab = Madhab.SHAFII,
    val method: CalcMethod = CalcMethod.AUTO,
    /** The place the times are calculated for, whichever source it came from. The tile reads this. */
    val coordinates: Coordinates? = null,
    val cityName: String = "",
    /** Chosen on the wrist; null is automatic. See [LocationSource]. */
    val locationSource: LocationSource? = null,
    /**
     * The last place the phone sent, kept even while the watch uses another, so "Same as phone" can
     * be chosen at any time and shows which place it means.
     */
    val phoneCoordinates: Coordinates? = null,
    val phoneCityName: String = "",
    /** Mirrors the phone. See WatchSyncContract.KEY_ADJUSTMENTS for why it is synced. */
    val adjustments: Map<PrayerSlot, Int> = emptyMap(),
    val hijriOffsetDays: Int = 0,
) {
    val calculationPrefs: CalculationPrefs
        get() = CalculationPrefs(
            sect = sect,
            madhab = madhab,
            method = method,
            adjustments = adjustments,
            hijriOffsetDays = hijriOffsetDays,
        )
}

class WearSettingsStore(private val context: Context) {

    val settings: Flow<WearSettings> = context.dataStore.data.map { prefs ->
        WearSettings(
            sect = enumOr(prefs[Keys.SECT], Sect.SUNNI),
            madhab = enumOr(prefs[Keys.MADHAB], Madhab.SHAFII),
            method = enumOr(prefs[Keys.METHOD], CalcMethod.AUTO),
            // Range-checked, not trusted. This store is written by SettingsSyncService,
            // which has to be an exported component — see Coordinates.orNull.
            coordinates = Coordinates.orNull(prefs[Keys.LATITUDE], prefs[Keys.LONGITUDE]),
            cityName = prefs[Keys.CITY] ?: "",
            locationSource = prefs.source(),
            phoneCoordinates = prefs.phonePlace()?.first,
            phoneCityName = prefs.phonePlace()?.second.orEmpty(),
            adjustments = AdjustmentCodec.decode(prefs[Keys.ADJUSTMENTS]),
            hijriOffsetDays = (prefs[Keys.HIJRI_OFFSET] ?: 0).coerceIn(
                -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
                CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            ),
        )
    }

    /** Applies a settings payload received from the phone. */
    suspend fun applyFromPhone(
        sect: String?,
        madhab: String?,
        method: String?,
        latitude: Double?,
        longitude: Double?,
        cityName: String?,
        adjustments: String? = null,
        hijriOffsetDays: Int = 0,
    ) {
        write { prefs ->
            sect?.let { prefs[Keys.SECT] = it }
            madhab?.let { prefs[Keys.MADHAB] = it }
            method?.let { prefs[Keys.METHOD] = it }
            // The place travels as one: coordinates and their name together, or nothing. A phone
            // with no location yet sends its (empty) city alone, and that used to blank the name of
            // a place the watch was still using. The phone's place is always kept; it becomes the
            // watch's place only if the wearer has not chosen another (acceptsPhoneLocation).
            Coordinates.orNull(latitude, longitude)?.let { place ->
                val name = cityName.orEmpty()
                prefs[Keys.PHONE_LATITUDE] = place.latitude
                prefs[Keys.PHONE_LONGITUDE] = place.longitude
                val current = Coordinates.orNull(prefs[Keys.LATITUDE], prefs[Keys.LONGITUDE])
                if (acceptsPhoneLocation(prefs.source(), place, current)) prefs.setPlace(place, name)
                prefs[Keys.PHONE_CITY] = name
            }
            // Written unconditionally, unlike the fields above. Those are absent from an
            // older phone's payload and must keep whatever the watch already had; these two
            // have "cleared" as a legitimate value the user can choose, and a null-guard
            // here would make removing a correction on the phone unable to reach the watch.
            prefs[Keys.ADJUSTMENTS] = adjustments.orEmpty()
            prefs[Keys.HIJRI_OFFSET] = hijriOffsetDays
        }
    }

    /**
     * The watch's own fix. Used only if the wearer chose this watch, or chose nothing and the phone
     * has never sent a place. Decided inside the write, against what is stored at that moment, so a
     * fix that lands just after a choice or a sync cannot undo it. Returns whether it was used.
     */
    suspend fun adoptWatchFix(coordinates: Coordinates): Boolean {
        var used = false
        write { prefs ->
            if (acceptsWatchFix(prefs.source(), isRealPlace(prefs.phonePlace()?.first))) {
                // No name: the watch has no geocoder, on purpose (see LocationSource).
                prefs.setPlace(coordinates, "")
                used = true
            }
        }
        return used
    }

    /**
     * Makkah when the watch has nothing else, from the "No location yet" screen. The source is left
     * alone, so the watch's first real fix or the phone's place still replaces it, as it always has:
     * a fallback tapped on a day with no signal must not pin someone in London to Makkah's times.
     * Choosing Makkah on the Location screen is the deliberate version ([chooseLocation]).
     */
    suspend fun useMakkahUntilLocated() {
        write { it.setPlace(QiblaEngine.KAABA, MAKKAH) }
    }

    /**
     * The wearer's choice on the Location screen. "This watch" does not move the place by itself: the
     * caller asks for a fix, and until it lands the times stay on the last place rather than vanishing.
     */
    suspend fun chooseLocation(source: LocationSource) {
        write { prefs ->
            prefs[Keys.LOCATION_SOURCE] = source.name
            when (source) {
                LocationSource.PHONE -> prefs.phonePlace()?.let { (place, name) -> prefs.setPlace(place, name) }
                LocationSource.MAKKAH -> prefs.setPlace(QiblaEngine.KAABA, MAKKAH)
                LocationSource.WATCH -> Unit
            }
        }
    }

    /**
     * The place the phone last sent, with its name. Read from its own keys, or, on a watch upgraded from
     * 1.2.0 that has none yet, recognised in the old keys: before 1.5.0 the watch wrote its own fix with no
     * name and its fallback as Makkah, so a named place there that is not Makkah can only have come from
     * the phone. Without this an upgraded watch kept the old bug (its fix replacing the phone's city on
     * every open) until the phone app next ran, which can be days (independent review, 22 Oct 2026).
     */
    private fun Preferences.phonePlace(): Pair<Coordinates, String>? {
        Coordinates.orNull(this[Keys.PHONE_LATITUDE], this[Keys.PHONE_LONGITUDE])
            ?.let { return it to this[Keys.PHONE_CITY].orEmpty() }
        val legacy = Coordinates.orNull(this[Keys.LATITUDE], this[Keys.LONGITUDE]) ?: return null
        val name = this[Keys.CITY].orEmpty()
        return if (name.isNotBlank() && legacy != QiblaEngine.KAABA) legacy to name else null
    }

    /** Null, automatic, for an absent or unknown name, so a renamed choice never crashes an update. */
    private fun Preferences.source(): LocationSource? =
        this[Keys.LOCATION_SOURCE]?.let { name -> LocationSource.entries.firstOrNull { it.name == name } }

    private fun MutablePreferences.setPlace(coordinates: Coordinates, cityName: String) {
        this[Keys.LATITUDE] = coordinates.latitude
        this[Keys.LONGITUDE] = coordinates.longitude
        this[Keys.CITY] = cityName
    }

    suspend fun setSect(sect: Sect) {
        write {
            it[Keys.SECT] = sect.name
            // The phone's setter has done this since the first release and the watch's never
            // did: a method synced from the phone (say Karachi) stayed put when the wearer
            // switched to Shia on the wrist, and the Jafari Maghrib was silently lost.
            // Same rule as the phone, from the same definition. See HANDOVER §15 lesson 84
            // for why a fix in one module has to be looked for in the sibling.
            if (!enumOr(it[Keys.METHOD], CalcMethod.AUTO).offeredTo(sect)) {
                it[Keys.METHOD] = CalcMethod.AUTO.name
            }
        }
    }

    suspend fun setMadhab(madhab: Madhab) {
        write { it[Keys.MADHAB] = madhab.name }
    }

    /** Every write goes through here, so the tile redraws with the new settings at once. */
    private suspend fun write(change: suspend (MutablePreferences) -> Unit) {
        context.dataStore.edit { prefs ->
            // An upgraded watch's phone place is copied into its own keys before anything else is written,
            // so a watch fix stored in the same breath cannot erase the only record of it.
            if (prefs[Keys.PHONE_LATITUDE] == null) {
                prefs.phonePlace()?.let { (place, name) ->
                    prefs[Keys.PHONE_LATITUDE] = place.latitude
                    prefs[Keys.PHONE_LONGITUDE] = place.longitude
                    prefs[Keys.PHONE_CITY] = name
                }
            }
            change(prefs)
        }
        refreshTile(context)
    }

    private object Keys {
        val SECT = stringPreferencesKey("sect")
        val MADHAB = stringPreferencesKey("madhab")
        val METHOD = stringPreferencesKey("method")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")
        val LOCATION_SOURCE = stringPreferencesKey("location_source")
        val PHONE_LATITUDE = doublePreferencesKey("phone_latitude")
        val PHONE_LONGITUDE = doublePreferencesKey("phone_longitude")
        val PHONE_CITY = stringPreferencesKey("phone_city")
        val ADJUSTMENTS = stringPreferencesKey("adjustments")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset_days")
    }

    private companion object {
        /** Stored and synced as this, the same as the phone; shown in the watch's language. */
        const val MAKKAH = "Makkah"
    }

    /** Tolerates renamed or removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
