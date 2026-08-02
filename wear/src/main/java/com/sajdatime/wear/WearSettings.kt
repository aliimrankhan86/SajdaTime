package com.sajdatime.wear

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.AdjustmentCodec
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sajdatime_wear")

/**
 * The watch's own copy of the settings that affect calculation.
 *
 * Kept local so the watch works with the phone out of range. [SettingsSyncService]
 * overwrites it whenever the phone sends an update.
 */
data class WearSettings(
    val sect: Sect = Sect.SUNNI,
    val madhab: Madhab = Madhab.SHAFII,
    val method: CalcMethod = CalcMethod.AUTO,
    val coordinates: Coordinates? = null,
    val cityName: String = "",
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
        context.dataStore.edit { prefs ->
            sect?.let { prefs[Keys.SECT] = it }
            madhab?.let { prefs[Keys.MADHAB] = it }
            method?.let { prefs[Keys.METHOD] = it }
            latitude?.let { prefs[Keys.LATITUDE] = it }
            longitude?.let { prefs[Keys.LONGITUDE] = it }
            cityName?.let { prefs[Keys.CITY] = it }
            // Written unconditionally, unlike the fields above. Those are absent from an
            // older phone's payload and must keep whatever the watch already had; these two
            // have "cleared" as a legitimate value the user can choose, and a null-guard
            // here would make removing a correction on the phone unable to reach the watch.
            prefs[Keys.ADJUSTMENTS] = adjustments.orEmpty()
            prefs[Keys.HIJRI_OFFSET] = hijriOffsetDays
        }
    }

    suspend fun setLocation(coordinates: Coordinates, cityName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = coordinates.latitude
            prefs[Keys.LONGITUDE] = coordinates.longitude
            prefs[Keys.CITY] = cityName
        }
    }

    suspend fun setSect(sect: Sect) {
        context.dataStore.edit { it[Keys.SECT] = sect.name }
    }

    suspend fun setMadhab(madhab: Madhab) {
        context.dataStore.edit { it[Keys.MADHAB] = madhab.name }
    }

    private object Keys {
        val SECT = stringPreferencesKey("sect")
        val MADHAB = stringPreferencesKey("madhab")
        val METHOD = stringPreferencesKey("method")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")
        val ADJUSTMENTS = stringPreferencesKey("adjustments")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset_days")
    }

    /** Tolerates renamed or removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
