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
 * The amber "the system is withholding something" card with the action as its own button
 * (not a whole-card tap), turning into a calm "Allowed" card once granted. [onDismiss] is for
 * Times only; Settings never offers a close button.
 */
@Composable
fun PermissionCard(
    title: String,
    body: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val container = if (granted) scheme.secondaryContainer else scheme.tertiaryContainer
    val content = if (granted) scheme.onSecondaryContainer else scheme.onTertiaryContainer
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sajdaSurface(shape, container)
            .border(1.dp, (if (granted) scheme.primary else scheme.tertiary).copy(alpha = 0.35f), shape)
            .padding(16.dp),
    ) {
        if (!granted) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = scheme.tertiary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = content)
                    Text(body, style = MaterialTheme.typography.bodyMedium, color = content)
                }
                if (onDismiss != null) {
                    // 48dp, separate from the action button: one gesture opens the system
                    // screen, the other closes the card, and they must never be the same tap.
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.notice_dismiss),
                        tint = content,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(role = Role.Button, onClick = onDismiss)
                            .padding(12.dp),
                    )
                }
            }
        Spacer(Modifier.height(12.dp))
        }
        PermissionButton(
            label = title,
            granted = granted,
            onClick = onClick,
            amber = true,
            allowedColor = content,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
