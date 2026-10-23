package com.sajdatime.core

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * The user's own mosque's jama'ah (iqamah) and Jumu'ah times (1.6.0). Typed in by the user: there is
 * no feed and no server, so this is weaker than an app whose mosques keep their own times, and the
 * "minutes after it begins" rule is the answer to seasons (a mosque that moves its jama'ah with the
 * start time needs entering once, not every week).
 */
sealed interface JamaahRule {
    data class Fixed(val time: LocalTime) : JamaahRule

    /** Minutes after the prayer begins, counted from the start time the user sees (adjustments applied). */
    data class After(val minutes: Int) : JamaahRule

    companion object {
        /** 0 to 60 in steps of 5: a congregation more than an hour after the adhan is a fixed time. */
        val AFTER_CHOICES: List<Int> = (0..60 step 5).toList()
    }
}

/** A prayer absent from [rules] has no jama'ah time set, which is everyone's starting point. */
data class JamaahTimes(
    val rules: Map<PrayerSlot, JamaahRule> = emptyMap(),
    /** Friday only, and only a fixed time: Jumu'ah is not counted from the start time. */
    val jumuah: LocalTime? = null,
) {
    val isEmpty: Boolean get() = rules.isEmpty() && jumuah == null
    val count: Int get() = rules.size + if (jumuah != null) 1 else 0
}

sealed interface JamaahResult {
    data class Shown(val at: Instant, val isJumuah: Boolean = false) : JamaahResult

    /**
     * The typed time is earlier than the prayer begins, or not before the next one. Never shown as a
     * time: a jama'ah earlier than the adhan would tell someone to pray before the prayer is due. The
     * usual cause is a fixed time typed in summer that the start time has since passed.
     */
    data object OutOfWindow : JamaahResult
}

object JamaahPlan {

    /**
     * Today's jama'ah for each prayer that has a rule, checked against the prayer's window: from its
     * start until the next prayer begins, with Fajr ending at sunrise and Jumu'ah between Dhuhr and Asr.
     * [day] must be the times the user sees, adjustments included, so "15 minutes after" counts from
     * the time on their screen.
     *
     * On Friday, Dhuhr's line is Jumu'ah or nothing. It never falls back to Dhuhr's own jama'ah, which
     * many mosques do not hold that day.
     */
    fun on(day: DayPrayerTimes, zone: ZoneId, jamaah: JamaahTimes): Map<PrayerSlot, JamaahResult> {
        val friday = day.date.dayOfWeek == DayOfWeek.FRIDAY
        val out = mutableMapOf<PrayerSlot, JamaahResult>()
        for (slot in TRACKED_SLOTS) {
            if (friday && slot == PrayerSlot.DHUHR) {
                out[slot] = jumuah(day, zone, jamaah.jumuah ?: continue)
                continue
            }
            out[slot] = result(day, zone, slot, jamaah.rules[slot] ?: continue)
        }
        return out
    }

    /** One prayer's rule on [day], whatever the weekday: the Jama'ah times screen previews each row with it. */
    fun result(day: DayPrayerTimes, zone: ZoneId, slot: PrayerSlot, rule: JamaahRule): JamaahResult {
        val start = day[slot]
        val at = when (rule) {
            is JamaahRule.Fixed -> fixedAt(day, rule.time, zone, afterMidnight = slot == PrayerSlot.ISHA && rule.time < LocalTime.NOON)
            is JamaahRule.After -> start.plus(Duration.ofMinutes(rule.minutes.toLong()))
        }
        return check(at, start, windowEnd(day, slot), isJumuah = false)
    }

    /** A Jumu'ah time against [day]'s Dhuhr to Asr window, whatever the weekday (for the same preview). */
    fun jumuah(day: DayPrayerTimes, zone: ZoneId, time: LocalTime): JamaahResult =
        check(fixedAt(day, time, zone), day[PrayerSlot.DHUHR], windowEnd(day, PrayerSlot.DHUHR), isJumuah = true)

    private fun check(at: Instant, start: Instant, end: Instant, isJumuah: Boolean): JamaahResult =
        if (!at.isBefore(start) && at.isBefore(end)) JamaahResult.Shown(at, isJumuah) else JamaahResult.OutOfWindow

    /**
     * A clock time on this day, in this zone. A time that a clock change skips moves forward with it
     * (ZonedDateTime's rule). An Isha jama'ah typed as a small-hours time (00:15) belongs to the night
     * after this day, as it would on a mosque board.
     */
    private fun fixedAt(day: DayPrayerTimes, time: LocalTime, zone: ZoneId, afterMidnight: Boolean = false): Instant =
        (if (afterMidnight) day.date.plusDays(1) else day.date).atTime(time).atZone(zone).toInstant()

    /** When a prayer's window closes. Isha runs to the next Fajr, taken as this Fajr a day later. */
    private fun windowEnd(day: DayPrayerTimes, slot: PrayerSlot): Instant = when (slot) {
        PrayerSlot.FAJR -> day[PrayerSlot.SUNRISE]
        PrayerSlot.DHUHR -> day[PrayerSlot.ASR]
        PrayerSlot.ASR -> day[PrayerSlot.MAGHRIB]
        PrayerSlot.MAGHRIB -> day[PrayerSlot.ISHA]
        else -> day[PrayerSlot.FAJR].plus(Duration.ofDays(1))
    }
}

/**
 * How [JamaahTimes] survives a round trip through one stored string: "FAJR=+15,DHUHR=13:30,JUMUAH=13:15".
 * Anything it cannot read is dropped entry by entry, as [AdjustmentCodec] does, and minutes are kept
 * to [JamaahRule.AFTER_CHOICES], so a damaged string loses an entry and never invents a time.
 */
object JamaahCodec {
    private const val JUMUAH = "JUMUAH"

    fun encode(j: JamaahTimes): String = buildList {
        for (slot in TRACKED_SLOTS) {
            when (val r = j.rules[slot]) {
                is JamaahRule.Fixed -> add("${slot.name}=${r.time}")
                is JamaahRule.After -> add("${slot.name}=+${r.minutes}")
                null -> Unit
            }
        }
        j.jumuah?.let { add("$JUMUAH=$it") }
    }.joinToString(",")

    fun decode(raw: String?): JamaahTimes {
        if (raw.isNullOrBlank()) return JamaahTimes()
        val rules = mutableMapOf<PrayerSlot, JamaahRule>()
        var jumuah: LocalTime? = null
        for (entry in raw.split(",")) {
            val (key, value) = entry.split("=").takeIf { it.size == 2 } ?: continue
            if (key == JUMUAH) {
                jumuah = parseTime(value) ?: continue
                continue
            }
            val slot = TRACKED_SLOTS.firstOrNull { it.name == key } ?: continue
            rules[slot] = if (value.startsWith("+")) {
                val m = value.drop(1).toIntOrNull()?.takeIf { it in JamaahRule.AFTER_CHOICES } ?: continue
                JamaahRule.After(m)
            } else {
                JamaahRule.Fixed(parseTime(value) ?: continue)
            }
        }
        return JamaahTimes(rules, jumuah)
    }

    private fun parseTime(s: String): LocalTime? = runCatching { LocalTime.parse(s) }.getOrNull()
}
