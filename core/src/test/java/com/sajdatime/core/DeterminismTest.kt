package com.sajdatime.core

import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * adhan-java leaves the wall-clock millisecond of the *call* in the millisecond field of
 * every time it returns, so calling it twice with identical inputs yields instants a few
 * milliseconds apart. The engine truncates to the minute to undo that.
 *
 * Without this, a countdown recomputed each second would jitter and alarms would land at
 * an arbitrary point within their minute.
 */
class DeterminismTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val date = LocalDate.of(2026, 4, 10)
    private val prefs = CalculationPrefs(Sect.SUNNI, Madhab.SHAFII, CalcMethod.MUSLIM_WORLD_LEAGUE)

    @Test
    fun `repeated computation returns identical instants`() {
        val first = PrayerEngine.compute(london, date, prefs)
        Thread.sleep(5) // Guarantees the wall clock has moved on between calls.
        val second = PrayerEngine.compute(london, date, prefs)

        PrayerSlot.entries.forEach { slot ->
            assertEquals("$slot drifted between identical calls", first[slot], second[slot])
        }
    }

    @Test
    fun `computed times land exactly on a minute boundary`() {
        val times = PrayerEngine.compute(london, date, prefs)
        PrayerSlot.entries.forEach { slot ->
            val instant = times[slot]
            assertEquals(
                "$slot is not on a whole minute: $instant",
                0L,
                instant.toEpochMilli() % 60_000L,
            )
        }
    }

    @Test
    fun `madhabs sharing the standard asr rule produce the same time`() {
        fun asr(madhab: Madhab) = PrayerEngine.compute(
            london,
            date,
            CalculationPrefs(Sect.SUNNI, madhab, CalcMethod.MUSLIM_WORLD_LEAGUE),
        )[PrayerSlot.ASR]

        assertEquals(asr(Madhab.SHAFII), asr(Madhab.MALIKI))
        assertEquals(asr(Madhab.SHAFII), asr(Madhab.HANBALI))
    }
}
