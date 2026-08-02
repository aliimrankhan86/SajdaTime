package com.sajdatime.core

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
import kotlin.math.abs
import kotlin.math.withSign

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
     *
     * That mismatch has now been reported (Slough, Aug 2026) — and this rule is NOT the
     * cause, so do not start here. The rule only engages from roughly mid-May to late July,
     * when 18 degrees becomes unreachable; on 1 August at 51.5N it changes nothing at all
     * (Isha 23:17 with it and without it). The real gap is the *method*: the default MWL 17
     * degrees lands 78 minutes after every local mosque, which use Moonsighting's shafaq
     * rule. See docs/HANDOVER.md §10, "Isha in the UK", before touching this constant.
     *
     * One correction, measured from adhan's own bytecode and confirmed on the numbers:
     * this rule does nothing whatsoever when the method is Moonsighting Committee. adhan
     * computes the night portions and then discards them, substituting its own seasonal
     * model, plus a one-seventh clamp above 55 degrees. All three rules give byte-identical
     * Moonsighting output at every latitude tested. Setting the rule here is therefore
     * harmless rather than meaningful, and PolarAndHemisphereTest guards that assumption in
     * case a library upgrade changes it. It bites only on the plain angle methods, and
     * there the choice is real: at 51.5N on 21 June, MIDDLE_OF_THE_NIGHT collapses Fajr and
     * Isha onto 01:04, SEVENTH_OF_THE_NIGHT gives 03:42/22:27, and TWILIGHT_ANGLE gives
     * 02:32/23:29. Those are three different religious approximations, not three roundings.
     */
    private val HIGH_LATITUDE_RULE = HighLatitudeRule.TWILIGHT_ANGLE

    /** Umm al-Qura lengthens the Isha interval from 90 to 120 minutes during Ramadan. */
    private const val UMM_AL_QURA_RAMADAN_EXTRA_MINUTES = 30L

    /**
     * Beyond the polar circles the sun can fail to set at all in summer, or to rise at all
     * in winter, and adhan then returns null for **every** field — not only Fajr and Isha
     * but Dhuhr and Asr as well, because it derives them all from a sunrise and sunset that
     * do not exist. Measured boundary: null from 66 degrees on 21 June and from 68 degrees
     * on 21 December; 65.5 degrees still computes. See docs/HANDOVER.md §10.
     *
     * Until this was measured those nulls reached [instantOf], whose parameter is not
     * nullable, and threw NullPointerException. That took out the home screen, the watch
     * tile, the ongoing notification and the alarm scheduler together, in both solstices,
     * for everyone living above roughly 65.5 degrees — Tromso, Kiruna, Rovaniemi, Murmansk,
     * Norilsk, Longyearbyen. Those places have mosques. This was a real crash, not a
     * theoretical one, and it is why nothing here may return null.
     *
     * The classical answer is *aqrab al-bilad*: use the times of the nearest place where
     * night and day are still distinguishable. Longitude and hemisphere are preserved, so
     * solar noon stays true to where the user actually is. Which latitude counts as "nearest"
     * is a fiqh question with two published answers, and [polarReferenceLatitude] picks
     * between them by method rather than imposing one.
     *
     * This is an approximation and the app says so on screen — see
     * [DayPrayerTimes.approximatedFrom]. Showing projected times silently would be the app
     * taking a position on the user's behalf without telling them, which is the one thing
     * it must not do.
     */
    private const val FIQH_COUNCIL_REFERENCE_LATITUDE = 45.0

    /** See [polarReferenceLatitude]. Moonsighting publish their own figure and it is not 45. */
    private const val MOONSIGHTING_REFERENCE_LATITUDE = 60.0

    /**
     * The latitude to borrow times from when the sun will not rise or set where the user is.
     *
     * There is no single right answer here, so the app follows whichever body the user has
     * already chosen rather than picking one for them. Both figures are verified against the
     * publishing body's own words, not against another calculator — see docs/HANDOVER.md §10,
     * "Aladhan was stale and I shipped it", for why that distinction is not pedantry.
     *
     * **45 degrees — Islamic Fiqh Council of the Muslim World League**, resolution 6 of the
     * ninth session, Makkah, 12-19 Rajab 1406 (March 1986), endorsed by the European Council
     * for Fatwa and Research. It sets three bands: below 48 degrees the signs are visible all
     * year and must be used; between 48 and 66 Fajr and Isha are taken by analogy with the
     * nearest place where they are clear; beyond 66 *all* times are estimated from 45 degrees.
     * The band edges are not arbitrary and that is the strongest evidence they are reported
     * correctly: 18 degrees of solar depression last occurs at midsummer at latitude
     * 90 - 23.44 - 18 = 48.56, and 66.56 is the polar circle itself. At 45 degrees the sun
     * still reaches 21.56 degrees below the horizon at midsummer, so a real Fajr and a real
     * Isha exist there — the projection borrows measured twilight rather than an estimate of
     * an estimate.
     *
     * **60 degrees — Moonsighting Committee**, in their own words: "at latitudes more than
     * 60degrees, we slide down to 60degrees and calculate Fajr & Isha using the rule of Sab'u
     * Lail in summer." They chose it knowing it breaches the eighteen-hour fasting limit of
     * the fatwa they cite, on the empirical ground that Oslo copes. Verified rather than
     * assumed: their page states the resulting Oslo extremes as 19h38m and 7h43m, and this
     * engine at 60 degrees produces 19h39m and 7h41m. Within rounding, so adhan's Moonsighting
     * model plus this projection really does reproduce what they publish.
     *
     * Rejected, with reasons, so they are not tried again:
     * - *One constant for everybody.* Whichever number were chosen, it would impose one body's
     *   ruling on users who had explicitly selected another. That is the failure this app
     *   exists to avoid.
     * - *Clamp to the highest latitude that still computes.* That boundary is an artefact of
     *   the library, it moves daily, no scholar stands behind it, and Moonsighting measured
     *   the result: fasts "of more than 23 hours in summer and less than 3 hours in winter".
     * - *Use Makkah's times.* A real position, held by Dar al-Ifta al-Misriyyah and used by
     *   some Norwegian mosques, but it discards the user's own solar noon, so Dhuhr would stop
     *   matching the sun overhead. Worth revisiting only as an explicit user choice.
     */
    private fun polarReferenceLatitude(method: CalcMethod): Double =
        if (method == CalcMethod.MOON_SIGHTING) {
            MOONSIGHTING_REFERENCE_LATITUDE
        } else {
            FIQH_COUNCIL_REFERENCE_LATITUDE
        }

    /** No legitimate prayer is a whole day away from its own solar noon. See [isUsable]. */
    private const val MAX_MILLIS_FROM_DHUHR = 24L * 60 * 60 * 1000

    /**
     * How far Dhuhr may sit from the midpoint of sunrise and sunset before the day is
     * disbelieved. Chosen from measurement, not taste: swept over every 0.5 degrees of
     * latitude, four longitudes and every day of 2026, the largest offset anywhere below
     * 65 degrees is **three minutes**, which is the equation of time and the declination
     * drifting across the day. Above 65 degrees the same sweep throws up 59, 87, 143, 212
     * and 214 minutes. Thirty is therefore ten times the worst honest case and well clear
     * of every dishonest one. See [isUsable].
     */
    private const val MAX_MILLIS_DHUHR_OFF_MIDPOINT = 30L * 60 * 1000

    /**
     * Whether adhan's answer can be believed, which is a stronger question than whether it
     * returned anything.
     *
     * Null is the obvious failure and the one that used to crash the app. The subtler one is
     * that above the polar circles adhan can return a *value* that is nonsense. Where the sun
     * only grazes the horizon, the shadow ratio Asr is defined by is never reached, and rather
     * than giving up adhan hands back whatever its root finder landed on. Measured across
     * 262,070 day-computations: 2,239 faults, every latitude from 66 to 89.5 in **both**
     * hemispheres, worst case a 27 January Asr returned as 13 March — forty-five days out.
     * Those days were silently displayed, exported to PDF and used to schedule alarms.
     *
     * So the day has to be in order, and every slot has to sit within a day of its own Dhuhr.
     * Dhuhr is the right anchor because it is the one time that does not depend on latitude at
     * all: it comes from the date, the longitude and the sun's right ascension, and was
     * measured identical at 0, 30, 50, 60 and 65 degrees.
     *
     * There is a third failure that order and range both survive, so it needs its own test.
     * On the day polar day ends, sunrise and sunset happen minutes apart either side of
     * midnight, and adhan can pair a sunrise from one night with a sunset from another.
     * Measured at 78N on 24 August 2026: Maghrib 17:00, then 22:29 the following day. A
     * sunset does not move five and a half hours overnight. The physics that catches it is
     * that **solar noon is the midpoint of the day arc** — so if Dhuhr is not near the middle
     * of sunrise and sunset, the two are not from the same day. See
     * [MAX_MILLIS_DHUHR_OFF_MIDPOINT] for how the tolerance was measured.
     *
     * A day that fails any of the three is projected instead, and flagged. If that ever
     * rejects a day that was really fine, the user sees an honest "approximate" banner rather
     * than a confident wrong number, which is the safe direction to be wrong in.
     */
    private val AdhanPrayerTimes.isUsable: Boolean
        get() {
            val all = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
            if (all.any { it == null }) return false
            val ms = all.map { it.time }
            if (ms != ms.sorted()) return false
            if (ms.any { abs(it - dhuhr.time) > MAX_MILLIS_FROM_DHUHR }) return false
            val middayArc = (sunrise.time + maghrib.time) / 2
            return abs(dhuhr.time - middayArc) <= MAX_MILLIS_DHUHR_OFF_MIDPOINT
        }

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
        val dateComponents = DateComponents(date.year, date.monthValue, date.dayOfMonth)
        val params = parametersFor(method, prefs)

        // Try where the user actually is first. Only if the answer there is unusable does
        // the projection kick in, so everyone below the polar circles — which is very nearly
        // everyone — is unaffected by this and gets their own astronomy.
        val here = AdhanCoordinates(coordinates.latitude, coordinates.longitude)
        val local = AdhanPrayerTimes(here, dateComponents, params).takeIf { it.isUsable }
        val reference = polarReferenceLatitude(method)
        val adhanCoords = if (local != null) {
            here
        } else {
            AdhanCoordinates(reference.withSign(coordinates.latitude), coordinates.longitude)
        }
        val base = local ?: AdhanPrayerTimes(adhanCoords, dateComponents, params)

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
            approximatedFrom = reference.takeIf { local == null },
            times = mapOf(
                PrayerSlot.FAJR to instantOf(base.fajr),
                PrayerSlot.SUNRISE to instantOf(base.sunrise),
                PrayerSlot.DHUHR to instantOf(base.dhuhr),
                PrayerSlot.ASR to instantOf(base.asr),
                PrayerSlot.MAGHRIB to instantOf(maghrib),
                PrayerSlot.ISHA to instantOf(ishaFor(method, base, date, prefs)),
            ).adjustedBy(prefs),
        )
    }

    /**
     * Applies the user's per-prayer corrections to the finished times.
     *
     * Deliberately last, and deliberately not adhan's own `CalculationParameters.adjustments`,
     * which would have been the obvious move. Three of this engine's outputs never pass
     * through that field: the Shia Maghrib comes from a second `PrayerTimes` built with its
     * own probe parameters, the Umm al-Qura Ramadan Isha is arithmetic done here, and a
     * projected polar day is computed at a borrowed latitude. Handing the offsets to adhan
     * would have silently corrected three prayers out of six and left the user hunting for
     * why Maghrib ignored them. Applying them here covers every path by construction.
     *
     * It also keeps [isUsable] honest: that check decides whether a day is real astronomy or
     * a projection, and it must judge what the sky did, not what the user typed.
     */
    private fun Map<PrayerSlot, Instant>.adjustedBy(
        prefs: CalculationPrefs,
    ): Map<PrayerSlot, Instant> {
        if (prefs.adjustments.isEmpty()) return this
        val moved = mapValues { (slot, at) ->
            at.plusSeconds(60L * prefs.adjustmentMinutes(slot))
        }

        // Then held in order, and this is not defensive tidying — the bound alone does not
        // make it safe, which was assumed and turned out to be false.
        //
        // Measured: at 59.9 degrees on 1 January, Dhuhr and Asr are twenty-six minutes
        // apart. A user allowed to push one prayer thirty minutes later and the next thirty
        // earlier can therefore cross them, and the sweep in AdjustmentTest found exactly
        // that — Dhuhr 12:35, Asr 12:31. An inverted day is not a cosmetic problem: the
        // whole app keys off "which prayer is next", and a timetable that runs backwards is
        // unusable in the specific way a wrong-by-a-few-minutes one is not.
        //
        // Tightening the bound instead was considered and rejected. The gap keeps shrinking
        // with latitude, so any fixed bound small enough to be safe at 60 degrees is too
        // small to be useful at 51 — and still fails further north. Clamping to the
        // neighbour costs the user nothing in every case where their correction is legal,
        // and in the one case where it is not it gives them as much of it as the sky allows.
        var floor: Instant? = null
        return PrayerSlot.entries.associateWith { slot ->
            val at = maxOf(moved.getValue(slot), floor ?: Instant.MIN)
            floor = at
            at
        }
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

    /**
     * The prayer whose time is in **now** — the one a user opening the app can still pray —
     * or null when there is no such prayer.
     *
     * The rule is one line: take every slot at or before [now] and keep the last one. What
     * makes it correct is that [DayPrayerTimes.ordered] includes **sunrise**, so the moment
     * Fajr's window shuts is itself a candidate. Between sunrise and Dhuhr the last slot to
     * have passed is sunrise, which is not a prayer, and the answer is correctly "none".
     * Without sunrise in the list the same code would claim Fajr is still in until midday.
     * That is the whole trick; there is no special case for it.
     *
     * Yesterday is scanned as well, for the same reason [nextPrayer] scans tomorrow. After
     * midnight the prayer that is in is the previous evening's Isha, and at high latitudes
     * Isha itself can fall after midnight, so "before Fajr means Isha" is not a shortcut
     * that survives a Manchester June — the real times have to be looked at.
     *
     * Note what this deliberately does not model: madhab differences in when a window
     * *ends*, and the Jafari position that Dhuhr and Asr may be combined. Both would make
     * this answer longer and more equivocal, and the screen has room for one word. The app
     * says which prayer has most recently come in; the disclaimer already tells the user it
     * is a helper rather than an authority, and anyone needing the finer ruling has to ask
     * someone qualified regardless.
     */
    fun currentPrayer(
        coordinates: Coordinates,
        prefs: CalculationPrefs,
        now: Instant,
        zone: ZoneId,
    ): PrayerSlot? {
        val today = now.atZone(zone).toLocalDate()
        val started = (-1L..0L)
            .flatMap { offset -> compute(coordinates, today.plusDays(offset), prefs).ordered }
            .filter { (_, at) -> at <= now }
        val (slot, _) = started.lastOrNull() ?: return null
        return slot.takeIf { it.isPrayer }
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
        prefs: CalculationPrefs,
    ): java.util.Date {
        if (method != CalcMethod.UMM_AL_QURA || !isRamadan(date, prefs.hijriOffset)) return base.isha
        return java.util.Date(
            base.isha.time + UMM_AL_QURA_RAMADAN_EXTRA_MINUTES * 60_000L,
        )
    }

    /**
     * The offset is applied here and not only on the home screen's date line, which is the
     * whole reason this takes a parameter. A user who shifts the calendar because their
     * mosque sighted the moon a day earlier has moved when Ramadan starts *for them*, and
     * Umm al-Qura's Isha rule keys off exactly that. Shifting the printed date but not the
     * rule that depends on it would show "1 Ramadan" beside an Isha still calculated as
     * though it were Sha'ban.
     */
    private fun isRamadan(date: LocalDate, offsetDays: Int): Boolean = runCatching {
        HijrahDate.from(date.plusDays(offsetDays.toLong()))
            .get(ChronoField.MONTH_OF_YEAR) == 9
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
