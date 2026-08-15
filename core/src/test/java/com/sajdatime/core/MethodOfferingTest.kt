package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [CalcMethod.offeredTo] is the one definition of which convention belongs to which school.
 * The picker filters by it and both sect setters (phone and watch) discard a method the new
 * school was never offered by it — so if it drifts, a Sunni user can be left on a Shia
 * Maghrib or the reverse, silently. Pinned here so a new method cannot be added without
 * someone deciding which side of the line it sits on.
 */
class MethodOfferingTest {

    @Test
    fun `AUTO is offered to both schools`() {
        assertTrue(CalcMethod.AUTO.offeredTo(Sect.SUNNI))
        assertTrue(CalcMethod.AUTO.offeredTo(Sect.SHIA))
    }

    @Test
    fun `the two Shia conventions are offered to Shia only`() {
        for (m in listOf(CalcMethod.JAFARI, CalcMethod.TEHRAN)) {
            assertTrue(m.name, m.offeredTo(Sect.SHIA))
            assertFalse(m.name, m.offeredTo(Sect.SUNNI))
        }
    }

    @Test
    fun `every other method is offered to Sunni only`() {
        val sunni = CalcMethod.entries - setOf(CalcMethod.AUTO, CalcMethod.JAFARI, CalcMethod.TEHRAN)
        for (m in sunni) {
            assertTrue(m.name, m.offeredTo(Sect.SUNNI))
            assertFalse(m.name, m.offeredTo(Sect.SHIA))
        }
    }

    @Test
    fun `every method is offered to exactly one school, except AUTO`() {
        // A method offered to neither would be unreachable; one offered to both other than
        // AUTO would let a Sunni convention survive a switch to Shia.
        for (m in CalcMethod.entries) {
            val count = listOf(Sect.SUNNI, Sect.SHIA).count { m.offeredTo(it) }
            assertEquals(m.name, if (m == CalcMethod.AUTO) 2 else 1, count)
        }
    }
}
