package com.sajdatime.app.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sajdatime.app.MainActivity
import com.sajdatime.app.R
import com.sajdatime.core.AppLocale
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.label
import com.sajdatime.app.data.AlertStyle
import java.time.Duration
import java.time.Instant

/**
 * Notification channels and posting. Everything is generated on-device; nothing is
 * fetched or reported.
 *
 * There are two alert channels because Android binds sound and importance to the channel,
 * not to the individual notification. Switching between a quiet notification and a full
 * alarm therefore needs two channels, and the alarm channel must be recreated whenever
 * the user picks a different sound, since channel settings are immutable once created.
 */
object Notifications {

    const val CHANNEL_PRAYERS = "prayer_times"
    const val CHANNEL_ONGOING = "next_prayer_badge"

    /**
     * Versioned by sound: a channel's sound cannot be changed after creation, so a new
     * tone means a new channel id and the old channel is deleted.
     */
    private const val CHANNEL_ALARM_PREFIX = "prayer_alarm_v"

    const val ID_ONGOING = 1000
    private const val ID_PRAYER_BASE = 2000

    /** One slot: tomorrow's reminder replaces today's, and the prayer ids (2000 to 2005) are untouched. */
    const val ID_SUHOOR = 3000

    /** Two firm pulses: noticeable in a pocket, not startling in a quiet room. */
    private val PRAYER_VIBRATION = longArrayOf(0, 350, 250, 350)

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_PRAYERS,
                context.getString(R.string.channel_prayers),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.channel_prayers_desc)
                enableVibration(true)
                vibrationPattern = PRAYER_VIBRATION
            },
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ONGOING,
                context.getString(R.string.channel_ongoing),
                // LOW keeps the badge silent — it is a glanceable status, not an alert.
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.channel_ongoing_desc)
                setShowBadge(false)
            },
        )
    }

    /**
     * The alarm channel's id: one per sound, and a separate one once the user has given
     * SajdaTime Do Not Disturb access.
     *
     * Why the Do Not Disturb state is part of the id. Android lets a channel bypass Do Not
     * Disturb only if the app held that access when the channel was CREATED. Re-creating an
     * existing channel updates the flag only if the user has never touched any of that
     * channel's settings (PreferencesHelper.createNotificationChannel in AOSP: it needs
     * `getUserLockedFields() == 0 && hasDndAccess`). So a user who once changed the alarm's
     * vibration, then granted access, would still be silenced. A new id after the grant always
     * gets a fresh channel that really bypasses, and the stale one is deleted below.
     *
     * The id without access is exactly 1.3.1's, so nobody's existing channel (and anything they
     * customised on it) changes until they actually grant access.
     */
    internal fun alarmChannelId(soundUri: String, bypassesDnd: Boolean): String =
        CHANNEL_ALARM_PREFIX + soundUri.hashCode().toUInt().toString(16) + if (bypassesDnd) "_dnd" else ""

    /**
     * Creates (or reuses) the alarm channel for [soundUri] and returns its id. Stale
     * channels from previously chosen sounds, or from before Do Not Disturb access was
     * granted or withdrawn, are deleted so the system Settings list does not fill up with
     * dead entries.
     */
    fun ensureAlarmChannel(context: Context, soundUri: String): String {
        val id = alarmChannelId(soundUri, bypassesDnd = hasDndAccess(context))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return id

        val manager = context.getSystemService(NotificationManager::class.java) ?: return id

        manager.notificationChannels
            .filter { it.id.startsWith(CHANNEL_ALARM_PREFIX) && it.id != id }
            .forEach { manager.deleteNotificationChannel(it.id) }

        if (manager.getNotificationChannel(id) != null) return id

        manager.createNotificationChannel(
            NotificationChannel(
                id,
                context.getString(R.string.channel_alarm),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.channel_alarm_desc)
                enableVibration(true)
                vibrationPattern = PRAYER_VIBRATION
                setSound(
                    resolveAlarmSound(soundUri),
                    AudioAttributes.Builder()
                        // USAGE_ALARM is what actually carries this through Do Not Disturb,
                        // together with CATEGORY_ALARM on the notification itself. DND
                        // allows alarms by default, so this needs no special permission.
                        // Verified on an API 36 emulator under ZEN_MODE_IMPORTANT_
                        // INTERRUPTIONS: the prayer alarm posted with mIntercept=false
                        // while the silent next-prayer badge was correctly intercepted.
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                // The second, stronger mechanism, and the one the Settings button asks for.
                // USAGE_ALARM above passes Do Not Disturb only while the user's DND rules
                // allow alarms; a bypassing channel passes even when they do not. Android
                // honours this only if the user has given SajdaTime Do Not Disturb access
                // (ACCESS_NOTIFICATION_POLICY in the manifest puts the app on that list);
                // without access the channel reads back mBypassDnd=false, which is why the id
                // changes once access is granted (alarmChannelId).
                setBypassDnd(true)
            },
        )
        return id
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Opens the system list where the user gives SajdaTime Do Not Disturb access, and the
     * app's own notification settings on the rare phone that has no such list.
     *
     * The list only shows apps that declare ACCESS_NOTIFICATION_POLICY, which 1.3.1 did not,
     * so on the owner's Samsung this button opened a list without SajdaTime in it.
     */
    fun openDndAccessSettings(context: Context) {
        val list = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        val appNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        }
        for (intent in listOf(list, appNotifications)) {
            // Settings calls this with the Activity. Anything else (the instrumented test, a
            // future notification action) needs a new task or startActivity throws.
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (runCatching { context.startActivity(intent) }.isSuccess) return
        }
    }

    /** True once the user has allowed the app to sound through Do Not Disturb. */
    fun hasDndAccess(context: Context): Boolean = runCatching {
        context.getSystemService(NotificationManager::class.java)
            ?.isNotificationPolicyAccessGranted == true
    }.getOrDefault(false)

    /**
     * True when the user has silenced this phone with the ringer control.
     *
     * This is asked because the alarm stream deliberately ignores it. On stock Android the
     * streams the ringer mutes are RING, NOTIFICATION, SYSTEM and SYSTEM_ENFORCED —
     * STREAM_ALARM is not among them, and turning it into one is an opt-in device resource
     * (`config_audio_ringer_mode_affects_alarm_stream`) that defaults to false. That is
     * correct for a clock someone set for one morning and wrong for a recurring prayer
     * alert, which is why a tester reported the alarm sounding through a silenced phone.
     *
     * Vibrate counts as silenced. Someone who has turned the ringer down to vibrate has
     * asked for no sound just as plainly as someone on full silent, and the quieter
     * channel this routes to still vibrates.
     *
     * Deliberately not consulting Do Not Disturb: DND allows alarms by default, has its
     * own separate opt-in on the Settings screen, and reading the ringer mode is the
     * question the user actually answered with the volume keys.
     */
    private fun phoneIsSilenced(context: Context): Boolean = runCatching {
        when (context.getSystemService(AudioManager::class.java)?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> true
            else -> false
        }
    }.getOrDefault(false)

    fun postPrayerAlert(
        base: Context,
        slot: PrayerSlot,
        at: Instant,
        style: AlertStyle,
        alarmSoundUri: String,
        respectSilent: Boolean,
        approximate: Boolean = false,
        alarmOnApproximate: Boolean = false,
    ) {
        // Everything below builds text the user reads, so it is built from a context
        // pinned to the app's own language rather than the device's. Wrapped here at the
        // entry rather than at each getString, so a %d added to a notification string
        // later cannot quietly reintroduce the bug. See AppLocale.kt.
        val context = AppLocale.wrap(base)
        if (!canPost(context)) return
        ensureChannels(context)

        val effectiveStyle = effectiveAlertStyle(
            style = style,
            respectSilent = respectSilent,
            approximate = approximate,
            alarmOnApproximate = alarmOnApproximate,
            phoneIsSilenced = { phoneIsSilenced(context) },
        )

        val builder = NotificationCompat.Builder(context, alertChannelId(context, effectiveStyle, alarmSoundUri))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_prayer_title, slot.label(context)))
            .setContentText(
                context.getString(
                    R.string.notif_prayer_body,
                    slot.label(context),
                    TimeFormat.clock(context, at),
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .markIfApproximate(context, approximate)
            .applyStyle(effectiveStyle, alarmSoundUri)

        post(context, ID_PRAYER_BASE + slot.ordinal, builder.build())
    }

    /**
     * The optional suhoor reminder: "Suhoor ends at 05:10 / Fajr begins in 30m", a little before Fajr
     * on the days of Ramadan.
     *
     * It goes through exactly the same style rules as a prayer alert ([effectiveAlertStyle]: a silent
     * phone, a projected polar time) and the same two channels, so a person who has already chosen
     * how loud their alerts are gets no surprise, and there is no third channel to explain in system
     * settings. The shade entry clears itself at Fajr, because "Fajr begins in 30m" left on the screen
     * after Fajr is a statement that has stopped being true.
     *
     * The countdown in the body is rounded to the nearest minute from [now]. An alarm delivered a few
     * seconds late would otherwise read "29m" for a reminder the person set to 30.
     */
    fun postSuhoorReminder(
        base: Context,
        fajrAt: Instant,
        now: Instant,
        style: AlertStyle,
        alarmSoundUri: String,
        respectSilent: Boolean,
        approximate: Boolean = false,
        alarmOnApproximate: Boolean = false,
    ) {
        val context = AppLocale.wrap(base)
        if (!canPost(context)) return
        ensureChannels(context)

        val effectiveStyle = effectiveAlertStyle(
            style = style,
            respectSilent = respectSilent,
            approximate = approximate,
            alarmOnApproximate = alarmOnApproximate,
            phoneIsSilenced = { phoneIsSilenced(context) },
        )

        val shownFrom = fajrAt.minusSeconds(60L * suhoorMinutesLeft(now, fajrAt))
        val builder = NotificationCompat.Builder(context, alertChannelId(context, effectiveStyle, alarmSoundUri))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_suhoor_title, TimeFormat.clock(context, fajrAt)))
            .setContentText(
                context.getString(
                    R.string.notif_suhoor_body,
                    PrayerSlot.FAJR.label(context),
                    TimeFormat.remaining(context, shownFrom, fajrAt),
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setTimeoutAfter(Duration.between(now, fajrAt).toMillis().coerceAtLeast(1L))
            .setContentIntent(openAppIntent(context))
            .markIfApproximate(context, approximate)
            .applyStyle(effectiveStyle, alarmSoundUri)

        post(context, ID_SUHOOR, builder.build())
    }

    /** The channel an alert of [style] belongs on; the alarm channel is created on demand. */
    private fun alertChannelId(context: Context, style: AlertStyle, alarmSoundUri: String): String =
        when (style) {
            AlertStyle.NOTIFICATION -> CHANNEL_PRAYERS
            AlertStyle.ALARM -> ensureAlarmChannel(context, alarmSoundUri)
        }

    /** Category, and below Android O the sound and vibration a channel would otherwise carry. */
    private fun NotificationCompat.Builder.applyStyle(
        style: AlertStyle,
        alarmSoundUri: String,
    ): NotificationCompat.Builder {
        when (style) {
            AlertStyle.NOTIFICATION ->
                setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVibrate(PRAYER_VIBRATION)

            AlertStyle.ALARM -> {
                setCategory(NotificationCompat.CATEGORY_ALARM)
                // Below Android O there are no channels, so sound and vibration have to
                // be attached to the notification itself.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    setSound(resolveAlarmSound(alarmSoundUri), AudioManager.STREAM_ALARM)
                    setVibrate(PRAYER_VIBRATION)
                }
            }
        }
        return this
    }

    /** The optional silent badge showing what is next and how long until it starts. */
    fun postOngoingBadge(
        base: Context,
        slot: PrayerSlot,
        at: Instant,
        now: Instant,
        approximate: Boolean = false,
    ) {
        // Pinned to the app's language, as above. This one carries a countdown, so it is
        // the notification that actually showed "In ٢h ١٤m" on an Arabic phone.
        val context = AppLocale.wrap(base)
        if (!canPost(context)) return
        ensureChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                context.getString(R.string.notif_next_title, slot.label(context), TimeFormat.clock(context, at)),
            )
            .setContentText(
                context.getString(R.string.notif_next_body, TimeFormat.remaining(context, now, at)),
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(openAppIntent(context))
            .markIfApproximate(context, approximate)
            .build()

        post(context, ID_ONGOING, notification)
    }

    fun clearOngoingBadge(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_ONGOING)
    }

    private fun resolveAlarmSound(soundUri: String): Uri =
        soundUri.takeIf { it.isNotBlank() }?.let(Uri::parse)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    /**
     * Every caller checks [canPost] first, which lint cannot follow across the helper.
     * SecurityException is still caught: some OEM builds and work profiles refuse the
     * post even when the permission reads as granted, and a missed notification must
     * never take down the alarm receiver that is about to reschedule the next day.
     */
    @SuppressLint("MissingPermission")
    private fun post(context: Context, id: Int, notification: android.app.Notification) {
        if (!canPost(context)) return
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

/**
 * Which of the two alert channels this alert actually lands on.
 *
 * Two independent reasons drop an alarm to a notification, and **neither suppresses the
 * alert** — it still arrives, on time, in the shade and as a heads-up, still marked. Only
 * the sound is dropped. Nothing about the ringer or the channel suppresses a notification's
 * visual treatment, so "quiet, but still there" is what both cases produce.
 *
 *  1. **The phone is silenced and the user asked that to be respected.** The alarm stream
 *     deliberately ignores the ringer, which is right for a clock someone set for one
 *     morning and wrong for a recurring alert five times a day. See [phoneIsSilenced].
 *  2. **The time was projected from another latitude.** Above the polar circles there is no
 *     true Fajr or Isha to calculate, so the app borrows the times from a lower latitude
 *     under a published ruling. A notification states such a time and marks it approximate;
 *     an alarm asserts it hard enough to wake someone at two in the morning, on a number
 *     the app itself is telling them not to be certain of. The user can override this —
 *     see [com.sajdatime.app.data.AppSettings.alarmOnApproximateDays], and read the note
 *     there before removing the override, because the cost of not having one is a polar
 *     user losing their Fajr alarm for two months a year.
 *
 * Pure, and split out of [Notifications.postPrayerAlert] only so it can be tested: posting
 * needs a live NotificationManager and this does not. [phoneIsSilenced] is a lambda rather
 * than a Boolean so the AudioManager read stays skipped in the cases that never reach it,
 * which is the overwhelming majority of alerts.
 */
internal fun effectiveAlertStyle(
    style: AlertStyle,
    respectSilent: Boolean,
    approximate: Boolean,
    alarmOnApproximate: Boolean,
    phoneIsSilenced: () -> Boolean,
): AlertStyle {
    if (style != AlertStyle.ALARM) return style
    if (approximate && !alarmOnApproximate) return AlertStyle.NOTIFICATION
    return if (respectSilent && phoneIsSilenced()) AlertStyle.NOTIFICATION else style
}

/**
 * Whole minutes from [now] to [fajrAt], rounded to the nearest and never negative. Pure, so the
 * rounding is tested: it is what makes a reminder set to 30 minutes read "30m" and not "29m" when the
 * system delivers it a few seconds late.
 */
internal fun suhoorMinutesLeft(now: Instant, fajrAt: Instant): Long =
    ((fajrAt.epochSecond - now.epochSecond + 30L) / 60L).coerceAtLeast(0L)

/**
 * Says so when the time being announced was projected from another latitude rather than
 * measured where the user is.
 *
 * It goes in `setSubText`, which Android draws small and grey in the notification's header
 * line, beside the app name. That placement is the point: the alert's own words must stay
 * "Time for Fajr" and nothing else, because a notification is read in one glance on a lock
 * screen and burying the prayer behind a caveat helps nobody. The caveat rides alongside.
 *
 * The home screen and the PDF have carried this marking for a while and these two surfaces
 * did not, which meant the *only* two places a projected time was presented without any
 * qualification were the two that reach the user when the app is closed. Above the polar
 * circles that is most of the day.
 */
private fun NotificationCompat.Builder.markIfApproximate(
    context: Context,
    approximate: Boolean,
): NotificationCompat.Builder =
    if (approximate) setSubText(context.getString(R.string.notif_approximate)) else this
