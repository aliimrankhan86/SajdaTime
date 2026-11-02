package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Jama'ah times (JamaahTimes.kt). The case that matters most is the guard: a jama'ah time is never
 * shown before its prayer begins or after the next one begins, because showing one earlier than the
 * adhan would tell someone to pray before the prayer is due.
 */
class JamaahPlanTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val zone = ZoneId.of("Europe/London")
    private val prefs = CalculationPrefs()
    private fun day(date: String, p: CalculationPrefs = prefs) = PrayerEngine.compute(london, LocalDate.parse(date), p)
    private fun t(s: String) = LocalTime.parse(s)
    private fun shownAt(r: JamaahResult?) = (r as JamaahResult.InWindow).at

    // Thursday 4 Feb 2027: Dhuhr about 12:20, Asr about 14:40 in London.
    private val thursday = day("2027-02-04")
    private val friday = day("2027-02-05")

    @Test fun `minutes after counts from the start time`() {
        val plan = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.After(15))))
        assertEquals(thursday[PrayerSlot.ASR].plus(Duration.ofMinutes(15)), shownAt(plan[PrayerSlot.ASR]))
    }

    @Test fun `minutes after counts from the adjusted start the user sees`() {
        val adjusted = day("2027-02-04", prefs.copy(adjustments = mapOf(PrayerSlot.ASR to 5)))
        val plan = JamaahPlan.on(adjusted, zone, JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.After(10))))
        assertEquals(thursday[PrayerSlot.ASR].plus(Duration.ofMinutes(15)), shownAt(plan[PrayerSlot.ASR]))
    }

    @Test fun `a fixed time inside the window is shown on that day's clock`() {
        val plan = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.DHUHR to JamaahRule.Fixed(t("13:30")))))
        assertEquals(Instant.parse("2027-02-04T13:30:00Z"), shownAt(plan[PrayerSlot.DHUHR]))
    }

    @Test fun `a fixed time before the prayer begins is never shown`() {
        val plan = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.Fixed(t("13:00")))))
        assertEquals(JamaahResult.Before, plan[PrayerSlot.ASR])
    }

    @Test fun `a fixed time after the next prayer begins is never shown`() {
        val plan = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.DHUHR to JamaahRule.Fixed(t("16:00")))))
        assertEquals(JamaahResult.After(PrayerSlot.ASR), plan[PrayerSlot.DHUHR])
    }

    @Test fun `Fajr's window ends at sunrise`() {
        val afterSunrise = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.FAJR to JamaahRule.Fixed(t("09:00")))))
        assertEquals(JamaahResult.After(PrayerSlot.SUNRISE), afterSunrise[PrayerSlot.FAJR])
        val tooLate = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.FAJR to JamaahRule.After(60))))
        val fajrToSunrise = Duration.between(thursday[PrayerSlot.FAJR], thursday[PrayerSlot.SUNRISE]).toMinutes()
        assertTrue(fajrToSunrise > 60)
        assertTrue(tooLate[PrayerSlot.FAJR] is JamaahResult.InWindow)
    }

    @Test fun `Maghrib's window ends at Isha, so a long gap is caught`() {
        val gap = Duration.between(thursday[PrayerSlot.MAGHRIB], thursday[PrayerSlot.ISHA]).toMinutes()
        assertTrue("Maghrib to Isha is $gap minutes", gap > 60)
        val atIsha = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.MAGHRIB to JamaahRule.Fixed(t("23:00")))))
        assertEquals(JamaahResult.After(PrayerSlot.ISHA), atIsha[PrayerSlot.MAGHRIB])
    }

    @Test fun `on Friday Dhuhr's line is Jumu'ah, and never falls back to Dhuhr's own jama'ah`() {
        val rules = mapOf(PrayerSlot.DHUHR to JamaahRule.After(10))
        val none = JamaahPlan.on(friday, zone, JamaahTimes(rules))
        assertFalse(PrayerSlot.DHUHR in none)
        val withJumuah = JamaahPlan.on(friday, zone, JamaahTimes(rules, jumuah = t("13:15")))
        val r = withJumuah[PrayerSlot.DHUHR] as JamaahResult.InWindow
        assertTrue(r.isJumuah)
        assertEquals(Instant.parse("2027-02-05T13:15:00Z"), r.at)
        // Thursday is unaffected by a Jumu'ah time.
        val thu = JamaahPlan.on(thursday, zone, JamaahTimes(rules, jumuah = t("13:15")))
        assertFalse((thu[PrayerSlot.DHUHR] as JamaahResult.InWindow).isJumuah)
    }

    @Test fun `Jumu'ah must fall between Dhuhr and Asr`() {
        val early = JamaahPlan.on(friday, zone, JamaahTimes(jumuah = t("11:00")))
        assertEquals(JamaahResult.Before, early[PrayerSlot.DHUHR])
        val late = JamaahPlan.on(friday, zone, JamaahTimes(jumuah = t("16:30")))
        assertEquals(JamaahResult.After(PrayerSlot.ASR), late[PrayerSlot.DHUHR])
    }

    @Test fun `an Isha time that is not before the next Fajr is After Fajr`() {
        // 05:30 typed for Isha reads as the small hours after this night (before noon); in June London's Fajr is earlier.
        val plan = JamaahPlan.on(day("2027-06-20"), zone, JamaahTimes(mapOf(PrayerSlot.ISHA to JamaahRule.Fixed(t("05:30")))))
        assertEquals(JamaahResult.After(PrayerSlot.FAJR), plan[PrayerSlot.ISHA])
    }

    @Test fun `Fajr after sunrise names sunrise as the slot it passed`() {
        val plan = JamaahPlan.on(thursday, zone, JamaahTimes(mapOf(PrayerSlot.FAJR to JamaahRule.Fixed(t("09:00")))))
        assertEquals(PrayerSlot.SUNRISE, (plan[PrayerSlot.FAJR] as JamaahResult.After).next)
    }

    // --- the time zone guard -----------------------------------------------------------

    @Test fun `London rules in Dubai are hidden`() {
        val rules = mapOf(PrayerSlot.DHUHR to JamaahRule.Fixed(t("13:30")))
        val stored = JamaahTimes(rules, zoneId = "Europe/London")
        assertTrue(JamaahPlan.on(thursday, ZoneId.of("Asia/Dubai"), stored).isEmpty())
        assertEquals(1, JamaahPlan.on(thursday, zone, stored).size)
        // No zone stored (v1): shown, whatever the zone.
        assertEquals(1, JamaahPlan.on(thursday, ZoneId.of("Asia/Dubai"), JamaahTimes(rules)).size)
    }

    @Test fun `visibleIn compares zone ids and never offsets`() {
        val london = JamaahTimes(zoneId = "Europe/London")
        assertTrue(london.visibleIn("Europe/London"))
        assertFalse(london.visibleIn("Asia/Dubai"))
        // Lisbon and London share an offset all year, but they are different zones.
        assertFalse(london.visibleIn("Europe/Lisbon"))
        assertTrue(JamaahTimes().visibleIn("Asia/Dubai"))
    }

    @Test fun `a fixed time keeps its clock reading across the spring clock change`() {
        // 28 Mar 2027 is the UK change to summer time: 13:30 local is 12:30 UTC.
        val dst = day("2027-03-28")
        val plan = JamaahPlan.on(dst, zone, JamaahTimes(mapOf(PrayerSlot.DHUHR to JamaahRule.Fixed(t("13:30")))))
        assertEquals(Instant.parse("2027-03-28T12:30:00Z"), shownAt(plan[PrayerSlot.DHUHR]))
    }

    @Test fun `an Isha jama'ah after midnight belongs to that night`() {
        val summer = day("2027-06-20")
        val plan = JamaahPlan.on(summer, zone, JamaahTimes(mapOf(PrayerSlot.ISHA to JamaahRule.Fixed(t("00:15")))))
        assertEquals(Instant.parse("2027-06-20T23:15:00Z"), shownAt(plan[PrayerSlot.ISHA]))
    }

    @Test fun `prayers with no rule are absent`() =
        assertEquals(emptyMap<PrayerSlot, JamaahResult>(), JamaahPlan.on(thursday, zone, JamaahTimes()))

    @Test fun `far north approximated days still give an answer, not a crash`() {
        val tromso = PrayerEngine.compute(Coordinates(69.65, 18.96), LocalDate.parse("2027-06-21"), prefs)
        val all = JamaahTimes(TRACKED_SLOTS.associateWith { JamaahRule.After(10) })
        assertEquals(5, JamaahPlan.on(tromso, ZoneId.of("Europe/Oslo"), all).size)
    }

    // --- the codec ---------------------------------------------------------------------

    @Test fun `codec round trips every kind of rule and Jumu'ah`() {
        val j = JamaahTimes(
            mapOf(PrayerSlot.FAJR to JamaahRule.After(15), PrayerSlot.DHUHR to JamaahRule.Fixed(t("13:30")), PrayerSlot.ISHA to JamaahRule.After(0)),
            jumuah = t("13:15"),
        )
        assertEquals("FAJR=+15,DHUHR=13:30,ISHA=+0,JUMUAH=13:15", JamaahCodec.encode(j))
        assertEquals(j, JamaahCodec.decode(JamaahCodec.encode(j)))
    }

    @Test fun `codec drops what it cannot read and never invents a time`() {
        assertEquals(
            JamaahTimes(mapOf(PrayerSlot.ASR to JamaahRule.After(10))),
            JamaahCodec.decode("ASR=+10,FAJR=+7,DHUHR=25:00,SUNRISE=+5,BOGUS,MAGHRIB=+999,JUMUAH=noon"),
        )
        assertTrue(JamaahCodec.decode(null).isEmpty)
        assertTrue(JamaahCodec.decode("").isEmpty)
    }

    @Test fun `codec version 2 writes the zone and reads it back`() {
        val j = JamaahTimes(mapOf(PrayerSlot.DHUHR to JamaahRule.Fixed(t("13:30"))), jumuah = t("13:15"), zoneId = "Europe/London")
        assertEquals("DHUHR=13:30,JUMUAH=13:15,ZONE=Europe/London", JamaahCodec.encode(j))
        assertEquals(j, JamaahCodec.decode(JamaahCodec.encode(j)))
    }

    @Test fun `codec version 1 has no zone and reads as null`() {
        val j = JamaahCodec.decode("DHUHR=13:30,JUMUAH=13:15")
        assertEquals(null, j.zoneId)
        assertEquals(2, j.count)
    }

    @Test fun `a damaged zone is dropped and the rules survive`() {
        val j = JamaahCodec.decode("DHUHR=13:30,ZONE=Not/AZone")
        assertEquals(null, j.zoneId)
        assertEquals(JamaahRule.Fixed(t("13:30")), j.rules[PrayerSlot.DHUHR])
        assertTrue(j.visibleIn("Asia/Dubai"))
    }
}
