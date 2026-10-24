package com.sajdatime.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sajdatime.core.AdjustmentCodec
import com.sajdatime.core.BellRules
import com.sajdatime.core.BellState
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.FastKind
import com.sajdatime.core.JamaahCodec
import com.sajdatime.core.JamaahRule
import com.sajdatime.core.JamaahTimes
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.app.ui.theme.ThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * A settings file that cannot be parsed becomes an empty one.
 *
 * DataStore's default is to rethrow on every read, and a read happens at the top of every
 * receiver and every launch. One unreadable file (a full disk during a write, a bad flash
 * block: rare, because DataStore writes atomically, but not impossible) would then crash the
 * app on open and crash the alarm receiver at every prayer, silently, with no crash reporting
 * to say so. Starting again from defaults costs the user a repeat of setup. Every field below
 * already has a default for exactly this case. Tested in SettingsCorruptionTest.
 */
internal fun settingsCorruptionHandler(): ReplaceFileCorruptionHandler<Preferences> =
    ReplaceFileCorruptionHandler { emptyPreferences() }

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sajdatime",
    corruptionHandler = settingsCorruptionHandler(),
)

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
 * All user state, stored locally on the device only. Nothing here is ever transmitted, with one
 * exception that is the user's own choice: [analyticsEnabled] decides whether anonymous-ID usage
 * counts are sent to Google Analytics (see `UsageCounts`). Even then, the *settings* held here —
 * sect, madhab, method, alerts, city, coordinates — are never part of what is sent.
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
    /**
     * Per-prayer correction in minutes, for matching the local mosque. Empty for everyone
     * who has never opened the row, which is the intended common case — this is a
     * reconciliation tool, not a setting anybody should have to think about.
     */
    val adjustments: Map<PrayerSlot, Int> = emptyMap(),
    /** Days to shift the Hijri date by, for mosques that follow local sighting. */
    val hijriOffsetDays: Int = 0,
    /** The glanceable badge is on out of the box; it is silent and costs nothing. */
    val ongoingBadge: Boolean = true,
    /** Chosen sound for [AlertStyle.ALARM]. Empty means the device's default alarm. */
    val alarmSoundUri: String = "",
    /**
     * Whether an Alarm falls back to a quiet notification while the phone is on silent, vibrate or
     * Do Not Disturb. Shown inverted as "Play on silent" (off by default), so a phone on silent stays
     * quiet unless the user asks otherwise, like other prayer apps. Existing users keep their choice.
     */
    val alarmRespectsSilent: Boolean = true,
    /** The phone sound a built-in adhan replaced, so the phone's own picker can open on it. */
    val phoneAlarmSoundUri: String = "",
    /**
     * Chosen sound for [AlertStyle.NOTIFICATION] (owner, 18 Oct 2026: "user can select adhan or another
     * sound for an alarm or notification"). Empty means the phone's usual notification tone, which is
     * what every notification played before this existed, so nobody's sound changes until they pick one.
     * It plays on the notification volume, so silent, vibrate and Do Not Disturb treat it like any
     * other notification.
     */
    val notificationSoundUri: String = "",
    /** As [phoneAlarmSoundUri], for the notification sound. */
    val phoneNotificationSoundUri: String = "",
    /**
     * Whether a prayer whose time had to be projected from another latitude may still ring
     * as a full alarm. **Off by default** — those days drop to a notification.
     *
     * Above the polar circles the sun does not always rise or set, so there is no true Fajr
     * or Isha to calculate and the app borrows the times from a lower latitude. That is a
     * published ruling, not a guess, but it is still a borrowed answer, and mosques in the
     * same town follow different ones. A notification states the time and marks it
     * approximate; an alarm asserts it hard enough to wake someone at two in the morning.
     *
     * **The switch exists because the default has a real cost, and it is not hypothetical.**
     * At Tromso every day from late May to late July is projected, so a user who set a Fajr
     * alarm would find it silently stop working for two months of the year. Leaving them no
     * way back would be the app settling a religious question on their behalf — the same
     * objection that got auto-switching the calculation method rejected (HANDOVER §11, A2).
     * Turning this on says "wake me anyway", and it is the user's to say.
     *
     * Shown only to users whose own location actually produces such days — see
     * `SettingsScreen.hasApproximateDays`. Everyone below the polar circles never sees it.
     */
    val alarmOnApproximateDays: Boolean = false,
    val disclaimerSeen: Boolean = false,
    /**
     * Whether the user has opted in to sending usage counts. Off until they say yes, and the
     * only record of that consent. Absent on every existing install, so it reads as false.
     */
    val analyticsEnabled: Boolean = false,
    /** True once the user has tapped Yes or No on the usage counts question, in setup or later. */
    val analyticsAnswered: Boolean = false,
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
    /**
     * Whether the Ramadan card and the notice before it appear on the Times screen. On, because a
     * person who never opens Settings should get Ramadan without asking for it, and a switch,
     * because not everyone fasts (illness, travel, pregnancy) and a daily fasting card must be
     * removable. Turning it off hides the card, the notice and the day 1 and 2 hint. It leaves the
     * widget labels, the timetable PDF and the suhoor reminder to their own settings.
     */
    val ramadanCardEnabled: Boolean = true,
    /**
     * The Hijri year whose notice the user closed, or 0. A year and not a flag, so the notice
     * returns next Ramadan without anything having to reset it.
     */
    val ramadanNoticeDismissedYear: Int = 0,
    /**
     * The [com.sajdatime.core.EidDay.code] of the Eid greeting the user last closed, or 0. One
     * code and not a flag: each Eid has its own code, so closing Eid al-Fitr 1448 leaves Eid
     * al-Adha 1448 and every later Eid to show without anything having to reset it.
     */
    val eidGreetingDismissedCode: Int = 0,
    /**
     * Minutes before Fajr at which the suhoor reminder fires during Ramadan, or 0 for off.
     * **60 by default** (owner, 21 Oct 2026: "60 mins before suhoor ends by default unless the user
     * switches it off - Only during ramadan then off itself automatically"). This reverses the 1.4.0
     * rule "off by default, nothing makes noise until asked", on purpose: a person who never opens
     * Settings should still be woken for suhoor. It stays polite because the default style is a
     * quiet Notification on the phone's usual tone, it only exists on days of Ramadan
     * ([com.sajdatime.app.notify.SuhoorReminderPlan]), and the Ramadan notice says so from 3 days
     * before. An unset key reads as 60, so 1.4.0 users who never chose get it too; anyone who chose
     * Off has 0 stored and keeps it. Read through [SUHOOR_CHOICES], so a stored value that is not
     * one of the five choices (a corrupt store, an older or newer build) reads as off.
     */
    val suhoorReminderMinutes: Int = SUHOOR_DEFAULT_MINUTES,
    /**
     * How the reminder announces itself. The same two styles as a prayer alert, and it goes through
     * the same [com.sajdatime.app.notify.effectiveAlertStyle], so the silent phone switch, the
     * polar approximate day downgrade and the Do Not Disturb handling apply to it unchanged.
     */
    val suhoorReminderStyle: AlertStyle = AlertStyle.NOTIFICATION,
    /**
     * Whether the suhoor reminder plays the built-in adhan of the user's school. **False by default,
     * and the reminder never borrows the prayer sounds** (owner, 21 Oct 2026: "Never the adhan but if
     * the user wants to choose it then they should be able to select adhaan"): an adhan an hour before
     * Fajr announces a prayer that has not come. False is the phone's usual tone, on the suhoor
     * reminder's own channel ([com.sajdatime.app.notify.Notifications.ensureSuhoorChannel]).
     */
    val suhoorUsesAdhan: Boolean = false,
    /**
     * Minutes before each chosen prayer at which a quiet heads up arrives, or 0 for off. **Off by
     * default** (owner, 17 Oct 2026, from the competitor review: "prenotifications" was the feature a
     * Pillars user missed most). Read through [PRAYER_REMINDER_CHOICES], like the suhoor minutes.
     * Always a notification, never an alarm: it is a nudge before the alert the person already chose.
     */
    val prayerReminderMinutes: Int = 0,
    /**
     * Which prayers get that heads up. All five until the person unticks some; it means nothing
     * while [prayerReminderMinutes] is 0.
     */
    val prayerReminderFor: Set<PrayerSlot> = PrayerSlot.entries.filter { it.isPrayer }.toSet(),
    /**
     * "My prayers", the private tracker (1.6.0). **Off by default**: nothing new appears on the Times
     * screen unless the user puts it there. Switching it off keeps the record; only Delete clears it.
     * The record itself lives in [TrackerRepository], never here and never in the usage counts.
     */
    val trackerEnabled: Boolean = false,
    /** Whether the "days in a row" line shows. Off by default (Ali, 16 Oct 2026): a streak is opt in. */
    val trackerShowStreak: Boolean = false,
    /**
     * The user's own mosque's jama'ah and Jumu'ah times (1.6.0), typed in by them. Empty for everyone
     * until set. Shown on Times only, never synced to the watch or the widgets in 1.6.0, and never
     * part of the usage counts.
     */
    val jamaah: JamaahTimes = JamaahTimes(),
    /**
     * Minutes before each jama'ah at which a quiet heads up arrives (1.6.1), or 0 for off. Off by
     * default, read through [PRAYER_REMINDER_CHOICES] like the prayer reminder. A notification only,
     * never the alarm or the adhan, and never a change to Do Not Disturb.
     */
    val jamaahReminderMinutes: Int = 0,
    /**
     * Which Sunnah fasts get a reminder the evening before (1.6.1). Empty, so off, by default, and
     * only ever acted on for Sunni users ([com.sajdatime.core.FastingDays]).
     */
    val fastReminders: Set<FastKind> = emptySet(),
    /**
     * The style each prayer had when its alert last went off (the bell or the alerts dialog), so the
     * bell turns it back on as it was (1.6.0 step 3). A prayer with no entry comes back as a notification.
     */
    val lastAlertStyle: Map<PrayerSlot, AlertStyle> = emptyMap(),
    /** Reminders the bell took away, which the bell (or its Undo) gives back and nothing else does. */
    val bellRemovedReminder: Set<PrayerSlot> = emptySet(),
    /** True once the user closed the "Allow notifications" notice on Times. A bell turned on while blocked clears it. */
    val notificationsNoticeDismissed: Boolean = false,
    /** True once the app has launched the system's notification permission ask from the bell or the notice. */
    val notificationPermissionAsked: Boolean = false,
) {
    val calculationPrefs: CalculationPrefs
        get() = CalculationPrefs(
            sect = sect,
            madhab = madhab,
            method = method,
            adjustments = adjustments,
            hijriOffsetDays = hijriOffsetDays,
        )

    /** The prayers that announce themselves at all, in any style. */
    val notifyFor: Set<PrayerSlot> get() = alertFor.keys

    /**
     * True when at least one prayer, or the suhoor reminder, is set to the loud style. The reminder
     * counts so that a person whose only alarm is the reminder still gets the Do Not Disturb card
     * and the sound row in Settings.
     */
    val usesAlarm: Boolean
        get() = alertFor.containsValue(AlertStyle.ALARM) ||
            (suhoorReminderMinutes > 0 && suhoorReminderStyle == AlertStyle.ALARM)

    /**
     * True when any prayer is a notification, so the Notification sound row applies. The suhoor
     * reminder does not count since 21 Oct 2026: it has its own sound choice and never uses this one.
     */
    val usesNotification: Boolean
        get() = alertFor.containsValue(AlertStyle.NOTIFICATION)

    /** True when any prayer is an alarm, so the Alarm sound row applies (not the suhoor reminder, as above). */
    val prayersUseAlarm: Boolean
        get() = alertFor.containsValue(AlertStyle.ALARM)

    companion object {
        /** The reminder choices, in minutes before Fajr. 0 is off. All are well clear of Fajr itself. */
        val SUHOOR_CHOICES: List<Int> = listOf(0, 15, 30, 45, 60)

        /** The reminder a person gets without choosing: an hour before Fajr (owner, 21 Oct 2026). */
        const val SUHOOR_DEFAULT_MINUTES = 60

        /** Minutes before a prayer. 0 is off. Kept short: this is "get ready", not a second alarm. */
        val PRAYER_REMINDER_CHOICES: List<Int> = listOf(0, 5, 10, 15, 20, 30)
    }
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
        // A method the new school was never offered goes back to AUTO rather than being
        // silently kept: a Sunni convention on a Shia user loses the Jafari Maghrib, and a
        // Shia one on a Sunni user gains it. This used to reset only on the way to Shia;
        // the other direction became reachable in one gesture once first-run setup gained a
        // method step with a Back button, so both directions are covered now, from the
        // same definition of "offered" the list itself uses.
        val method = enumOr(it[Keys.METHOD], CalcMethod.AUTO)
        if (!method.offeredTo(sect)) it[Keys.METHOD] = CalcMethod.AUTO.name
        // The same rule for the alarm sound: each school has its own built-in adhan, so a chosen
        // adhan follows the school (alarmSoundForSect). Nothing else about the sound changes.
        val sound = it[Keys.ALARM_SOUND] ?: ""
        it[Keys.ALARM_SOUND] = alarmSoundForSect(sect, sound, it[Keys.PHONE_ALARM_SOUND] ?: "", context.packageName)
        val notificationSound = it[Keys.NOTIFICATION_SOUND] ?: ""
        it[Keys.NOTIFICATION_SOUND] = alarmSoundForSect(
            sect, notificationSound, it[Keys.PHONE_NOTIFICATION_SOUND] ?: "", context.packageName,
        )
    }

    suspend fun setMadhab(madhab: Madhab) = edit { it[Keys.MADHAB] = madhab.name }

    suspend fun setMethod(method: CalcMethod) = edit { it[Keys.METHOD] = method.name }

    suspend fun setLocation(coordinates: Coordinates, cityName: String) = edit {
        it[Keys.LATITUDE] = coordinates.latitude
        it[Keys.LONGITUDE] = coordinates.longitude
        it[Keys.CITY] = cityName
        it[Keys.DEFAULT_LOCATION] = false
    }

    /** [style] of null silences that prayer. Off also records the style, for the bell (BellRules). */
    suspend fun setAlert(slot: PrayerSlot, style: AlertStyle?) = edit { prefs ->
        prefs.writeBell(BellRules.setFromSettings(prefs.readBell(), slot, style))
    }

    /**
     * The bell turns [slot] off, in one edit so the file never holds half a change. Returns whether a
     * reminder that would have fired went off, which picks the message.
     */
    suspend fun bellOff(slot: PrayerSlot): Boolean {
        var reminderRemoved = false
        edit { prefs ->
            val (next, removed) = BellRules.off(prefs.readBell(), slot, prefs.readReminderMinutes())
            reminderRemoved = removed
            prefs.writeBell(next)
        }
        return reminderRemoved
    }

    /** The bell (or its Undo) turns [slot] on again with its last style and the reminder the bell took. */
    suspend fun bellOn(slot: PrayerSlot) = edit { prefs ->
        prefs.writeBell(BellRules.on(prefs.readBell(), slot, AlertStyle.NOTIFICATION))
    }

    /** A [minutes] of 0 clears the correction rather than storing a no-op. */
    suspend fun setAdjustment(slot: PrayerSlot, minutes: Int) = edit { prefs ->
        val clamped = minutes.coerceIn(
            -CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
            CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
        )
        val current = AdjustmentCodec.decode(prefs[Keys.ADJUSTMENTS])
        val updated = if (clamped == 0) current - slot else current + (slot to clamped)
        prefs[Keys.ADJUSTMENTS] = AdjustmentCodec.encode(updated)
    }

    /** Drops every correction at once, so a user who has lost track can start again. */
    suspend fun clearAdjustments() = edit { it[Keys.ADJUSTMENTS] = "" }

    suspend fun setHijriOffsetDays(days: Int) = edit {
        it[Keys.HIJRI_OFFSET] = days.coerceIn(
            -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
        )
    }

    suspend fun setOngoingBadge(enabled: Boolean) = edit { it[Keys.ONGOING] = enabled }

    /** A sound from the phone's own list, or "" for the phone's default alarm. */
    suspend fun setAlarmSound(uri: String) = edit { it[Keys.ALARM_SOUND] = uri }

    suspend fun setAlarmRespectsSilent(respects: Boolean) =
        edit { it[Keys.ALARM_RESPECTS_SILENT] = respects }

    /**
     * A built-in adhan. The sound it replaces, when that is a phone sound, is remembered in
     * [Keys.PHONE_ALARM_SOUND], so the phone's own picker opens on it later (MainActivity).
     * Copied here rather than when the phone sound is picked, so a sound chosen before 1.5.0, which
     * never went through the new code, is remembered too (independent review, 17 Oct 2026).
     */
    suspend fun setBuiltInAdhan(adhan: BuiltInAdhan) = edit {
        val current = it[Keys.ALARM_SOUND] ?: ""
        if (BuiltInAdhan.of(current) == null) it[Keys.PHONE_ALARM_SOUND] = current
        it[Keys.ALARM_SOUND] = adhan.uri(context.packageName)
    }

    /** A sound from the phone's own list, or "" for the phone's usual notification tone. */
    suspend fun setNotificationSound(uri: String) = edit { it[Keys.NOTIFICATION_SOUND] = uri }

    /** [setBuiltInAdhan], for the notification sound. */
    suspend fun setBuiltInNotificationAdhan(adhan: BuiltInAdhan) = edit {
        val current = it[Keys.NOTIFICATION_SOUND] ?: ""
        if (BuiltInAdhan.of(current) == null) it[Keys.PHONE_NOTIFICATION_SOUND] = current
        it[Keys.NOTIFICATION_SOUND] = adhan.uri(context.packageName)
    }

    suspend fun setAlarmOnApproximateDays(allowed: Boolean) =
        edit { it[Keys.ALARM_ON_APPROXIMATE] = allowed }

    /** Anything that is not one of [AppSettings.SUHOOR_CHOICES] is stored as 0, off. */
    suspend fun setSuhoorReminderMinutes(minutes: Int) = edit {
        it[Keys.SUHOOR_MINUTES] = minutes.takeIf { m -> m in AppSettings.SUHOOR_CHOICES } ?: 0
    }

    suspend fun setSuhoorReminderStyle(style: AlertStyle) = edit {
        it[Keys.SUHOOR_STYLE] = style.name
    }

    suspend fun setSuhoorUsesAdhan(adhan: Boolean) = edit { it[Keys.SUHOOR_ADHAN] = adhan }

    /** Anything that is not one of [AppSettings.PRAYER_REMINDER_CHOICES] is stored as 0, off. */
    suspend fun setPrayerReminderMinutes(minutes: Int) = edit {
        it[Keys.PRAYER_REMINDER_MINUTES] = minutes.takeIf { m -> m in AppSettings.PRAYER_REMINDER_CHOICES } ?: 0
    }

    suspend fun setPrayerReminder(slot: PrayerSlot, on: Boolean) = edit { prefs ->
        prefs.writeBell(BellRules.setReminderFromSettings(prefs.readBell(), slot, on))
    }

    suspend fun setTrackerEnabled(enabled: Boolean) = edit { it[Keys.TRACKER_ENABLED] = enabled }

    suspend fun setTrackerShowStreak(show: Boolean) = edit { it[Keys.TRACKER_STREAK] = show }

    /** [rule] of null clears that prayer's jama'ah time. */
    suspend fun setJamaahRule(slot: PrayerSlot, rule: JamaahRule?) = edit { prefs ->
        val current = JamaahCodec.decode(prefs[Keys.JAMAAH])
        val rules = if (rule == null) current.rules - slot else current.rules + (slot to rule)
        prefs[Keys.JAMAAH] = JamaahCodec.encode(current.copy(rules = rules))
    }

    suspend fun setJumuah(time: java.time.LocalTime?) = edit { prefs ->
        prefs[Keys.JAMAAH] = JamaahCodec.encode(JamaahCodec.decode(prefs[Keys.JAMAAH]).copy(jumuah = time))
    }

    suspend fun setJamaahReminderMinutes(minutes: Int) = edit {
        it[Keys.JAMAAH_REMINDER] = minutes.takeIf { m -> m in AppSettings.PRAYER_REMINDER_CHOICES } ?: 0
    }

    suspend fun setFastReminder(kind: FastKind, on: Boolean) = edit { prefs ->
        val current = decodeFastKinds(prefs[Keys.FAST_REMINDERS])
        val updated = if (on) current + kind else current - kind
        prefs[Keys.FAST_REMINDERS] = updated.sortedBy { it.ordinal }.joinToString(",") { it.name }
    }

    suspend fun setThemeChoice(choice: ThemeChoice) = edit { it[Keys.THEME] = choice.name }

    suspend fun completeOnboarding() = edit { it[Keys.ONBOARDED] = true }

    suspend fun markDisclaimerSeen() = edit { it[Keys.DISCLAIMER] = true }

    suspend fun setAnalyticsEnabled(enabled: Boolean) = edit { it[Keys.ANALYTICS] = enabled }

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

    /** Closes the "Allow notifications" notice on home; a bell turned on while still blocked brings it back. */
    suspend fun setNotificationsNoticeDismissed(dismissed: Boolean) =
        edit { it[Keys.NOTIFICATIONS_NOTICE_DISMISSED] = dismissed }

    suspend fun setNotificationPermissionAsked(asked: Boolean) =
        edit { it[Keys.NOTIFICATION_PERMISSION_ASKED] = asked }

    /** Closes the calculation-method notice on home for good. Settings still shows the row. */
    suspend fun dismissMethodNotice() = edit { it[Keys.METHOD_NOTICE_DISMISSED] = true }

    suspend fun setRamadanCardEnabled(enabled: Boolean) = edit { it[Keys.RAMADAN_CARD] = enabled }

    /** Closes the Ramadan notice for [hijriYear] only, so it returns the following year. */
    suspend fun dismissRamadanNotice(hijriYear: Int) = edit {
        it[Keys.RAMADAN_NOTICE_DISMISSED_YEAR] = hijriYear
    }

    /** Closes the Eid greeting for the Eid whose [code] this is, and for that Eid only. */
    suspend fun dismissEidGreeting(code: Int) = edit { it[Keys.EID_GREETING_DISMISSED_CODE] = code }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
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
        val ADJUSTMENTS = stringPreferencesKey("prayer_adjustments")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset_days")
        val ONGOING = booleanPreferencesKey("ongoing_badge")
        val ALARM_SOUND = stringPreferencesKey("alarm_sound")
        val ALARM_RESPECTS_SILENT = booleanPreferencesKey("alarm_respects_silent")

        /**
         * The phone sound (or "" for the default) in use when a built-in adhan replaced it (1.5.0).
         * Absent until a built-in adhan is first chosen, and only read while one is.
         */
        val PHONE_ALARM_SOUND = stringPreferencesKey("phone_alarm_sound")
        val NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val PHONE_NOTIFICATION_SOUND = stringPreferencesKey("phone_notification_sound")
        val ALARM_ON_APPROXIMATE = booleanPreferencesKey("alarm_on_approximate_days")

        // Read but never written: the two keys ALERTS replaced. Kept so that an existing
        // install's choices survive the upgrade instead of silently reverting to the
        // defaults — which for someone who had turned four prayers off would mean four
        // unexpected alerts a day.
        val NOTIFY = stringPreferencesKey("notify_slots")
        val ALERT_STYLE = stringPreferencesKey("alert_style")
        val DISCLAIMER = booleanPreferencesKey("disclaimer_seen")
        val ANALYTICS = booleanPreferencesKey("analytics_enabled")
        val DEFAULT_LOCATION = booleanPreferencesKey("using_default_location")
        val EXACT_ALARM_DISMISSED = booleanPreferencesKey("exact_alarm_notice_dismissed")
        val METHOD_NOTICE_DISMISSED = booleanPreferencesKey("method_notice_dismissed")
        val THEME = stringPreferencesKey("theme_choice")
        val RAMADAN_CARD = booleanPreferencesKey("ramadan_card_enabled")
        val RAMADAN_NOTICE_DISMISSED_YEAR = intPreferencesKey("ramadan_notice_dismissed_year")
        val EID_GREETING_DISMISSED_CODE = intPreferencesKey("eid_greeting_dismissed_code")
        val SUHOOR_MINUTES = intPreferencesKey("suhoor_reminder_minutes")
        val SUHOOR_STYLE = stringPreferencesKey("suhoor_reminder_style")
        val SUHOOR_ADHAN = booleanPreferencesKey("suhoor_reminder_adhan")
        val PRAYER_REMINDER_MINUTES = intPreferencesKey("prayer_reminder_minutes")
        val PRAYER_REMINDER_FOR = stringPreferencesKey("prayer_reminder_for")
        val TRACKER_ENABLED = booleanPreferencesKey("tracker_enabled")
        val TRACKER_STREAK = booleanPreferencesKey("tracker_show_streak")
        val JAMAAH = stringPreferencesKey("jamaah_times")
        val JAMAAH_REMINDER = intPreferencesKey("jamaah_reminder_minutes")
        val FAST_REMINDERS = stringPreferencesKey("fast_reminders")
        val LAST_ALERT_STYLE = stringPreferencesKey("last_alert_style")
        val BELL_REMOVED_REMINDER = stringPreferencesKey("bell_removed_reminder")
        val NOTIFICATIONS_NOTICE_DISMISSED = booleanPreferencesKey("notifications_notice_dismissed")
        val NOTIFICATION_PERMISSION_ASKED = booleanPreferencesKey("notification_permission_asked")
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

    private fun Preferences.readReminderMinutes(): Int =
        (this[Keys.PRAYER_REMINDER_MINUTES] ?: 0).takeIf { it in AppSettings.PRAYER_REMINDER_CHOICES } ?: 0

    /** The four values the bell rules work on. Absent or garbage bell keys read as empty. */
    private fun Preferences.readBell() = BellState(
        alertFor = decodeAlerts(),
        lastStyle = this[Keys.LAST_ALERT_STYLE]?.let(AlertCodec::decode).orEmpty(),
        reminderFor = decodeSlots(this[Keys.PRAYER_REMINDER_FOR]),
        bellRemovedReminder = decodeBellRemoved(this[Keys.BELL_REMOVED_REMINDER]),
    )

    private fun MutablePreferences.writeBell(state: BellState<AlertStyle>) {
        this[Keys.ALERTS] = AlertCodec.encode(state.alertFor)
        this[Keys.LAST_ALERT_STYLE] = AlertCodec.encode(state.lastStyle)
        // Written in the enum's order, so the stored string is stable (see AlertCodec.encode).
        this[Keys.PRAYER_REMINDER_FOR] = state.reminderFor.sortedBy { it.ordinal }.joinToString(",") { it.name }
        this[Keys.BELL_REMOVED_REMINDER] = state.bellRemovedReminder.sortedBy { it.ordinal }.joinToString(",") { it.name }
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
            alertFor = decodeAlerts(),
            adjustments = AdjustmentCodec.decode(this[Keys.ADJUSTMENTS]),
            hijriOffsetDays = this[Keys.HIJRI_OFFSET] ?: 0,
            ongoingBadge = this[Keys.ONGOING] ?: true,
            alarmSoundUri = this[Keys.ALARM_SOUND] ?: "",
            alarmRespectsSilent = this[Keys.ALARM_RESPECTS_SILENT] ?: true,
            phoneAlarmSoundUri = this[Keys.PHONE_ALARM_SOUND] ?: "",
            notificationSoundUri = this[Keys.NOTIFICATION_SOUND] ?: "",
            phoneNotificationSoundUri = this[Keys.PHONE_NOTIFICATION_SOUND] ?: "",
            alarmOnApproximateDays = this[Keys.ALARM_ON_APPROXIMATE] ?: false,
            disclaimerSeen = this[Keys.DISCLAIMER] ?: false,
            analyticsEnabled = this[Keys.ANALYTICS] ?: false,
            analyticsAnswered = Keys.ANALYTICS in this,
            usingDefaultLocation = this[Keys.DEFAULT_LOCATION] ?: false,
            exactAlarmNoticeDismissed = this[Keys.EXACT_ALARM_DISMISSED] ?: false,
            methodNoticeDismissed = this[Keys.METHOD_NOTICE_DISMISSED] ?: false,
            themeChoice = enumOr(this[Keys.THEME], ThemeChoice.SYSTEM),
            ramadanCardEnabled = this[Keys.RAMADAN_CARD] ?: true,
            ramadanNoticeDismissedYear = this[Keys.RAMADAN_NOTICE_DISMISSED_YEAR] ?: 0,
            eidGreetingDismissedCode = this[Keys.EID_GREETING_DISMISSED_CODE] ?: 0,
            suhoorReminderMinutes = (this[Keys.SUHOOR_MINUTES] ?: AppSettings.SUHOOR_DEFAULT_MINUTES)
                .takeIf { it in AppSettings.SUHOOR_CHOICES } ?: 0,
            suhoorReminderStyle = enumOr(this[Keys.SUHOOR_STYLE], AlertStyle.NOTIFICATION),
            suhoorUsesAdhan = this[Keys.SUHOOR_ADHAN] ?: false,
            prayerReminderMinutes = (this[Keys.PRAYER_REMINDER_MINUTES] ?: 0)
                .takeIf { it in AppSettings.PRAYER_REMINDER_CHOICES } ?: 0,
            prayerReminderFor = decodeSlots(this[Keys.PRAYER_REMINDER_FOR]),
            trackerEnabled = this[Keys.TRACKER_ENABLED] ?: false,
            trackerShowStreak = this[Keys.TRACKER_STREAK] ?: false,
            jamaah = JamaahCodec.decode(this[Keys.JAMAAH]),
            jamaahReminderMinutes = (this[Keys.JAMAAH_REMINDER] ?: 0).takeIf { it in AppSettings.PRAYER_REMINDER_CHOICES } ?: 0,
            fastReminders = decodeFastKinds(this[Keys.FAST_REMINDERS]),
            lastAlertStyle = this[Keys.LAST_ALERT_STYLE]?.let(AlertCodec::decode).orEmpty(),
            bellRemovedReminder = decodeBellRemoved(this[Keys.BELL_REMOVED_REMINDER]),
            notificationsNoticeDismissed = this[Keys.NOTIFICATIONS_NOTICE_DISMISSED] ?: false,
            notificationPermissionAsked = this[Keys.NOTIFICATION_PERMISSION_ASKED] ?: false,
        )
    }

    /** Tolerates renamed/removed enum constants across app updates rather than crashing. */
    private inline fun <reified T : Enum<T>> enumOr(name: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: fallback
}

/**
 * The prayers named in a stored "FAJR,ASR" string. Absent means never touched, so all five; blank
 * means the person unticked every one. Unknown names are dropped, as in [AlertCodec.decode].
 */
internal fun decodeSlots(raw: String?): Set<PrayerSlot> {
    val prayers = PrayerSlot.entries.filter { it.isPrayer }
    if (raw == null) return prayers.toSet()
    return raw.split(",").mapNotNull { name -> prayers.firstOrNull { it.name == name } }.toSet()
}

/** Unlike [decodeSlots], absent means none: the bell has taken nothing yet. */
internal fun decodeBellRemoved(raw: String?): Set<PrayerSlot> = if (raw == null) emptySet() else decodeSlots(raw)

/** "MONDAY_THURSDAY,SPECIAL"; unknown names dropped, absent or blank is none. */
internal fun decodeFastKinds(raw: String?): Set<FastKind> =
    raw.orEmpty().split(",").mapNotNull { name -> FastKind.entries.firstOrNull { it.name == name } }.toSet()

/**
 * The place name to show. The Makkah fallback is stored under a fixed English name, so it is
 * shown in the app's own language instead ("مكة", "مکہ", "Mekke"); a real place keeps the name
 * the phone or the lookup gave it. [context] must carry the app's language (AppLocale.wrap, or
 * a Compose LocalContext, which already does). Added 17 Oct 2026 with the Arabic translation.
 */
fun AppSettings.placeName(context: android.content.Context): String =
    if (usingDefaultLocation) context.getString(com.sajdatime.app.R.string.location_makkah) else cityName
