package com.sajdatime.wear

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
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
) {
    val calculationPrefs: CalculationPrefs
        get() = CalculationPrefs(sect = sect, madhab = madhab, method = method)
}

class WearSettingsStore(private val context: Context) {

    val settings: Flow<WearSettings> = context.dataStore.data.map { prefs ->
        val lat = prefs[Keys.LATITUDE]
        val lng = prefs[Keys.LONGITUDE]
        WearSettings(
            sect = enumOr(prefs[Keys.SECT], Sect.SUNNI),
            madhab = enumOr(prefs[Keys.MADHAB], Madhab.SHAFII),
            method = enumOr(prefs[Keys.METHOD], CalcMethod.AUTO),
            coordinates = if (lat != null && lng != null) Coordinates(lat, lng) else null,
            cityName = prefs[Keys.CITY] ?: "",
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
    ) {
        context.dataStore.edit { prefs ->
            sect?.let { prefs[Keys.SECT] = it }
            madhab?.let { prefs[Keys.MADHAB] = it }
            method?.let { prefs[Keys.METHOD] = it }
            latitude?.let { prefs[Keys.LATITUDE] = it }
            longitude?.let { prefs[Keys.LONGITUDE] = it }
            cityName?.let { prefs[Keys.CITY] = it }
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
    }

    /** Tolerates renamed or removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
