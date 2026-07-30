package com.sajdatime.app.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.net.toUri
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.onboarding.madhabLabel

@Composable
fun SettingsScreen(
    state: UiState,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetNotify: (PrayerSlot, Boolean) -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onSetAlertStyle: (AlertStyle) -> Unit,
    onPickAlarmSound: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
) {
    val context = LocalContext.current
    var methodPicker by remember { mutableStateOf(false) }
    var locationSheet by remember { mutableStateOf(false) }
    var disclaimer by remember { mutableStateOf(false) }
    val settings = state.settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 40.dp),
    ) {
        Text(
            text = stringResource(R.string.title_settings),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .padding(start = 20.dp, top = 8.dp, bottom = 4.dp)
                .semantics { heading() },
        )

        Group(stringResource(R.string.settings_group_school)) {
            Column(Modifier.selectableGroup()) {
                Sect.entries.forEach { sect ->
                    RadioRow(
                        label = stringResource(
                            if (sect == Sect.SUNNI) R.string.sect_sunni else R.string.sect_shia,
                        ),
                        selected = settings.sect == sect,
                        onSelect = { onSetSect(sect) },
                    )
                }
            }
        }

        // Madhab only changes the Asr rule, which the Jafari school fixes anyway, so it
        // is hidden entirely for Shia users rather than shown disabled.
        if (settings.sect == Sect.SUNNI) {
            Group(stringResource(R.string.settings_group_madhab)) {
                Column(Modifier.selectableGroup()) {
                    Madhab.entries.forEach { madhab ->
                        RadioRow(
                            label = madhabLabel(madhab),
                            supporting = stringResource(
                                if (madhab == Madhab.HANAFI) R.string.madhab_hanafi_desc
                                else R.string.madhab_standard_desc,
                            ),
                            selected = settings.madhab == madhab,
                            onSelect = { onSetMadhab(madhab) },
                        )
                    }
                }
            }
        }

        Group(stringResource(R.string.settings_group_method)) {
            SettingRow(
                title = stringResource(R.string.settings_method),
                subtitle = if (settings.method == CalcMethod.AUTO) {
                    stringResource(
                        R.string.settings_method_auto,
                        PrayerEngine.resolveMethod(settings.calculationPrefs).label,
                    )
                } else {
                    settings.method.label
                },
                onClick = { methodPicker = true },
            )
        }

        Group(stringResource(R.string.settings_group_alerts)) {
            if (!PrayerAlarmScheduler.canScheduleExact(context)) {
                WarningRow(
                    title = stringResource(R.string.settings_exact_alarms_title),
                    body = stringResource(R.string.settings_exact_alarms_desc),
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            runCatching {
                                context.startActivity(
                                    Intent(SystemSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .setData("package:${context.packageName}".toUri()),
                                )
                            }
                        }
                    },
                )
            }

            Column(Modifier.selectableGroup()) {
                RadioRow(
                    label = stringResource(R.string.alert_style_notification),
                    supporting = stringResource(R.string.alert_style_notification_desc),
                    selected = settings.alertStyle == AlertStyle.NOTIFICATION,
                    onSelect = { onSetAlertStyle(AlertStyle.NOTIFICATION) },
                )
                RadioRow(
                    label = stringResource(R.string.alert_style_alarm),
                    supporting = stringResource(R.string.alert_style_alarm_desc),
                    selected = settings.alertStyle == AlertStyle.ALARM,
                    onSelect = { onSetAlertStyle(AlertStyle.ALARM) },
                )
            }

            // Sound and Do Not Disturb only matter in alarm mode, so they appear only
            // once it is chosen rather than sitting there greyed out.
            if (settings.alertStyle == AlertStyle.ALARM) {
                SettingRow(
                    title = stringResource(R.string.settings_alarm_sound),
                    // Naming the chosen tone is the only confirmation the user gets that
                    // the pick stuck. The generic hint stays until something is chosen.
                    subtitle = rememberRingtoneTitle(settings.alarmSoundUri)
                        ?: stringResource(R.string.settings_alarm_sound_desc),
                    onClick = onPickAlarmSound,
                )
                if (!Notifications.hasDndAccess(context)) {
                    WarningRow(
                        title = stringResource(R.string.settings_dnd_title),
                        body = stringResource(R.string.settings_dnd_desc),
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(SystemSettings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                                )
                            }
                        },
                    )
                }
            }
        }

        Group(stringResource(R.string.settings_group_notifications)) {
            PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
                SwitchRow(
                    title = slot.label,
                    checked = slot in settings.notifyFor,
                    onCheckedChange = { onSetNotify(slot, it) },
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = 20.dp))
            SwitchRow(
                title = stringResource(R.string.settings_ongoing_badge),
                subtitle = stringResource(R.string.settings_ongoing_badge_desc),
                checked = settings.ongoingBadge,
                onCheckedChange = onSetOngoingBadge,
            )
        }

        Group(stringResource(R.string.settings_group_location)) {
            SettingRow(
                title = stringResource(R.string.action_change_location),
                subtitle = settings.cityName.ifBlank {
                    stringResource(R.string.location_set_generic)
                },
                onClick = { locationSheet = true },
            )
        }

        Group(stringResource(R.string.settings_group_about)) {
            SettingRow(
                title = stringResource(R.string.about_version),
                subtitle = versionName(context),
            )
            SettingRow(
                title = stringResource(R.string.about_disclaimer),
                subtitle = stringResource(R.string.about_disclaimer_short),
                onClick = { disclaimer = true },
            )
            // Play requires a privacy policy reachable from inside the app, not only from
            // the Console field. Opening the hosted policy satisfies that.
            val privacyUrl = stringResource(R.string.privacy_policy_url)
            SettingRow(
                title = stringResource(R.string.about_privacy),
                subtitle = stringResource(R.string.about_privacy_desc),
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, privacyUrl.toUri()))
                    }
                },
            )
            SettingRow(
                title = stringResource(R.string.about_charity),
                subtitle = stringResource(R.string.about_charity_desc),
            )
            SettingRow(
                title = stringResource(R.string.about_credits),
                subtitle = stringResource(R.string.about_credits_desc),
            )
            SettingRow(
                title = stringResource(R.string.about_data),
                subtitle = stringResource(R.string.about_data_desc),
            )
            // The one thing the app ever asks for in return. Given its own card rather
            // than another grey subtitle so it is actually read, and placed last so it is
            // what the About screen leaves you with.
            DuaRequest()
        }
    }

    if (methodPicker) {
        MethodPickerDialog(
            current = settings.method,
            sect = settings.sect,
            onDismiss = { methodPicker = false },
            onSelect = {
                onSetMethod(it)
                methodPicker = false
            },
        )
    }

    if (locationSheet) {
        LocationSheet(
            state = state,
            onDismiss = { locationSheet = false },
            onUseGps = onRefreshLocation,
            onSearchCity = onSearchCity,
        )
    }

    if (disclaimer) {
        AlertDialog(
            onDismissRequest = { disclaimer = false },
            confirmButton = {
                TextButton(onClick = { disclaimer = false }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
            title = { Text(stringResource(R.string.disclaimer_title)) },
            text = { Text(stringResource(R.string.disclaimer_body)) },
        )
    }
}

@Composable
private fun MethodPickerDialog(
    current: CalcMethod,
    sect: Sect,
    onDismiss: () -> Unit,
    onSelect: (CalcMethod) -> Unit,
) {
    // Jafari and Tehran are Shia conventions; the rest are Sunni. Showing all of them to
    // everyone invites a wrong pick, so the list follows the chosen school.
    val available = CalcMethod.entries.filter { method ->
        val shiaOnly = method == CalcMethod.JAFARI || method == CalcMethod.TEHRAN
        method == CalcMethod.AUTO || if (sect == Sect.SHIA) shiaOnly else !shiaOnly
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { Text(stringResource(R.string.settings_method)) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .selectableGroup(),
            ) {
                available.forEach { method ->
                    RadioRow(
                        label = method.label,
                        selected = current == method,
                        onSelect = { onSelect(method) },
                    )
                }
            }
        },
    )
}

// --- small building blocks -------------------------------------------------------------

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Spacer(Modifier.height(20.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .semantics { heading() },
    )
    content()
}

@Composable
private fun DuaRequest() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.about_dua),
                style = MaterialTheme.typography.labelMedium,
                // Full opacity, not a faded label: dimming this to 75% lands at 4.65:1 in
                // the dark theme, which clears AA by a margin too thin to leave untested.
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.about_dua_desc),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun WarningRow(title: String, body: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String? = null, onClick: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                },
            )
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RadioRow(
    label: String,
    supporting: String? = null,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onSelect)
            .heightIn(min = 56.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = null)
    }
}

private fun versionName(context: android.content.Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
}.getOrDefault("")

/**
 * Resolves a saved ringtone URI back to the name the user saw when they picked it.
 *
 * Returns null when nothing is set, and also when the tone has since been deleted or
 * lives on a card that is not mounted, so the caller falls back to the generic hint
 * rather than showing a blank row.
 */
@Composable
private fun rememberRingtoneTitle(uri: String): String? {
    val context = LocalContext.current
    return remember(uri) {
        if (uri.isBlank()) {
            null
        } else {
            runCatching {
                RingtoneManager.getRingtone(context, uri.toUri())?.getTitle(context)
            }.getOrNull()?.takeIf { it.isNotBlank() }
        }
    }
}
