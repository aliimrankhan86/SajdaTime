package com.sajdatime.app.notify

import com.sajdatime.core.PrayerSlot
import java.time.Instant

/** One planned heads up: the prayer it is for, that prayer's time, and when the reminder fires. */
data class PrayerReminder(val slot: PrayerSlot, val prayerAt: Instant, val at: Instant)

/**
 * Which "prayer in N minutes" reminders to lay down, as a pure function so it is tested without an
 * AlarmManager. It takes the scheduler's own list of upcoming prayers, so a reminder can only ever be
 * N minutes before a time the alerts themselves use (adjustments and method included).
 *
 * A reminder whose moment has already passed is skipped, even when its prayer is still ahead: opening
 * the app 3 minutes before Asr must not produce a "10 minutes" heads up late.
 */
object PrayerReminderPlan {

    fun plan(
        upcoming: List<Pair<PrayerSlot, Instant>>,
        minutesBefore: Int,
        slots: Set<PrayerSlot>,
        now: Instant,
    ): List<PrayerReminder> {
        if (minutesBefore <= 0) return emptyList()
        return upcoming
            .filter { (slot, _) -> slot in slots }
            .map { (slot, prayerAt) -> PrayerReminder(slot, prayerAt, prayerAt.minusSeconds(60L * minutesBefore)) }
            .filter { it.at.isAfter(now) }
    }
}
