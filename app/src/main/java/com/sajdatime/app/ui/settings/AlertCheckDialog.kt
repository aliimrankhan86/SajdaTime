package com.sajdatime.app.ui.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.sajdatime.app.R
import com.sajdatime.app.notify.AlertCheck
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.components.PermissionButton
import com.sajdatime.app.ui.theme.Space

/**
 * The check, read when first shown and again whenever the app comes back to the foreground, so a
 * fix made on the phone's own screen shows as soon as the person returns (same pattern as
 * rememberGranted, which only holds one Boolean).
 */
@Composable
internal fun rememberAlertCheck(usesAlarm: Boolean): AlertCheck {
    val context = LocalContext.current
    var check by remember(usesAlarm) { mutableStateOf(AlertCheck.read(context, usesAlarm)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { check = AlertCheck.read(context, usesAlarm) }
    return check
}

/**
 * "Alert check": one block per thing that can stop an alert arriving. Each says where it stands in
 * words and a glyph (never colour alone); a failing one carries a solid button to the exact system
 * screen. Closes with the vendor-neutral advice and a button to the app's own settings page.
 * iPhone: Android only as built. A notification and Focus check would carry over; exact alarms and battery do not exist there.
 */
@Composable
internal fun AlertCheckDialog(check: AlertCheck, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ChooserDialog(title = stringResource(R.string.alert_check_title), onDismiss = onDismiss) {
        CheckBlock(
            ok = check.notifications,
            okText = stringResource(R.string.alert_check_notifications_ok),
            failText = stringResource(R.string.alert_check_notifications_fail),
            button = stringResource(R.string.alert_check_notifications_button),
            onFix = { Notifications.openAppNotificationSettings(context) },
        )
        check.exactAlarms?.let {
            CheckBlock(
                ok = it,
                okText = stringResource(R.string.alert_check_exact_ok),
                failText = stringResource(R.string.alert_check_exact_fail),
                button = stringResource(R.string.alert_check_exact_button),
                onFix = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
            )
        }
        CheckBlock(
            ok = check.battery,
            okText = stringResource(R.string.alert_check_battery_ok),
            failText = stringResource(R.string.alert_check_battery_fail),
            button = stringResource(R.string.alert_check_app_settings),
            onFix = { AlertCheck.openAppSettings(context) },
        )
        check.dnd?.let {
            CheckBlock(
                ok = it,
                okText = stringResource(R.string.alert_check_dnd_ok),
                failText = stringResource(R.string.alert_check_dnd_fail),
                button = stringResource(R.string.action_allow),
                onFix = { Notifications.openDndAccessSettings(context) },
            )
        }
        Text(
            text = stringResource(R.string.alert_check_background),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // A failing battery check already carries this same button, so it is not shown twice.
        if (check.battery) {
            Spacer(Modifier.height(Space.Related))
            CtaButton(onClick = { AlertCheck.openAppSettings(context) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.alert_check_app_settings))
            }
        }
    }
}

@Composable
private fun CheckBlock(ok: Boolean, okText: String, failText: String, button: String, onFix: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (ok) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
            contentDescription = null, // the words beside it say the same
            tint = if (ok) scheme.primary else scheme.tertiary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(Space.Related))
        Text(
            text = if (ok) okText else failText,
            style = MaterialTheme.typography.bodyLarge,
            color = scheme.onSurface,
        )
    }
    if (!ok) {
        Spacer(Modifier.height(Space.Related))
        PermissionButton(label = button, granted = false, onClick = onFix, modifier = Modifier.fillMaxWidth())
    }
    Spacer(Modifier.height(Space.Section))
}
