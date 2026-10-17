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
 * The one function that redraws every widget, and lays down the alarm for the redraw after it.
 * Everything that can make a widget stale calls it, and a phone with no widget placed pays almost
 * nothing: it finds no instance, makes sure no alarm is left set, and returns.
 */
object WidgetUpdater {

    /** Redraws every placed instance. Returns true when at least one exists. */
    fun refreshAll(context: Context, settings: AppSettings): Boolean {
        val manager = AppWidgetManager.getInstance(context) ?: return false
        val nextIds = manager.getAppWidgetIds(ComponentName(context, NextPrayerWidgetProvider::class.java))
        val todayIds = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
        val anyWidget = nextIds.isNotEmpty() || todayIds.isNotEmpty()
        // The same alarm also reposts the next prayer badge at each prayer (since 17 Oct 2026, when the
        // badge became a system countdown that times out at the prayer). Prayer alarms only exist for
        // prayers with an alert, so without this a badge would vanish at a prayer with alerts off and
        // stay gone for hours. So the alarm stays while a widget OR the badge needs it.
        if (!anyWidget && !settings.ongoingBadge) {
            // The last widget has gone and there is no badge, so the alarm that served them goes too.
            WidgetRefreshScheduler.cancel(context)
            return false
        }

        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val state = WidgetState.build(
            settings = settings,
            now = now,
            zone = zone,
            canScheduleExact = PrayerAlarmScheduler.canScheduleExact(context),
        )
        // Every redraw lays down the next one, so any trigger in the app keeps the chain alive and a
        // chain that was lost (reboot, force stop) is rebuilt by the first redraw that follows.
        runCatching { WidgetRefreshScheduler.scheduleNext(context, state, now, zone) }
        // The views are built once per provider and pushed to every instance of it, so the engine runs
        // once per refresh however many widgets are on the screen. A launcher that refuses an update
        // must never take the caller down, which may be the alarm receiver that is about to chain the
        // next prayer.
        if (!anyWidget) return false
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
