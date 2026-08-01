package com.sajdatime.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/** Fires at a prayer time: posts the alert, then lays down the next window of alarms. */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != PrayerAlarmScheduler.ACTION_PRAYER) return

        val slot = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SLOT)
            ?.let { name -> PrayerSlot.entries.firstOrNull { it.name == name } }
            ?: return
        val at = Instant.ofEpochMilli(
            intent.getLongExtra(PrayerAlarmScheduler.EXTRA_AT_MILLIS, System.currentTimeMillis()),
        )

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                settings.alertFor[slot]?.let { style ->
                    Notifications.postPrayerAlert(
                        base = appContext,
                        slot = slot,
                        at = at,
                        style = style,
                        alarmSoundUri = settings.alarmSoundUri,
                        respectSilent = settings.alarmRespectsSilent,
                    )
                }
                // Chain the next horizon and refresh the badge while we are already awake.
                PrayerAlarmScheduler.reschedule(appContext, settings)
                OngoingBadge.refresh(appContext, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
