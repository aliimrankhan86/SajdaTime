package com.sajdatime.app.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sajdatime.app.data.SettingsRepository
import java.util.concurrent.TimeUnit

/**
 * Safety net: rebuilds the alarm schedule once a day even if the user never opens the
 * app and no alarm managed to fire (aggressive OEM battery managers do kill alarms).
 */
class DailyRescheduleWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settings = SettingsRepository(applicationContext).current()
        Notifications.ensureChannels(applicationContext)
        PrayerAlarmScheduler.reschedule(applicationContext, settings)
        OngoingBadge.refresh(applicationContext, settings)
        return Result.success()
    }

    companion object {
        private const val NAME = "daily_prayer_reschedule"

        fun enqueue(context: Context) {
            // 12 hours, not 24: WorkManager may defer a periodic job well past its
            // window, and two chances a day keeps the 2-day alarm horizon topped up.
            val request = PeriodicWorkRequestBuilder<DailyRescheduleWorker>(12, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
