package com.sajdatime.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Alarms do not survive a reboot, and a time-zone change silently invalidates every
 * scheduled time. Both cases rebuild the schedule from scratch.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                Notifications.ensureChannels(appContext)
                PrayerAlarmScheduler.reschedule(appContext, settings)
                OngoingBadge.refresh(appContext, settings)
                // Also lays down the widgets' redraw alarm again, which a reboot and a clock change both lose.
                WidgetUpdater.refreshAll(appContext, settings)
                DailyRescheduleWorker.enqueue(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    internal companion object {
        /**
         * Android 12+ sends this when "Alarms & reminders" is granted or revoked.
         *
         * It matters in both directions. Granting is the moment the whole schedule can be
         * upgraded from an inexact alarm to an exact one, and without this that upgrade
         * waited until the user next opened the app — which for this app might be days,
         * because the notification *is* the product. Revoking cancels every future exact
         * alarm the app has already set, so the schedule has to be laid down again in
         * whatever weaker form is still permitted.
         *
         * Written out rather than referenced as AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_
         * PERMISSION_STATE_CHANGED so the constant does not need an API-31 guard here; the
         * string is inert on older versions, which never send it.
         */
        const val ACTION_EXACT_ALARM_STATE_CHANGED =
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"

        /**
         * Every action the manifest filter delivers here, kept in one place so a test can hold the two
         * lists together. ACTION_LOCALE_CHANGED is the phone's language: the schedule is unaffected, but
         * the widgets are drawn in the app's own words and numerals and would otherwise keep the old
         * language until the next prayer.
         */
        internal val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_LOCALE_CHANGED,
            ACTION_EXACT_ALARM_STATE_CHANGED,
        )
    }
}
