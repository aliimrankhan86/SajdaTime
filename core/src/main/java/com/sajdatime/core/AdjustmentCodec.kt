package com.sajdatime.core

/**
 * How [CalculationPrefs.adjustments] survives a round trip through a single string.
 *
 * "FAJR:5,ISHA:-3". It lives in `core` rather than beside the phone's other preference
 * codecs because it has two callers that must never disagree: the phone stores it, and it
 * also travels to the watch over the Data Layer as one string field. A second
 * implementation on the watch side would be a second chance to parse it differently, and
 * the worst bug in this project's history lived in exactly that kind of gap between two
 * modules that were each individually correct.
 *
 * Sunrise is encodable. It is not a prayer and is never alerted, but it is printed in the
 * timetable and on the PDF pinned to a wall, and a mosque that shifts its printed sunrise
 * while this app does not is the same mismatch the whole feature exists to remove.
 *
 * Every value is re-clamped on the way *out* as well as in, because the way in is not the
 * only way data arrives: `SettingsSyncService` has to be an exported component for Play
 * Services to deliver Data Layer events at all, and a preferences file can be restored
 * from a half-finished write. Same reasoning as [Coordinates.orNull], deliberately copied
 * rather than reinvented.
 */
object AdjustmentCodec {

    fun encode(adjustments: Map<PrayerSlot, Int>): String =
        adjustments.filterValues { it != 0 }
            .entries
            .sortedBy { it.key.ordinal }
            .joinToString(",") { "${it.key.name}:${it.value}" }

    fun decode(raw: String?): Map<PrayerSlot, Int> {
        if (raw.isNullOrBlank()) return emptyMap()
        return raw.split(",").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size != 2) return@mapNotNull null
            val slot = PrayerSlot.entries.firstOrNull { it.name == parts[0] }
                ?: return@mapNotNull null
            val minutes = parts[1].toIntOrNull()?.takeIf { it != 0 } ?: return@mapNotNull null
            slot to minutes.coerceIn(
                -CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
                CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
            )
        }.toMap()
    }
}
