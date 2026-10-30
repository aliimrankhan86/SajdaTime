package com.sajdatime.app.ui.components

import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.text.modifiers.TextAutoSizeLayoutScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em

/**
 * Shrinks a short label just enough that no word is split across two lines.
 *
 * Android breaks a word that is wider than its line. In the two fixed width slots that hold
 * one or two words, the tab bar and the theme selector, that split words at large text: on a
 * 320dp wide phone at 2.0 "Settings" became "Setting" over "s" and "Follow phone" lost a letter
 * to each line; at 2.0 on an ordinary phone Indonesian "Pengaturan" and Turkish "Telefona uy"
 * did the same (emulator, 19 Oct 2026). Wrapping between words is fine and stays. Only a break
 * inside a word makes the size step down, a tenth at a time, to no less than 60% of [max] and
 * never below the label's size at the normal text size, so at the size people normally use
 * nothing changes at all. Pass the style's own size as [max], and the style through
 * [scaledLineHeight] so its line spacing shrinks with it.
 */
data class WholeWords(private val max: TextUnit) : TextAutoSize {
    override fun TextAutoSizeLayoutScope.getFontSize(constraints: Constraints, text: AnnotatedString): TextUnit {
        // Never below the size it has at the phone's normal text size (DESIGN_SYSTEM §3), and never
        // above max: at a Small text size 1 / fontScale is over 1, and the label grew (review, 19 Oct 2026).
        val floor = max * maxOf(0.6f, minOf(1f, 1f / fontScale))
        var size = max
        while (size > floor) {
            val layout = performLayout(constraints, text, size)
            if (!splitsAWord(text, (0 until layout.lineCount - 1).map { layout.getLineEnd(it) })) return size
            size *= 0.9f
        }
        return floor
    }
}

/**
 * For text held to one line, where a second line or a clipped end would break the layout (the city
 * name in the header, the Qibla place line, the My prayers column headings): the size steps down until
 * it fits, and only past the floor does it end in an ellipsis. Owner, 30 Oct 2026, adding Bengali:
 * text that would overlap or break the design is made smaller until it fits. The floor is 60 percent
 * of [max], or, at large text sizes, the size the text has at normal text size when that is smaller
 * (at 2.0, half). So it can go below the 14sp floor (60 percent of a 14sp label is 8.4sp): that is
 * the owner's rule for this one case, recorded in DESIGN_SYSTEM.md §9. Text that fits never shrinks,
 * in any language; a long English town name can shrink too. Pass the style's own size as [max].
 */
@Composable
fun shrinkToFit(max: TextUnit): TextAutoSize =
    TextAutoSize.StepBased(minFontSize = max * minOf(0.6f, 1f / LocalDensity.current.fontScale), maxFontSize = max)

/**
 * The same style with its line height as a share of its font size, for text that [WholeWords]
 * sizes: auto size changes only the font size, so a fixed 18sp line under a shrunk label left
 * wide gaps between its two lines (independent review, 19 Oct 2026). The same at full size.
 */
fun TextStyle.scaledLineHeight(): TextStyle =
    if (lineHeight.isSp && fontSize.isSp) copy(lineHeight = (lineHeight.value / fontSize.value).em) else this

/** True when a line ends between two letters or digits, that is, inside a word. */
internal fun splitsAWord(text: CharSequence, lineEnds: List<Int>): Boolean =
    lineEnds.any { end -> end in 1 until text.length && text[end - 1].isLetterOrDigit() && text[end].isLetterOrDigit() }
