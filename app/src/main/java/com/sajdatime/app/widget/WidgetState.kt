package com.sajdatime.app.widget

import com.sajdatime.app.data.AppSettings
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.HijriDay
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Everything the two home screen widgets draw, computed once per refresh. Pure: no Android, so the
 * rules are tested on the JVM and the renderer only has to turn this into views.
 *
 * The widgets never calculate anything a screen does not. The times are [PrayerEngine.compute] with the
 * same [AppSettings] the Times screen uses, the next prayer is [PrayerEngine.nextPrayer], and the
 * highlight rule is the one the Times screen applies to its own rows ([isHighlighted]). The place the
 * times are for is carried since 18 Oct 2026, at the owner's request ([cityName]).
 *
 * Only two things on a widget can go stale if every refresh is missed: which row is highlighted and the
 * countdown. The clock times are for the calendar day and stay right.
 */
data class WidgetState(
    /** False on a first run or after settings were cleared: the widget then says how to fix it. */
    val hasLocation: Boolean,
    val today: DayPrayerTimes?,
    val next: NextPrayer?,
    /** True when the next prayer's own day was projected from another latitude (polar places). */
    val nextIsApproximate: Boolean,
    /**
     * Today is a day of Ramadan on the calendar the person uses, so the Fajr and Maghrib rows of the
     * Today widget carry "Suhoor ends" and "Iftar". Already false when the person has switched the
     * Ramadan card off, because that switch hides every Ramadan label, widgets included.
     */
    val ramadanToday: Boolean,
    /**
     * The next prayer's own day is a day of Ramadan, which is what decides its label. It differs from
     * [ramadanToday] on the eve, when tomorrow's Fajr is next and is already the end of the first
     * suhoor, and on the last day after Maghrib, when tomorrow's Fajr is Shawwal's and carries nothing.
     */
    val ramadanOnNextDay: Boolean,
    /**
     * Whether the countdown may be drawn. It is shown only when the refresh alarm is exact, because
     * then it cannot be left counting past zero for long. See PLAN.md section 4.3.
     */
    val countdownAllowed: Boolean,
    /**
     * The day's five prayers in order (Sunrise is not a prayer, so it has no bar): the ones whose time
     * has come today, the next one, and the ones after it. Empty without a location. After Isha the
     * next prayer is tomorrow's Fajr, so every bar is done until midnight, when the redraw alarm that
     * already moves the widget at midnight starts a fresh day.
     */
    val progress: List<Segment> = emptyList(),
    /**
     * Today's calendar date and its Hijri date with the person's own shift, for the large Today
     * widget's header. Set with or without a location: the date needs none. Hijri is null outside
     * the range the calendar covers, and the header then shows the ordinary date alone.
     */
    val date: LocalDate? = null,
    val hijri: HijriDay? = null,
    /**
     * The saved place label exactly as the Times screen holds it ("London, United Kingdom"), and
     * whether it is the Makkah fallback, whose name the renderer shows in the app's language
     * (AppSettings.placeName). Shown on every widget since 18 Oct 2026: the owner asked for the place
     * the times are for. It was left off in 1.4.0 because a home screen is seen by other people; he
     * decided the answer to "times for where?" matters more. Nothing new leaves the phone: it is the
     * label already saved.
     */
    val cityName: String = "",
    val usingDefaultLocation: Boolean = false,
) {
    /** The Times screen's rule: the next prayer, and only if it is still today. Sunrise is never next. */
    fun isHighlighted(slot: PrayerSlot): Boolean = next != null && slot == next.slot && !next.isTomorrow

    /** "Suhoor ends" for a Fajr and "Iftar" for a Maghrib on a Ramadan day, nothing otherwise. */
    fun ramadanWordFor(slot: PrayerSlot, ramadanOnThatDay: Boolean): RamadanWord? =
        if (!ramadanOnThatDay) {
            null
        } else {
            when (slot) {
                PrayerSlot.FAJR -> RamadanWord.SUHOOR_ENDS
                PrayerSlot.MAGHRIB -> RamadanWord.IFTAR
                else -> null
            }
        }

    /**
     * The town part of the saved label, "London" from "London, United Kingdom": a widget has room for
     * one word or two, and the country adds nothing the person does not know. Empty when there is no
     * label. Not used for the Makkah fallback, which the renderer names in the app's language.
     */
    val town: String get() = cityName.substringBefore(',').trim()

    enum class RamadanWord { SUHOOR_ENDS, IFTAR }

    enum class Segment { DONE, NEXT, TODO }

    companion object {
        fun build(
            settings: AppSettings,
            now: Instant,
            zone: ZoneId,
            canScheduleExact: Boolean,
        ): WidgetState {
            val todayDate = now.atZone(zone).toLocalDate()
            val hijri = HijriCalendar.of(todayDate, settings.hijriOffsetDays)
            val coordinates = settings.coordinates
                ?: return WidgetState(
                    hasLocation = false,
                    today = null,
                    next = null,
                    nextIsApproximate = false,
                    ramadanToday = false,
                    ramadanOnNextDay = false,
                    countdownAllowed = canScheduleExact,
                    date = todayDate,
                    hijri = hijri,
                    cityName = settings.cityName,
                    usingDefaultLocation = settings.usingDefaultLocation,
                )
            val prefs = settings.calculationPrefs
            val today = PrayerEngine.compute(coordinates, todayDate, prefs)
            val next = PrayerEngine.nextPrayer(coordinates, prefs, now, zone)
            val nextDate = next.at.atZone(zone).toLocalDate()
            val labels = settings.ramadanCardEnabled
            return WidgetState(
                hasLocation = true,
                today = today,
                next = next,
                nextIsApproximate = PrayerEngine.compute(coordinates, nextDate, prefs).approximated,
                ramadanToday = labels && HijriCalendar.isRamadan(todayDate, prefs.hijriOffset),
                ramadanOnNextDay = labels && HijriCalendar.isRamadan(nextDate, prefs.hijriOffset),
                countdownAllowed = canScheduleExact,
                progress = progress(today, next, now),
                date = todayDate,
                hijri = hijri,
                cityName = settings.cityName,
                usingDefaultLocation = settings.usingDefaultLocation,
            )
        }

        /** The five prayers the day's bars stand for, in order. */
        val PRAYERS = listOf(PrayerSlot.FAJR, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.MAGHRIB, PrayerSlot.ISHA)

        /**
         * Judged on the same clock the highlight uses: the next prayer is NEXT only while it is still
         * today, a prayer whose time is not after [now] is DONE, the rest are TODO.
         */
        internal fun progress(today: DayPrayerTimes, next: NextPrayer, now: Instant): List<Segment> =
            PRAYERS.map { slot ->
                when {
                    !next.isTomorrow && slot == next.slot -> Segment.NEXT
                    !today[slot].isAfter(now) -> Segment.DONE
                    else -> Segment.TODO
                }
            }
    }
}
