package com.sajdatime.app.notify

import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.FastKind
import com.sajdatime.core.FastReason
import com.sajdatime.core.FastingDays
import com.sajdatime.core.JamaahPlan
import com.sajdatime.core.JamaahResult
import com.sajdatime.core.JamaahTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * The two 1.6.1 reminders, planned as pure functions so they are tested without an AlarmManager.
 * Signed off on 23 Oct 2026 with two conditions: every request code band proven apart
 * (RequestCodeTest), and a rule for an Isha after midnight ([FastReminderPlan.eveningOf]).
 */

/** "Asr jama'ah at 3:30 pm, in 15 minutes." [date] is the prayer's day, for the request code. */
data class JamaahReminder(val slot: PrayerSlot, val date: LocalDate, val jamaahAt: Instant, val at: Instant, val isJumuah: Boolean)

object JamaahReminderPlan {

    /**
     * A reminder N minutes before each jama'ah that [JamaahPlan] would show, on the scheduler's three
     * days, still in the future. A jama'ah outside its prayer's window gets none: the screen says
     * "Check your jama'ah time" and a reminder would contradict it.
     */
    fun plan(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        jamaah: JamaahTimes,
        minutesBefore: Int,
        today: LocalDate,
        now: Instant,
        zone: ZoneId,
    ): List<JamaahReminder> {
        if (minutesBefore <= 0 || jamaah.isEmpty) return emptyList()
        return (-1L..1L).flatMap { offset ->
            val date = today.plusDays(offset)
            val day = PrayerEngine.compute(coordinates, date, prefs)
            JamaahPlan.on(day, zone, jamaah).mapNotNull { (slot, result) ->
                val shown = result as? JamaahResult.InWindow ?: return@mapNotNull null
                val at = shown.at.minus(Duration.ofMinutes(minutesBefore.toLong()))
                JamaahReminder(slot, date, shown.at, at, shown.isJumuah).takeIf { at.isAfter(now) }
            }
        }
    }
}

/** "Tomorrow is a Sunnah fast (Monday). Suhoor ends at 5:12 am." */
data class FastReminder(val fastDate: LocalDate, val reason: FastReason, val at: Instant, val fajrAt: Instant) {
    /** The day the reminder rings on, which is what its request code is keyed on. */
    val eve: LocalDate get() = fastDate.minusDays(1)
}

object FastReminderPlan {

    /**
     * A reminder the evening before each fast day from today to two days ahead, still in the future,
     * Sunni only. The evening's reminder rings on the day before, so this keeps it inside the window
     * `cancelAll` walks (yesterday to two days ahead), keyed on the day it rings.
     */
    fun plan(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        sect: Sect,
        kinds: Set<FastKind>,
        today: LocalDate,
        now: Instant,
        zone: ZoneId,
    ): List<FastReminder> {
        if (sect != Sect.SUNNI || kinds.isEmpty()) return emptyList()
        return (0L..2L).mapNotNull { offset ->
            val fastDate = today.plusDays(offset)
            val reason = FastingDays.fastOn(fastDate, prefs.hijriOffset, kinds) ?: return@mapNotNull null
            val at = eveningOf(coordinates, prefs, fastDate.minusDays(1), zone)
            val fajr = PrayerEngine.compute(coordinates, fastDate, prefs)[PrayerSlot.FAJR]
            FastReminder(fastDate, reason, at, fajr).takeIf { at.isAfter(now) }
        }
    }

    /**
     * When the reminder rings on [eve]: at Isha, not Maghrib, because Maghrib is the adhan and iftar.
     *
     * Unless Isha is late or borrowed: after [LATE_ISHA], after midnight, or projected from another
     * latitude (in a northern summer Isha can be past midnight, and an "evening before" reminder would
     * then arrive on the fast day itself). Then it rings [AFTER_MAGHRIB] after Maghrib instead, which
     * is past iftar and still the evening before. Condition (b) of the 1.6.1 sign-off.
     *
     * Where even that crosses midnight (the far north near midsummer, where the sun hardly sets), it
     * rings at [FALLBACK_EVENING] on the evening before, so it is never on the fast day itself.
     */
    fun eveningOf(coordinates: Coordinates, prefs: CalculationPrefs, eve: LocalDate, zone: ZoneId): Instant {
        val day = PrayerEngine.compute(coordinates, eve, prefs)
        val isha = day[PrayerSlot.ISHA].atZone(zone)
        val late = day.approximated || isha.toLocalDate() != eve || !isha.toLocalTime().isBefore(LATE_ISHA)
        val at = if (late) day[PrayerSlot.MAGHRIB].plus(AFTER_MAGHRIB) else isha.toInstant()
        return if (at.atZone(zone).toLocalDate() == eve) at else eve.atTime(FALLBACK_EVENING).atZone(zone).toInstant()
    }

    private val LATE_ISHA: LocalTime = LocalTime.of(23, 0)
    private val FALLBACK_EVENING: LocalTime = LocalTime.of(21, 0)
    private val AFTER_MAGHRIB: Duration = Duration.ofMinutes(30)
}
