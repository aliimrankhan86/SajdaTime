package com.sajdatime.app.notify

import android.Manifest
import android.annotation.SuppressLint
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
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.data.AlertStyle
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
     * Creates (or reuses) the alarm channel for [soundUri] and returns its id. Stale
     * channels from previously chosen sounds are deleted so the system Settings list
     * does not fill up with dead entries.
     */
    fun ensureAlarmChannel(context: Context, soundUri: String): String {
        val id = CHANNEL_ALARM_PREFIX + soundUri.hashCode().toUInt().toString(16)
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
                        // USAGE_ALARM is what lets the sound through Do Not Disturb once
                        // the user has granted notification policy access.
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                setBypassDnd(true)
            },
        )
        return id
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    /** True once the user has allowed the app to sound through Do Not Disturb. */
    fun hasDndAccess(context: Context): Boolean = runCatching {
        context.getSystemService(NotificationManager::class.java)
            ?.isNotificationPolicyAccessGranted == true
    }.getOrDefault(false)

    fun postPrayerAlert(
        context: Context,
        slot: PrayerSlot,
        at: Instant,
        style: AlertStyle,
        alarmSoundUri: String,
    ) {
        if (!canPost(context)) return
        ensureChannels(context)

        val channelId = when (style) {
            AlertStyle.NOTIFICATION -> CHANNEL_PRAYERS
            AlertStyle.ALARM -> ensureAlarmChannel(context, alarmSoundUri)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_prayer_title, slot.label))
            .setContentText(
                context.getString(
                    R.string.notif_prayer_body,
                    slot.label,
                    TimeFormat.clock(context, at),
                ),
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))

        when (style) {
            AlertStyle.NOTIFICATION ->
                builder.setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setVibrate(PRAYER_VIBRATION)

            AlertStyle.ALARM -> {
                builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                // Below Android O there are no channels, so sound and vibration have to
                // be attached to the notification itself.
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    builder.setSound(resolveAlarmSound(alarmSoundUri), AudioManager.STREAM_ALARM)
                    builder.setVibrate(PRAYER_VIBRATION)
                }
            }
        }

        post(context, ID_PRAYER_BASE + slot.ordinal, builder.build())
    }

    /** The optional silent badge showing what is next and how long until it starts. */
    fun postOngoingBadge(context: Context, slot: PrayerSlot, at: Instant, now: Instant) {
        if (!canPost(context)) return
        ensureChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                context.getString(R.string.notif_next_title, slot.label, TimeFormat.clock(context, at)),
            )
            .setContentText(
                context.getString(R.string.notif_next_body, TimeFormat.remaining(context, now, at)),
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .setShowWhen(false)
            .setContentIntent(openAppIntent(context))
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
