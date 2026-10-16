package com.sajdatime.app.notify

import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import java.time.Instant
import java.time.LocalDate

/**
 * One planned suhoor reminder: the Ramadan date it belongs to, when it fires, and the Fajr it
 * precedes. [date] is the day of the fast it is for, not the day the alarm happens to ring on, so a
 * reminder that fires at 23:30 on the eve still carries the right date and the right request code.
 */
data class SuhoorReminder(val date: LocalDate, val at: Instant, val fajrAt: Instant)

/**
 * Which suhoor reminders to lay down, as a pure function so it is tested without an AlarmManager.
 *
 * The reminder is at Fajr minus N minutes and Fajr is where suhoor ends, so it is a heads up that
 * the window is closing, never a ruling about when to stop eating. N is one of 15, 30, 45 or 60,
 * which keeps it strictly before Fajr: it can never land on the Fajr prayer alert.
 */
object SuhoorReminderPlan {

    /**
     * Reminders for the same three days the prayer scheduler covers (yesterday, today, tomorrow)
     * that fall inside Ramadan on the user's own calendar, only those still in the future. Empty
     * when the reminder is off. Yesterday is in the window for the same reason it is for prayers:
     * a reminder for a date can ring after midnight at the edge of the day.
     *
     * A day that is not Ramadan on the user's shifted calendar never gets a reminder, so the last
     * day's reminder is the last, and after Ramadan the scheduler lays down nothing and the next
     * reschedule cancels whatever is left.
     */
    fun plan(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        minutesBefore: Int,
        today: LocalDate,
        now: Instant,
    ): List<SuhoorReminder> {
        if (minutesBefore <= 0) return emptyList()
        return (-1L..1L).mapNotNull { offset ->
            val date = today.plusDays(offset)
            if (!HijriCalendar.isRamadan(date, prefs.hijriOffset)) return@mapNotNull null
            val fajr = PrayerEngine.compute(coordinates, date, prefs)[PrayerSlot.FAJR]
            val at = fajr.minusSeconds(60L * minutesBefore)
            SuhoorReminder(date = date, at = at, fajrAt = fajr).takeIf { at.isAfter(now) }
        }
    }
}
