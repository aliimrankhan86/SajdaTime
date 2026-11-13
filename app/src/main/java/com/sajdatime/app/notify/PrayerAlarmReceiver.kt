package com.sajdatime.app.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sajdatime.app.data.AppSettings
import com.sajdatime.core.FastReason
import com.sajdatime.core.FastingDays
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.Sect
import com.sajdatime.core.PrayerEngine
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import com.sajdatime.core.PrayerSlot
import com.sajdatime.app.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * Fires at a prayer time, or at the suhoor reminder before Fajr: posts the alert, then lays down the
 * next window of alarms. One receiver for both, so the reminder gets the same chaining and the same
 * four reschedule triggers as a prayer, and no second component exists to be left out of the manifest.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PrayerAlarmScheduler.ACTION_PRAYER -> onPrayer(context, intent)
            PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER -> onSuhoorReminder(context, intent)
            PrayerAlarmScheduler.ACTION_PRAYER_REMINDER -> onPrayerReminder(context, intent)
            PrayerAlarmScheduler.ACTION_FAST_REMINDER -> onFastReminder(context, intent)
        }
    }

    private fun onPrayerReminder(context: Context, intent: Intent) {
        val slot = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_SLOT)
            ?.let { name -> PrayerSlot.entries.firstOrNull { it.name == name } }
            ?: return
        if (!intent.hasExtra(PrayerAlarmScheduler.EXTRA_AT_MILLIS)) return
        val prayerAt = Instant.ofEpochMilli(intent.getLongExtra(PrayerAlarmScheduler.EXTRA_AT_MILLIS, 0L))

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                val now = Instant.now()
                if (shouldPostPrayerReminder(settings, slot, prayerAt, now)) {
                    Notifications.postPrayerReminder(
                        base = appContext,
                        slot = slot,
                        prayerAt = prayerAt,
                        now = now,
                        approximate = settings.isApproximateOn(prayerAt),
                    )
                }
                // Same chain as a prayer, so a person with every alert off still gets the next window.
                PrayerAlarmScheduler.rescheduleAndRefresh(appContext, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun onFastReminder(context: Context, intent: Intent) {
        if (!intent.hasExtra(PrayerAlarmScheduler.EXTRA_FAJR_AT_MILLIS) ||
            !intent.hasExtra(PrayerAlarmScheduler.EXTRA_DATE_EPOCH_DAY)
        ) {
            return
        }
        val reason = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_FAST_REASON)
            ?.let { name -> FastReason.entries.firstOrNull { it.name == name } }
            ?: return
        val fajrAt = Instant.ofEpochMilli(intent.getLongExtra(PrayerAlarmScheduler.EXTRA_FAJR_AT_MILLIS, 0L))
        val fastDate = LocalDate.ofEpochDay(intent.getLongExtra(PrayerAlarmScheduler.EXTRA_DATE_EPOCH_DAY, 0L))

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                val now = Instant.now()
                if (shouldPostFastReminder(settings, fastDate, reason, now)) {
                    Notifications.postFastReminder(appContext, reason, fajrAt)
                }
                // Same chain as a prayer, so the next window is laid down while awake.
                PrayerAlarmScheduler.rescheduleAndRefresh(appContext, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun onSuhoorReminder(context: Context, intent: Intent) {
        if (!intent.hasExtra(PrayerAlarmScheduler.EXTRA_FAJR_AT_MILLIS) ||
            !intent.hasExtra(PrayerAlarmScheduler.EXTRA_DATE_EPOCH_DAY)
        ) {
            return
        }
        val fajrAt = Instant.ofEpochMilli(intent.getLongExtra(PrayerAlarmScheduler.EXTRA_FAJR_AT_MILLIS, 0L))
        val date = LocalDate.ofEpochDay(intent.getLongExtra(PrayerAlarmScheduler.EXTRA_DATE_EPOCH_DAY, 0L))

        val appContext = context.applicationContext
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = SettingsRepository(appContext).current()
                val now = Instant.now()
                if (shouldPostReminder(settings, date, fajrAt, now)) {
                    Notifications.postSuhoorReminder(
                        base = appContext,
                        fajrAt = fajrAt,
                        now = now,
                        style = settings.suhoorReminderStyle,
                        soundUri = suhoorSoundUri(settings, appContext.packageName),
                        respectSilent = settings.alarmRespectsSilent,
                        approximate = settings.isApproximateOn(fajrAt),
                        alarmOnApproximate = settings.alarmOnApproximateDays,
                    )
                }
                // Same chain as a prayer: lay down the next window and refresh the badge while awake.
                PrayerAlarmScheduler.rescheduleAndRefresh(appContext, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun onPrayer(context: Context, intent: Intent) {
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
                Notifications.clearPrayerReminder(appContext, slot)
                settings.alertFor[slot]?.let { style ->
                    Notifications.postPrayerAlert(
                        base = appContext,
                        slot = slot,
                        at = at,
                        style = style,
                        alarmSoundUri = settings.alarmSoundUri,
                        notificationSoundUri = settings.notificationSoundUri,
                        respectSilent = settings.alarmRespectsSilent,
                        approximate = settings.isApproximateOn(at),
                        alarmOnApproximate = settings.alarmOnApproximateDays,
                    )
                }
                // Chain the next horizon and refresh the badge while we are already awake.
                PrayerAlarmScheduler.rescheduleAndRefresh(appContext, settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

/**
 * Whether the day containing [at] had to be projected from another latitude.
 *
 * Recomputed here rather than carried through the alarm's intent extras. The alarm may have
 * been laid down days ago, and the answer can change between then and now: the user can move,
 * change method, or simply cross the date on which the sun stops setting where they live. An
 * extra sub-second computation on a receiver that is already awake is cheaper than an alert
 * that describes yesterday's astronomy.
 */
private fun AppSettings.isApproximateOn(at: Instant): Boolean {
    val coordinates = coordinates ?: return false
    return runCatching {
        PrayerEngine.compute(
            coordinates,
            at.atZone(ZoneId.systemDefault()).toLocalDate(),
            calculationPrefs,
        ).approximatedFrom != null
    }.getOrDefault(false)
}

/**
 * Whether an arriving suhoor reminder should still be shown, judged on the settings of the moment it
 * fires and not on those it was scheduled with.
 *
 * Three ways it is stale, all of them silence and none of them an error:
 *  - the person switched the reminder off (reschedule cancels it, but a delivery already in flight
 *    can still arrive);
 *  - the date is not in Ramadan on the calendar they are using now (they shifted their Islamic date);
 *  - it is late. A reminder is a heads up that Fajr is coming. Delivered after a long Doze or a phone
 *    that was off, "Fajr begins in 14 minutes" is a statement that is no longer true, so anything more
 *    than [REMINDER_GRACE] past its time is dropped. The shortest choice is 15 minutes before Fajr,
 *    so five minutes late still leaves 10 minutes of real warning.
 */
internal fun shouldPostReminder(
    settings: AppSettings,
    date: LocalDate,
    fajrAt: Instant,
    now: Instant,
): Boolean {
    val minutes = settings.suhoorReminderMinutes
    if (minutes <= 0) return false
    if (!HijriCalendar.isRamadan(date, settings.calculationPrefs.hijriOffset)) return false
    val reminderAt = fajrAt.minusSeconds(60L * minutes)
    return !now.isAfter(reminderAt.plus(REMINDER_GRACE))
}

internal val REMINDER_GRACE: Duration = Duration.ofMinutes(5)

/**
 * Whether an arriving "prayer in N minutes" reminder should still be shown, on the settings of the
 * moment it fires: off, or that prayer unticked, is silence; so is arriving after a long Doze when
 * less than a minute is left, or more than [REMINDER_GRACE] late. Past either line the prayer alert
 * itself (if any) is the next thing the person should see, not a heads up about it.
 */
internal fun shouldPostPrayerReminder(
    settings: AppSettings,
    slot: PrayerSlot,
    prayerAt: Instant,
    now: Instant,
): Boolean {
    val minutes = settings.prayerReminderMinutes
    if (minutes <= 0 || slot !in settings.prayerReminderFor) return false
    if (!now.isBefore(prayerAt.minusSeconds(60))) return false
    val reminderAt = prayerAt.minusSeconds(60L * minutes)
    return !now.isAfter(reminderAt.plus(REMINDER_GRACE))
}

/**
 * Whether an arriving fast reminder should still be shown, on the settings of the moment it fires:
 * not Sunni, or that kind switched off, or the day no longer a fast on the calendar they use now, is
 * silence. So is arriving on the wrong day: "Tomorrow is a fast" is only true on the evening before.
 */
internal fun shouldPostFastReminder(
    settings: AppSettings,
    fastDate: LocalDate,
    reason: FastReason,
    now: Instant,
    zone: ZoneId = ZoneId.systemDefault(),
): Boolean {
    if (settings.sect != Sect.SUNNI) return false
    if (FastingDays.fastOn(fastDate, settings.calculationPrefs.hijriOffset, settings.fastReminders) != reason) return false
    return now.atZone(zone).toLocalDate() == fastDate.minusDays(1)
}
