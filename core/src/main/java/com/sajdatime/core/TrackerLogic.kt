package com.sajdatime.core

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * The private prayer tracker ("My prayers", 1.6.0): every rule about days, ticks, pauses and the
 * streak, as pure Kotlin so each edge case is a unit test (TrackerLogicTest).
 *
 * Ali's design of 16 Oct 2026 (docs/plans/2026-10-widget-ramadan-tracker/README.md, "Decisions on
 * Release B"), signed off again for 1.6 on 23 Oct 2026 (docs/plans/2026-10-release-1.6.0/SIGNOFF.md):
 *  - The app records, it never judges. There is no "missed" state, no count of unticked prayers and
 *    no year grid, because a grid of blanks is a missed-prayer counter in another form.
 *  - No "excused" days. A neutral Pause instead, with no reason asked or stored, so it covers a
 *    period, illness and travel alike. The streak waits while paused: paused days neither add to it
 *    nor break it.
 *  - The streak is hidden until the user switches it on.
 */

/** The five prayers in tracker order. Sunrise is not a prayer and has no tick. */
val TRACKED_SLOTS: List<PrayerSlot> = PrayerSlot.entries.filter { it.isPrayer }

/** One day's ticks. Stored as a bitmask, bits 0 to 4 in [TRACKED_SLOTS] order. */
data class DayRecord(val ticked: Set<PrayerSlot> = emptySet()) {
    val isComplete: Boolean get() = ticked.containsAll(TRACKED_SLOTS)

    fun toMask(): Int = TRACKED_SLOTS.foldIndexed(0) { i, acc, slot -> if (slot in ticked) acc or (1 shl i) else acc }

    companion object {
        /**
         * Bits 0 to 4 only. Bit 5 was "excused" in the 16 Oct draft and was never shipped; anything a
         * later version writes above bit 4 is ignored rather than misread as a tick.
         */
        const val MASK = (1 shl 5) - 1

        fun fromMask(mask: Int): DayRecord =
            DayRecord(TRACKED_SLOTS.filterIndexed { i, _ -> mask and MASK and (1 shl i) != 0 }.toSet())
    }
}

/**
 * Days on which tracking was paused, [start] to [end] inclusive. [end] is null while the pause is
 * still on. Stored as ranges, not a flag per day, so nothing has to be written each day of a pause.
 */
data class PauseRange(val start: LocalDate, val end: LocalDate?) {
    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && (end == null || !date.isAfter(end))
}

/** What a day's ring shows in the week card and the 30 day grid. */
enum class DayKind { BEFORE_START, FUTURE, PAUSED, SHARE }

/** What one tick shows. Never "missed": an unticked past prayer is a plain empty circle. */
enum class TickState { TICKED, NOT_TICKED, NOT_DUE_YET, PAUSED }

object TrackerLogic {

    fun isPaused(date: LocalDate, pauses: List<PauseRange>): Boolean = pauses.any { it.contains(date) }

    /** The pause that is still on, if any. */
    fun openPause(pauses: List<PauseRange>): PauseRange? = pauses.firstOrNull { it.end == null }

    /**
     * A prayer ticked before the pause stays ticked (Fable, 23 Oct 2026): pausing means "do not count
     * what is still to come", never "forget what I prayed". The screen still disables it while paused.
     */
    fun tickState(record: DayRecord?, slot: PrayerSlot, due: Boolean, paused: Boolean): TickState = when {
        record?.ticked?.contains(slot) == true -> TickState.TICKED
        paused -> TickState.PAUSED
        !due -> TickState.NOT_DUE_YET
        else -> TickState.NOT_TICKED
    }

    /**
     * Days in a row with all five ticked, ending today or yesterday. Today counts only once it is
     * complete, and an incomplete today never breaks the run (the day is not over). Paused days are
     * stepped over. A day with no record ends the run, which is also how the days before the tracker
     * was switched on end it.
     */
    fun streak(records: Map<LocalDate, DayRecord>, pauses: List<PauseRange>, today: LocalDate): Int {
        val earliest = records.keys.minOrNull() ?: return 0
        var count = 0
        if (!isPaused(today, pauses) && records[today]?.isComplete == true) count++
        var day = today.minusDays(1)
        // Bounded by the earliest record: a long pause before it must not loop for ever.
        while (!day.isBefore(earliest)) {
            if (!isPaused(day, pauses)) {
                if (records[day]?.isComplete != true) break
                count++
            }
            day = day.minusDays(1)
        }
        return count
    }

    /**
     * Starts a pause on [from], which the screen offers as today or yesterday only (Ali's rule: someone
     * who notices a day late is not penalised, but history is not rewritten further back). Does nothing
     * if a pause is already on. Joins a pause that ended the day before, so pause, resume, pause makes
     * one range.
     */
    fun pause(pauses: List<PauseRange>, from: LocalDate): List<PauseRange> {
        if (openPause(pauses) != null) return pauses
        return normalise(pauses + PauseRange(from, null))
    }

    /**
     * Ends the pause that is on, so [today] is an ordinary tracked day again. A pause started and
     * ended on the same day leaves nothing behind.
     */
    fun resume(pauses: List<PauseRange>, today: LocalDate): List<PauseRange> {
        val open = openPause(pauses) ?: return pauses
        val end = today.minusDays(1)
        val rest = pauses - open
        return if (end.isBefore(open.start)) rest else normalise(rest + PauseRange(open.start, end))
    }

    /** Sorted, with overlapping or touching ranges joined, and at most one open range (the last). */
    fun normalise(pauses: List<PauseRange>): List<PauseRange> {
        val out = mutableListOf<PauseRange>()
        for (r in pauses.sortedBy { it.start }) {
            val last = out.lastOrNull()
            if (last != null && (last.end == null || !r.start.isAfter(last.end.plusDays(1)))) {
                val end = if (last.end == null || r.end == null) null else maxOf(last.end, r.end)
                out[out.lastIndex] = PauseRange(last.start, end)
            } else {
                out += r
            }
        }
        return out
    }

    /** The days shown in the history sheet, newest first, ending today. */
    fun historyDates(today: LocalDate, days: Int = HISTORY_DAYS): List<LocalDate> =
        (0 until days).map { today.minusDays(it.toLong()) }

    const val HISTORY_DAYS = 30

    /**
     * A day's ring: before tracking began, to come, paused, or its share of five. A day to come is never
     * shown paused, even though a pause still on has no end date and so covers it.
     */
    fun dayKind(date: LocalDate, today: LocalDate, started: LocalDate, pauses: List<PauseRange>): DayKind = when {
        date.isBefore(started) -> DayKind.BEFORE_START
        date.isAfter(today) -> DayKind.FUTURE
        isPaused(date, pauses) -> DayKind.PAUSED
        else -> DayKind.SHARE
    }

    /** The date on or before [date] that falls on [firstDay]. */
    private fun weekStart(date: LocalDate, firstDay: DayOfWeek): LocalDate =
        date.minusDays(((date.dayOfWeek.value - firstDay.value + 7) % 7).toLong())

    /** The seven dates of the week holding [today], from the locale's [firstDay] (the week card). */
    fun weekOf(today: LocalDate, firstDay: DayOfWeek): List<LocalDate> =
        weekStart(today, firstDay).let { start -> (0L..6L).map { start.plusDays(it) } }

    /**
     * The first date of each week row of the Last 30 days grid, oldest first, the last row holding
     * today. The window is the [days] ending today; weeks wholly before [started] are dropped (Fable,
     * 23 Oct 2026: rows of empty numbers read as "you missed a lot"), the week holding it stays.
     */
    fun gridWeeks(today: LocalDate, started: LocalDate, firstDay: DayOfWeek, days: Int = HISTORY_DAYS): List<LocalDate> {
        val windowStart = today.minusDays((days - 1).toLong())
        return generateSequence(weekStart(maxOf(windowStart, started), firstDay)) { it.plusDays(7) }
            .takeWhile { !it.isAfter(today) }.toList()
    }

    /**
     * The first day the record covers (DETAILED-PLAN 9.11, as decided by Fable on 23 Oct 2026). Days
     * before it get no ring and cannot be chosen (Ali, Q4). [stored] is written each time tracking is
     * switched on ([startOnSwitchOn]) and moved earlier when a saved file brings older days ([TrackerRepository.merge]).
     * Nothing stored (a record from before the key existed): the earliest tick or pause. Today when nothing is known.
     */
    fun started(stored: LocalDate?, records: Map<LocalDate, DayRecord>, pauses: List<PauseRange>, today: LocalDate): LocalDate =
        stored?.let { minOf(it, today) }
            ?: (listOf(today) + records.filterValues { it.ticked.isNotEmpty() }.keys + pauses.map { it.start }).min()

    /**
     * The start to store when tracking is switched on (Fable, 23 Oct 2026). Someone who switched off and
     * on again is starting again, so the screen looks like a first day, not a month of blanks: today,
     * unless a tick exists inside the last [HISTORY_DAYS] days, then the oldest such day. Older ticks are
     * kept untouched (the file, the streak); only where the rings begin moves.
     */
    /**
     * The stored start after something is recorded on [date] (a tick, or a loaded file's oldest day):
     * moved back if [date] is earlier, so that day is drawn and can be changed. Last night's Isha ticked
     * on the first night is the case that needs it (review, 23 Oct 2026). Null stays null: nothing stored
     * means the start is worked out from the record anyway.
     */
    fun startAfterRecording(stored: LocalDate?, date: LocalDate): LocalDate? =
        stored?.let { if (date.isBefore(it)) date else it }

    fun startOnSwitchOn(records: Map<LocalDate, DayRecord>, today: LocalDate): LocalDate {
        val windowStart = today.minusDays((HISTORY_DAYS - 1).toLong())
        return records.filter { (d, r) -> r.ticked.isNotEmpty() && !d.isBefore(windowStart) && !d.isAfter(today) }
            .keys.minOrNull() ?: today
    }

    /**
     * "Fasts to make up" (Ali, 25 Oct 2026, moved into 1.6.0): one number the person sets, no reasons and
     * no dates. Anything stored or loaded passes through here, so a damaged file cannot show -4 or a
     * million. ponytail: 999 by one step at a time; a typed number if anyone owes more and says so.
     */
    const val MAX_FASTS = 999

    fun clampFasts(count: Int): Int = count.coerceIn(0, MAX_FASTS)

    /** One plus or minus, applied to the stored number, so two quick taps count twice (review, 25 Oct 2026). */
    fun adjustFasts(current: Int, delta: Int): Int = clampFasts(current + delta)

    /**
     * Loading a file keeps the larger count, never the sum: the same file loaded twice must not double
     * it, and a new phone at 0 takes the file's number.
     */
    fun mergeFasts(here: Int, incoming: Int): Int = clampFasts(maxOf(here, incoming))
}

/**
 * How the pause ranges survive a round trip through one stored string, and through the save file:
 * "2027-02-08..2027-02-12,2027-03-01.." (an empty end is a pause still on). Anything unreadable is
 * dropped entry by entry, as [AdjustmentCodec] does, so a damaged string loses a range, not the app.
 */
object PauseCodec {
    fun encode(pauses: List<PauseRange>): String =
        TrackerLogic.normalise(pauses).joinToString(",") { "${it.start}..${it.end ?: ""}" }

    fun decode(raw: String?): List<PauseRange> {
        if (raw.isNullOrBlank()) return emptyList()
        return TrackerLogic.normalise(
            raw.split(",").mapNotNull { entry ->
                val parts = entry.split("..")
                if (parts.size != 2) return@mapNotNull null
                val start = runCatching { LocalDate.parse(parts[0]) }.getOrNull() ?: return@mapNotNull null
                val end = if (parts[1].isEmpty()) null else runCatching { LocalDate.parse(parts[1]) }.getOrNull() ?: return@mapNotNull null
                if (end != null && end.isBefore(start)) null else PauseRange(start, end)
            },
        )
    }
}
