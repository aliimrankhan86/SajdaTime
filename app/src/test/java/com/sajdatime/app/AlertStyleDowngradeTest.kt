package com.sajdatime.app

import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.effectiveAlertStyle
import com.sajdatime.app.ui.settings.hasApproximateDays
import com.sajdatime.core.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * The two reasons an alarm is allowed to arrive without making a noise.
 *
 * Both are deliberate, both are invisible in a screenshot, and both look like a bug to
 * anyone who meets them without knowing why. That is exactly the shape of decision that
 * gets "fixed" back out by a later session, so it is pinned here rather than only
 * described in a comment.
 */
class AlertStyleDowngradeTest {

    private fun style(
        style: AlertStyle = AlertStyle.ALARM,
        respectSilent: Boolean = true,
        approximate: Boolean = false,
        alarmOnApproximate: Boolean = false,
        silenced: Boolean = false,
    ) = effectiveAlertStyle(
        style = style,
        respectSilent = respectSilent,
        approximate = approximate,
        alarmOnApproximate = alarmOnApproximate,
        phoneIsSilenced = { silenced },
    )

    @Test
    fun `an ordinary alarm on an ordinary day still rings`() {
        assertEquals(AlertStyle.ALARM, style())
    }

    @Test
    fun `a projected time does not ring`() {
        assertEquals(AlertStyle.NOTIFICATION, style(approximate = true))
    }

    /**
     * The escape hatch, and the reason it has to exist. At Tromso every day from late May to
     * late July is projected, so without this a user who set a Fajr alarm would find it
     * silently stop working for two months a year — the app settling a religious question on
     * their behalf, which is refused everywhere else in this project.
     */
    @Test
    fun `a projected time rings when the user has asked for it`() {
        assertEquals(
            AlertStyle.ALARM,
            style(approximate = true, alarmOnApproximate = true),
        )
    }

    @Test
    fun `a silenced phone still suppresses the sound, override or not`() {
        assertEquals(AlertStyle.NOTIFICATION, style(silenced = true))
        assertEquals(
            "the approximate override is not a licence to ignore the ringer",
            AlertStyle.NOTIFICATION,
            style(approximate = true, alarmOnApproximate = true, silenced = true),
        )
    }

    @Test
    fun `turning off respect for silent brings the sound back`() {
        assertEquals(AlertStyle.ALARM, style(respectSilent = false, silenced = true))
    }

    /**
     * Nothing here may ever turn a notification *into* an alarm. Every path is a downgrade,
     * so a user who asked for quiet can never be shouted at by a code path they did not
     * choose.
     */
    @Test
    fun `notification style is never upgraded, whatever the flags`() {
        listOf(false, true).forEach { respectSilent ->
            listOf(false, true).forEach { approximate ->
                listOf(false, true).forEach { alarmOnApproximate ->
                    listOf(false, true).forEach { silenced ->
                        assertEquals(
                            AlertStyle.NOTIFICATION,
                            style(
                                style = AlertStyle.NOTIFICATION,
                                respectSilent = respectSilent,
                                approximate = approximate,
                                alarmOnApproximate = alarmOnApproximate,
                                silenced = silenced,
                            ),
                        )
                    }
                }
            }
        }
    }

    /**
     * The AudioManager read is skipped whenever a cheaper answer already settles it.
     *
     * Not a micro-optimisation: this runs on a BroadcastReceiver that has already been woken
     * from Doze and is about to reschedule a two-day horizon, and the projected-time case
     * would otherwise query the ringer to reach a conclusion it had already reached.
     */
    @Test
    fun `the ringer is not consulted when the answer is already known`() {
        var asked = false
        effectiveAlertStyle(
            style = AlertStyle.ALARM,
            respectSilent = true,
            approximate = true,
            alarmOnApproximate = false,
            phoneIsSilenced = { asked = true; false },
        )
        assertFalse("read the ringer for a decision it did not need", asked)
    }

    // --- who is offered the switch at all -------------------------------------------------

    /**
     * The switch is hidden from everyone it cannot help, and shown to everyone it can.
     *
     * Both halves matter and both are easy to get wrong in opposite directions. Show it too
     * widely and a billion users get a question about polar astronomy in the one dialog they
     * open to turn on a Fajr alarm. Show it too narrowly and the polar user who lost their
     * alarm has nowhere to go.
     *
     * The Southern Hemisphere is not an afterthought, and an `abs()` missing from a latitude
     * test is a bug this project has already found once (HANDOVER §11, A5) — hence McMurdo
     * on the shown side and Cape Town on the hidden one.
     *
     * **Ushuaia was written into the wrong bucket and the test said so.** It is the
     * southernmost city on earth and reads as an extreme, but at 54.8°S it is *below* the
     * Antarctic Circle, the sun rises and sets there every day of the year, and it needs
     * this switch no more than London does. Kept here on the hidden side precisely because
     * it is the case that looks like it should be on the other one.
     */
    @Test
    fun `the approximate-days switch reaches exactly the latitudes it applies to`() {
        val newYear = LocalDate.of(2026, 1, 1)
        mapOf(
            "Makkah" to (21.4225 to 39.8262),
            "Karachi" to (24.86 to 67.01),
            "Jakarta" to (-6.21 to 106.85),
            "London" to (51.5074 to -0.1278),
            "Cape Town" to (-33.92 to 18.42),
            "Ushuaia" to (-54.80 to -68.30),
        ).forEach { (place, at) ->
            assertFalse(
                "$place should never be offered the approximate-days switch",
                settingsAt(at.first, at.second).hasApproximateDays(newYear),
            )
        }
        mapOf(
            "Longyearbyen" to (78.22 to 15.65),
            "Tromso" to (69.65 to 18.96),
            "McMurdo Station" to (-77.85 to 166.67),
        ).forEach { (place, at) ->
            assertTrue(
                "$place has projected days and must be offered the switch",
                settingsAt(at.first, at.second).hasApproximateDays(newYear),
            )
        }
    }

    /**
     * The sweep must not depend on when in the year it is run. Started in January it has to
     * reach the following June; started in June, the following January. A shorter window
     * would show the switch to a Tromso user in May and hide it from them in October.
     */
    @Test
    fun `the sweep finds the polar season from any starting month`() {
        val tromso = settingsAt(69.65, 18.96)
        (1..12).forEach { month ->
            assertTrue(
                "started in month $month and missed the polar season",
                tromso.hasApproximateDays(LocalDate.of(2026, month, 1)),
            )
        }
    }

    private fun settingsAt(latitude: Double, longitude: Double) =
        AppSettings(coordinates = Coordinates(latitude, longitude))
}
