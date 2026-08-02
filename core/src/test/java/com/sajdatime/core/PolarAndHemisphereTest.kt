package com.sajdatime.core

import com.batoulapps.adhan.CalculationMethod as AdhanMethod
import com.batoulapps.adhan.Coordinates as AdhanCoordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab as AdhanMadhab
import com.batoulapps.adhan.PrayerTimes as AdhanPrayerTimes
import com.batoulapps.adhan.data.DateComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The app used to crash outright above the polar circles.
 *
 * adhan returns null for **every** field once the sun stops rising or setting, and
 * [PrayerEngine] fed those straight into a non-null parameter. The result was a
 * NullPointerException that took down the home screen, the watch tile, the ongoing
 * notification and the alarm scheduler at once, in summer *and* in winter, for everyone
 * above roughly 65.5 degrees. Tromso, Kiruna, Rovaniemi, Murmansk, Norilsk and
 * Longyearbyen all have Muslim communities, so this was not hypothetical.
 *
 * Nothing here is about producing a *better* time. It is about never producing none.
 */
class PolarAndHemisphereTest {

    private val midsummer = LocalDate.of(2026, 6, 21)
    private val midwinter = LocalDate.of(2026, 12, 21)

    /** Places where adhan alone returns nothing at all. */
    private val polar = listOf(
        "Tromso" to Coordinates(69.65, 18.96),
        "Kiruna" to Coordinates(67.86, 20.23),
        "Murmansk" to Coordinates(68.97, 33.08),
        "Norilsk" to Coordinates(69.35, 88.20),
        "Longyearbyen" to Coordinates(78.22, 15.63),
        // The Southern Hemisphere has never had a user report, which is exactly why it is
        // tested: adhan's own Moonsighting safety clamp uses a raw latitude rather than an
        // absolute one, so the south is where a sign error would hide.
        "Antarctic 70S" to Coordinates(-70.0, 0.0),
        "Antarctic 80S" to Coordinates(-80.0, 140.0),
    )

    @Test
    fun `polar latitudes produce times instead of crashing`() {
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                for (method in CalcMethod.entries) {
                    val day = PrayerEngine.compute(at, date, CalculationPrefs(method = method))
                    assertEquals(
                        "$name $date $method should yield all six slots",
                        PrayerSlot.entries.size,
                        day.times.size,
                    )
                    assertTrue("$name $date $method must be flagged approximate", day.approximated)
                }
            }
        }
    }

    @Test
    fun `polar times stay in chronological order`() {
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                val ordered = PrayerEngine.compute(at, date, CalculationPrefs()).ordered
                val instants = ordered.map { it.second }
                assertEquals(
                    "$name $date must be ordered: ${ordered.map { "${it.first}=${it.second}" }}",
                    instants.sorted(),
                    instants,
                )
            }
        }
    }

    /** The scheduler and the watch tile call these, not compute, so they need their own check. */
    @Test
    fun `next and current prayer survive polar latitudes`() {
        val zone = ZoneId.of("Europe/Oslo")
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                for (hour in listOf(0, 6, 12, 23)) {
                    val now = date.atTime(hour, 0).atZone(zone).toInstant()
                    val next = PrayerEngine.nextPrayer(at, CalculationPrefs(), now, zone)
                    assertTrue("$name $date ${hour}h next prayer must be ahead", next.at > now)
                    // Only asserting it does not throw; null is a legitimate answer.
                    PrayerEngine.currentPrayer(at, CalculationPrefs(), now, zone)
                }
            }
        }
    }

    /**
     * The projection must stay off for everybody else. Reykjavik at 64.15 and Lulea at
     * 65.58 are the closest inhabited places to the boundary and must keep their own
     * astronomy; measured, adhan starts returning nulls at 66 degrees in June and 68 in
     * December.
     */
    @Test
    fun `ordinary latitudes are never approximated`() {
        val ordinary = listOf(
            "Slough" to Coordinates(51.51, -0.59),
            "Reykjavik" to Coordinates(64.15, -21.94),
            "Lulea" to Coordinates(65.58, 22.15),
            "Jakarta" to Coordinates(-6.21, 106.85),
            "Melbourne" to Coordinates(-37.81, 144.96),
            "Ushuaia" to Coordinates(-54.80, -68.30),
        )
        for ((name, at) in ordinary) {
            for (date in listOf(midsummer, midwinter)) {
                assertFalse(
                    "$name $date must use its own latitude",
                    PrayerEngine.compute(at, date, CalculationPrefs()).approximated,
                )
            }
        }
    }

    /**
     * The option labelled for Indonesia and Singapore has to reproduce what those countries
     * actually publish, or the label is a lie.
     *
     * The Singapore rows are the decisive ones because they come from **MUIS's own 2024
     * timetable**, not from an aggregator. That distinction is the whole point: an earlier
     * version of this test trusted Aladhan's JAKIM method, which still reports Fajr 20
     * degrees although Malaysia moved its national criterion to 18 in November 2019. The
     * label named Malaysia on that basis and was wrong. Aladhan is a fine cross-check for
     * *arithmetic*; it is not a source of truth for *what a country currently observes*.
     *
     * Malaysia is therefore absent by design. Do not add it back without a JAKIM timetable
     * in hand — at 18 degrees it is served by MUSLIM_WORLD_LEAGUE, not by this entry.
     */
    @Test
    fun `southeast asia option matches Kemenag and MUIS`() {
        val hm = DateTimeFormatter.ofPattern("HH:mm")
        val expected = listOf(
            // MUIS, "Prayer Times for Singapore Year 2024", Subuh/Isyak as printed.
            Triple("Singapore 1 Jan", Coordinates(1.35, 103.82) to "Asia/Singapore", LocalDate.of(2024, 1, 1) to ("05:43" to "20:24")),
            Triple("Singapore 21 Mar", Coordinates(1.35, 103.82) to "Asia/Singapore", LocalDate.of(2024, 3, 21) to ("05:52" to "20:24")),
            Triple("Singapore 21 Jun", Coordinates(1.35, 103.82) to "Asia/Singapore", LocalDate.of(2024, 6, 21) to ("05:36" to "20:28")),
            Triple("Singapore 23 Sep", Coordinates(1.35, 103.82) to "Asia/Singapore", LocalDate.of(2024, 9, 23) to ("05:37" to "20:09")),
            // Indonesia, cross-checked against Aladhan's Kemenag method. Kemenag has
            // publicly reaffirmed 20 degrees, so unlike JAKIM this one is not stale.
            Triple("Medan", Coordinates(3.59, 98.67) to "Asia/Jakarta", midsummer to ("04:53" to "19:53")),
            Triple("Surabaya", Coordinates(-7.25, 112.75) to "Asia/Jakarta", midsummer to ("04:16" to "18:37")),
        )
        for ((name, place, want) in expected) {
            val (at, tz) = place
            val (date, times) = want
            val day = PrayerEngine.compute(at, date, CalculationPrefs(method = CalcMethod.SINGAPORE))
            val zone = ZoneId.of(tz)
            assertEquals("$name Fajr", times.first, day[PrayerSlot.FAJR].atZone(zone).format(hm))
            assertEquals("$name Isha", times.second, day[PrayerSlot.ISHA].atZone(zone).format(hm))
        }
    }

    /**
     * Malaysia moved its national Fajr criterion from 20 to 18 degrees in November 2019,
     * which is Muslim World League's angle. This pins that relationship so the label cannot
     * drift back: if MWL ever stops matching Malaysia's criterion, this fails and the
     * guidance in strings.xml needs rewriting.
     */
    @Test
    fun `Malaysia is served by MWL, not by the Indonesia-Singapore option`() {
        val kl = Coordinates(3.14, 101.69)
        for (date in listOf(midsummer, midwinter)) {
            val mwl = PrayerEngine.compute(kl, date, CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE))
            val seAsia = PrayerEngine.compute(kl, date, CalculationPrefs(method = CalcMethod.SINGAPORE))
            val gap = (seAsia[PrayerSlot.FAJR].toEpochMilli() - mwl[PrayerSlot.FAJR].toEpochMilli()) / 60_000
            assertTrue(
                "20 vs 18 degrees at KL should separate Fajr by roughly 8-9 minutes, was $gap",
                gap in -11..-6,
            )
        }
    }

    /**
     * The sweep that found the second polar bug, kept because reading could never have found
     * it and neither could a spot check.
     *
     * Every 0.5 degrees of latitude from pole to pole, every day of a year, both madhabs.
     * Before the [PrayerEngine] usability guard this failed on 2,239 days — every latitude
     * from 66 to 89.5 in both hemispheres — because adhan does not only return null at
     * extreme latitudes, it can return a confident wrong number. The worst was a 27 January
     * Asr handed back as 13 March.
     *
     * Roughly a quarter of a million computations and about three seconds. Worth it: this is
     * the only test in the project that would have caught that class of fault.
     */
    @Test
    fun `no day anywhere on earth is out of order`() {
        val faults = mutableListOf<String>()
        var lat = -89.5
        while (lat <= 89.5) {
            var date = LocalDate.of(2026, 1, 1)
            while (date.year == 2026) {
                for (madhab in listOf(Madhab.SHAFII, Madhab.HANAFI)) {
                    val day = PrayerEngine.compute(
                        Coordinates(lat, 18.0),
                        date,
                        CalculationPrefs(madhab = madhab),
                    )
                    val instants = day.ordered.map { it.second }
                    if (instants != instants.sorted() && faults.size < 5) {
                        faults += "lat=$lat $date $madhab " +
                            day.ordered.joinToString(" ") { "${it.first}=${it.second}" }
                    }
                }
                date = date.plusDays(1)
            }
            lat += 0.5
        }
        assertTrue("times out of order: ${faults.joinToString(" | ")}", faults.isEmpty())
    }

    /**
     * The sweep above uses the default method, which is not enough for Jafari and Tehran:
     * their Maghrib comes from a **second** adhan call, made with the maghrib angle in the
     * Isha slot, and `isUsable` never sees it. So the two Shia conventions get their own pass.
     *
     * Note for anyone tempted to tighten [isUsable] using the finished Maghrib rather than
     * adhan's raw sunset: do not. Jafari Maghrib sits 4 degrees past sunset and Tehran 4.5, so
     * the midpoint of sunrise-to-Maghrib is legitimately much later than Dhuhr — measured at
     * 52 minutes at 68S in July, where the whole day is only 148 minutes long. That is the
     * rule working, not a fault, and the guard is deliberately applied before it.
     */
    @Test
    fun `the shia maghrib rule survives every latitude`() {
        for (method in listOf(CalcMethod.JAFARI, CalcMethod.TEHRAN)) {
            var lat = -89.0
            while (lat <= 89.0) {
                var date = LocalDate.of(2026, 1, 1)
                while (date.year == 2026) {
                    val day = PrayerEngine.compute(
                        Coordinates(lat, 18.0),
                        date,
                        CalculationPrefs(method = method),
                    )
                    val instants = day.ordered.map { it.second }
                    assertEquals(
                        "$method lat=$lat $date out of order: " +
                            day.ordered.joinToString(" ") { "${it.first}=${it.second}" },
                        instants.sorted(),
                        instants,
                    )
                    date = date.plusDays(1)
                }
                lat += 1.0
            }
        }
    }

    /**
     * Solar noon is the midpoint of the day arc. Where that is not true, adhan has paired a
     * sunrise from one night with a sunset from another, which is how the app came to show a
     * Maghrib of 17:00 at 78N on 24 August and 22:29 the day after. A sunset does not move
     * five and a half hours overnight.
     *
     * Measured before the fix: the largest honest offset anywhere below 65 degrees is three
     * minutes, so this bound is deliberately loose at fifteen and still catches everything.
     */
    @Test
    fun `dhuhr sits in the middle of the day arc unless the day is flagged`() {
        var lat = -89.5
        while (lat <= 89.5) {
            var date = LocalDate.of(2026, 1, 1)
            while (date.year == 2026) {
                val day = PrayerEngine.compute(Coordinates(lat, 18.0), date, CalculationPrefs())
                val sunrise = day[PrayerSlot.SUNRISE].toEpochMilli()
                val maghrib = day[PrayerSlot.MAGHRIB].toEpochMilli()
                val offMinutes =
                    kotlin.math.abs(day[PrayerSlot.DHUHR].toEpochMilli() - (sunrise + maghrib) / 2) / 60_000
                assertTrue(
                    "lat=$lat $date Dhuhr is ${offMinutes}min from the middle of the day",
                    offMinutes <= 30,
                )
                date = date.plusDays(1)
            }
            lat += 0.5
        }
    }

    /** The two days that were wrong in the shipped build, pinned by name. */
    @Test
    fun `the days that were silently wrong are now flagged instead`() {
        // 71.5N, 27 Jan 2026: adhan returned Asr as 13 March, forty-five days out.
        val vorkuta = PrayerEngine.compute(
            Coordinates(71.5, 18.0),
            LocalDate.of(2026, 1, 27),
            CalculationPrefs(),
        )
        assertTrue("the 27 January Asr fault must be caught", vorkuta.approximated)
        assertTrue(
            "Asr must land on its own day",
            vorkuta[PrayerSlot.ASR].atZone(ZoneId.of("UTC")).toLocalDate() in
                LocalDate.of(2026, 1, 26)..LocalDate.of(2026, 1, 28),
        )

        // 78N, 24 Aug 2026: sunrise and sunset paired from different nights.
        val svalbard = PrayerEngine.compute(
            Coordinates(78.0, 0.0),
            LocalDate.of(2026, 8, 24),
            CalculationPrefs(),
        )
        assertTrue("the 24 August pairing fault must be caught", svalbard.approximated)
    }

    /**
     * The reference latitude is a fiqh choice, not a constant, so it has to follow the method
     * the user picked rather than being imposed. 45 is the Islamic Fiqh Council's figure and
     * 60 is Moonsighting Committee's own. See PrayerEngine.polarReferenceLatitude for both
     * sources. If this ever fails, the strings and docs that quote those numbers are wrong too.
     */
    @Test
    fun `the reference latitude follows the chosen method`() {
        val tromso = Coordinates(69.65, 18.96)
        val date = midsummer
        for (method in CalcMethod.entries) {
            val from = PrayerEngine.compute(tromso, date, CalculationPrefs(method = method))
                .approximatedFrom
            val expected = if (method == CalcMethod.MOON_SIGHTING) 60.0 else 45.0
            assertEquals("$method reference latitude", expected, from)
        }
        // Southern hemisphere borrows from its own side of the equator, never across it.
        val antarctic = PrayerEngine.compute(Coordinates(-78.0, 18.0), date, CalculationPrefs())
        assertTrue("southern times must not be flagged as northern", antarctic.approximated)
        assertTrue(
            "a southern winter day must be short, not long",
            antarctic[PrayerSlot.MAGHRIB].toEpochMilli() - antarctic[PrayerSlot.SUNRISE].toEpochMilli() <
                12 * 3_600_000L,
        )
    }

    /**
     * Verification that the Moonsighting projection reproduces what Moonsighting themselves
     * publish, rather than merely matching another calculator — the distinction that the
     * Malaysia label got wrong. Their own page states the consequence of choosing 60 degrees:
     * at Oslo the longest fasting day is 19h38m and the shortest 7h43m. This engine, at their
     * reference latitude, must land on those figures.
     */
    @Test
    fun `moonsighting at its own reference latitude reproduces its published Oslo extremes`() {
        val oslo = Coordinates(60.0, 10.75)
        val prefs = CalculationPrefs(method = CalcMethod.MOON_SIGHTING)
        fun fastMinutes(date: LocalDate): Long {
            val day = PrayerEngine.compute(oslo, date, prefs)
            return (day[PrayerSlot.MAGHRIB].toEpochMilli() - day[PrayerSlot.FAJR].toEpochMilli()) / 60_000
        }
        val longest = fastMinutes(midsummer)
        val shortest = fastMinutes(midwinter)
        assertTrue("longest Oslo fast should be about 19h38m, was ${longest}min", longest in 1176..1180)
        assertTrue("shortest Oslo fast should be about 7h43m, was ${shortest}min", shortest in 459..465)
    }

    /**
     * **A12, settled 2 Aug 2026 — and settled in favour of changing nothing.**
     *
     * Moonsighting's prose says that above 60 degrees they "slide down to 60degrees and
     * calculate Fajr & Isha using the rule of Sab'u Lail in summer". Read literally that means
     * borrowing 60 degrees' much longer night, and an implementation of exactly that was built
     * on 2 Aug and thrown away the same day: swept over 60.5–66N it produced 1,043 non-monotonic
     * days out of 13,140, including **Isha 86 minutes before Maghrib** at Lulea on the solstice.
     * It was reverted as a regression, and the item was parked as *blocked on evidence* — nobody
     * could get moonsighting.com to serve a timetable, because their JSON endpoint 500s and the
     * HTML page builds its table in JavaScript.
     *
     * A browser renders JavaScript. Their generator at `moonsighting.com/pray.php` produced a
     * full 2026 timetable for any latitude, longitude and zone, and the answer is unambiguous:
     * **they publish the un-slid, local numbers, and this engine already reproduces them.**
     *
     * Their Lulea solstice row is `Fajr 00:52, Maghrib 00:10, Isha 00:14` — Isha four minutes
     * *after* Maghrib, a night of about forty minutes, kept in order. That is what a literal
     * slide destroys, and it is what the un-slid calculation already gives. So the prose is
     * describing something narrower than it sounds, the revert was right, and the case for
     * re-implementing it is closed rather than merely deferred.
     *
     * The values below are theirs, transcribed from that page, and pinned here as golden values
     * for the same reason the Aladhan figures are: **so that nobody re-implements the slide.**
     * Anyone who does will turn this red by up to 91 minutes.
     *
     * One minute of tolerance, because they round and this engine truncates.
     */
    @Test
    fun `moonsighting matches its own published timetable across the 60 to 66 degree band`() {
        // lat, lon, zone, date, then Fajr Sunrise Dhuhr Asr(Hanafi) Maghrib Isha as published.
        val published = listOf(
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-06-20", "00:52 01:00 12:38 18:55 00:09 00:14"),
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-06-21", "00:52 01:00 12:38 18:55 00:10 00:14"),
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-06-22", "00:53 01:00 12:38 18:56 00:09 00:14"),
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-07-15", "01:48 02:17 12:42 18:46 22:59 23:24"),
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-08-02", "02:38 03:26 12:43 18:20 21:51 22:36"),
            Published("Lulea", 65.5848, 22.1567, "Europe/Stockholm", "2026-12-21", "08:06 09:55 11:34 11:42 13:07 14:49"),
            Published("Trondheim", 63.4305, 10.3951, "Europe/Oslo", "2026-06-21", "02:33 03:02 13:25 19:33 23:41 00:07"),
            Published("Trondheim", 63.4305, 10.3951, "Europe/Oslo", "2026-07-15", "03:03 03:43 13:29 19:25 23:08 23:44"),
            Published("Trondheim", 63.4305, 10.3951, "Europe/Oslo", "2026-12-21", "08:13 10:01 12:21 12:54 14:35 16:16"),
            Published("Helsinki", 60.1699, 24.9384, "Europe/Helsinki", "2026-06-21", "03:11 03:54 13:27 19:24 22:53 23:33"),
            Published("Helsinki", 60.1699, 24.9384, "Europe/Helsinki", "2026-07-15", "03:32 04:22 13:31 19:17 22:32 23:20"),
            Published("Helsinki", 60.1699, 24.9384, "Europe/Helsinki", "2026-12-21", "07:37 09:24 12:23 13:27 15:16 16:56"),
        )

        for (row in published) {
            val zone = ZoneId.of(row.zone)
            val date = LocalDate.parse(row.date)
            val day = PrayerEngine.compute(
                Coordinates(row.latitude, row.longitude),
                date,
                CalculationPrefs(method = CalcMethod.MOON_SIGHTING, madhab = Madhab.HANAFI),
            )
            val expected = row.times.split(" ")
            for ((index, slot) in PrayerSlot.entries.withIndex()) {
                val ours = day[slot].atZone(zone)
                // Maghrib and Isha can fall after midnight this far north, and their table
                // prints them on the day the fast started. Compare the wall clock only.
                val theirs = LocalTime.parse(expected[index])
                val drift = Duration.between(theirs, ours.toLocalTime()).toMinutes()
                val minutes = if (drift > 720) drift - 1440 else if (drift < -720) drift + 1440 else drift
                assertTrue(
                    "${row.place} ${row.date} $slot: Moonsighting publish ${expected[index]}, " +
                        "this engine gives ${DateTimeFormatter.ofPattern("HH:mm").format(ours)} " +
                        "(${minutes}min out). Anything beyond a minute means the calculation has " +
                        "moved away from what the committee itself publishes — most likely because " +
                        "somebody implemented the 60-degree slide. Read the KDoc above before " +
                        "changing this.",
                    kotlin.math.abs(minutes) <= 1,
                )
            }
        }
    }

    private data class Published(
        val place: String,
        val latitude: Double,
        val longitude: Double,
        val zone: String,
        val date: String,
        val times: String,
    )

    /**
     * A canary, not a rule. adhan ignores [HighLatitudeRule] entirely when the method is
     * Moonsighting Committee — it substitutes its own seasonal model and, above 55 degrees,
     * its own one-seventh clamp. PrayerEngine sets the rule on every method regardless,
     * which is harmless precisely *because* of this. If an upgrade ever makes the rule bite
     * on Moonsighting, this test fails and the engine's comment needs rewriting before
     * anyone ships times that quietly moved by an hour.
     */
    @Test
    fun `high latitude rule is inert for Moonsighting`() {
        for (lat in listOf(51.51, 54.0, 56.0, 60.0)) {
            val results = HighLatitudeRule.entries.map { rule ->
                val p = AdhanMethod.MOON_SIGHTING_COMMITTEE.parameters.apply {
                    madhab = AdhanMadhab.SHAFI
                    highLatitudeRule = rule
                }
                val t = AdhanPrayerTimes(AdhanCoordinates(lat, 0.0), DateComponents(2026, 6, 21), p)
                // Minutes, not the raw Date. adhan rounds to the minute but leaves the
                // millisecond field holding the wall-clock time of the call, so two
                // identical computations are never equal as Dates — the same trap
                // PrayerEngine.instantOf exists to absorb.
                (t.fajr.time / 60_000) to (t.isha.time / 60_000)
            }
            assertEquals(
                "Moonsighting at $lat changed with the high latitude rule: $results",
                1,
                results.distinct().size,
            )
        }
    }
}
