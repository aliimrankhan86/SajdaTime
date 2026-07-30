package com.sajdatime.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.app.core.CalcMethod
import com.sajdatime.app.core.CalculationPrefs
import com.sajdatime.app.core.Coordinates
import com.sajdatime.app.core.Madhab
import com.sajdatime.app.core.PrayerSlot
import com.sajdatime.app.core.Sect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sajdatime")

/**
 * All user state, stored locally on the device only. Nothing here is ever transmitted.
 */
data class AppSettings(
    val onboardingComplete: Boolean = false,
    val sect: Sect = Sect.SUNNI,
    val madhab: Madhab = Madhab.SHAFII,
    val method: CalcMethod = CalcMethod.AUTO,
    val coordinates: Coordinates? = null,
    val cityName: String = "",
    /** Per-prayer notification switches. Absent slots default to on. */
    val notifyFor: Set<PrayerSlot> = PrayerSlot.entries.filter { it.isPrayer }.toSet(),
    val ongoingBadge: Boolean = false,
) {
    val calculationPrefs: CalculationPrefs
        get() = CalculationPrefs(sect = sect, madhab = madhab, method = method)
}

class SettingsRepository(private val context: Context) {

    val settings: Flow<AppSettings> = context.dataStore.data.map { it.toAppSettings() }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setSect(sect: Sect) = edit {
        it[Keys.SECT] = sect.name
        // A Shia user has no madhab choice, so reset the method to AUTO (-> Jafari)
        // instead of silently keeping a Sunni convention they picked earlier.
        if (sect == Sect.SHIA) it[Keys.METHOD] = CalcMethod.AUTO.name
    }

    suspend fun setMadhab(madhab: Madhab) = edit { it[Keys.MADHAB] = madhab.name }

    suspend fun setMethod(method: CalcMethod) = edit { it[Keys.METHOD] = method.name }

    suspend fun setLocation(coordinates: Coordinates, cityName: String) = edit {
        it[Keys.LATITUDE] = coordinates.latitude
        it[Keys.LONGITUDE] = coordinates.longitude
        it[Keys.CITY] = cityName
    }

    suspend fun setNotify(slot: PrayerSlot, enabled: Boolean) = edit { prefs ->
        val current = prefs.decodeNotifySet()
        val updated = if (enabled) current + slot else current - slot
        prefs[Keys.NOTIFY] = updated.joinToString(",") { it.name }
    }

    suspend fun setOngoingBadge(enabled: Boolean) = edit { it[Keys.ONGOING] = enabled }

    suspend fun completeOnboarding() = edit { it[Keys.ONBOARDED] = true }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarding_complete")
        val SECT = stringPreferencesKey("sect")
        val MADHAB = stringPreferencesKey("madhab")
        val METHOD = stringPreferencesKey("method")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY = stringPreferencesKey("city")
        val NOTIFY = stringPreferencesKey("notify_slots")
        val ONGOING = booleanPreferencesKey("ongoing_badge")
    }

    private fun Preferences.decodeNotifySet(): Set<PrayerSlot> {
        val raw = this[Keys.NOTIFY] ?: return PrayerSlot.entries.filter { it.isPrayer }.toSet()
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { name ->
            PrayerSlot.entries.firstOrNull { it.name == name && it.isPrayer }
        }.toSet()
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val lat = this[Keys.LATITUDE]
        val lng = this[Keys.LONGITUDE]
        return AppSettings(
            onboardingComplete = this[Keys.ONBOARDED] ?: false,
            sect = enumOr(this[Keys.SECT], Sect.SUNNI),
            madhab = enumOr(this[Keys.MADHAB], Madhab.SHAFII),
            method = enumOr(this[Keys.METHOD], CalcMethod.AUTO),
            coordinates = if (lat != null && lng != null) Coordinates(lat, lng) else null,
            cityName = this[Keys.CITY] ?: "",
            notifyFor = decodeNotifySet(),
            ongoingBadge = this[Keys.ONGOING] ?: false,
        )
    }

    /** Tolerates renamed/removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
