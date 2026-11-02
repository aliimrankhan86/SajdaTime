package com.sajdatime.app.ui.components

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Motion

/**
 * One prayer's bell at the end of its row on Times (1.6.0 step 3, STEP-3-DESIGN section 3): a 24dp icon in a
 * 48dp target, a switch like [Tick] with its own screen reader stop, "Dhuhr alert on, notification".
 *
 * The shape says the state, not the colour alone: a filled bell (notification), an alarm clock, a crossed
 * bell (off). [blocked] is the phone refusing notifications: the stored state is still drawn, in grey, and
 * spoken with the reason. [snap] skips the crossfade, for Undo, where the state was never meant to change.
 */
@Composable
fun Bell(
    on: Boolean,
    alarm: Boolean,
    blocked: Boolean,
    prayerName: String,
    onNext: Boolean,
    snap: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val spoken = stringResource(
        when {
            !on -> R.string.bell_off_a11y
            blocked -> R.string.bell_blocked_a11y
            alarm -> R.string.bell_alarm_a11y
            else -> R.string.bell_on_a11y
        },
        prayerName,
    )
    // Ponytail: one progress value, 0 for the on icon and 1 for the crossed bell, read inside graphicsLayer
    // so only drawing reruns. Quick both ways (section 6 row 4); Undo snaps.
    val target = if (on) 0f else 1f
    val progress = remember { Animatable(target) }
    LaunchedEffect(target, snap) {
        if (snap) progress.snapTo(target) else progress.animateTo(target, Motion.quick())
    }
    val onColour = when {
        blocked -> scheme.outline
        onNext -> scheme.onPrimaryContainer
        else -> scheme.primary
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            // Round, so the press ripple matches the round mark, as Material's IconButton does.
            .clip(CircleShape)
            .toggleable(value = on, role = Role.Switch) { now ->
                haptics.performHapticFeedback(if (now) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                onChange(now)
            }
            // toggleable already says on or off; this adds which prayer, and why a bell is grey.
            .semantics { contentDescription = spoken },
    ) {
        Icon(
            imageVector = if (alarm) Icons.Rounded.Alarm else Icons.Rounded.Notifications,
            contentDescription = null,
            tint = onColour,
            modifier = Modifier.size(24.dp).graphicsLayer { alpha = 1f - progress.value },
        )
        Icon(
            imageVector = Icons.Outlined.NotificationsOff,
            contentDescription = null,
            tint = scheme.outline,
            modifier = Modifier.size(24.dp).graphicsLayer { alpha = progress.value },
        )
    }
}
