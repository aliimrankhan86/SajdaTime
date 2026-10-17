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

    /**
     * The optional suhoor reminder rides the same receiver, the same alarm ladder and the same
     * four reschedule triggers as the prayer alerts, with its own action so a prayer alarm and a
     * reminder can never be mistaken for one another. The extras carry the Fajr it precedes and the
     * Ramadan date it is for, which the receiver checks against the settings of the moment it fires.
     */
    const val ACTION_SUHOOR_REMINDER = "com.sajdatime.app.action.SUHOOR_REMINDER"
    const val EXTRA_FAJR_AT_MILLIS = "fajr_at"
    const val EXTRA_DATE_EPOCH_DAY = "date"

    /**
     * Request codes for the reminders start here. Prayer codes are `dayOfYear * 10 + slot`, which
     * tops out at 3,669, so the two families cannot meet, and they have different actions as well.
     * Suhoor codes run 10,001 to 10,366: one per Ramadan date, so rescheduling replaces rather than
     * duplicates, exactly as for prayers.
     */
    internal const val SUHOOR_REQUEST_BASE = 10_000

    /**
     * The "prayer in N minutes" reminder (1.5.0). Its own action, and codes `30,000 + dayOfYear * 10 +
     * slot`, so 30,010 to 33,669: clear of prayers, suhoor and the widget redraw alarm (20,000). The
     * day is the prayer's, not the reminder's, matching how prayer codes are made.
     */
    const val ACTION_PRAYER_REMINDER = "com.sajdatime.app.action.PRAYER_REMINDER"
    internal const val PRAYER_REMINDER_REQUEST_BASE = 30_000

    /** How far ahead to lay down alarms. Two days survives a missed daily job. */
    private const val HORIZON_DAYS = 2

    fun reschedule(context: Context, settings: AppSettings) {
        val coordinates = settings.coordinates ?: return
        val manager = ContextCompat.getSystemService(context, AlarmManager::class.java) ?: return

        cancelAll(context, manager)

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = now.atZone(zone).toLocalDate()

        // Laid down BEFORE the early return below: a person can switch every prayer alert off and
        // still want the suhoor reminder, and an early return first would silently drop it. The
        // planner is wrapped so that nothing about a reminder can ever stop the prayer alarms that
        // follow, which are the thing this class exists for.
        runCatching {
            SuhoorReminderPlan.plan(
                coordinates = coordinates,
                prefs = settings.calculationPrefs,
                minutesBefore = settings.suhoorReminderMinutes,
                today = today,
                now = now,
            )
        }.getOrDefault(emptyList()).forEach { reminder ->
            schedule(context, manager, suhoorIntent(context, reminder), reminder.at)
        }

        val upcoming = upcoming(coordinates, settings, today, now)

        // Also before the early return: a reminder is wanted with or without that prayer's alert.
        // Wrapped for the same reason as the suhoor reminder above.
        runCatching {
            PrayerReminderPlan.plan(upcoming, settings.prayerReminderMinutes, settings.prayerReminderFor, now)
        }.getOrDefault(emptyList()).forEach { reminder ->
            schedule(context, manager, prayerReminderIntent(context, reminder), reminder.at)
        }

        if (settings.alertFor.isEmpty()) return

        // The style is not consulted here any more: every prayer that alerts at all gets
        // the same, strongest available alarm. Which sound it then makes is decided when
        // it fires, in PrayerAlarmReceiver. See schedule().
        upcoming
            .filter { (slot, _) -> slot in settings.alertFor }
            .forEach { (slot, at) -> schedule(context, manager, alarmIntent(context, slot, at), at) }
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
     * here and it is now overruled: a prayer alert that arrives after the prayer has
     * started is a functional failure, the whole point of the notification is that it
     * marks the beginning of the window, and the icon is true — an alarm really is
     * scheduled.
     *
     * That rejection called the icon "a cosmetic cost", which measurement on a real device
     * has since shown to be understated, so the honest version is recorded here rather
     * than the comfortable one. `setAlarmClock` publishes an `AlarmClockInfo`, and
     * `AlarmManager.getNextAlarmClock` returns the soonest one *across every app*. With
     * Fajr at 02:51 the phone's lock screen and status bar report 02:51 as "next alarm",
     * in place of the 07:00 the user set in their own clock app. Their alarm still fires —
     * nothing is broken — but someone glancing at the lock screen to check it is set will
     * see a time they did not choose. Verified on a Redmi Note 13 Pro, HyperOS 3 / API 36:
     * *"Next alarm clock information: user:0 time:… = 2026-08-02 02:51:00"*.
     *
     * Kept anyway, on this project's own tiebreaker: dependable beats elegant. Reversing it
     * costs a missed Fajr; keeping it costs a confusing readout. If it is ever revisited,
     * revisit it with that asymmetry in view and not because the icon looks untidy.
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
     * Rung 3 is worth less than "inexact" makes it sound, and the gap was measured rather
     * than guessed. On HyperOS (Xiaomi/Redmi/POCO) an alarm scheduled this way does not drift
     * inside its hour: the OEM's `power_pending` policy overwrites `whenElapsed` outright and
     * parks it about three days out, where it stays until the user next picks the phone up.
     * Both builds were run on one Redmi Note 13 Pro holding the same 02:51:00 Fajr alarm,
     * same standby bucket, same charger, Doze inactive. Rung 1 fired at 02:51:00.037. Rung 3
     * never fired at all. Rung 3 stays because it never throws and a deferred alarm beats a
     * SecurityException — but do not read it as "within the hour" and do not promote it.
     * Evidence in HANDOVER §10, "The overnight A/B at Fajr".
     *
     * App Standby buckets used to be listed here as the thing rung 1 does *not* fix, because
     * Google's public tables hold a Restricted-bucket app to one alarm per day and document
     * no exemption for `setAlarmClock`. The implementation says otherwise, and it is worth
     * knowing which one to trust: `AlarmManagerService.isExemptFromAppStandby` returns true
     * on `a.alarmClock != null`, so rung 1 never reaches the bucket branch at all. Confirmed
     * on a device at bucket 45 (RESTRICTED), off charger, `App Standby Parole: false` —
     * every alarm read `app_standby` in the past and `whenElapsed == maxWhenElapsed`.
     *
     * The same check explains rung 3's other weakness: an app without the exact-alarm route
     * has `FLAG_ALLOW_WHILE_IDLE` swapped for `FLAG_ALLOW_WHILE_IDLE_COMPAT` (the `flags=0x20`
     * seen in every Xiaomi dump of the old build), and the exemption does not test `_COMPAT`.
     * So rung 3 *was* bucket-throttled and rung 1 is not — one more reason the ladder starts
     * where it does.
     *
     * Still true, and still the reason USE_EXACT_ALARM keeps coming up: at targetSdk 34+
     * SCHEDULE_EXACT_ALARM no longer floors the app at WORKING_SET, so it will keep drifting
     * to RESTRICTED. That now costs the app's *jobs* — DailyRescheduleWorker gets no
     * alarmClock exemption — rather than its alarms. See HANDOVER §6 and
     * docs/reviews/2026-08-03-standby-bucket-exemption.md.
     */
    private fun schedule(
        context: Context,
        manager: AlarmManager,
        pendingIntent: PendingIntent,
        at: Instant,
    ) {
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
        // horizon we are about to write covers every alarm we could have created. The suhoor
        // reminders are cancelled in the same pass, which is what makes switching the reminder
        // off, or leaving Ramadan, take effect on the very next reschedule.
        val today = LocalDate.now()
        (-1..HORIZON_DAYS).forEach { dayOffset ->
            val date = today.plusDays(dayOffset.toLong())
            PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
                cancelPending(context, manager, ACTION_PRAYER, prayerRequestCode(slot, date))
                cancelPending(context, manager, ACTION_PRAYER_REMINDER, prayerReminderRequestCode(slot, date))
            }
            cancelPending(context, manager, ACTION_SUHOOR_REMINDER, suhoorRequestCode(date))
        }
    }

    private fun cancelPending(context: Context, manager: AlarmManager, action: String, requestCode: Int) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).setAction(action)
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        pending?.let {
            manager.cancel(it)
            it.cancel()
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
            prayerRequestCode(slot, date),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun suhoorIntent(context: Context, reminder: SuhoorReminder): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
            .setAction(ACTION_SUHOOR_REMINDER)
            .putExtra(EXTRA_FAJR_AT_MILLIS, reminder.fajrAt.toEpochMilli())
            .putExtra(EXTRA_DATE_EPOCH_DAY, reminder.date.toEpochDay())
        return PendingIntent.getBroadcast(
            context,
            suhoorRequestCode(reminder.date),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun prayerReminderIntent(context: Context, reminder: PrayerReminder): PendingIntent {
        val date = reminder.prayerAt.atZone(ZoneId.systemDefault()).toLocalDate()
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
            .setAction(ACTION_PRAYER_REMINDER)
            .putExtra(EXTRA_SLOT, reminder.slot.name)
            .putExtra(EXTRA_AT_MILLIS, reminder.prayerAt.toEpochMilli())
        return PendingIntent.getBroadcast(
            context,
            prayerReminderRequestCode(reminder.slot, date),
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
    internal fun prayerRequestCode(slot: PrayerSlot, date: LocalDate): Int =
        date.dayOfYear * 10 + slot.ordinal

    /** Stable per Ramadan date, in a range no prayer code can reach. */
    internal fun suhoorRequestCode(date: LocalDate): Int =
        SUHOOR_REQUEST_BASE + date.dayOfYear

    /** Stable per (slot, the prayer's calendar day), in its own range. */
    internal fun prayerReminderRequestCode(slot: PrayerSlot, date: LocalDate): Int =
        PRAYER_REMINDER_REQUEST_BASE + prayerRequestCode(slot, date)
}
