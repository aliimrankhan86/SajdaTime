package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Step 5 (the skies): the pure rules behind the sky and its mark (STEP-5-DESIGN section 7). */
class SkyPeriodTest {
    private val day = LocalDate.parse("2026-10-24")
    private fun at(h: Int, m: Int = 0): Instant = day.atTime(h, m).atZone(ZoneId.of("UTC")).toInstant()

    private val today = DayPrayerTimes(
        date = day,
        times = mapOf(
            PrayerSlot.FAJR to at(5, 0),
            PrayerSlot.SUNRISE to at(7, 40),
            PrayerSlot.DHUHR to at(11, 0),
            PrayerSlot.ASR to at(14, 0),
            PrayerSlot.MAGHRIB to at(17, 0),
            PrayerSlot.ISHA to at(18, 30),
        ),
    )
    private val tomorrowFajr = at(5, 2).plusSeconds(86_400)

    @Test
    fun `period follows the open prayer`() {
        assertEquals(SkyPeriod.DAWN, Sky.periodOf(PrayerSlot.FAJR))
        assertEquals(SkyPeriod.DAY, Sky.periodOf(PrayerSlot.SUNRISE))
        assertEquals(SkyPeriod.DAY, Sky.periodOf(null))
        assertEquals(SkyPeriod.DAY, Sky.periodOf(PrayerSlot.DHUHR))
        assertEquals(SkyPeriod.EVENING, Sky.periodOf(PrayerSlot.ASR))
        assertEquals(SkyPeriod.EVENING, Sky.periodOf(PrayerSlot.MAGHRIB))
        assertEquals(SkyPeriod.NIGHT, Sky.periodOf(PrayerSlot.ISHA))
    }

    @Test
    fun `end of each period`() {
        assertEquals(at(7, 40), Sky.endOf(SkyPeriod.DAWN, today, at(6), tomorrowFajr))
        assertEquals(at(14), Sky.endOf(SkyPeriod.DAY, today, at(9), tomorrowFajr))
        assertEquals(at(18, 30), Sky.endOf(SkyPeriod.EVENING, today, at(16), tomorrowFajr))
        assertEquals(tomorrowFajr, Sky.endOf(SkyPeriod.NIGHT, today, at(21), tomorrowFajr))
        assertEquals(at(5), Sky.endOf(SkyPeriod.NIGHT, today, at(2), tomorrowFajr))
        assertNull(Sky.endOf(SkyPeriod.NIGHT, today, at(21), null))
    }

    @Test
    fun `progress at the named moments`() {
        val p = today.prayersOnly
        assertEquals(0f, Sky.progress(at(4, 59), p), 0f)
        assertEquals(0f, Sky.progress(at(5), p), 0f)
        // Fajr 05:00 to Dhuhr 11:00, halfway is 08:00: (0 + 0.5) / 4.
        assertEquals(0.125f, Sky.progress(at(8), p), 1e-6f)
        assertEquals(1f, Sky.progress(at(18, 30), p), 0f)
        assertEquals(1f, Sky.progress(at(23, 59), p), 0f)
    }

    @Test
    fun `progress never goes backwards across a day`() {
        assertMonotonicAndBounded(today)
    }

    @Test
    fun `progress holds on a real day and a polar approximated day`() {
        val prefs = CalculationPrefs(Sect.SUNNI, Madhab.SHAFII, CalcMethod.MUSLIM_WORLD_LEAGUE)
        val london = PrayerEngine.compute(Coordinates(51.5074, -0.1278), day, prefs)
        assertMonotonicAndBounded(london)
        val tromso = PrayerEngine.compute(Coordinates(69.65, 18.96), LocalDate.parse("2027-06-21"), prefs)
        assertTrue("Tromso in June is approximated", tromso.approximated)
        assertMonotonicAndBounded(tromso)
    }

    private fun assertMonotonicAndBounded(times: DayPrayerTimes) {
        val start = times.date.atStartOfDay(ZoneId.of("UTC")).toInstant()
        var last = -1f
        for (minute in 0 until 24 * 60) {
            val p = Sky.progress(start.plusSeconds(minute * 60L), times.prayersOnly)
            assertTrue("progress $p out of range at minute $minute", p in 0f..1f)
            assertTrue("progress went back at minute $minute: $last then $p", p >= last)
            last = p
        }
    }

    @Test
    fun `ordered with sunrise is refused`() {
        try {
            Sky.progress(at(9), today.ordered)
            fail("six entries must throw")
        } catch (expected: IllegalArgumentException) {
        }
    }

    @Test
    fun `mark fits only when the name leaves room`() {
        // 2.0 text: the name wins at any width.
        assertFalse(Sky.markFits(10f, 5000f, 1f, 2.0f))
        // London (90) on a 288 row.
        assertTrue(Sky.markFits(90f, 288f, 1f, 1.0f))
        // Kuala Lumpur (160) on a 288 row.
        assertFalse(Sky.markFits(160f, 288f, 1f, 1.0f))
        // Boundary: row less 150 exactly.
        assertTrue(Sky.markFits(138f, 288f, 1f, 1.0f))
        assertFalse(Sky.markFits(138.5f, 288f, 1f, 1.0f))
        // Density 3 scales the 150dp margin: 864 - 450 = 414.
        assertTrue(Sky.markFits(414f, 864f, 3f, 1.0f))
        assertFalse(Sky.markFits(415f, 864f, 3f, 1.0f))
    }
}
