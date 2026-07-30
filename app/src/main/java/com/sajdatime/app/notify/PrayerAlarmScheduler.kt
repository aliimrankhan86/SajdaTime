package com.sajdatime.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.sajdatime.app.core.Coordinates
import com.sajdatime.app.core.PrayerEngine
import com.sajdatime.app.core.PrayerSlot
import com.sajdatime.app.data.AppSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Schedules the local alarms that fire each prayer notification.
 *
 * Reliability strategy — alarms are (re)scheduled from four independent triggers, so no
 * single failure silently stops notifications:
 *   1. every time the app is opened
 *   2. every time an alarm fires (chains the next window)
 *   3. a daily WorkManager job
 *   4. device boot / time-zone change / app update
 */
object PrayerAlarmScheduler {

    const val ACTION_PRAYER = "com.sajdatime.app.action.PRAYER_ALARM"
    const val EXTRA_SLOT = "slot"
    const val EXTRA_AT_MILLIS = "at"

    /** How far ahead to lay down alarms. Two days survives a missed daily job. */
    private const val HORIZON_DAYS = 2

    fun reschedule(context: Context, settings: AppSettings) {
        val coordinates = settings.coordinates ?: return
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return

        cancelAll(context, manager)
        if (settings.notifyFor.isEmpty()) return

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = now.atZone(zone).toLocalDate()

        upcoming(coordinates, settings, today, now)
            .filter { (slot, _) -> slot in settings.notifyFor }
            .forEach { (slot, at) -> schedule(context, manager, slot, at) }
    }

    /** Prayer slots between [now] and the end of the scheduling horizon, in order. */
    private fun upcoming(
        coordinates: Coordinates,
        settings: AppSettings,
        today: LocalDate,
        now: Instant,
    ): List<Pair<PrayerSlot, Instant>> =
        // Starts yesterday: at high latitudes Isha can fall after midnight and therefore
        // belongs to the previous day's timetable while still being in the future.
        PrayerEngine.computeRange(
            coordinates = coordinates,
            start = today.minusDays(1),
            days = HORIZON_DAYS + 1,
            prefs = settings.calculationPrefs,
        )
            .flatMap { it.prayersOnly }
            .filter { (_, at) -> at.isAfter(now) }

    /**
     * Schedules one alarm, degrading rather than crashing.
     *
     * On Android 12+ exact alarms need the "Alarms & reminders" permission, and from
     * Android 13 that permission is *denied by default*. Every exact-alarm API throws
     * SecurityException without it — including setAlarmClock, which is a common and
     * costly misconception. The fallback is therefore an inexact but Doze-aware alarm,
     * which usually lands within a few minutes and never throws.
     *
     * The Settings screen and the home banner both prompt the user to grant the
     * permission, since a prayer alert is worth much more when it is on the minute.
     */
    private fun schedule(
        context: Context,
        manager: AlarmManager,
        slot: PrayerSlot,
        at: Instant,
    ) {
        val pendingIntent = alarmIntent(context, slot, at)
        val triggerAt = at.toEpochMilli()

        val scheduled = runCatching {
            if (canScheduleExact(manager)) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }

        // Some OEM builds and work profiles revoke exact-alarm capability between the
        // permission check and the call, so a failure still falls back rather than
        // taking down whichever component happened to trigger the reschedule.
        if (scheduled.isFailure) {
            runCatching {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        }
    }

    fun canScheduleExact(manager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    fun canScheduleExact(context: Context): Boolean {
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return false
        return canScheduleExact(manager)
    }

    private fun cancelAll(context: Context, manager: AlarmManager) {
        // Request codes are derived from slot + day-of-year, so cancelling the same
        // horizon we are about to write covers every alarm we could have created.
        val today = LocalDate.now()
        (-1..HORIZON_DAYS).forEach { dayOffset ->
            val date = today.plusDays(dayOffset.toLong())
            PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
                val intent = Intent(context, PrayerAlarmReceiver::class.java)
                    .setAction(ACTION_PRAYER)
                val pending = PendingIntent.getBroadcast(
                    context,
                    requestCode(slot, date),
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
                )
                pending?.let {
                    manager.cancel(it)
                    it.cancel()
                }
            }
        }
    }

    private fun alarmIntent(
        context: Context,
        slot: PrayerSlot,
        at: Instant,
    ): PendingIntent {
        val date = at.atZone(ZoneId.systemDefault()).toLocalDate()
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
            .setAction(ACTION_PRAYER)
            .putExtra(EXTRA_SLOT, slot.name)
            .putExtra(EXTRA_AT_MILLIS, at.toEpochMilli())
        return PendingIntent.getBroadcast(
            context,
            requestCode(slot, date),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Stable per (slot, calendar day) so rescheduling replaces rather than duplicates. */
    private fun requestCode(slot: PrayerSlot, date: LocalDate): Int =
        date.dayOfYear * 10 + slot.ordinal
}
