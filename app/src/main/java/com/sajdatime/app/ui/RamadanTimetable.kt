package com.sajdatime.app.ui

import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.RamadanInfo
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * One line of the Ramadan timetable: the day of the month and that day's engine times. Suhoor ends
 * and iftar are the Fajr and Maghrib entries of the same [DayPrayerTimes], never a second calculation,
 * so the sheet, the card and the list on the Times screen cannot disagree.
 */
data class RamadanRow(val dayNumber: Int, val times: DayPrayerTimes) {
    val date: LocalDate get() = times.date
    val suhoorEnds: Instant get() = times[PrayerSlot.FAJR]
    val iftar: Instant get() = times[PrayerSlot.MAGHRIB]
    val fastLength: Duration get() = Duration.between(suhoorEnds, iftar)

    /** True when this day's times were projected from a reference latitude (above the polar circles). */
    val approximated: Boolean get() = times.approximated
}

object RamadanTimetable {

    /**
     * Every day of [info]'s month for this place, in order, day 1 first. Plain engine calls
     * (29 or 30 of them, a few milliseconds), the same function the list on the Times screen and
     * the PDF use, with the user's own adjustments and Islamic date shift already inside [prefs].
     */
    fun rows(coordinates: Coordinates, prefs: CalculationPrefs, info: RamadanInfo): List<RamadanRow> =
        PrayerEngine.computeRange(coordinates, info.firstDay, info.length, prefs)
            .mapIndexed { index, day -> RamadanRow(dayNumber = index + 1, times = day) }
}
