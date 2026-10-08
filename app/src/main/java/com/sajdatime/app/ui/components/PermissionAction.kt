package com.sajdatime.app.ui.components

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
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
 * The button is 48dp tall at least and announces itself as a button; the allowed line is not
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
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            // Amber cards use the accent pair that ColorContrastTest already checks.
            colors = if (amber) {
                ButtonDefaults.buttonColors(containerColor = scheme.tertiary, contentColor = scheme.onTertiary)
            } else {
                ButtonDefaults.buttonColors()
            },
        ) {
            Text(label)
        }
    }
}

/**
 * A permission that can only be granted on a system screen: the amber [NoticeCard] with its
 * own button until it is granted, then a calm "Allowed" card with a tick.
 *
 * The not-granted card is the shared [NoticeCard], so there is one amber card in the app and
 * one rule for it (the action is a button, never the whole card; see docs/DESIGN_SYSTEM.md,
 * "The action is a button, never the card"). The button says what happens next, not the
 * title again, which is why [actionLabel] defaults to "Allow in Settings" and the title stays
 * "Allow exact alarms". [onDismiss] is for Times only; Settings never offers a close button.
 */
@Composable
fun PermissionCard(
    title: String,
    body: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = stringResource(R.string.action_allow_in_settings),
    onDismiss: (() -> Unit)? = null,
) {
    if (!granted) {
        NoticeCard(
            title = title,
            body = body,
            modifier = modifier,
            actionLabel = actionLabel,
            onAction = onClick,
            onDismiss = onDismiss,
        )
        return
    }
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sajdaSurface(shape, scheme.secondaryContainer)
            .border(1.dp, scheme.primary.copy(alpha = 0.35f), shape)
            .padding(16.dp),
    ) {
        PermissionButton(
            label = title,
            granted = true,
            onClick = onClick,
            allowedColor = scheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
