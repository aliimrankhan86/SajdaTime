package com.sajdatime.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sajdatime.app.ui.RamadanState
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Eid
import com.sajdatime.core.Coordinates
import com.sajdatime.core.HijriCalendar
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * The calendar and the card's numbers, on a real Android runtime.
 *
 * The Islamic calendar comes from `java.time.chrono.HijrahChronology`, which on Android below 26 is
 * desugared and carries its own copy of the Umm al-Qura table (minSdk is 24). The unit tests run on a
 * JVM with the JDK's table, so this is the check that the device's table gives the same months. If it
 * ever does not, the Ramadan dates would be a day out on those phones only, which no JVM test could see.
 * RAMADAN-LOCATION-SPEC.md section 3 states the assumption.
 *
 * The second half repeats the London numbers of RamadanStateTest on the device, so the engine, the time
 * zone data and the card arithmetic agree with the JVM run to the minute.
 */
@RunWith(AndroidJUnit4::class)
class RamadanDeviceTest {

    @Test
    fun the_device_calendar_starts_ramadan_1448_on_8_february_2027_and_1447_on_18_february_2026() {
        val first = HijriCalendar.ramadan(LocalDate.of(2027, 2, 8), 0)
        assertNotNull(first)
        assertEquals(1448, first!!.hijriYear)
        assertEquals(LocalDate.of(2027, 2, 8), first.firstDay)
        assertEquals(LocalDate.of(2027, 3, 8), first.lastDay)
        assertEquals(29, first.length)
        assertEquals("1 Ramadan 1448", 1, first.dayNumber)

        assertEquals("the last day of Ramadan 1448", 29, HijriCalendar.ramadan(LocalDate.of(2027, 3, 8), 0)!!.dayNumber)
        assertFalse("1 Shawwal is not Ramadan", HijriCalendar.isRamadan(LocalDate.of(2027, 3, 9), 0))
        assertEquals("1 Shawwal 1448", 10, HijriCalendar.of(LocalDate.of(2027, 3, 9), 0)!!.month)

        val previous = HijriCalendar.ramadan(LocalDate.of(2026, 2, 18), 0)
        assertNotNull(previous)
        assertEquals(1447, previous!!.hijriYear)
        assertEquals(LocalDate.of(2026, 2, 18), previous.firstDay)
        assertEquals(30, previous.length)
        assertEquals(1, previous.dayNumber)
    }

    @Test
    fun a_shift_of_the_islamic_date_moves_the_start_by_that_many_days() {
        // A plus one shift means the user's calendar is a day ahead, so Ramadan starts a day earlier.
        assertEquals(1, HijriCalendar.ramadan(LocalDate.of(2027, 2, 7), 1)!!.dayNumber)
        assertEquals(LocalDate.of(2027, 2, 7), HijriCalendar.ramadan(LocalDate.of(2027, 2, 7), 1)!!.firstDay)
        assertEquals(1, HijriCalendar.ramadan(LocalDate.of(2027, 2, 9), -1)!!.dayNumber)
        assertEquals(LocalDate.of(2027, 2, 9), HijriCalendar.ramadan(LocalDate.of(2027, 2, 9), -1)!!.firstDay)
    }

    @Test
    fun the_eids_of_2027_fall_where_the_unit_tests_say() {
        assertEquals(Eid.FITR, HijriCalendar.eidOn(LocalDate.of(2027, 3, 9), 0)?.eid)
        assertEquals(Eid.ADHA, HijriCalendar.eidOn(LocalDate.of(2027, 5, 16), 0)?.eid)
    }

    @Test
    fun the_card_numbers_for_london_on_22_february_2027_match_the_jvm_tests() {
        val london = Coordinates(51.5074, -0.1278)
        val zone = ZoneId.of("Europe/London")
        val prefs = CalculationPrefs(method = CalcMethod.MUSLIM_WORLD_LEAGUE)
        val day = LocalDate.of(2027, 2, 22)
        val state = RamadanState.build(london, prefs, PrayerEngine.compute(london, day, prefs))!!

        assertEquals(15, state.info.dayNumber)
        assertEquals(Duration.ofMinutes(12 * 60 + 18), state.fastLength)
        assertEquals(4L, state.fastChangeMinutes)
        assertEquals(LocalDateTime.parse("2027-02-22T05:10").atZone(zone).toInstant(), state.suhoorEnds)
        assertEquals(LocalDateTime.parse("2027-02-22T17:28").atZone(zone).toInstant(), state.iftar)
        assertEquals(LocalDateTime.parse("2027-02-23T05:08").atZone(zone).toInstant(), state.tomorrowSuhoorEnds)
        assertEquals(PrayerEngine.compute(london, day, prefs)[PrayerSlot.FAJR], state.suhoorEnds)
    }
}
