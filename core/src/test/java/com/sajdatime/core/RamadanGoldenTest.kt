package com.sajdatime.core

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
 * Pins the engine's Ramadan 1448 output for ten reference setups so the Ramadan card, the timetable,
 * the suhoor reminder and the widgets all rest on numbers somebody has looked at.
 *
 * The expected values are from docs/plans/2026-10-widget-ramadan-tracker/RAMADAN-LOCATION-SPEC.md
 * section 9.2. They were produced by a line by line Java mirror of the engine run against the real
 * adhan 1.2.1 jar, and the Fajr and Maghrib columns were cross checked against the Aladhan API on
 * 16 October 2026 (tolerance two minutes, see 9.4). The mirror is not the engine: this test is what
 * proves the two agree, and a disagreement is a mirror mistake until it is shown otherwise.
 *
 * Fajr and Maghrib, the two times the Ramadan card calls suhoor and iftar, must match to the minute.
 * The other four may differ by a minute, because a method's own Dhuhr adjustment lands either side
 * of a rounding boundary. User minutes are not applied here: the card shows adjusted times, these
 * are the unadjusted engine.
 */
class RamadanGoldenTest {

    private val clock = DateTimeFormatter.ofPattern("HH:mm")

    private class Setup(
        val name: String,
        val where: Coordinates,
        val zone: String,
        val prefs: CalculationPrefs,
        /** "yyyy-MM-dd Fajr Sunrise Dhuhr Asr Maghrib Isha", with "-" for a column the table omits. */
        val rows: List<String>,
    )

    private val setups = listOf(
        Setup(
            "London, Muslim World League, Shafi'i",
            Coordinates(51.5074, -0.1278), "Europe/London",
            CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-07 05:36 07:29 12:16 14:35 17:00 18:48",
                "2027-02-08 05:34 07:28 12:16 14:36 17:02 18:50",
                "2027-02-09 05:33 07:26 12:16 14:38 17:04 18:51",
                "2027-02-21 05:12 07:03 12:15 14:54 17:26 19:11",
                "2027-02-22 05:10 07:01 12:15 14:56 17:28 19:13",
                "2027-02-23 05:08 06:59 12:15 14:57 17:30 19:15",
                "2027-03-07 04:42 06:33 12:13 15:12 17:51 19:36",
                "2027-03-08 04:40 06:31 12:12 15:13 17:53 19:37",
                "2027-03-09 04:38 06:29 12:12 15:14 17:54 19:39",
            ),
        ),
        Setup(
            "London, Moonsighting Committee",
            Coordinates(51.5074, -0.1278), "Europe/London",
            CalculationPrefs(method = CalcMethod.MOON_SIGHTING, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 05:51 - - - 17:05 18:29",
                "2027-02-22 05:25 - - - 17:31 18:51",
                "2027-03-08 04:56 - - - 17:56 19:13",
            ),
        ),
        Setup(
            "Makkah, Umm al-Qura",
            Coordinates(21.4225, 39.8262), "Asia/Riyadh",
            CalculationPrefs(method = CalcMethod.UMM_AL_QURA, madhab = Madhab.SHAFII),
            listOf(
                // Isha is Maghrib plus 90 before Ramadan, plus 120 inside it, on 7 and 9 March it is 90 again.
                "2027-02-07 05:38 06:56 12:35 15:50 18:14 19:44",
                "2027-02-08 05:38 06:55 12:35 15:50 18:15 20:15",
                "2027-02-22 05:30 06:47 12:34 15:54 18:22 20:22",
                "2027-03-08 05:20 06:36 12:32 15:55 18:27 20:27",
                "2027-03-09 05:19 06:35 12:31 15:55 18:28 19:58",
            ),
        ),
        Setup(
            "Istanbul, Diyanet (18 and 17 degrees, with Diyanet's precaution minutes)",
            Coordinates(41.0082, 28.9784), "Europe/Istanbul",
            CalculationPrefs(method = CalcMethod.TURKEY, madhab = Madhab.SHAFII),
            listOf(
                // The plain angle rows these replaced (verified earlier), plus Sunrise -7, Dhuhr +5,
                // Asr +4, Maghrib +7 (18 Oct 2026). Fajr and Isha unchanged.
                "2027-02-08 06:34 08:01 13:23 16:10 18:36 19:57",
                "2027-02-22 06:18 07:42 13:23 16:23 18:53 20:13",
                "2027-03-08 05:57 07:21 13:20 16:34 19:10 20:29",
            ),
        ),
        Setup(
            "Karachi, University of Karachi, Hanafi",
            Coordinates(24.8607, 67.0011), "Asia/Karachi",
            CalculationPrefs(method = CalcMethod.KARACHI, madhab = Madhab.HANAFI),
            listOf(
                "2027-02-08 05:53 07:11 12:47 16:45 18:22 19:39",
                "2027-02-22 05:45 07:01 12:47 16:53 18:30 19:47",
                "2027-03-08 05:33 06:49 12:44 16:58 18:37 19:53",
            ),
        ),
        Setup(
            "Jakarta, the Singapore preset",
            Coordinates(-6.2088, 106.8456), "Asia/Jakarta",
            CalculationPrefs(method = CalcMethod.SINGAPORE, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 04:36 05:57 12:08 15:24 18:17 19:29",
                "2027-02-22 04:39 05:58 12:07 15:16 18:14 19:24",
                "2027-03-08 04:40 05:58 12:05 15:07 18:09 19:18",
            ),
        ),
        Setup(
            "Sydney, Muslim World League",
            Coordinates(-33.8688, 151.2093), "Australia/Sydney",
            CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 04:51 06:23 13:10 16:51 19:55 21:21",
                "2027-02-22 05:08 06:36 13:10 16:47 19:41 21:03",
                "2027-03-08 05:23 06:48 13:07 16:39 19:23 20:43",
            ),
        ),
        Setup(
            "Tromso, Muslim World League (real astronomy in Feb and Mar)",
            Coordinates(69.6496, 18.9560), "Europe/Oslo",
            CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 05:14 08:50 11:59 12:56 15:08 18:34",
                "2027-02-22 04:20 07:43 11:59 13:32 16:13 19:26",
                "2027-03-08 03:11 06:39 11:56 14:09 17:12 20:28",
            ),
        ),
        Setup(
            "Tromso, Moonsighting Committee",
            Coordinates(69.6496, 18.9560), "Europe/Oslo",
            CalculationPrefs(method = CalcMethod.MOON_SIGHTING, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 07:05 - - - 15:11 16:39",
                "2027-02-22 06:00 - - - 16:16 17:40",
                "2027-03-08 04:58 - - - 17:15 18:35",
            ),
        ),
        Setup(
            "Tehran, Jafari",
            Coordinates(35.6892, 51.3890), "Asia/Tehran",
            CalculationPrefs(sect = Sect.SHIA, method = CalcMethod.JAFARI, madhab = Madhab.SHAFII),
            listOf(
                "2027-02-08 05:42 06:59 12:19 15:16 17:55 18:45",
                "2027-02-22 05:29 06:44 12:18 15:27 18:08 18:58",
                "2027-03-08 05:11 06:26 12:15 15:34 18:21 19:10",
            ),
        ),
    )

    private fun minutesOf(s: String): Int = LocalTime.parse(s, clock).let { it.hour * 60 + it.minute }

    @Test
    fun `suhoor end and iftar match the pinned values to the minute at every reference place`() {
        for (setup in setups) {
            val zone = ZoneId.of(setup.zone)
            for (row in setup.rows) {
                val parts = row.split(" ")
                val date = LocalDate.parse(parts[0])
                val day = PrayerEngine.compute(setup.where, date, setup.prefs)
                fun local(slot: PrayerSlot): Int =
                    day[slot].atZone(zone).toLocalTime().let { it.hour * 60 + it.minute }

                val label = "${setup.name} $date"
                assertEquals("$label Fajr", minutesOf(parts[1]), local(PrayerSlot.FAJR))
                assertEquals("$label Maghrib", minutesOf(parts[5]), local(PrayerSlot.MAGHRIB))
            }
        }
    }

    @Test
    fun `the other four times are within a minute of the pinned values`() {
        val columns = listOf(
            2 to PrayerSlot.SUNRISE,
            3 to PrayerSlot.DHUHR,
            4 to PrayerSlot.ASR,
            6 to PrayerSlot.ISHA,
        )
        for (setup in setups) {
            val zone = ZoneId.of(setup.zone)
            for (row in setup.rows) {
                val parts = row.split(" ")
                val date = LocalDate.parse(parts[0])
                val day = PrayerEngine.compute(setup.where, date, setup.prefs)
                for ((index, slot) in columns) {
                    if (parts[index] == "-") continue
                    val actual = day[slot].atZone(zone).toLocalTime().let { it.hour * 60 + it.minute }
                    val difference = kotlin.math.abs(actual - minutesOf(parts[index]))
                    assertTrue(
                        "${setup.name} $date $slot expected ${parts[index]} off by $difference",
                        difference <= 1,
                    )
                }
            }
        }
    }

    @Test
    fun `Umm al-Qura Isha at Makkah is exactly 90 minutes outside Ramadan and 120 inside it`() {
        // The one rule in the engine that the Ramadan calendar moves. Exact, not within a minute.
        val makkah = setups.first { it.name.startsWith("Makkah") }
        fun gap(date: String): Long {
            val day = PrayerEngine.compute(makkah.where, LocalDate.parse(date), makkah.prefs)
            return Duration.between(day[PrayerSlot.MAGHRIB], day[PrayerSlot.ISHA]).toMinutes()
        }
        assertEquals(90L, gap("2027-02-07"))
        assertEquals(120L, gap("2027-02-08"))
        assertEquals(120L, gap("2027-02-22"))
        assertEquals(120L, gap("2027-03-08"))
        assertEquals(90L, gap("2027-03-09"))
    }

    @Test
    fun `the shift reaches the Umm al-Qura Isha rule on every boundary day at every offset`() {
        // Isha minus Maghrib in minutes at Makkah for offsets -2, -1, 0, +1, +2 (spec 9.2).
        val table = mapOf(
            "2027-02-06" to listOf(90, 90, 90, 90, 120),
            "2027-02-07" to listOf(90, 90, 90, 120, 120),
            "2027-02-08" to listOf(90, 90, 120, 120, 120),
            "2027-02-09" to listOf(90, 120, 120, 120, 120),
            "2027-02-10" to listOf(120, 120, 120, 120, 120),
            "2027-03-07" to listOf(120, 120, 120, 120, 90),
            "2027-03-08" to listOf(120, 120, 120, 90, 90),
            "2027-03-09" to listOf(120, 120, 90, 90, 90),
            "2027-03-10" to listOf(120, 90, 90, 90, 90),
        )
        val makkah = Coordinates(21.4225, 39.8262)
        for ((date, gaps) in table) {
            for ((i, offset) in (-2..2).withIndex()) {
                val prefs = CalculationPrefs(method = CalcMethod.UMM_AL_QURA, hijriOffsetDays = offset)
                val day = PrayerEngine.compute(makkah, LocalDate.parse(date), prefs)
                val actual = Duration.between(day[PrayerSlot.MAGHRIB], day[PrayerSlot.ISHA]).toMinutes()
                assertEquals("$date offset $offset", gaps[i].toLong(), actual)
            }
        }
    }

    @Test
    fun `London daylight saving shows the true change in the fast, plus 5 minutes and not plus 65`() {
        val london = Coordinates(51.5074, -0.1278)
        val zone = ZoneId.of("Europe/London")
        val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)
        fun fast(date: String): Long {
            val day = PrayerEngine.compute(london, LocalDate.parse(date), prefs)
            return Duration.between(day[PrayerSlot.FAJR], day[PrayerSlot.MAGHRIB]).toMinutes()
        }
        fun clockOf(date: String, slot: PrayerSlot): String =
            PrayerEngine.compute(london, LocalDate.parse(date), prefs)[slot]
                .atZone(zone).toLocalTime().format(clock)
        assertEquals("03:52", clockOf("2027-03-27", PrayerSlot.FAJR))
        assertEquals("18:25", clockOf("2027-03-27", PrayerSlot.MAGHRIB))
        assertEquals("04:49", clockOf("2027-03-28", PrayerSlot.FAJR))
        assertEquals("19:27", clockOf("2027-03-28", PrayerSlot.MAGHRIB))
        assertEquals("04:47", clockOf("2027-03-29", PrayerSlot.FAJR))
        assertEquals("19:28", clockOf("2027-03-29", PrayerSlot.MAGHRIB))
        assertEquals(14 * 60 + 33L, fast("2027-03-27"))
        assertEquals(5L, fast("2027-03-28") - fast("2027-03-27"))
        assertEquals(3L, fast("2027-03-29") - fast("2027-03-28"))
    }

    @Test
    fun `Sydney's fast gets shorter through the month`() {
        val sydney = setups.first { it.name.startsWith("Sydney") }
        fun fast(date: String): Long {
            val day = PrayerEngine.compute(sydney.where, LocalDate.parse(date), sydney.prefs)
            return Duration.between(day[PrayerSlot.FAJR], day[PrayerSlot.MAGHRIB]).toMinutes()
        }
        assertTrue(fast("2027-02-22") < fast("2027-02-08"))
        assertTrue(fast("2027-03-08") < fast("2027-02-22"))
    }

    @Test
    fun `Tromso is real astronomy through Ramadan 1448 and projected on exactly the pinned ranges of 2027`() {
        val tromso = setups.first { it.name.startsWith("Tromso, Muslim") }
        // None of Ramadan 1448 is projected there: nothing is marked.
        var date = LocalDate.of(2027, 2, 8)
        repeat(29) {
            assertFalse(
                "$date",
                PrayerEngine.compute(tromso.where, date, tromso.prefs).approximated,
            )
            date = date.plusDays(1)
        }
        // And the year's projected days are exactly the three ranges in the spec.
        val projected = generateSequence(LocalDate.of(2027, 1, 1)) { it.plusDays(1) }
            .takeWhile { it.year == 2027 }
            .filter { PrayerEngine.compute(tromso.where, it, tromso.prefs).approximated }
            .toList()
        val expected = (rangeOf(1, 1, 1, 21) + rangeOf(5, 18, 7, 26) + rangeOf(11, 16, 12, 31))
        assertEquals(expected, projected)
    }

    @Test
    fun `invariants hold on every day of Ramadan 1447 and 1448 at every reference place`() {
        for (setup in setups) {
            for (first in listOf(LocalDate.of(2026, 2, 18), LocalDate.of(2027, 2, 8))) {
                val length = if (first.year == 2026) 30 else 29
                for (i in 0 until length) {
                    val date = first.plusDays(i.toLong())
                    val day = PrayerEngine.compute(setup.where, date, setup.prefs)
                    val ordered = day.ordered.map { it.second }
                    assertEquals("${setup.name} $date ordering", ordered.sorted(), ordered)
                    val fast = Duration.between(day[PrayerSlot.FAJR], day[PrayerSlot.MAGHRIB])
                    assertTrue("${setup.name} $date fast $fast", fast > Duration.ZERO && fast < Duration.ofHours(24))
                    // Reference places sit between 6 and 18 hours in February and March.
                    assertTrue(
                        "${setup.name} $date fast $fast",
                        fast > Duration.ofHours(6) && fast < Duration.ofHours(18),
                    )
                    // The day number never depends on where the user is standing.
                    assertEquals(i + 1, HijriCalendar.of(date, 0)!!.day)
                }
            }
        }
    }

    private fun rangeOf(m1: Int, d1: Int, m2: Int, d2: Int): List<LocalDate> {
        val start = LocalDate.of(2027, m1, d1)
        val end = LocalDate.of(2027, m2, d2)
        return generateSequence(start) { it.plusDays(1) }.takeWhile { !it.isAfter(end) }.toList()
    }
}
