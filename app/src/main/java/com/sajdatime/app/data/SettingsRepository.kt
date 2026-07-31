package com.sajdatime.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.app.ui.theme.ThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sajdatime")

/**
 * How a prayer time announces itself.
 *
 * The default is deliberately the quieter one: a notification that vibrates. Five full
 * alarms a day, unasked for, is the kind of thing that gets an app uninstalled. Users who
 * want an adhan or a louder tone opt into [ALARM] and choose their own sound.
 */
enum class AlertStyle {
    /** Heads-up notification with vibration. Respects Do Not Disturb. */
    NOTIFICATION,

    /** Alarm-category alert with a user-chosen sound, and it may bypass Do Not Disturb. */
    ALARM,
}

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
    /** The glanceable badge is on out of the box; it is silent and costs nothing. */
    val ongoingBadge: Boolean = true,
    val alertStyle: AlertStyle = AlertStyle.NOTIFICATION,
    /** Chosen sound for [AlertStyle.ALARM]. Empty means the device's default alarm. */
    val alarmSoundUri: String = "",
    val disclaimerSeen: Boolean = false,
    /** True once the user has been told the app fell back to Makkah. */
    val usingDefaultLocation: Boolean = false,
    /**
     * True once the user has closed the exact-alarm notice on the home screen.
     *
     * Dismissing hides it on **home only** — Settings keeps showing it for as long as the
     * permission is missing, so the way back is always one screen away. This is why the
     * flag is not reset when the permission is later granted and revoked again: the notice
     * is still on the screen whose job is fixing it, and re-nagging on home is precisely
     * what the user asked not to happen.
     */
    val exactAlarmNoticeDismissed: Boolean = false,
    /**
     * Light, dark, or whatever the phone is set to. Following the system is the default,
     * and on a device that expresses no preference that resolves to light.
     */
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
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
        it[Keys.DEFAULT_LOCATION] = false
    }

    suspend fun setNotify(slot: PrayerSlot, enabled: Boolean) = edit { prefs ->
        val current = prefs.decodeNotifySet()
        val updated = if (enabled) current + slot else current - slot
        prefs[Keys.NOTIFY] = updated.joinToString(",") { it.name }
    }

    suspend fun setOngoingBadge(enabled: Boolean) = edit { it[Keys.ONGOING] = enabled }

    suspend fun setAlertStyle(style: AlertStyle) = edit { it[Keys.ALERT_STYLE] = style.name }

    suspend fun setAlarmSound(uri: String) = edit { it[Keys.ALARM_SOUND] = uri }

    suspend fun setThemeChoice(choice: ThemeChoice) = edit { it[Keys.THEME] = choice.name }

    suspend fun completeOnboarding() = edit { it[Keys.ONBOARDED] = true }

    suspend fun markDisclaimerSeen() = edit { it[Keys.DISCLAIMER] = true }

    /**
     * Records that the app fell back to Makkah because no location could be established,
     * so the UI can say so plainly instead of showing times for a place the user is not in.
     */
    suspend fun useDefaultLocation() = edit {
        it[Keys.LATITUDE] = DEFAULT_LATITUDE
        it[Keys.LONGITUDE] = DEFAULT_LONGITUDE
        it[Keys.CITY] = DEFAULT_CITY
        it[Keys.DEFAULT_LOCATION] = true
    }

    /** Closes the exact-alarm notice on home for good. Settings still shows it. */
    suspend fun dismissExactAlarmNotice() = edit { it[Keys.EXACT_ALARM_DISMISSED] = true }

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
        val ALERT_STYLE = stringPreferencesKey("alert_style")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound")
        val DISCLAIMER = booleanPreferencesKey("disclaimer_seen")
        val DEFAULT_LOCATION = booleanPreferencesKey("using_default_location")
        val EXACT_ALARM_DISMISSED = booleanPreferencesKey("exact_alarm_notice_dismissed")
        val THEME = stringPreferencesKey("theme_choice")
    }

    private companion object {
        /** The Kaaba. Used only when no real location can be established. */
        const val DEFAULT_LATITUDE = 21.4224779
        const val DEFAULT_LONGITUDE = 39.8251832
        const val DEFAULT_CITY = "Makkah"
    }

    private fun Preferences.decodeNotifySet(): Set<PrayerSlot> {
        val raw = this[Keys.NOTIFY] ?: return PrayerSlot.entries.filter { it.isPrayer }.toSet()
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { name ->
            PrayerSlot.entries.firstOrNull { it.name == name && it.isPrayer }
        }.toSet()
    }

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            onboardingComplete = this[Keys.ONBOARDED] ?: false,
            sect = enumOr(this[Keys.SECT], Sect.SUNNI),
            madhab = enumOr(this[Keys.MADHAB], Madhab.SHAFII),
            method = enumOr(this[Keys.METHOD], CalcMethod.AUTO),
            // Range-checked rather than trusted: see Coordinates.orNull.
            coordinates = Coordinates.orNull(this[Keys.LATITUDE], this[Keys.LONGITUDE]),
            cityName = this[Keys.CITY] ?: "",
            notifyFor = decodeNotifySet(),
            ongoingBadge = this[Keys.ONGOING] ?: true,
            alertStyle = enumOr(this[Keys.ALERT_STYLE], AlertStyle.NOTIFICATION),
            alarmSoundUri = this[Keys.ALARM_SOUND] ?: "",
            disclaimerSeen = this[Keys.DISCLAIMER] ?: false,
            usingDefaultLocation = this[Keys.DEFAULT_LOCATION] ?: false,
            exactAlarmNoticeDismissed = this[Keys.EXACT_ALARM_DISMISSED] ?: false,
            themeChoice = enumOr(this[Keys.THEME], ThemeChoice.SYSTEM),
        )
    }

    /** Tolerates renamed/removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
