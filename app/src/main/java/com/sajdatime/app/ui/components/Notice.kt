package com.sajdatime.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
 * Every notice in the app (Quiet Glass, 17 Oct 2026). One anatomy, so a person learns it
 * once and reads every notice the same way, in this order:
 *
 *  1. a round mark ([icon] on a soft tile in the tone's colour)
 *  2. the [title], if there is one
 *  3. one [brief] line: what is wrong, or what is happening
 *  4. [details], when given: the full explanation, folded until the card is tapped, with a
 *     chevron that says so and "expanded" or "collapsed" for a screen reader
 *  5. [footnote], when given: small print that must always be visible
 *  6. the action, when given: a bold link with a direction arrow, spoken as [actionSpokenAs]
 *  7. a close button, when [onDismiss] is given (Times only; Settings never hides a problem)
 *
 * Rules every caller keeps: the brief is one sentence; anything longer goes in details; the
 * action is a verb ("Allow", "See why", "Change location"), never "Tap here"; the card itself
 * never does the action, because opening and closing must never be the same gesture.
 *
 * Contrast: text is the tone's on-container ink (asserted in ColorContrastTest); the mark is
 * the tone's accent on a 14% tile of itself, 3:1 or better.
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
    // 24, the same as every other card: at 20 a notice between two 24dp cards read as a mistake.
    val shape = RoundedCornerShape(24.dp)
    var open by rememberSaveable(title, brief) { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, tween(200), label = "notice fold")
    val stateText = stringResource(if (open) R.string.state_expanded else R.string.state_collapsed)
    val hasAction = actionLabel != null && onAction != null

    Column(
        modifier
            .fillMaxWidth()
            .sajdaSurface(shape, container)
            .border(1.dp, accent.copy(alpha = 0.22f), shape)
            .padding(bottom = if (hasAction) 6.dp else 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (details != null) {
                        Modifier
                            .clickable(role = Role.Button) { open = !open }
                            .semantics { stateDescription = stateText }
                    } else {
                        Modifier
                    },
                )
                .padding(start = 14.dp, top = 14.dp, end = if (onDismiss != null) 4.dp else 14.dp),
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(50)).background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f).padding(top = if (title != null) 1.dp else 7.dp)) {
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = ink,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Text(brief, style = MaterialTheme.typography.bodyMedium, color = ink)
            }
            if (details != null) {
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = ink,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp).rotate(turn),
                )
            }
            if (onDismiss != null) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.notice_dismiss),
                    tint = ink,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(role = Role.Button, onClick = onDismiss)
                        .padding(12.dp),
                )
            }
        }
        if (details != null) {
            AnimatedVisibility(open) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ink,
                    modifier = Modifier.padding(start = NoticeTextStart, end = 16.dp, top = 6.dp),
                )
            }
        }
        if (footnote != null) {
            Text(
                text = footnote,
                style = MaterialTheme.typography.bodyMedium,
                color = ink,
                modifier = Modifier.padding(start = NoticeTextStart, end = 16.dp, top = 8.dp),
            )
        }
        if (hasAction) {
            NoticeAction(actionLabel!!, actionSpokenAs ?: actionLabel, ink, onAction!!)
        }
    }
}

/** Where a notice's text starts: 14 padding + 36 mark + 12 gap. */
private val NoticeTextStart = 62.dp

/**
 * A notice's action as a link: bold (labelLarge is SemiBold), in the notice's own ink, with a
 * direction arrow that mirrors in right to left. Its own 12dp padding lines its words up with
 * the text above; 48dp tall so it is honestly tappable.
 */
@Composable
private fun NoticeAction(label: String, spokenAs: String, ink: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(start = NoticeTextStart - 12.dp, top = 2.dp)
            .clip(RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = spokenAs }
            .heightIn(min = 48.dp)
            .padding(horizontal = 12.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = ink)
        Spacer(Modifier.width(6.dp))
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
    }
}
