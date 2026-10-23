package com.sajdatime.app.ui.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.sajdaSurface

/**
 * A system permission that can only be granted on a system screen ("Alarms & reminders",
 * "Do Not Disturb access"). The answer is re-read every time the app comes back to the
 * foreground, because the user grants it elsewhere and returns, and a card that still said
 * "Allow" after they had allowed it would be wrong.
 */
@Composable
fun rememberGranted(check: (Context) -> Boolean): State<Boolean> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(check(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { state.value = check(context) }
    return state
}

/**
 * A real button until the permission is granted, then a plain "Allowed" line with a tick.
 * The button is 52dp tall at least and announces itself as a button; the allowed line is not
 * clickable (there is nothing left to do) and says "Allowed" as its state, so TalkBack does
 * not rely on the tick glyph alone.
 */
@Composable
fun PermissionButton(
    label: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    amber: Boolean = false,
    allowedColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val scheme = MaterialTheme.colorScheme
    if (granted) {
        val allowed = stringResource(R.string.permission_allowed)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .heightIn(min = 48.dp)
                .semantics(mergeDescendants = true) { stateDescription = allowed },
        ) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = allowedColor)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$label · $allowed",
                style = MaterialTheme.typography.bodyLarge,
                color = allowedColor,
            )
        }
    } else {
        // 52dp and 16sp, the same as a standalone CtaButton (DESIGN_SYSTEM 4.1).
        Button(
            onClick = onClick,
            modifier = modifier.heightIn(min = 52.dp),
            // Amber cards use the accent pair that ColorContrastTest already checks.
            colors = if (amber) {
                ButtonDefaults.buttonColors(containerColor = scheme.tertiary, contentColor = scheme.onTertiary)
            } else {
                ctaButtonColors()
            },
        ) {
            Text(label, style = actionLabelStyle())
        }
    }
}

/**
 * A permission the system is withholding, as a [Notice] (warning tone), turning into a calm
 * "Allowed" line once granted. [onDismiss] is for Times only; Settings never hides a problem.
 * The action opens the phone's own settings screen. Redesigned 17 Oct 2026: see [Notice].
 */
@Composable
fun PermissionCard(
    title: String,
    body: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    details: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    // Granted: nothing. A permanent "Allowed" card at the top of Settings is the furniture
    // DESIGN_SYSTEM §4 forbids; the user did the right thing and needs no badge for it
    // (research F21, 17 Oct 2026). Onboarding keeps its "Allowed" line via PermissionButton.
    if (granted) return
    Notice(
        title = title,
        brief = body,
        details = details,
        actionLabel = stringResource(R.string.action_allow),
        actionSpokenAs = title,
        onAction = onClick,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}
