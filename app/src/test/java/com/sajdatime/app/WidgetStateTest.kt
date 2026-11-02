package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.widget.WidgetState
import com.sajdatime.app.widget.WidgetState.RamadanWord
import com.sajdatime.app.widget.WidgetState.Segment
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.SkyPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * What the two widgets are told to draw. Ramadan 1448 in London is 8 Feb to 8 Mar 2027 and London is on
 * GMT then, so local time and UTC agree and every moment below reads as it is written.
 */
class WidgetStateTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")

    private fun settings(
        offset: Int = 0,
        cardOn: Boolean = true,
        where: Coordinates? = london,
    ) = AppSettings(
        coordinates = where,
        method = CalcMethod.MUSLIM_WORLD_LEAGUE,
        hijriOffsetDays = offset,
        ramadanCardEnabled = cardOn,
    )

    private fun at(date: String, time: String): Instant =
        LocalDateTime.parse("${date}T$time").atZone(zone).toInstant()

    private fun build(
        date: String,
        time: String,
        s: AppSettings = settings(),
        exact: Boolean = true,
    ) = WidgetState.build(s, at(date, time), zone, exact)

    @Test
    fun `no location gives an empty state that says so`() {
        val state = build("2027-02-22", "04:00", settings(where = null))
        assertFalse(state.hasLocation)
        assertNull(state.today)
        assertNull(state.next)
        assertFalse(state.ramadanToday)
        assertFalse(state.ramadanOnNextDay)
        assertFalse(state.isHighlighted(PrayerSlot.FAJR))
    }

    /**
     * The Next prayer widget's sky is the phone's rule (Sky.periodOf on the prayer whose window is open).
     * London 22 Feb 2027, Muslim World League: Fajr about 05:20, sunrise about 07:00, Dhuhr 12:15,
     * Asr about 15:00, Maghrib about 17:30, Isha about 19:00.
     */
    @Test
    fun `the sky follows the prayer whose window is open`() {
        val cases = listOf(
            "02:00" to SkyPeriod.NIGHT, // last night's Isha is still the current prayer
            "06:30" to SkyPeriod.DAWN, // Fajr is in, sunrise is not yet
            "10:00" to SkyPeriod.DAY, // after sunrise, before Dhuhr: no prayer, Day
            "16:00" to SkyPeriod.EVENING, // Asr
            "21:00" to SkyPeriod.NIGHT, // Isha
        )
        for ((time, expected) in cases) assertEquals(time, expected, build("2027-02-22", time).period)
    }

    @Test
    fun `with no location the sky is Day`() {
        assertEquals(SkyPeriod.DAY, build("2027-02-22", "21:00", settings(where = null)).period)
    }

    @Test
    fun `before Fajr the next prayer is today's Fajr and only it is highlighted`() {
        val state = build("2027-02-22", "04:00")
        assertTrue(state.hasLocation)
        assertEquals(PrayerSlot.FAJR, state.next!!.slot)
        assertFalse(state.next.isTomorrow)
        for (slot in PrayerSlot.entries) {
            assertEquals("$slot", slot == PrayerSlot.FAJR, state.isHighlighted(slot))
        }
    }

    @Test
    fun `after Isha the next prayer is tomorrow's Fajr and nothing is highlighted`() {
        val state = build("2027-02-22", "23:30")
        assertEquals(PrayerSlot.FAJR, state.next!!.slot)
        assertTrue(state.next.isTomorrow)
        for (slot in PrayerSlot.entries) assertFalse("$slot", state.isHighlighted(slot))
    }

    @Test
    fun `sunrise is never highlighted and at most one row is, at every minute of a day`() {
        var moment = at("2027-03-01", "00:00")
        val end = at("2027-03-02", "00:00")
        while (moment.isBefore(end)) {
            val state = WidgetState.build(settings(), moment, zone, true)
            assertFalse("sunrise at $moment", state.isHighlighted(PrayerSlot.SUNRISE))
            val lit = PrayerSlot.entries.count { state.isHighlighted(it) }
            assertTrue("$lit rows at $moment", lit <= 1)
            moment = moment.plusSeconds(60)
        }
    }

    @Test
    fun `the widget carries the app's Theme choice, with or without a location`() {
        for (choice in ThemeChoice.entries) {
            assertEquals(choice, build("2027-02-22", "04:00", settings().copy(themeChoice = choice)).theme)
            assertEquals(choice, build("2027-02-22", "04:00", settings(where = null).copy(themeChoice = choice)).theme)
        }
        assertEquals(ThemeChoice.SYSTEM, build("2027-02-22", "04:00").theme)
    }

    @Test
    fun `the countdown flag mirrors whether exact alarms are allowed`() {
        assertTrue(build("2027-02-22", "04:00", exact = true).countdownAllowed)
        assertFalse(build("2027-02-22", "04:00", exact = false).countdownAllowed)
        assertTrue(build("2027-02-22", "04:00", settings(where = null), exact = true).countdownAllowed)
        assertFalse(build("2027-02-22", "04:00", settings(where = null), exact = false).countdownAllowed)
    }

    @Test
    fun `on a day of Ramadan today and the next prayer are both Ramadan`() {
        val state = build("2027-02-22", "10:00")
        assertTrue(state.ramadanToday)
        assertTrue(state.ramadanOnNextDay)
    }

    @Test
    fun `on the eve at 23 00 tomorrow's Fajr is already Ramadan though today is not`() {
        val state = build("2027-02-07", "23:00")
        assertFalse(state.ramadanToday)
        assertTrue(state.ramadanOnNextDay)
        assertEquals(PrayerSlot.FAJR, state.next!!.slot)
        assertTrue(state.next.isTomorrow)
    }

    @Test
    fun `on the last day after Isha tomorrow's Fajr is Shawwal and carries no suhoor word`() {
        val state = build("2027-03-08", "23:00")
        assertTrue(state.ramadanToday)
        assertFalse(state.ramadanOnNextDay)
        assertNull(state.ramadanWordFor(state.next!!.slot, state.ramadanOnNextDay))
    }

    @Test
    fun `on the first of Shawwal nothing is Ramadan any more`() {
        val state = build("2027-03-09", "10:00")
        assertFalse(state.ramadanToday)
        assertFalse(state.ramadanOnNextDay)
    }

    @Test
    fun `turning the Ramadan card off hides the widget words too`() {
        val state = build("2027-02-22", "10:00", settings(cardOn = false))
        assertFalse(state.ramadanToday)
        assertFalse(state.ramadanOnNextDay)
    }

    @Test
    fun `the person's own Islamic date shift moves the widget words with the rest`() {
        // 8 Feb 2027 is day 1 on the default calendar and the eve once it is set back a day.
        val onTheDay = build("2027-02-08", "04:00", settings(offset = -1))
        assertFalse(onTheDay.ramadanToday)
        val evening = build("2027-02-08", "23:00", settings(offset = -1))
        assertTrue(evening.ramadanOnNextDay)
    }

    @Test
    fun `the word for a prayer is Suhoor ends for Fajr and Iftar for Maghrib and nothing else`() {
        val state = build("2027-02-22", "10:00")
        assertEquals(RamadanWord.SUHOOR_ENDS, state.ramadanWordFor(PrayerSlot.FAJR, true))
        assertEquals(RamadanWord.IFTAR, state.ramadanWordFor(PrayerSlot.MAGHRIB, true))
        for (slot in listOf(PrayerSlot.SUNRISE, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.ISHA)) {
            assertNull("$slot", state.ramadanWordFor(slot, true))
        }
        assertNull(state.ramadanWordFor(PrayerSlot.FAJR, false))
        assertNull(state.ramadanWordFor(PrayerSlot.MAGHRIB, false))
    }

    @Test
    fun `a projected polar day marks the next prayer approximate`() {
        val tromso = Coordinates(69.6492, 18.9553)
        val oslo = zone // a fixed zone is enough: only the date matters to the engine here
        val state = WidgetState.build(
            settings(where = tromso),
            LocalDateTime.parse("2027-06-21T12:00").atZone(oslo).toInstant(),
            oslo,
            true,
        )
        assertNotNull(state.next)
        assertTrue(state.nextIsApproximate)
    }

    @Test
    fun `an ordinary London day is not approximate`() {
        assertFalse(build("2027-02-22", "04:00").nextIsApproximate)
    }

    // --- the day's bars and the header date (hero countdown design, 18 Oct 2026) ---------------

    @Test
    fun `before Fajr the bars show Fajr next and the rest still to come`() {
        val state = build("2027-02-22", "04:00")
        assertEquals(listOf(Segment.NEXT, Segment.TODO, Segment.TODO, Segment.TODO, Segment.TODO), state.progress)
    }

    @Test
    fun `after Isha every bar is done until midnight`() {
        assertEquals(List(5) { Segment.DONE }, build("2027-02-22", "23:30").progress)
    }

    @Test
    fun `without a location there are no bars`() {
        assertTrue(build("2027-02-22", "04:00", settings(where = null)).progress.isEmpty())
    }

    /**
     * Every minute of a day: five bars, done ones first, then at most one next, then the rest, and the
     * next bar is exactly the highlighted row. So the bars can never disagree with the list beside them.
     */
    @Test
    fun `the bars run done then next then to come and agree with the highlight, at every minute of a day`() {
        var moment = at("2027-03-01", "00:00")
        val end = at("2027-03-02", "00:00")
        while (moment.isBefore(end)) {
            val state = WidgetState.build(settings(), moment, zone, true)
            val bars = state.progress
            assertEquals("$moment", 5, bars.size)
            assertEquals("$moment $bars", bars.sortedBy { it.ordinal }, bars)
            assertTrue("$moment $bars", bars.count { it == Segment.NEXT } <= 1)
            for ((i, slot) in WidgetState.PRAYERS.withIndex()) {
                assertEquals("$slot at $moment", state.isHighlighted(slot), bars[i] == Segment.NEXT)
            }
            moment = moment.plusSeconds(60)
        }
    }

    @Test
    fun `the header date is the calendar day, with the Hijri date and the person's shift, location or not`() {
        // 18 Oct 2026 is 7 Jumada al-Ula 1448 on the Umm al-Qura calendar.
        val plain = build("2026-10-18", "00:56", settings(where = null))
        assertEquals(java.time.LocalDate.of(2026, 10, 18), plain.date)
        assertEquals(Triple(7, 5, 1448), plain.hijri!!.let { Triple(it.day, it.month, it.year) })
        assertEquals(8, build("2026-10-18", "00:56", settings(offset = 1)).hijri!!.day)
    }

    @Test
    fun `the widget carries the saved place and shortens it to the town`() {
        val state = WidgetState.build(settings().copy(cityName = "Slough, United Kingdom"), at("2027-02-22", "04:00"), zone, true)
        assertEquals("Slough, United Kingdom", state.cityName)
        assertEquals("Slough", state.town)
        assertFalse(state.usingDefaultLocation)
        assertEquals("Makkah", state.copy(cityName = "Makkah").town)
        assertEquals("", state.copy(cityName = "").town)
        assertTrue(WidgetState.build(settings().copy(usingDefaultLocation = true), at("2027-02-22", "04:00"), zone, true).usingDefaultLocation)
    }
}
