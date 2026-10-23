package com.sajdatime.app.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * The one set of animation durations (Fable, 23 Oct 2026), the way [Space] is the one set of gaps.
 * A call site picks by what moves, never by mood, and never writes a number:
 *
 *  - [QUICK] 150: an icon or arrow swaps or turns (the bell, the fold chevrons).
 *  - [STANDARD] 250: a shape, colour, position or block of text changes in place (the tick, the
 *    theme thumb, the pause row, Now and Next).
 *  - [DRAW] 400: a drawing runs across the screen (rings, sky, day path). Nothing is longer.
 *
 * An exit uses the token one step shorter than its enter; the call site passes that itself.
 * BUILD-INSTRUCTIONS section 6 and docs/DESIGN_SYSTEM.md section 2 (Motion) have the rule and its
 * reasons. DesignRulesTest bans a written tween outside this file (the Qibla dial's 120 ms
 * follows the compass and is the one exception).
 */
object Motion {
    const val QUICK = 150
    const val STANDARD = 250
    const val DRAW = 400

    fun <T> quick() = tween<T>(QUICK, easing = FastOutSlowInEasing)
    fun <T> standard() = tween<T>(STANDARD, easing = FastOutSlowInEasing)
    fun <T> draw() = tween<T>(DRAW, easing = FastOutSlowInEasing)
}
