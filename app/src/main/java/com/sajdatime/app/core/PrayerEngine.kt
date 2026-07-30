package com.sajdatime.app.core

import com.batoulapps.adhan.CalculationMethod as AdhanMethod
import com.batoulapps.adhan.CalculationParameters
import com.batoulapps.adhan.Coordinates as AdhanCoordinates
import com.batoulapps.adhan.Madhab as AdhanMadhab
import com.batoulapps.adhan.PrayerTimes as AdhanPrayerTimes
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.data.DateComponents
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

/**
 * Offline prayer time calculation. Wraps adhan-java (MIT) and adds the Shia/Jafari
 * Maghrib rule that adhan itself does not model.
 *
 * All computation is local; the app never needs a network connection to produce times.
 */
object PrayerEngine {

    /**
     * Jafari and Tehran define Maghrib as the moment the sun sits a few degrees *below*
     * the horizon (when the eastern redness fades), not at sunset. adhan exposes no
     * maghrib angle, but its Isha calculation is exactly "evening time at N degrees of
     * solar depression" — so we run a second pass with ishaAngle set to the maghrib
     * angle and read the resulting Isha. Same validated solar math, no hand-rolled
     * astronomy. See PrayerEngineTest for the numeric verification.
     */
    private val maghribAngleFor: Map<CalcMethod, Double> = mapOf(
        CalcMethod.JAFARI to 4.0,
        CalcMethod.TEHRAN to 4.5,
    )

    /**
     * Above roughly 48 degrees latitude the sun never dips far enough below the horizon
     * in summer for a true Fajr or Isha to exist, so a fallback rule is required.
     *
     * TWILIGHT_ANGLE splits the night in proportion to each prayer's twilight angle. It
     * is what Aladhan and most mainstream apps use, and its output for London in June
     * (Fajr 02:31, Isha 23:27) has been verified against Aladhan to the minute.
     *
     * adhan's own default, MIDDLE_OF_THE_NIGHT, is unusable at UK latitudes: it collapses
     * Fajr and Isha onto the same instant (both 01:02 for London on 21 June), leaving no
     * window for Isha at all. That is why this is set explicitly rather than left alone.
     *
     * ponytail: one sensible default, not a setting. Add a user-facing choice only if UK
     * users report a mismatch with their local mosque.
     */
    private val HIGH_LATITUDE_RULE = HighLatitudeRule.TWILIGHT_ANGLE

    /** Umm al-Qura lengthens the Isha interval from 90 to 120 minutes during Ramadan. */
    private const val UMM_AL_QURA_RAMADAN_EXTRA_MINUTES = 30L

    /** Resolves [CalcMethod.AUTO] into the convention appropriate for the user's sect. */
    fun resolveMethod(prefs: CalculationPrefs): CalcMethod =
        if (prefs.method != CalcMethod.AUTO) {
            prefs.method
        } else {
            when (prefs.sect) {
                Sect.SUNNI -> CalcMethod.MUSLIM_WORLD_LEAGUE
                Sect.SHIA -> CalcMethod.JAFARI
            }
        }

    fun compute(
        coordinates: Coordinates,
        date: LocalDate,
        prefs: CalculationPrefs,
    ): DayPrayerTimes {
        val method = resolveMethod(prefs)
        val adhanCoords = AdhanCoordinates(coordinates.latitude, coordinates.longitude)
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)

        val base = AdhanPrayerTimes(adhanCoords, dateComponents, parametersFor(method, prefs))

        // Shia conventions push Maghrib past sunset; Sunni ones use sunset itself.
        val maghrib = maghribAngleFor[method]?.let { angle ->
            val probe = CalculationParameters(0.0, angle, AdhanMethod.OTHER).apply {
                madhab = AdhanMadhab.SHAFI
                highLatitudeRule = HIGH_LATITUDE_RULE
            }
            AdhanPrayerTimes(adhanCoords, dateComponents, probe).isha
        } ?: base.maghrib

        return DayPrayerTimes(
            date = date,
            times = mapOf(
                PrayerSlot.FAJR to instantOf(base.fajr),
                PrayerSlot.SUNRISE to instantOf(base.sunrise),
                PrayerSlot.DHUHR to instantOf(base.dhuhr),
                PrayerSlot.ASR to instantOf(base.asr),
                PrayerSlot.MAGHRIB to instantOf(maghrib),
                PrayerSlot.ISHA to instantOf(ishaFor(method, base, date)),
            ),
        )
    }

    /**
     * Finds the next prayer at or after [now], rolling into tomorrow once Isha has passed.
     * Sunrise is skipped — it is displayed but never the "next prayer".
     */
    fun nextPrayer(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        now: Instant,
        zone: ZoneId,
    ): NextPrayer {
        val today = now.atZone(zone).toLocalDate()

        // Yesterday is included deliberately. At high latitudes Isha can fall after
        // midnight, so at 00:30 the genuinely next prayer may belong to the previous
        // calendar day's timetable. Scanning today only would silently skip it.
        val candidates = (-1L..1L)
            .flatMap { offset -> compute(coordinates, today.plusDays(offset), prefs).prayersOnly }
            .filter { (_, at) -> at > now }
            .sortedBy { (_, at) -> at }

        val (slot, at) = candidates.first()
        return NextPrayer(
            slot = slot,
            at = at,
            isTomorrow = at.atZone(zone).toLocalDate() != today,
        )
    }

    /** Times for [days] consecutive days starting at [start]. Used by the PDF export. */
    fun computeRange(
        coordinates: Coordinates,
        start: LocalDate,
        days: Int,
        prefs: CalculationPrefs,
    ): List<DayPrayerTimes> =
        (0 until days).map { compute(coordinates, start.plusDays(it.toLong()), prefs) }

    private fun parametersFor(method: CalcMethod, prefs: CalculationPrefs): CalculationParameters {
        val params = when (method) {
            CalcMethod.AUTO -> AdhanMethod.MUSLIM_WORLD_LEAGUE.parameters
            CalcMethod.MUSLIM_WORLD_LEAGUE -> AdhanMethod.MUSLIM_WORLD_LEAGUE.parameters
            CalcMethod.EGYPTIAN -> AdhanMethod.EGYPTIAN.parameters
            CalcMethod.KARACHI -> AdhanMethod.KARACHI.parameters
            CalcMethod.UMM_AL_QURA -> AdhanMethod.UMM_AL_QURA.parameters
            CalcMethod.DUBAI -> AdhanMethod.DUBAI.parameters
            CalcMethod.MOON_SIGHTING -> AdhanMethod.MOON_SIGHTING_COMMITTEE.parameters
            CalcMethod.NORTH_AMERICA -> AdhanMethod.NORTH_AMERICA.parameters
            CalcMethod.KUWAIT -> AdhanMethod.KUWAIT.parameters
            CalcMethod.QATAR -> AdhanMethod.QATAR.parameters
            CalcMethod.SINGAPORE -> AdhanMethod.SINGAPORE.parameters
            // Diyanet is not bundled in adhan; its published angles are Fajr 18 / Isha 17.
            CalcMethod.TURKEY -> CalculationParameters(18.0, 17.0, AdhanMethod.OTHER)
            CalcMethod.JAFARI -> CalculationParameters(16.0, 14.0, AdhanMethod.OTHER)
            CalcMethod.TEHRAN -> CalculationParameters(17.7, 14.0, AdhanMethod.OTHER)
        }
        params.madhab = adhanMadhabFor(method, prefs.madhab)
        params.highLatitudeRule = HIGH_LATITUDE_RULE
        return params
    }

    /**
     * Umm al-Qura fixes Isha at Maghrib + 90 minutes, except during Ramadan when the
     * official calendar uses 120. adhan implements only the 90-minute rule, so the extra
     * half hour is applied here. Ramadan is month 9 of the Hijri year, and java.time's
     * HijrahChronology *is* the Umm al-Qura calendar, so the two agree by construction.
     */
    private fun ishaFor(
        method: CalcMethod,
        base: AdhanPrayerTimes,
        date: LocalDate,
    ): java.util.Date {
        if (method != CalcMethod.UMM_AL_QURA || !isRamadan(date)) return base.isha
        return java.util.Date(
            base.isha.time + UMM_AL_QURA_RAMADAN_EXTRA_MINUTES * 60_000L,
        )
    }

    private fun isRamadan(date: LocalDate): Boolean = runCatching {
        HijrahDate.from(date).get(ChronoField.MONTH_OF_YEAR) == 9
    }.getOrDefault(false)

    /**
     * Asr timing: Hanafi waits until an object's shadow is twice its length; Shafi'i,
     * Maliki, Hanbali and the Jafari school all use once its length. adhan models this
     * as a two-value enum, so three of the four Sunni madhabs collapse onto SHAFI.
     */
    private fun adhanMadhabFor(method: CalcMethod, madhab: Madhab): AdhanMadhab =
        when {
            method == CalcMethod.JAFARI || method == CalcMethod.TEHRAN -> AdhanMadhab.SHAFI
            madhab == Madhab.HANAFI -> AdhanMadhab.HANAFI
            else -> AdhanMadhab.SHAFI
        }

    /**
     * adhan rounds its results to the nearest minute but leaves the millisecond field
     * carrying the wall-clock time of the call, so two identical computations return
     * instants a few milliseconds apart. Truncating makes the engine deterministic,
     * lands alarms exactly on the minute, and keeps the displayed countdown stable.
     */
    private fun instantOf(date: java.util.Date): Instant =
        Instant.ofEpochMilli(date.time).truncatedTo(ChronoUnit.MINUTES)
}
