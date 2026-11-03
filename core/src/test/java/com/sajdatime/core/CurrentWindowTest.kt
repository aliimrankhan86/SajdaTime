package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CurrentWindowTest {
    private val utc = ZoneId.of("UTC")
    private val london = Coordinates(51.51, -0.12)
    private val prefs = CalculationPrefs()
    private val date = LocalDate.parse("2026-01-10")
    private val day = PrayerEngine.compute(london, date, prefs)
    private val next = PrayerEngine.compute(london, date.plusDays(1), prefs)
    private fun after(slot: PrayerSlot, m: Long = 1) = day[slot].plusSeconds(60 * m)

    @Test
    fun `each prayer counts to the next row on the list`() {
        fun w(slot: PrayerSlot) = CurrentWindow.at(london, prefs, after(slot), utc, true)!!
        assertEquals(WindowTarget.SUNRISE to day[PrayerSlot.SUNRISE], w(PrayerSlot.FAJR).let { it.target to it.at })
        assertEquals(WindowTarget.ASR to day[PrayerSlot.ASR], w(PrayerSlot.DHUHR).let { it.target to it.at })
        assertEquals(WindowTarget.MAGHRIB to day[PrayerSlot.MAGHRIB], w(PrayerSlot.ASR).let { it.target to it.at })
        assertEquals(WindowTarget.ISHA to day[PrayerSlot.ISHA], w(PrayerSlot.MAGHRIB).let { it.target to it.at })
    }

    @Test
    fun `no line between sunrise and Dhuhr`() {
        assertNull(CurrentWindow.at(london, prefs, after(PrayerSlot.SUNRISE), utc, true))
    }

    @Test
    fun `Isha counts to Islamic midnight while the night row shows, then to Fajr`() {
        val night = Night.between(day.sunset!!, next[PrayerSlot.FAJR])!!
        val early = CurrentWindow.at(london, prefs, after(PrayerSlot.ISHA), utc, true)!!
        assertEquals(WindowTarget.MIDNIGHT, early.target)
        assertEquals(night.midnight, early.at)
        val late = CurrentWindow.at(london, prefs, night.midnight.plusSeconds(60), utc, true)!!
        assertEquals(WindowTarget.FAJR, late.target)
        assertEquals(next[PrayerSlot.FAJR], late.at)
    }

    @Test
    fun `night row off means Isha counts to Fajr`() {
        val w = CurrentWindow.at(london, prefs, after(PrayerSlot.ISHA), utc, false)!!
        assertEquals(WindowTarget.FAJR, w.target)
        assertEquals(next[PrayerSlot.FAJR], w.at)
    }

    @Test
    fun `after midnight on the calendar Isha is still yesterday's and counts to today's Fajr`() {
        val now = Instant.parse("2026-01-11T01:00:00Z")
        val w = CurrentWindow.at(london, prefs, now, utc, false)!!
        assertEquals(PrayerSlot.ISHA, w.current)
        assertEquals(next[PrayerSlot.FAJR], w.at)
        assertTrue(w.remaining(now) > Duration.ZERO)
    }
}
