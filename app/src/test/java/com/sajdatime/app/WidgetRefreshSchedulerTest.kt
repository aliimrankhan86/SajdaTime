package com.sajdatime.app

import com.sajdatime.app.widget.WidgetRefreshScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When the widgets next have to be redrawn. The rule is one line, the earlier of the next prayer and
 * local midnight plus two seconds, so these tests pin its edges: the two seconds, the midnight that
 * wins after Isha, a zone that skips midnight, and the property that matters most, that the answer is
 * always in the future so the alarm can never loop on itself.
 */
class WidgetRefreshSchedulerTest {

    private val london = ZoneId.of("Europe/London")
    private val slack = Duration.ofSeconds(2)

    private fun at(zone: ZoneId, text: String): Instant =
        LocalDateTime.parse(text).atZone(zone).toInstant()

    @Test
    fun `a prayer later today is the boundary and the redraw lands two seconds after it`() {
        val now = at(london, "2027-02-22T13:00:00")
        val asr = at(london, "2027-02-22T15:00:00")
        assertEquals(asr.plus(slack), WidgetRefreshScheduler.nextRefreshAt(asr, now, london))
    }

    // London 22 Feb 2027: sunrise about 07:00, Dhuhr about 12:15 (see WidgetStateTest). No prayer alarm fires at
    // sunrise, but the Next widget's sky changes there, so it is a redraw boundary (step 8).

    @Test
    fun `sunrise ahead of the next prayer is the boundary, so the dawn sky does not outlive it`() {
        val now = at(london, "2027-02-22T06:00:00")
        val dhuhr = at(london, "2027-02-22T12:15:00")
        val sunrise = at(london, "2027-02-22T07:00:00")
        assertEquals(sunrise.plus(slack), WidgetRefreshScheduler.nextRefreshAt(dhuhr, now, london, sunrise))
    }

    @Test
    fun `a sunrise that has passed is ignored`() {
        val now = at(london, "2027-02-22T13:00:00")
        val asr = at(london, "2027-02-22T15:00:00")
        val sunrise = at(london, "2027-02-22T07:00:00")
        assertEquals(asr.plus(slack), WidgetRefreshScheduler.nextRefreshAt(asr, now, london, sunrise))
    }

    @Test
    fun `no sunrise behaves as before`() {
        val now = at(london, "2027-02-22T06:00:00")
        val dhuhr = at(london, "2027-02-22T12:15:00")
        assertEquals(dhuhr.plus(slack), WidgetRefreshScheduler.nextRefreshAt(dhuhr, now, london, null))
        assertEquals(WidgetRefreshScheduler.nextRefreshAt(dhuhr, now, london), WidgetRefreshScheduler.nextRefreshAt(dhuhr, now, london, null))
    }

    @Test
    fun `after Isha the next prayer is tomorrow, so midnight wins and the day list turns over`() {
        val now = at(london, "2027-02-22T21:30:00")
        val tomorrowFajr = at(london, "2027-02-23T05:08:00")
        val midnight = at(london, "2027-02-23T00:00:00")
        assertEquals(midnight.plus(slack), WidgetRefreshScheduler.nextRefreshAt(tomorrowFajr, now, london))
    }

    @Test
    fun `a prayer exactly at midnight is the same boundary either way`() {
        val now = at(london, "2027-06-21T22:00:00")
        val midnight = at(london, "2027-06-22T00:00:00")
        assertEquals(midnight.plus(slack), WidgetRefreshScheduler.nextRefreshAt(midnight, now, london))
    }

    @Test
    fun `the last minute before midnight still waits for it`() {
        val now = at(london, "2027-02-22T23:59:30")
        val fajr = at(london, "2027-02-23T05:08:00")
        val midnight = at(london, "2027-02-23T00:00:00")
        assertEquals(midnight.plus(slack), WidgetRefreshScheduler.nextRefreshAt(fajr, now, london))
    }

    @Test
    fun `midnight is local, not UTC, so Auckland turns over at its own midnight`() {
        val auckland = ZoneId.of("Pacific/Auckland")
        val now = at(auckland, "2027-02-22T21:00:00")
        val fajr = at(auckland, "2027-02-23T05:30:00")
        val midnight = at(auckland, "2027-02-23T00:00:00")
        assertEquals(midnight.plus(slack), WidgetRefreshScheduler.nextRefreshAt(fajr, now, auckland))
    }

    @Test
    fun `in a zone that skips midnight the day starts at the first real instant and the answer is still in the future`() {
        // Havana's clocks go forward from 00:00 to 01:00 on 14 March 2027, so local midnight does not exist.
        val havana = ZoneId.of("America/Havana")
        val now = at(havana, "2027-03-13T22:00:00")
        val fajr = ZonedDateTime.of(LocalDate.of(2027, 3, 14).atTime(6, 0), havana).toInstant()
        val startOfDay = LocalDate.of(2027, 3, 14).atStartOfDay(havana).toInstant()
        val result = WidgetRefreshScheduler.nextRefreshAt(fajr, now, havana)
        assertEquals(startOfDay.plus(slack), result)
        assertTrue(result.isAfter(now))
    }

    @Test
    fun `the answer is always later than now across the year, in zones east and west and half hour offsets`() {
        val zones = listOf("Europe/London", "Asia/Karachi", "Asia/Kolkata", "Pacific/Auckland", "America/Los_Angeles", "Asia/Kathmandu")
        for (id in zones) {
            val zone = ZoneId.of(id)
            var now = Instant.parse("2027-01-01T00:00:00Z")
            repeat(24 * 365 / 3) {
                // The prayer is a few seconds to a day away, which covers both branches of the rule.
                for (gapSeconds in listOf(1L, 59L, 3_600L, 43_200L, 86_399L)) {
                    // A sunrise two hours out, as on a morning with the dawn sky up, joins the sweep.
                    val result = WidgetRefreshScheduler.nextRefreshAt(now.plusSeconds(gapSeconds), now, zone, now.plus(Duration.ofHours(2)))
                    assertTrue("$id $now +${gapSeconds}s gave $result", result.isAfter(now))
                    assertTrue("$id $now +${gapSeconds}s is more than a day out: $result", result.isBefore(now.plus(Duration.ofHours(26))))
                }
                now = now.plus(Duration.ofHours(3))
            }
        }
    }

    @Test
    fun `an instant whose prayer is a second away still gets a redraw after the prayer, not before it`() {
        val now = at(london, "2027-02-22T15:00:00")
        val asr = now.plusSeconds(1)
        val result = WidgetRefreshScheduler.nextRefreshAt(asr, now, london)
        assertTrue(result.isAfter(asr))
    }
}
