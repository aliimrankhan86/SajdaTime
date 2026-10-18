package com.sajdatime.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * The one spacing scale (owner, 18 Oct 2026: "the gaps are uneven and I want it to look
 * structured", asked many times). Every gap between two pieces of text, or between text and
 * its button, is one of these, chosen by how closely the two belong together, never by eye:
 *
 *  - [Tight] 4: a title and the line that belongs to it (notice title and brief).
 *  - [Related] 8: a paragraph and the button that acts on it; a button and the one beside it.
 *  - [Paragraph] 12: one paragraph and the next, inside the same block.
 *  - [Group] 16: one group (a paragraph with its button) and the next; card padding.
 *  - [Section] 24: one section and the next on a screen.
 *
 * Closer means more related (proximity), so a reader sees which button belongs to which
 * paragraph without reading. docs/DESIGN_SYSTEM.md section 5 has the rule and its reasons.
 */
object Space {
    val Tight = 4.dp
    val Related = 8.dp
    val Paragraph = 12.dp
    val Group = 16.dp
    val Section = 24.dp
}
