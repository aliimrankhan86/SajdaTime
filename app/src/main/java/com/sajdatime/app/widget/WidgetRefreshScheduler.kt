package com.sajdatime.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * The one alarm that keeps the widgets honest when nothing else wakes the app: it fires at the moment
 * the highlighted row has to move, which is the next prayer, or at local midnight, when the Today list
 * has to become the next day's, whichever comes first.
 *
 * It is deliberately the weak kind of alarm. A widget is not an alert, so this one is a non waking
 * `RTC` alarm: exact when the person has allowed exact alarms and inexact when not, never
 * `setAlarmClock` (that would claim the status bar clock for a redraw) and never allowed to wake a
 * sleeping phone. A phone that is asleep is not showing the widget, and the alarm is delivered the
 * moment it wakes. Every other trigger in the app redraws the widgets too (see [WidgetUpdater]), and
 * [PrayerAlarmScheduler]'s strong alarms redraw them at each prayer when alerts are on, so this alarm
 * only has to cover the person who has alerts off.
 *
 * One alarm, one request code. Rescheduling replaces it, so it cannot pile up, and it cannot collide
 * with a prayer (up to 3,669) or a suhoor reminder (10,001 to 10,366). With no widget placed, or no
 * location yet, it is cancelled and nothing is left set: a phone without the widget pays nothing.
 */
object WidgetRefreshScheduler {

    const val ACTION_REFRESH = "com.sajdatime.app.action.WIDGET_REFRESH"

    internal const val REQUEST_CODE = 20_000

    /** A moment after the boundary, so the redraw is computed on the far side of it and never on it. */
    private val SLACK: Duration = Duration.ofSeconds(2)

    /**
     * When the next redraw is due: [nextPrayerAt] or the coming local midnight, whichever is first,
     * plus [SLACK]. Always later than [now] when [nextPrayerAt] is, so the alarm can never be set in the
     * past and a receiver that fires can never reschedule itself into a loop.
     *
     * Midnight is the start of the next local day as the zone defines it, so on a day when a zone's
     * clocks skip midnight (a few still do) it is the first instant that day, not an instant that
     * does not exist.
     */
    internal fun nextRefreshAt(nextPrayerAt: Instant, now: Instant, zone: ZoneId): Instant {
        val midnight = now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
        val boundary = if (nextPrayerAt.isBefore(midnight)) nextPrayerAt else midnight
        return boundary.plus(SLACK)
    }

    /** Sets the alarm for the state just drawn, or cancels it when there is nothing time based to keep right. */
    fun scheduleNext(context: Context, state: WidgetState, now: Instant, zone: ZoneId) {
        val next = state.next
        if (!state.hasLocation || next == null) {
            cancel(context)
            return
        }
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return
        val triggerAt = nextRefreshAt(next.at, now, zone).toEpochMilli()
        val pending = pendingIntent(context, PendingIntent.FLAG_UPDATE_CURRENT)
            ?: return

        // Exact only when it is allowed. runCatching because some builds revoke the capability between
        // the check and the call, and a redraw alarm must never be the thing that throws.
        val exact = state.countdownAllowed && runCatching {
            manager.setExact(AlarmManager.RTC, triggerAt, pending)
        }.isSuccess
        if (!exact) runCatching { manager.set(AlarmManager.RTC, triggerAt, pending) }
    }

    fun cancel(context: Context) {
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return
        // FLAG_NO_CREATE: only cancel what exists, and create nothing just to cancel it.
        pendingIntent(context, PendingIntent.FLAG_NO_CREATE)?.let {
            manager.cancel(it)
            it.cancel()
        }
    }

    private fun pendingIntent(context: Context, flag: Int): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, WidgetRefreshReceiver::class.java).setAction(ACTION_REFRESH),
            flag or PendingIntent.FLAG_IMMUTABLE,
        )
}
