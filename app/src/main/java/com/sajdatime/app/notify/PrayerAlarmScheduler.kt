package com.sajdatime.app.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.MainActivity
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
        if (settings.alertFor.isEmpty()) return

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = now.atZone(zone).toLocalDate()

        // The style is not consulted here any more: every prayer that alerts at all gets
        // the same, strongest available alarm. Which sound it then makes is decided when
        // it fires, in PrayerAlarmReceiver. See schedule().
        upcoming(coordinates, settings, today, now)
            .filter { (slot, _) -> slot in settings.alertFor }
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
     * Schedules one alarm as precisely as the system currently permits, trying three
     * mechanisms in descending order of guarantee and never throwing.
     *
     * **Both styles now get `setAlarmClock` — this changed after a tester reported late
     * notifications, and the docs say why.** Of the four `AlarmManager` APIs only this
     * one is documented as never being moved: *"Because these alarms are highly visible
     * to users, the system never adjusts their delivery time. The system identifies these
     * alarms as the most critical ones and leaves low-power modes if necessary to deliver
     * the alarms."* The alarm this code used for notification style,
     * `setExactAndAllowWhileIdle`, carries the opposite promise in the same reference:
     * *"the OS will allow itself more flexibility for scheduling these alarms than regular
     * exact alarms… When the device is idle it may take even more liberties with
     * scheduling in order to optimize for battery life"*, plus a documented rate limit
     * that Google's own pages state three different ways (once per nine minutes, about
     * every minute, seven per hour). No page gives an upper bound on how late it can be.
     *
     * The rejected alternative was to keep the quieter alarm for notification style so it
     * "does not claim a status bar icon it has not earned". That was the previous comment
     * here and it is now overruled: the icon is a cosmetic cost, a prayer alert that
     * arrives after the prayer has started is a functional failure, and the whole point
     * of the notification is that it marks the beginning of the window. The icon is also
     * true — an alarm really is scheduled.
     *
     * The ladder, and why it is a ladder rather than one call and a catch:
     *
     *  1. `setAlarmClock` — the strongest guarantee, but it *always* requires
     *     SCHEDULE_EXACT_ALARM. Unlike `setExact*`, its reference note has no "unless the
     *     app is exempt from battery restrictions" clause.
     *  2. `setExactAndAllowWhileIdle` — throttled, but still exact, and an app the user
     *     has exempted from battery optimisation may call it *without* the permission.
     *     Going straight from 1 to 3, as this did, threw that away.
     *  3. `setAndAllowWhileIdle` — inexact, never throws, always available.
     *
     * What this does **not** fix, and must not be claimed to: App Standby buckets. Google
     * documents no exemption from them for `setAlarmClock`, and a Restricted-bucket app is
     * held to *"One alarm per day, either an exact alarm or an inexact alarm"*. At
     * targetSdk 34+ SCHEDULE_EXACT_ALARM no longer floors the app at WORKING_SET either —
     * only USE_EXACT_ALARM does, and this app is not eligible for it. See HANDOVER §10.
     */
    private fun schedule(
        context: Context,
        manager: AlarmManager,
        slot: PrayerSlot,
        at: Instant,
    ) {
        val pendingIntent = alarmIntent(context, slot, at)
        val triggerAt = at.toEpochMilli()

        // Each rung is attempted only if the one above it failed. runCatching rather than
        // a permission check alone, because some OEM builds and work profiles revoke the
        // capability between the check and the call.
        val exact = canScheduleExact(manager) && runCatching {
            manager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, openAppIntent(context)),
                pendingIntent,
            )
        }.isSuccess

        if (exact) return

        val stillExact = runCatching {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }.isSuccess

        if (stillExact) return

        runCatching {
            manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun canScheduleExact(manager: AlarmManager): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    fun canScheduleExact(context: Context): Boolean {
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java)
            ?: return false
        return canScheduleExact(manager)
    }

    /**
     * Opens the system's "Alarms & reminders" screen for this app.
     *
     * There is no runtime-permission dialog for SCHEDULE_EXACT_ALARM — a settings screen
     * is the only route. Shared by the two banners and by onboarding so all three send
     * the user to the same place, and so the runCatching is written once: the intent is
     * absent on some builds, and an ActivityNotFoundException here would crash the app
     * from a button whose whole purpose is making it more reliable.
     */
    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    .setData("package:${context.packageName}".toUri()),
            )
        }
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

    /** Where the system sends the user if they tap the alarm-clock icon in the status bar. */
    private fun openAppIntent(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    /** Stable per (slot, calendar day) so rescheduling replaces rather than duplicates. */
    private fun requestCode(slot: PrayerSlot, date: LocalDate): Int =
        date.dayOfYear * 10 + slot.ordinal
}
