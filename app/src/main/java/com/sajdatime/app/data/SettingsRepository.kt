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
 *
 * This is chosen **per prayer**, not once for the whole app. A tester asked for exactly
 * that — an alarm loud enough to wake him for Fajr, and a quiet notification for the four
 * he is already awake for — and a single global radio button could not express it.
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
    /**
     * How each prayer announces itself. A prayer absent from this map is silent — there
     * is no separate on/off, because "off" is simply having no style.
     *
     * All five start as a quiet notification. Nothing here is ever loud until asked.
     */
    val alertFor: Map<PrayerSlot, AlertStyle> = PrayerSlot.entries
        .filter { it.isPrayer }
        .associateWith { AlertStyle.NOTIFICATION },
    /** The glanceable badge is on out of the box; it is silent and costs nothing. */
    val ongoingBadge: Boolean = true,
    /** Chosen sound for [AlertStyle.ALARM]. Empty means the device's default alarm. */
    val alarmSoundUri: String = "",
    /**
     * Whether an alarm-style alert falls back to a silent notification while the phone
     * itself is silenced.
     *
     * On by default, and that is a real decision rather than a shrug. An alarm plays on
     * the alarm stream, which Android deliberately does *not* mute with the ringer — that
     * is right for a clock the user set for one morning, and wrong for a recurring alert
     * five times a day from an app they installed yesterday. Making noise from a phone
     * someone has visibly silenced is how a charity app earns an uninstall.
     *
     * The user who genuinely wants to be woken for Fajr regardless turns this off, and
     * the switch says plainly what that means. Requested by a tester.
     */
    val alarmRespectsSilent: Boolean = true,
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
     * True once the user has closed the calculation-method notice on the home screen.
     *
     * Same contract as [exactAlarmNoticeDismissed]: dismissing hides it on home only, and
     * Settings still shows the method row it points at. Choosing any method also stops it
     * appearing, so a user who reads the notice and acts on it is never asked twice — see
     * `HomeScreen.MethodBanner`.
     */
    val methodNoticeDismissed: Boolean = false,
    /**
     * Light, dark, or whatever the phone is set to. Following the system is the default,
     * and on a device that expresses no preference that resolves to light.
     */
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
) {
    val calculationPrefs: CalculationPrefs
        get() = CalculationPrefs(sect = sect, madhab = madhab, method = method)

    /** The prayers that announce themselves at all, in any style. */
    val notifyFor: Set<PrayerSlot> get() = alertFor.keys

    /** True when at least one prayer is set to the loud style. */
    val usesAlarm: Boolean get() = alertFor.containsValue(AlertStyle.ALARM)
}

/**
 * How [AppSettings.alertFor] is written to and read from a preference string, kept free of
 * DataStore and of Android so it can be tested without either.
 *
 * It is worth testing on its own because the failure is silent in both directions. A
 * decode that drops entries turns a prayer off without telling anyone, and a migration
 * that misreads the two keys this replaced would hand somebody who had deliberately
 * silenced four prayers four unexpected alerts a day after an update.
 */
internal object AlertCodec {

    /** "FAJR:ALARM,DHUHR:NOTIFICATION", in the enum's own order. */
    fun encode(alerts: Map<PrayerSlot, AlertStyle>): String =
        // Sorted rather than left to map iteration order: DataStore compares the written
        // value against the stored one, so a stable string means no pointless writes.
        alerts.entries
            .sortedBy { it.key.ordinal }
            .joinToString(",") { "${it.key.name}:${it.value.name}" }

    /**
     * An empty string means every prayer is silent — which is exactly why the caller must
     * distinguish an absent key from an empty one, and why this function is never asked
     * to invent a default.
     *
     * Every part is validated rather than trusted: an unknown slot or style is dropped, so
     * a preferences file written by a future version, or one holding a since-renamed enum
     * constant, loses a single entry instead of throwing on a background thread at Fajr.
     */
    fun decode(raw: String): Map<PrayerSlot, AlertStyle> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":").takeIf { it.size == 2 } ?: return@mapNotNull null
            val slot = prayerNamed(parts[0]) ?: return@mapNotNull null
            val style = AlertStyle.entries.firstOrNull { it.name == parts[1] } ?: return@mapNotNull null
            slot to style
        }.toMap()
    }

    /**
     * An install from before per-prayer styles: fold the two keys this replaced —
     * "which prayers" and one global "how" — into the new shape.
     *
     * [notifyRaw] absent means the old default, which was all five on.
     */
    fun migrate(notifyRaw: String?, styleName: String?): Map<PrayerSlot, AlertStyle> {
        val style = AlertStyle.entries.firstOrNull { it.name == styleName } ?: AlertStyle.NOTIFICATION
        val slots = when {
            notifyRaw == null -> PrayerSlot.entries.filter { it.isPrayer }
            notifyRaw.isBlank() -> emptyList()
            else -> notifyRaw.split(",").mapNotNull(::prayerNamed)
        }
        return slots.associateWith { style }
    }

    private fun prayerNamed(name: String): PrayerSlot? =
        PrayerSlot.entries.firstOrNull { it.name == name && it.isPrayer }
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

    /** [style] of null silences that prayer. */
    suspend fun setAlert(slot: PrayerSlot, style: AlertStyle?) = edit { prefs ->
        val current = prefs.decodeAlerts()
        val updated = if (style == null) current - slot else current + (slot to style)
        prefs[Keys.ALERTS] = AlertCodec.encode(updated)
    }

    suspend fun setOngoingBadge(enabled: Boolean) = edit { it[Keys.ONGOING] = enabled }

    suspend fun setAlarmSound(uri: String) = edit { it[Keys.ALARM_SOUND] = uri }

    suspend fun setAlarmRespectsSilent(respects: Boolean) =
        edit { it[Keys.ALARM_RESPECTS_SILENT] = respects }

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

    /** Closes the calculation-method notice on home for good. Settings still shows the row. */
    suspend fun dismissMethodNotice() = edit { it[Keys.METHOD_NOTICE_DISMISSED] = true }

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
        val ALERTS = stringPreferencesKey("alert_for")
        val ONGOING = booleanPreferencesKey("ongoing_badge")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound")
        val ALARM_RESPECTS_SILENT = booleanPreferencesKey("alarm_respects_silent")

        // Read but never written: the two keys ALERTS replaced. Kept so that an existing
        // install's choices survive the upgrade instead of silently reverting to the
        // defaults — which for someone who had turned four prayers off would mean four
        // unexpected alerts a day.
        val NOTIFY = stringPreferencesKey("notify_slots")
        val ALERT_STYLE = stringPreferencesKey("alert_style")
        val DISCLAIMER = booleanPreferencesKey("disclaimer_seen")
        val DEFAULT_LOCATION = booleanPreferencesKey("using_default_location")
        val EXACT_ALARM_DISMISSED = booleanPreferencesKey("exact_alarm_notice_dismissed")
        val METHOD_NOTICE_DISMISSED = booleanPreferencesKey("method_notice_dismissed")
        val THEME = stringPreferencesKey("theme_choice")
    }

    private companion object {
        /** The Kaaba. Used only when no real location can be established. */
        const val DEFAULT_LATITUDE = 21.4224779
        const val DEFAULT_LONGITUDE = 39.8251832
        const val DEFAULT_CITY = "Makkah"
    }

    private fun Preferences.decodeAlerts(): Map<PrayerSlot, AlertStyle> =
        this[Keys.ALERTS]
            ?.let(AlertCodec::decode)
            ?: AlertCodec.migrate(this[Keys.NOTIFY], this[Keys.ALERT_STYLE])

    private fun Preferences.toAppSettings(): AppSettings {
        return AppSettings(
            onboardingComplete = this[Keys.ONBOARDED] ?: false,
            sect = enumOr(this[Keys.SECT], Sect.SUNNI),
            madhab = enumOr(this[Keys.MADHAB], Madhab.SHAFII),
            method = enumOr(this[Keys.METHOD], CalcMethod.AUTO),
            // Range-checked rather than trusted: see Coordinates.orNull.
            coordinates = Coordinates.orNull(this[Keys.LATITUDE], this[Keys.LONGITUDE]),
            cityName = this[Keys.CITY] ?: "",
            alertFor = decodeAlerts(),
            ongoingBadge = this[Keys.ONGOING] ?: true,
            alarmSoundUri = this[Keys.ALARM_SOUND] ?: "",
            alarmRespectsSilent = this[Keys.ALARM_RESPECTS_SILENT] ?: true,
            disclaimerSeen = this[Keys.DISCLAIMER] ?: false,
            usingDefaultLocation = this[Keys.DEFAULT_LOCATION] ?: false,
            exactAlarmNoticeDismissed = this[Keys.EXACT_ALARM_DISMISSED] ?: false,
            methodNoticeDismissed = this[Keys.METHOD_NOTICE_DISMISSED] ?: false,
            themeChoice = enumOr(this[Keys.THEME], ThemeChoice.SYSTEM),
        )
    }

    /** Tolerates renamed/removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}
