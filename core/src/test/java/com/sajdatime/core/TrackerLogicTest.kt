package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The tracker's rules (TrackerLogic.kt), with the cases the 16 Oct plan asked for
 * (TEST-AND-RECORD.md 2.3, as changed by Ali's Pause decision): a pause that bridges, an open-ended
 * pause, a same-day resume, a start one day back, adjacent and overlapping ranges, a year boundary.
 */
class TrackerLogicTest {

    private fun d(s: String) = LocalDate.parse(s)
    private val all = DayRecord(TRACKED_SLOTS.toSet())
    private val fajrOnly = DayRecord(setOf(PrayerSlot.FAJR))
    private fun records(vararg pairs: Pair<String, DayRecord>) = pairs.associate { d(it.first) to it.second }
    private fun closed(a: String, b: String) = PauseRange(d(a), d(b))

    @Test fun `nothing recorded is zero`() =
        assertEquals(0, TrackerLogic.streak(emptyMap(), emptyList(), d("2027-02-20")))

    @Test fun `today complete only is one`() =
        assertEquals(1, TrackerLogic.streak(records("2027-02-20" to all), emptyList(), d("2027-02-20")))

    @Test fun `an incomplete today does not break yesterday's run`() =
        assertEquals(1, TrackerLogic.streak(records("2027-02-19" to all, "2027-02-20" to fajrOnly), emptyList(), d("2027-02-20")))

    @Test fun `three complete days before an empty today`() =
        assertEquals(3, TrackerLogic.streak(records("2027-02-17" to all, "2027-02-18" to all, "2027-02-19" to all), emptyList(), d("2027-02-20")))

    @Test fun `an incomplete day stops it`() =
        assertEquals(2, TrackerLogic.streak(records("2027-02-16" to all, "2027-02-17" to fajrOnly, "2027-02-18" to all, "2027-02-19" to all), emptyList(), d("2027-02-20")))

    @Test fun `a day with no record stops it`() =
        assertEquals(2, TrackerLogic.streak(records("2027-02-16" to all, "2027-02-18" to all, "2027-02-19" to all), emptyList(), d("2027-02-20")))

    @Test fun `a pause in the middle bridges the run`() {
        val r = records("2027-02-10" to all, "2027-02-11" to all, "2027-02-17" to all, "2027-02-18" to all, "2027-02-19" to all)
        assertEquals(5, TrackerLogic.streak(r, listOf(closed("2027-02-12", "2027-02-16")), d("2027-02-20")))
    }

    @Test fun `paused days neither add nor break, even with ticks stored on them`() {
        val r = records("2027-02-17" to all, "2027-02-18" to all, "2027-02-19" to all)
        assertEquals(2, TrackerLogic.streak(r, listOf(closed("2027-02-18", "2027-02-18")), d("2027-02-20")))
    }

    @Test fun `an open pause holds the streak where it was`() {
        val r = records("2027-02-15" to all, "2027-02-16" to all)
        assertEquals(2, TrackerLogic.streak(r, listOf(PauseRange(d("2027-02-17"), null)), d("2027-02-20")))
    }

    @Test fun `a long pause before the first record does not loop`() =
        assertEquals(1, TrackerLogic.streak(records("2027-02-20" to all), listOf(closed("2000-01-01", "2027-02-19")), d("2027-02-21")))

    @Test fun `only paused days is zero`() =
        assertEquals(0, TrackerLogic.streak(records("2027-02-18" to fajrOnly), listOf(closed("2027-02-18", "2027-02-19")), d("2027-02-20")))

    @Test fun `year boundary`() =
        assertEquals(3, TrackerLogic.streak(records("2026-12-30" to all, "2026-12-31" to all, "2027-01-01" to all), emptyList(), d("2027-01-02")))

    @Test fun `leap day`() =
        assertEquals(3, TrackerLogic.streak(records("2028-02-28" to all, "2028-02-29" to all, "2028-03-01" to all), emptyList(), d("2028-03-02")))

    @Test fun `a clock change day is only a date`() =
        assertEquals(2, TrackerLogic.streak(records("2027-03-27" to all, "2027-03-28" to all), emptyList(), d("2027-03-29")))

    @Test fun `pause from yesterday then resume today leaves yesterday paused and today tracked`() {
        val paused = TrackerLogic.pause(emptyList(), d("2027-02-19"))
        assertEquals(listOf(PauseRange(d("2027-02-19"), null)), paused)
        val resumed = TrackerLogic.resume(paused, d("2027-02-20"))
        assertEquals(listOf(closed("2027-02-19", "2027-02-19")), resumed)
        assertTrue(TrackerLogic.isPaused(d("2027-02-19"), resumed))
        assertFalse(TrackerLogic.isPaused(d("2027-02-20"), resumed))
    }

    @Test fun `a same day pause and resume leaves nothing`() {
        val paused = TrackerLogic.pause(emptyList(), d("2027-02-20"))
        assertEquals(emptyList<PauseRange>(), TrackerLogic.resume(paused, d("2027-02-20")))
    }

    @Test fun `pausing while paused changes nothing`() {
        val paused = listOf(PauseRange(d("2027-02-10"), null))
        assertEquals(paused, TrackerLogic.pause(paused, d("2027-02-20")))
    }

    @Test fun `resume with no pause changes nothing`() {
        val p = listOf(closed("2027-02-10", "2027-02-11"))
        assertEquals(p, TrackerLogic.resume(p, d("2027-02-20")))
    }

    @Test fun `a pause starting the day after one ended joins it`() {
        val p = TrackerLogic.pause(listOf(closed("2027-02-10", "2027-02-12")), d("2027-02-13"))
        assertEquals(listOf(PauseRange(d("2027-02-10"), null)), p)
    }

    @Test fun `overlapping and adjacent ranges are joined, separate ones kept`() {
        val p = TrackerLogic.normalise(
            listOf(closed("2027-03-01", "2027-03-02"), closed("2027-02-10", "2027-02-15"), closed("2027-02-14", "2027-02-20"), closed("2027-02-21", "2027-02-22")),
        )
        assertEquals(listOf(closed("2027-02-10", "2027-02-22"), closed("2027-03-01", "2027-03-02")), p)
    }

    @Test fun `tick states`() {
        assertEquals(TickState.NOT_DUE_YET, TrackerLogic.tickState(null, PrayerSlot.ISHA, due = false, paused = false))
        assertEquals(TickState.NOT_TICKED, TrackerLogic.tickState(null, PrayerSlot.ISHA, due = true, paused = false))
        assertEquals(TickState.TICKED, TrackerLogic.tickState(fajrOnly, PrayerSlot.FAJR, due = true, paused = false))
        assertEquals(TickState.PAUSED, TrackerLogic.tickState(fajrOnly, PrayerSlot.FAJR, due = true, paused = true))
    }

    @Test fun `every mask round trips and bits above four are ignored`() {
        for (mask in 0..31) assertEquals(mask, DayRecord.fromMask(mask).toMask())
        assertEquals(DayRecord.fromMask(31), DayRecord.fromMask(0b11_1111))
        assertTrue(DayRecord.fromMask(31).isComplete)
        assertFalse(DayRecord.fromMask(30).isComplete)
    }

    @Test fun `sunrise is never tracked`() = assertFalse(PrayerSlot.SUNRISE in TRACKED_SLOTS)

    @Test fun `history is thirty days, newest first, ending today`() {
        val h = TrackerLogic.historyDates(d("2027-03-01"))
        assertEquals(30, h.size)
        assertEquals(d("2027-03-01"), h.first())
        assertEquals(d("2027-01-31"), h.last())
    }

    @Test fun `pause codec round trips, keeps an open end, and drops what it cannot read`() {
        val p = listOf(closed("2027-02-08", "2027-02-12"), PauseRange(d("2027-03-01"), null))
        assertEquals("2027-02-08..2027-02-12,2027-03-01..", PauseCodec.encode(p))
        assertEquals(p, PauseCodec.decode(PauseCodec.encode(p)))
        assertEquals(listOf(closed("2027-02-08", "2027-02-12")), PauseCodec.decode("2027-02-08..2027-02-12,rubbish,2027-02-20..2027-02-01"))
        assertEquals(emptyList<PauseRange>(), PauseCodec.decode(null))
    }
}
