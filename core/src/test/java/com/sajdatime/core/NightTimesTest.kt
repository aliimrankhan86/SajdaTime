package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Night times (1.6.0): Islamic midnight and the start of the last third. */
class NightTimesTest {
    private val utc = ZoneId.of("UTC")
    private fun t(s: String): Instant = Instant.parse(s)

    @Test
    fun `night is sunset to Fajr, midnight halfway and last third at two thirds`() {
        // Night = sunset to next Fajr by Ali's decision 2 Nov 2026 (Aladhan midnightMode=1; its default, sunset to
        // sunrise, differs by about an hour). Worked by hand: sunset 16:20, next Fajr 06:20 -> night 14h;
        // half 7h = 23:20; two thirds 9h20m = 01:40.
        val n = Night.between(t("2026-01-10T16:20:00Z"), t("2026-01-11T06:20:00Z"))!!
        assertEquals(t("2026-01-10T23:20:00Z"), n.midnight)
        assertEquals(t("2026-01-11T01:40:00Z"), n.lastThirdStart)
        assertFalse(n.approximated)
    }

    @Test
    fun `London pair checked against the live Aladhan API (midnightMode 1)`() {
        // Sunset 2026-11-02 16:32Z, next Fajr 2026-11-03 05:04Z, verified against the live API.
        val n = Night.between(t("2026-11-02T16:32:00Z"), t("2026-11-03T05:04:00Z"))!!
        assertEquals(t("2026-11-02T22:48:00Z"), n.midnight)
        assertEquals(t("2026-11-03T00:53:20Z"), n.lastThirdStart)
    }

    @Test
    fun `a night that does not end after it begins gives nothing`() {
        assertNull(Night.between(t("2026-01-10T16:20:00Z"), t("2026-01-10T16:20:00Z")))
        assertNull(Night.between(t("2026-01-10T16:20:00Z"), t("2026-01-10T05:00:00Z")))
    }

    @Test
    fun `from the engine, midnight is halfway from sunset to the Fajr the list shows`() {
        val london = Coordinates(51.51, -0.12)
        val prefs = CalculationPrefs()
        val now = t("2026-01-10T12:00:00Z")
        val n = Night.at(london, prefs, now, utc)!!
        val today = PrayerEngine.compute(london, LocalDate.parse("2026-01-10"), prefs)
        val fajr = PrayerEngine.compute(london, LocalDate.parse("2026-01-11"), prefs)[PrayerSlot.FAJR]
        val night = Duration.between(today.sunset, fajr).toMillis()
        assertEquals(today.sunset!!.plusMillis(night / 2), n.midnight)
        assertEquals(fajr.minusMillis(night / 3), n.lastThirdStart, )
        assertFalse(n.approximated)
        assertTrue(n.midnight > today[PrayerSlot.ISHA])
    }

    @Test
    fun `before Fajr it is last evening's night`() {
        val london = Coordinates(51.51, -0.12)
        val prefs = CalculationPrefs()
        val fajr = PrayerEngine.compute(london, LocalDate.parse("2026-01-10"), prefs)[PrayerSlot.FAJR]
        val before = Night.at(london, prefs, fajr.minusSeconds(60), utc)!!
        val after = Night.at(london, prefs, fajr, utc)!!
        val yesterdaySunset = PrayerEngine.compute(london, LocalDate.parse("2026-01-09"), prefs).sunset!!
        assertTrue(before.midnight > yesterdaySunset && before.lastThirdStart < fajr)
        assertTrue(after.midnight > fajr)
    }

    @Test
    fun `Sunni and Shia see the same sunset, not the Jafari Maghrib`() {
        val london = Coordinates(51.51, -0.12)
        val date = LocalDate.parse("2026-01-10")
        val sunni = PrayerEngine.compute(london, date, CalculationPrefs(sect = Sect.SUNNI))
        val shia = PrayerEngine.compute(london, date, CalculationPrefs(sect = Sect.SHIA))
        assertEquals(sunni.sunset, shia.sunset)
        assertTrue(shia[PrayerSlot.MAGHRIB] > shia.sunset)
        // Methods whose adhan offsets push maghrib past sunset must not move the night either.
        val sunsets = listOf(CalcMethod.MUSLIM_WORLD_LEAGUE, CalcMethod.TURKEY, CalcMethod.DUBAI, CalcMethod.MOON_SIGHTING)
            .map { PrayerEngine.compute(london, date, CalculationPrefs(method = it)).sunset }
        assertEquals(1, sunsets.toSet().size)
    }

    @Test
    fun `a projected high latitude night is flagged approximate`() {
        // Tromso in June: the sun does not set, so sunset and Fajr are projected from a lower latitude.
        val n = Night.at(Coordinates(69.65, 18.96), CalculationPrefs(), t("2026-06-21T12:00:00Z"), utc)
        assertNotNull(n)
        assertTrue(n!!.approximated)
        assertTrue(n.midnight < n.lastThirdStart)
    }
}
