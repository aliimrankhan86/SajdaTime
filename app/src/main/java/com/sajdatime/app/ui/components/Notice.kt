package com.sajdatime.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import com.sajdatime.app.ui.theme.Space
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.sajdaSurface

/**
 * The two kinds of notice the app has, and the only two.
 *
 *  - [WARNING], amber: something is wrong or withheld (exact alarms, Do Not Disturb, no
 *    location, approximate times, a compass that needs calibrating). Amber, not red: the app
 *    still works, and an error colour would say it is broken.
 *  - [QUIET], soft green: information or an occasion (Ramadan is near, Eid Mubarak).
 */
enum class NoticeTone { WARNING, QUIET }

/**
 * Every notice in the app (Quiet Glass, 17 Oct 2026; restructured 18 Oct 2026). One anatomy,
 * so a person learns it once and reads every notice the same way:
 *
 *  1. a round mark ([icon] on a soft tile in the tone's colour)
 *  2. the [title], if there is one, then [brief] 4dp under it (they belong together)
 *  3. a close X alone at the top right, when [onDismiss] is given (Times only; Settings never
 *     hides a problem)
 *  4. [footnote], when given: small print that must always be visible
 *  5. [details], when given, folded until "Details" is pressed
 *  6. one row of buttons, 16dp under the text: the action as a solid button in the tone's
 *     colour, then "Details" with a down chevron when there are details
 *
 * Why this shape (owner, 18 Oct 2026, from the emulator): the fold chevron used to sit right
 * beside the close X, two icon buttons that do opposite things a finger's width apart, and the
 * action was a bold link the owner could not see as a button. Every reference consulted agrees:
 * a close control is the last trailing element and stands alone; "show more" is a labelled
 * button with a down chevron; the main action is a filled button. The card itself is never
 * clickable, so opening, folding and closing are three different targets.
 *
 * Rules every caller keeps: the brief is one sentence; anything longer goes in details; the
 * action is a verb ("Allow", "See why", "Change location"), never "Tap here".
 *
 * Contrast: text is the tone's on-container ink; both buttons (the action and "Details") are
 * solid, tertiary/onTertiary in a warning and the CtaButton green in a quiet notice (all
 * asserted in ColorContrastTest); the mark is the tone's
 * accent on a 14% tile of itself, 3:1 or better.
 */
@Composable
fun Notice(
    brief: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    tone: NoticeTone = NoticeTone.WARNING,
    icon: ImageVector = Icons.Rounded.WarningAmber,
    details: String? = null,
    footnote: String? = null,
    actionLabel: String? = null,
    actionSpokenAs: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, ink, accent) = when (tone) {
        NoticeTone.WARNING -> Triple(scheme.tertiaryContainer, scheme.onTertiaryContainer, scheme.tertiary)
        NoticeTone.QUIET -> Triple(scheme.secondaryContainer, scheme.onSecondaryContainer, scheme.onSecondaryContainer)
    }
    val buttonColors = when (tone) {
        NoticeTone.WARNING -> ButtonDefaults.buttonColors(containerColor = scheme.tertiary, contentColor = scheme.onTertiary)
        NoticeTone.QUIET -> ctaButtonColors()
    }
    // 24, the same as every other card: at 20 a notice between two 24dp cards read as a mistake.
    val shape = RoundedCornerShape(24.dp)
    var open by rememberSaveable(title, brief) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(200), label = "notice fold")
    val hasAction = actionLabel != null && onAction != null

    Column(
        modifier
            .fillMaxWidth()
            .sajdaSurface(shape, container)
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(Space.Group),
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.size(MarkSize).clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(Space.Paragraph))
            // The text column's first line is centred on the 36dp mark: 7dp down for a 23sp title
            // line, 8dp for a 21sp brief line.
            Column(Modifier.weight(1f).padding(top = if (title != null) 6.dp else 8.dp)) {
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = ink,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.height(Space.Tight))
                }
                Text(brief, style = MaterialTheme.typography.bodyMedium, color = ink)
                if (footnote != null) {
                    Spacer(Modifier.height(Space.Related))
                    Text(footnote, style = MaterialTheme.typography.bodyMedium, color = ink)
                }
                if (details != null) {
                    AnimatedVisibility(open) {
                        Text(
                            text = details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ink,
                            modifier = Modifier.padding(top = Space.Paragraph),
                        )
                    }
                }
            }
            if (onDismiss != null) {
                // Alone in the corner. The offset draws it into the card's padding so the glyph
                // lines up with the 16dp margin; the text column still keeps clear of its 48dp.
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.offset(x = 12.dp, y = (-12).dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.notice_dismiss),
                        tint = ink,
                    )
                }
            }
        }
        if (hasAction || details != null) {
            Spacer(Modifier.height(Space.Group))
            // FlowRow, not Row: "Hide details" beside a long action label in Urdu, Arabic or at 2x
            // text does not fit one line, and a Row would squeeze the second button. It wraps
            // under instead, Space.Related below (independent review, 18 Oct 2026).
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.Related),
                verticalArrangement = Arrangement.spacedBy(Space.Related),
                modifier = Modifier.padding(start = MarkSize + Space.Paragraph),
            ) {
                if (hasAction) {
                    val spoken = actionSpokenAs ?: actionLabel!!
                    Button(
                        onClick = onAction!!,
                        colors = buttonColors,
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .semantics(mergeDescendants = true) { contentDescription = spoken },
                    ) {
                        Text(actionLabel!!)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
                if (details != null) {
                    val stateText = stringResource(if (open) R.string.state_expanded else R.string.state_collapsed)
                    // Solid, in the notice's own button colour, like the action beside it: the
                    // owner chose solid buttons for every action (18 Oct 2026).
                    Button(
                        onClick = { open = !open },
                        colors = buttonColors,
                        modifier = Modifier
                            .heightIn(min = 44.dp)
                            .semantics { stateDescription = stateText },
                    ) {
                        Text(stringResource(if (open) R.string.notice_hide_details else R.string.notice_show_details))
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp).rotate(turn),
                        )
                    }
                }
            }
        }
    }
}

/** The notice's round mark. The text and the button row both start after it. */
private val MarkSize = 36.dp
