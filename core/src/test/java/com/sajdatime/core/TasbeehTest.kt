package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TasbeehTest {

    private fun Tasbeeh.taps(n: Int): Tasbeeh = (1..n).fold(this) { t, _ -> t.tap() }

    @Test
    fun `it starts at nothing with a target of 33`() {
        assertEquals(Tasbeeh(0, 33, 0), Tasbeeh())
        assertFalse(Tasbeeh().reached)
    }

    @Test
    fun `each tap counts one`() {
        assertEquals(1, Tasbeeh().tap().count)
        assertEquals(32, Tasbeeh().taps(32).count)
        assertFalse(Tasbeeh().taps(32).reached)
    }

    @Test
    fun `the 33rd tap reaches the target and adds the round on that tap`() {
        val full = Tasbeeh().taps(33)
        assertEquals(33, full.count)
        assertEquals(1, full.rounds)
        assertEquals(33L, full.total)
        assertTrue(full.reached)
    }

    @Test
    fun `the tap after a full round starts the next round at 1`() {
        val next = Tasbeeh().taps(34)
        assertEquals(1, next.count)
        assertEquals(1, next.rounds)
        assertEquals(34L, next.total)
        assertFalse(next.reached)
    }

    @Test
    fun `the total goes up by one on every tap, through every round`() {
        for (target in Tasbeeh.TARGETS) {
            var t = Tasbeeh(target = target)
            for (beads in 1..350) {
                t = t.tap()
                assertEquals("target $target after $beads", beads.toLong(), t.total)
                if (target > 0) assertEquals("target $target after $beads", beads / target, t.rounds)
            }
        }
    }

    @Test
    fun `three rounds of 33 are 99 beads and the target is reached exactly three times`() {
        var t = Tasbeeh()
        var reached = 0
        repeat(99) {
            t = t.tap()
            if (t.reached) reached++
        }
        assertEquals(3, reached)
        assertEquals(33, t.count)
        assertEquals(3, t.rounds)
        assertEquals(99L, t.total)
    }

    @Test
    fun `every target is reached on exactly its own number`() {
        for (target in Tasbeeh.TARGETS.filter { it > 0 }) {
            val start = Tasbeeh(target = target)
            assertFalse(start.taps(target - 1).reached)
            assertTrue(start.taps(target).reached)
            assertEquals(target.toLong(), start.taps(target).total)
        }
    }

    @Test
    fun `with no target it climbs and is never reached`() {
        val t = Tasbeeh(target = 0).taps(250)
        assertEquals(250, t.count)
        assertEquals(0, t.rounds)
        assertFalse(t.reached)
        assertEquals(250L, t.total)
    }

    @Test
    fun `reset clears the count and the rounds and keeps the target`() {
        assertEquals(Tasbeeh(0, 99, 0), Tasbeeh(target = 99).taps(150).reset())
    }

    @Test
    fun `choosing another target starts again from nothing`() {
        val counting = Tasbeeh().taps(40)
        for (target in listOf(99, 100, 0)) {
            assertEquals(Tasbeeh(0, target, 0), counting.withTarget(target))
        }
    }

    @Test
    fun `choosing the target already in use keeps the count`() {
        val counting = Tasbeeh().taps(40)
        assertEquals(counting, counting.withTarget(33))
    }

    @Test
    fun `a target that is not offered is ignored`() {
        val counting = Tasbeeh().taps(5)
        assertEquals(counting, counting.withTarget(7))
        assertEquals(counting, counting.withTarget(-1))
    }

    @Test
    fun `the count stops at the cap and does not overflow`() {
        val t = Tasbeeh(count = Tasbeeh.MAX, target = 0).tap()
        assertEquals(Tasbeeh.MAX, t.count)
        val r = Tasbeeh(count = 32, target = 33, rounds = Tasbeeh.MAX).tap()
        assertEquals(33, r.count)
        assertEquals(Tasbeeh.MAX, r.rounds)
    }

    @Test
    fun `a stored state is made safe before it is used`() {
        assertEquals(Tasbeeh(0, 33, 0), Tasbeeh.restore(-5, 7, -1))
        assertEquals(Tasbeeh(33, 33, 2), Tasbeeh.restore(500, 33, 2))
        assertEquals(Tasbeeh(12, 99, 1), Tasbeeh.restore(12, 99, 1))
        // A full round is a counted round: it cannot come back beside zero rounds.
        assertEquals(Tasbeeh(33, 33, 1), Tasbeeh.restore(33, 33, 0))
        assertEquals(Tasbeeh(Tasbeeh.MAX, 0, 0), Tasbeeh.restore(Int.MAX_VALUE, 0, 0))
        // And what restore returns always taps cleanly.
        assertEquals(1, Tasbeeh.restore(500, 33, 2).tap().count)
    }
}
