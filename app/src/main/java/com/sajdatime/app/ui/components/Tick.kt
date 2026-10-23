package com.sajdatime.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.TickState

/**
 * One prayer's tick in "My prayers" (DESIGN_SYSTEM §4): a 24dp mark in a 48dp target.
 *
 * An unticked prayer is a plain outline circle in every case, never red, never a badge, never
 * counted: the app records, it does not judge (Ali, 16 Oct 2026). A prayer whose time has not come
 * is the same circle faded and cannot be ticked. Its own screen reader stop, "Fajr, prayed", because
 * it sits inside a row that is otherwise read as one.
 *
 * [label] is what it is spoken as before the state: the prayer, or on the history sheet the date and
 * the prayer.
 */
@Composable
fun Tick(
    state: TickState,
    label: String,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    /** A record from a newer version of the app: shown, never changed. */
    readOnly: Boolean = false,
) {
    val scheme = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val ticked = state == TickState.TICKED
    val enabled = !readOnly && (state == TickState.TICKED || state == TickState.NOT_TICKED)
    val spoken = stringResource(
        R.string.tracker_tick_a11y,
        label,
        stringResource(
            when (state) {
                TickState.TICKED -> R.string.tracker_state_prayed
                TickState.NOT_DUE_YET -> R.string.tracker_state_not_due
                else -> R.string.tracker_state_not_ticked
            },
        ),
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .toggleable(value = ticked, enabled = enabled, role = Role.Checkbox) { on ->
                haptics.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                onChange(on)
            }
            // toggleable already says checked or not, and disabled; this adds which prayer.
            .semantics { contentDescription = spoken },
    ) {
        Icon(
            imageVector = if (ticked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (ticked) scheme.primary else scheme.outline,
            modifier = Modifier.size(24.dp).alpha(if (state == TickState.NOT_DUE_YET) 0.38f else 1f),
        )
    }
}
