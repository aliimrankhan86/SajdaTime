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
    /** True on the tap that completes a round, and until the next tap. The screen buzzes on it. */
    val reached: Boolean get() = target > 0 && count == target

    /**
     * One bead. With a target, the tap after a full round starts the next round at 1, so the count on
     * screen is always "this many in this round". With no target it simply climbs.
     */
    fun tap(): Tasbeeh = when {
        target > 0 && count >= target -> copy(count = 1, rounds = (rounds + 1).coerceAtMost(MAX))
        else -> copy(count = (count + 1).coerceAtMost(MAX))
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

    /** How many beads in all, for the spoken summary. */
    val total: Long get() = if (target > 0) rounds.toLong() * target + count else count.toLong()

    companion object {
        const val DEFAULT_TARGET = 33

        /** 0 is "no target". */
        val TARGETS = listOf(33, 99, 100, 0)

        /** Far past any real use; it only stops a stored number growing without end. */
        const val MAX = 99_999

        /**
         * What was stored, made safe: a target that is not one of ours becomes the default, and a
         * count is brought inside its round. A file edited or damaged outside the app cannot put the
         * counter in a state [tap] never produces.
         */
        fun restore(count: Int, target: Int, rounds: Int): Tasbeeh {
            val safeTarget = if (target in TARGETS) target else DEFAULT_TARGET
            val top = if (safeTarget > 0) safeTarget else MAX
            return Tasbeeh(count.coerceIn(0, top), safeTarget, rounds.coerceIn(0, MAX))
        }
    }
}
