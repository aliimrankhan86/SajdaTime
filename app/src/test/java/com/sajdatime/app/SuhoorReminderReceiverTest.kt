package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.shouldPostReminder
import com.sajdatime.app.notify.suhoorMinutesLeft
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

/**
 * What the receiver decides when a suhoor reminder arrives: show it, or stay silent because it has
 * gone stale. A late reminder is dropped rather than shown with a countdown that is no longer true.
 */
class SuhoorReminderReceiverTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val day = LocalDate.parse("2027-02-20") // day 13 of Ramadan 1448
    private fun settings(minutes: Int = 30, offset: Int = 0) =
        AppSettings(coordinates = london, suhoorReminderMinutes = minutes, hijriOffsetDays = offset)

    private val fajr: Instant =
        PrayerEngine.compute(london, day, settings().calculationPrefs)[PrayerSlot.FAJR]
    private val reminderAt: Instant = fajr.minusSeconds(30 * 60)

    @Test
    fun `on time it is shown`() {
        assertTrue(shouldPostReminder(settings(), day, fajr, reminderAt))
    }

    @Test
    fun `up to five minutes late it is still shown`() {
        assertTrue(shouldPostReminder(settings(), day, fajr, reminderAt.plusSeconds(5 * 60)))
    }

    @Test
    fun `six minutes late it is dropped`() {
        assertFalse(shouldPostReminder(settings(), day, fajr, reminderAt.plusSeconds(6 * 60)))
        assertFalse(shouldPostReminder(settings(), day, fajr, reminderAt.plusSeconds(5 * 60 + 1)))
    }

    @Test
    fun `after Fajr it is dropped, however the delay came about`() {
        assertFalse(shouldPostReminder(settings(), day, fajr, fajr.plusSeconds(1)))
    }

    @Test
    fun `switched off since it was scheduled, it is dropped`() {
        assertFalse(shouldPostReminder(settings(minutes = 0), day, fajr, reminderAt))
    }

    @Test
    fun `a date outside Ramadan on the calendar in use now is dropped`() {
        // 8 Feb 2027 is day 1 on the default calendar and the eve once the person shifts it back a day.
        val first = LocalDate.parse("2027-02-08")
        val fajrFirst = PrayerEngine.compute(london, first, settings().calculationPrefs)[PrayerSlot.FAJR]
        val at = fajrFirst.minusSeconds(30 * 60)
        assertTrue(shouldPostReminder(settings(), first, fajrFirst, at))
        assertFalse(shouldPostReminder(settings(offset = -1), first, fajrFirst, at))
    }

    @Test
    fun `the day after the last day of Ramadan is dropped`() {
        val shawwal = LocalDate.parse("2027-03-09")
        val f = PrayerEngine.compute(london, shawwal, settings().calculationPrefs)[PrayerSlot.FAJR]
        assertFalse(shouldPostReminder(settings(), shawwal, f, f.minusSeconds(30 * 60)))
    }

    @Test
    fun `a longer reminder has the same five minute grace measured from its own time`() {
        val s = settings(minutes = 60)
        val at = fajr.minusSeconds(60 * 60)
        assertTrue(shouldPostReminder(s, day, fajr, at.plusSeconds(5 * 60)))
        assertFalse(shouldPostReminder(s, day, fajr, at.plusSeconds(5 * 60 + 1)))
    }

    @Test
    fun `the minutes left round to the nearest minute so a few seconds late still reads as chosen`() {
        val f = Instant.parse("2027-02-20T05:10:00Z")
        assertEquals(30, suhoorMinutesLeft(f.minusSeconds(30 * 60), f))
        assertEquals(30, suhoorMinutesLeft(f.minusSeconds(30 * 60 - 3), f))
        assertEquals(30, suhoorMinutesLeft(f.minusSeconds(30 * 60 + 20), f))
        assertEquals(29, suhoorMinutesLeft(f.minusSeconds(29 * 60 + 29), f))
        assertEquals(15, suhoorMinutesLeft(f.minusSeconds(15 * 60), f))
    }

    @Test
    fun `the minutes left are never negative`() {
        val f = Instant.parse("2027-02-20T05:10:00Z")
        assertEquals(0, suhoorMinutesLeft(f.plusSeconds(600), f))
        assertEquals(0, suhoorMinutesLeft(f, f))
    }
}
