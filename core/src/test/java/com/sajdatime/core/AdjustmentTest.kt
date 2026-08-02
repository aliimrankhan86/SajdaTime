package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * The per-prayer corrections and the Hijri shift.
 *
 * The interesting tests here are not the round trips. They are [every path takes the
 * correction] and [ordering survives the worst legal pair of corrections] — one guards a
 * bug this feature nearly shipped with, the other answers a question that was raised in
 * review and would otherwise have been settled by reasoning rather than measurement.
 */
class AdjustmentTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val date = LocalDate.of(2026, 8, 2)

    // --- the codec ---------------------------------------------------------------------

    @Test
    fun `a correction survives the round trip`() {
        val original = mapOf(PrayerSlot.FAJR to 5, PrayerSlot.ISHA to -3)
        assertEquals(original, AdjustmentCodec.decode(AdjustmentCodec.encode(original)))
    }

    @Test
    fun `nothing encodes to nothing, and nothing decodes back`() {
        assertEquals("", AdjustmentCodec.encode(emptyMap()))
        assertEquals(emptyMap<PrayerSlot, Int>(), AdjustmentCodec.decode(""))
        assertEquals(emptyMap<PrayerSlot, Int>(), AdjustmentCodec.decode(null))
    }

    @Test
    fun `a zero is dropped rather than stored`() {
        assertEquals("", AdjustmentCodec.encode(mapOf(PrayerSlot.ASR to 0)))
        assertEquals(emptyMap<PrayerSlot, Int>(), AdjustmentCodec.decode("ASR:0"))
    }

    /**
     * The watch's sync listener has to be an exported component, so anything can address it.
     * A junk payload must degrade to "no correction", never to a thrown exception on a
     * background thread that would take the tile down with it.
     */
    @Test
    fun `rubbish decodes to nothing instead of throwing`() {
        listOf("FAJR", "FAJR:", ":5", "NOTAPRAYER:5", "FAJR:abc", ",,,", "FAJR:5:9")
            .forEach { assertEquals(it, emptyMap<PrayerSlot, Int>(), AdjustmentCodec.decode(it)) }
    }

    @Test
    fun `a value beyond the bound is clamped on the way out, not trusted`() {
        assertEquals(
            mapOf(PrayerSlot.FAJR to CalculationPrefs.MAX_ADJUSTMENT_MINUTES),
            AdjustmentCodec.decode("FAJR:9999"),
        )
        assertEquals(
            mapOf(PrayerSlot.ISHA to -CalculationPrefs.MAX_ADJUSTMENT_MINUTES),
            AdjustmentCodec.decode("ISHA:-9999"),
        )
    }

    // --- the engine --------------------------------------------------------------------

    @Test
    fun `a correction moves the prayer by exactly that many minutes`() {
        val plain = PrayerEngine.compute(london, date, CalculationPrefs())
        val nudged = PrayerEngine.compute(
            london,
            date,
            CalculationPrefs(adjustments = mapOf(PrayerSlot.FAJR to 7)),
        )
        assertEquals(
            7L,
            ChronoUnit.MINUTES.between(plain[PrayerSlot.FAJR], nudged[PrayerSlot.FAJR]),
        )
        // And only that prayer.
        assertEquals(plain[PrayerSlot.ASR], nudged[PrayerSlot.ASR])
    }

    /**
     * The bug this feature nearly shipped with.
     *
     * adhan has a `CalculationParameters.adjustments` field, and using it would have been
     * the obvious move. Three of this engine's six outputs never pass through it: the Shia
     * Maghrib is a second `PrayerTimes` built from probe parameters, the Umm al-Qura Ramadan
     * Isha is arithmetic done in this file, and a projected polar day is computed at a
     * borrowed latitude. Handing the offsets to adhan would have silently corrected some
     * prayers and not others.
     *
     * So this asserts the property that matters rather than the implementation: whichever
     * path produced the time, the correction reached it.
     */
    @Test
    fun `every path takes the correction, including the ones that bypass adhan`() {
        val cases = listOf(
            "Shia Maghrib probe" to Triple(
                london,
                date,
                CalculationPrefs(sect = Sect.SHIA, method = CalcMethod.JAFARI),
            ),
            "Umm al-Qura Ramadan Isha" to Triple(
                Coordinates(21.4225, 39.8262),
                // 2026-02-20 falls inside Ramadan 1447 on the Umm al-Qura calendar.
                LocalDate.of(2026, 2, 20),
                CalculationPrefs(method = CalcMethod.UMM_AL_QURA),
            ),
            "projected polar day" to Triple(
                Coordinates(78.22, 15.65),
                LocalDate.of(2026, 6, 21),
                CalculationPrefs(),
            ),
        )

        cases.forEach { (name, case) ->
            val (coordinates, day, prefs) = case
            val plain = PrayerEngine.compute(coordinates, day, prefs)
            PrayerSlot.entries.forEach { slot ->
                val nudged = PrayerEngine.compute(
                    coordinates,
                    day,
                    prefs.copy(adjustments = mapOf(slot to 11)),
                )
                assertEquals(
                    "$name: $slot did not take the correction",
                    11L,
                    ChronoUnit.MINUTES.between(plain[slot], nudged[slot]),
                )
            }
        }
    }

    /**
     * A day can never come out of the engine running backwards, however the user sets it.
     *
     * This test was written expecting to pass trivially and it did not, which is the reason
     * it is worth keeping. At 59.9 degrees on 1 January the sweep produced Dhuhr 12:35 and
     * Asr 12:31: midwinter squeezes the two to twenty-six minutes apart, so the legal pair
     * of corrections (+30, -30) crosses them. The bound was assumed to be sufficient and
     * was not. `PrayerEngine.adjustedBy` now holds the order, and this is the test that
     * says so.
     *
     * Widening [CalculationPrefs.MAX_ADJUSTMENT_MINUTES] will not break this. Removing the
     * clamp will.
     */
    @Test
    fun `the worst legal pair of corrections cannot invert a day`() {
        val bound = CalculationPrefs.MAX_ADJUSTMENT_MINUTES
        val slots = PrayerSlot.entries
        var checked = 0

        for (latitude in listOf(0.0, 21.4, 51.5, 55.0, 58.0, 59.9, -33.9, -45.0)) {
            val coordinates = Coordinates(latitude, 0.0)
            for (dayOfYear in 1..365 step 7) {
                val day = LocalDate.ofYearDay(2026, dayOfYear)
                // The cruellest legal setting: push each prayer as late as allowed and the
                // one after it as early as allowed, everywhere at once.
                val worst = slots.mapIndexed { i, slot ->
                    slot to if (i % 2 == 0) bound else -bound
                }.toMap()
                val times = PrayerEngine.compute(coordinates, day, CalculationPrefs(adjustments = worst))
                    .ordered
                    .map { it.second }
                assertTrue(
                    "inverted at $latitude on $day: $times",
                    times == times.sorted(),
                )
                checked++
            }
        }
        // Guards against the loop silently doing nothing, which is how a green test lies.
        assertTrue("swept nothing", checked > 400)
    }

    // --- the Hijri shift ---------------------------------------------------------------

    @Test
    fun `the Hijri shift moves Ramadan, so the Umm al-Qura Isha rule moves with it`() {
        val makkah = Coordinates(21.4225, 39.8262)
        // The day before Ramadan begins on the Umm al-Qura calendar. Shifting the calendar
        // forward a day brings the 120-minute Ramadan Isha into effect early, which is
        // exactly what a user following an earlier local sighting is asking for.
        val eve = LocalDate.of(2026, 2, 17)
        val prefs = CalculationPrefs(method = CalcMethod.UMM_AL_QURA)

        val plain = PrayerEngine.compute(makkah, eve, prefs)[PrayerSlot.ISHA]
        val shifted = PrayerEngine.compute(makkah, eve, prefs.copy(hijriOffsetDays = 2))[PrayerSlot.ISHA]

        assertNotEquals(
            "the Hijri shift did not reach the Ramadan Isha rule",
            plain,
            shifted,
        )
        assertEquals(30L, ChronoUnit.MINUTES.between(plain, shifted))
    }

    @Test
    fun `a stored Hijri offset beyond the bound is clamped`() {
        assertEquals(
            CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            CalculationPrefs(hijriOffsetDays = 99).hijriOffset,
        )
        assertEquals(
            -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            CalculationPrefs(hijriOffsetDays = -99).hijriOffset,
        )
    }

    @Test
    fun `no corrections means byte-identical output, so the common case is untouched`() {
        val plain = PrayerEngine.compute(london, date, CalculationPrefs())
        val empty = PrayerEngine.compute(london, date, CalculationPrefs(adjustments = emptyMap()))
        assertEquals(plain.times, empty.times)
    }
}
