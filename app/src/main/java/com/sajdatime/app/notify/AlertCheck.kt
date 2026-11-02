package com.sajdatime.app.notify

import android.app.ActivityManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri

/**
 * The "Alert check" in Settings: what the phone is withholding that would stop an alert arriving.
 * Each field is true when that thing is fine; `null` means the check does not apply (no exact
 * alarm permission before Android 12; no Do Not Disturb question unless a prayer is an alarm).
 *
 * Reads only. Nothing is stored or sent, so no usage count and no privacy page change.
 */
data class AlertCheck(
    val notifications: Boolean,
    val exactAlarms: Boolean?,
    val battery: Boolean,
    val dnd: Boolean?,
) {
    /** How many of the checks that apply are failing; 0 means "Everything is allowed". */
    val failing: Int get() = listOfNotNull(notifications, exactAlarms, battery, dnd).count { !it }

    companion object {
        fun read(context: Context, usesAlarm: Boolean) = AlertCheck(
            notifications = notificationsAllowed(context) && !prayerChannelBlocked(context),
            exactAlarms = PrayerAlarmScheduler.canScheduleExact(context)
                .takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S },
            battery = !backgroundRestricted(context),
            dnd = Notifications.hasDndAccess(context).takeIf { usesAlarm },
        )

        /** Android 13's permission and the older "turn off for this app" switch ; also the Times bell's test. [read] adds the prayer channels on top. */
        fun notificationsAllowed(context: Context): Boolean =
            NotificationManagerCompat.from(context).areNotificationsEnabled()

        /** Someone switched off a prayer alert channel by hand: the app-wide switch is still on, but that alert never shows. */
        private fun prayerChannelBlocked(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && runCatching {
                context.getSystemService(android.app.NotificationManager::class.java).notificationChannels.any {
                    it.importance == android.app.NotificationManager.IMPORTANCE_NONE && Notifications.isPrayerAlertChannel(it.id)
                }
            }.getOrDefault(false)

        /**
         * Only "Restrict background activity" for this app counts as failing. Ordinary battery
         * optimisation is deliberately NOT asked about: prayer alerts use setAlarmClock, which is exempt,
         * and the permission that opens that screen (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) is one Play
         * restricts, so the app must not declare it. Below Android 9 there is no such setting.
         */
        private fun backgroundRestricted(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && runCatching {
                context.getSystemService(ActivityManager::class.java)?.isBackgroundRestricted == true
            }.getOrDefault(false)

        /**
         * The app's own page in the phone's settings, where battery and background use live on every
         * make. Never a vendor specific screen: those change between phone models and OS versions.
         */
        fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
            if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching { context.startActivity(intent) }
        }
    }
}
