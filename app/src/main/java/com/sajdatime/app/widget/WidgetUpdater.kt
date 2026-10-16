package com.sajdatime.app.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.SettingsRepository
import com.sajdatime.app.notify.PrayerAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

/**
 * The one function that redraws every widget. Everything that can make a widget stale calls it, and
 * a phone with no widget placed pays almost nothing: it finds no instance and returns.
 */
object WidgetUpdater {

    /** Redraws every placed instance. Returns true when at least one exists. */
    fun refreshAll(context: Context, settings: AppSettings): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val nextIds = manager.getAppWidgetIds(ComponentName(context, NextPrayerWidgetProvider::class.java))
        val todayIds = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
        if (nextIds.isEmpty() && todayIds.isEmpty()) return false

        val now = Instant.now()
        val state = WidgetState.build(
            settings = settings,
            now = now,
            zone = ZoneId.systemDefault(),
            canScheduleExact = PrayerAlarmScheduler.canScheduleExact(context),
        )
        // The views are built once per provider and pushed to every instance of it, so the engine runs
        // once per refresh however many widgets are on the screen. A launcher that refuses an update
        // must never take the caller down, which may be the alarm receiver that is about to chain the
        // next prayer.
        runCatching {
            if (nextIds.isNotEmpty()) manager.updateAppWidget(nextIds, WidgetRenderer.nextPrayer(context, state, now))
            if (todayIds.isNotEmpty()) manager.updateAppWidget(todayIds, WidgetRenderer.today(context, state, now))
        }
        return true
    }

    /** As above, reading the settings itself, for the callers that have none to hand. */
    suspend fun refreshAll(context: Context): Boolean =
        refreshAll(context, SettingsRepository(context).current())

    /**
     * The lifecycle callbacks of both providers arrive on the main thread inside onReceive, and reading
     * the settings is a suspend call, so the redraw runs under goAsync on a background dispatcher, the
     * same shape as PrayerAlarmReceiver.
     */
    internal fun refreshFromProvider(receiver: BroadcastReceiver, context: Context) {
        val appContext = context.applicationContext
        val pending = receiver.goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                refreshAll(appContext)
            } finally {
                pending.finish()
            }
        }
    }
}
