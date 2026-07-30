package com.sajdatime.app.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.sajdatime.app.MainActivity
import com.sajdatime.app.R
import com.sajdatime.app.core.PrayerSlot
import java.time.Instant

/**
 * Notification channels and posting. All notifications are generated on-device; nothing
 * is fetched or reported.
 */
object Notifications {

    const val CHANNEL_PRAYERS = "prayer_times"
    const val CHANNEL_ONGOING = "next_prayer_badge"

    const val ID_ONGOING = 1000
    private const val ID_PRAYER_BASE = 2000

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

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    fun postPrayerAlert(context: Context, slot: PrayerSlot, at: Instant) {
        if (!canPost(context)) return
        ensureChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYERS)
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
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context))
            .build()

        post(context, ID_PRAYER_BASE + slot.ordinal, notification)
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
