package com.sajdatime.core

import com.batoulapps.adhan.CalculationMethod as AdhanMethod
import com.batoulapps.adhan.Coordinates as AdhanCoordinates
import com.batoulapps.adhan.HighLatitudeRule
import com.batoulapps.adhan.Madhab as AdhanMadhab
import com.batoulapps.adhan.PrayerTimes as AdhanPrayerTimes
import com.batoulapps.adhan.data.DateComponents
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * The app used to crash outright above the polar circles.
 *
 * adhan returns null for **every** field once the sun stops rising or setting, and
 * [PrayerEngine] fed those straight into a non-null parameter. The result was a
 * NullPointerException that took down the home screen, the watch tile, the ongoing
 * notification and the alarm scheduler at once, in summer *and* in winter, for everyone
 * above roughly 65.5 degrees. Tromso, Kiruna, Rovaniemi, Murmansk, Norilsk and
 * Longyearbyen all have Muslim communities, so this was not hypothetical.
 *
 * Nothing here is about producing a *better* time. It is about never producing none.
 */
class PolarAndHemisphereTest {

    private val midsummer = LocalDate.of(2026, 6, 21)
    private val midwinter = LocalDate.of(2026, 12, 21)

    /** Places where adhan alone returns nothing at all. */
    private val polar = listOf(
        "Tromso" to Coordinates(69.65, 18.96),
        "Kiruna" to Coordinates(67.86, 20.23),
        "Murmansk" to Coordinates(68.97, 33.08),
        "Norilsk" to Coordinates(69.35, 88.20),
        "Longyearbyen" to Coordinates(78.22, 15.63),
        // The Southern Hemisphere has never had a user report, which is exactly why it is
        // tested: adhan's own Moonsighting safety clamp uses a raw latitude rather than an
        // absolute one, so the south is where a sign error would hide.
        "Antarctic 70S" to Coordinates(-70.0, 0.0),
        "Antarctic 80S" to Coordinates(-80.0, 140.0),
    )

    @Test
    fun `polar latitudes produce times instead of crashing`() {
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                for (method in CalcMethod.entries) {
                    val day = PrayerEngine.compute(at, date, CalculationPrefs(method = method))
                    assertEquals(
                        "$name $date $method should yield all six slots",
                        PrayerSlot.entries.size,
                        day.times.size,
                    )
                    assertTrue("$name $date $method must be flagged approximate", day.approximated)
                }
            }
        }
    }

    @Test
    fun `polar times stay in chronological order`() {
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                val ordered = PrayerEngine.compute(at, date, CalculationPrefs()).ordered
                val instants = ordered.map { it.second }
                assertEquals(
                    "$name $date must be ordered: ${ordered.map { "${it.first}=${it.second}" }}",
                    instants.sorted(),
                    instants,
                )
            }
        }
    }

    /** The scheduler and the watch tile call these, not compute, so they need their own check. */
    @Test
    fun `next and current prayer survive polar latitudes`() {
        val zone = ZoneId.of("Europe/Oslo")
        for ((name, at) in polar) {
            for (date in listOf(midsummer, midwinter)) {
                for (hour in listOf(0, 6, 12, 23)) {
                    val now = date.atTime(hour, 0).atZone(zone).toInstant()
                    val next = PrayerEngine.nextPrayer(at, CalculationPrefs(), now, zone)
                    assertTrue("$name $date ${hour}h next prayer must be ahead", next.at > now)
                    // Only asserting it does not throw; null is a legitimate answer.
                    PrayerEngine.currentPrayer(at, CalculationPrefs(), now, zone)
                }
            }
        }
    }

    /**
     * The projection must stay off for everybody else. Reykjavik at 64.15 and Lulea at
     * 65.58 are the closest inhabited places to the boundary and must keep their own
     * astronomy; measured, adhan starts returning nulls at 66 degrees in June and 68 in
     * December.
     */
    @Test
    fun `ordinary latitudes are never approximated`() {
        val ordinary = listOf(
            "Slough" to Coordinates(51.51, -0.59),
            "Reykjavik" to Coordinates(64.15, -21.94),
            "Lulea" to Coordinates(65.58, 22.15),
            "Jakarta" to Coordinates(-6.21, 106.85),
            "Melbourne" to Coordinates(-37.81, 144.96),
            "Ushuaia" to Coordinates(-54.80, -68.30),
        )
        for ((name, at) in ordinary) {
            for (date in listOf(midsummer, midwinter)) {
                assertFalse(
                    "$name $date must use its own latitude",
                    PrayerEngine.compute(at, date, CalculationPrefs()).approximated,
                )
            }
        }
    }

    /**
     * The option labelled for Indonesia, Malaysia and Singapore has to actually reproduce
     * what those countries publish, or the label is a lie. These figures are Aladhan's, for
     * Kemenag (method 20) in Indonesia and JAKIM (method 17) in Malaysia. If an adhan
     * upgrade moves them, the label needs revisiting rather than the numbers forcing.
     */
    @Test
    fun `southeast asia option matches Kemenag and JAKIM`() {
        val hm = DateTimeFormatter.ofPattern("HH:mm")
        val expected = listOf(
            Triple("Medan", Coordinates(3.59, 98.67) to "Asia/Jakarta", midsummer to ("04:53" to "19:53")),
            Triple("Surabaya", Coordinates(-7.25, 112.75) to "Asia/Jakarta", midsummer to ("04:16" to "18:37")),
            Triple("Kuching", Coordinates(1.55, 110.34) to "Asia/Kuching", midsummer to ("05:10" to "20:02")),
            Triple("Kota Bharu", Coordinates(6.13, 102.24) to "Asia/Kuala_Lumpur", midsummer to ("05:33" to "20:44")),
        )
        for ((name, place, want) in expected) {
            val (at, tz) = place
            val (date, times) = want
            val day = PrayerEngine.compute(at, date, CalculationPrefs(method = CalcMethod.SINGAPORE))
            val zone = ZoneId.of(tz)
            assertEquals("$name Fajr", times.first, day[PrayerSlot.FAJR].atZone(zone).format(hm))
            assertEquals("$name Isha", times.second, day[PrayerSlot.ISHA].atZone(zone).format(hm))
        }
    }

    /**
     * A canary, not a rule. adhan ignores [HighLatitudeRule] entirely when the method is
     * Moonsighting Committee — it substitutes its own seasonal model and, above 55 degrees,
     * its own one-seventh clamp. PrayerEngine sets the rule on every method regardless,
     * which is harmless precisely *because* of this. If an upgrade ever makes the rule bite
     * on Moonsighting, this test fails and the engine's comment needs rewriting before
     * anyone ships times that quietly moved by an hour.
     */
    @Test
    fun `high latitude rule is inert for Moonsighting`() {
        for (lat in listOf(51.51, 54.0, 56.0, 60.0)) {
            val results = HighLatitudeRule.entries.map { rule ->
                val p = AdhanMethod.MOON_SIGHTING_COMMITTEE.parameters.apply {
                    madhab = AdhanMadhab.SHAFI
                    highLatitudeRule = rule
                }
                val t = AdhanPrayerTimes(AdhanCoordinates(lat, 0.0), DateComponents(2026, 6, 21), p)
                // Minutes, not the raw Date. adhan rounds to the minute but leaves the
                // millisecond field holding the wall-clock time of the call, so two
                // identical computations are never equal as Dates — the same trap
                // PrayerEngine.instantOf exists to absorb.
                (t.fajr.time / 60_000) to (t.isha.time / 60_000)
            }
            assertEquals(
                "Moonsighting at $lat changed with the high latitude rule: $results",
                1,
                results.distinct().size,
            )
        }
    }
}
