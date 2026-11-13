package com.sajdatime.core

/**
 * A digital tasbeeh: a count, an optional target, and how many times the target has been reached
 * (Ali, 12 Nov 2026: "add a digital tasbeeh in the app. Which works properly").
 *
 * A counter and nothing more, on purpose. It shows no words of dhikr and does not say what to recite or
 * how many times: which words, in which order, and whether 33, 33 and 34 or another count, differ between
 * schools, and an app that printed one would be giving a ruling (CLAUDE.md, the disclaimer). The targets
 * are the round numbers people count to; the choice is the user's.
 *
 * Plain Kotlin with no Android import, so an iPhone version lifts it as it is.
 */
data class Tasbeeh(
    val count: Int = 0,
    val target: Int = DEFAULT_TARGET,
    val rounds: Int = 0,
) {
    /**
     * True on the tap that completes a round, and until the next tap. The screen buzzes on it. That same
     * tap adds the round to [rounds], so while this is true the round on screen is already counted.
     */
    val reached: Boolean get() = target > 0 && count == target

    /**
     * One bead. With a target, the tap after a full round starts the next round at 1, so the count on
     * screen is always "this many in this round". With no target it simply climbs.
     *
     * The round is added on the tap that reaches the target, not on the one after (Ali on his phone,
     * 12 Nov 2026). It was the tap after: while the count sat on the target the stored rounds were one
     * short and the screen added it back for show. Now what is stored is what is shown, on every tap.
     * There is no pause at the target: the next tap always counts.
     */
    fun tap(): Tasbeeh {
        val next = if (target > 0 && count >= target) 1 else (count + 1).coerceAtMost(MAX)
        val completes = target > 0 && next == target
        return copy(count = next, rounds = if (completes) (rounds + 1).coerceAtMost(MAX) else rounds)
    }

    /** Back to nothing counted. The target stays: it is a preference, not part of the count. */
    fun reset(): Tasbeeh = copy(count = 0, rounds = 0)

    /**
     * Counting to [newTarget] instead, starting again from nothing. A count of 60 means nothing against
     * a new target of 33, so changing the target is a fresh start rather than a sum to explain. Choosing
     * the target already in use changes nothing, so a stray tap on it cannot wipe a count. A target that
     * is not one of [TARGETS] is ignored.
     */
    fun withTarget(newTarget: Int): Tasbeeh =
        if (newTarget == target || newTarget !in TARGETS) this else Tasbeeh(count = 0, target = newTarget, rounds = 0)

    /**
     * How many beads in all since the last start, shown under the ring. On a full round the beads on
     * screen are already inside [rounds], so they are not added a second time.
     */
    val total: Long get() = when {
        target <= 0 -> count.toLong()
        reached -> rounds.toLong() * target
        else -> rounds.toLong() * target + count
    }

    companion object {
        const val DEFAULT_TARGET = 33

        /** 0 is "no target". */
        val TARGETS = listOf(33, 99, 100, 0)

        /** Far past any real use; it only stops a stored number growing without end. */
        const val MAX = 99_999

        /**
         * What was stored, made safe: a target that is not one of ours becomes the default, and a
         * count is brought inside its round, so a file edited or damaged outside the app still gives
         * numbers that count on cleanly from the next tap.
         */
        fun restore(count: Int, target: Int, rounds: Int): Tasbeeh {
            val safeTarget = if (target in TARGETS) target else DEFAULT_TARGET
            val top = if (safeTarget > 0) safeTarget else MAX
            val safeCount = count.coerceIn(0, top)
            // A full round on screen is always a counted round ([tap]), so it cannot sit beside zero rounds.
            val least = if (safeTarget > 0 && safeCount == safeTarget) 1 else 0
            return Tasbeeh(safeCount, safeTarget, rounds.coerceIn(least, MAX))
        }
    }
}
