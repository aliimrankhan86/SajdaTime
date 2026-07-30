package com.sajdatime.app.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.sajdatime.app.core.CalcMethod
import com.sajdatime.app.core.Madhab
import com.sajdatime.app.core.PrayerEngine
import com.sajdatime.app.core.PrayerSlot
import com.sajdatime.app.core.Sect
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.onboarding.madhabLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: UiState,
    onBack: () -> Unit,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetNotify: (PrayerSlot, Boolean) -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onRefreshLocation: () -> Unit,
) {
    val context = LocalContext.current
    var methodPicker by remember { mutableStateOf(false) }
    val settings = state.settings

    // System back returns to the prayer times rather than leaving the app.
    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                // Matches the page behind it; the default surface colour leaves a
                // visible band between the bar and the content.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Group(stringResource(R.string.settings_group_school)) {
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

            // Madhab only changes the Asr rule, which the Jafari school fixes anyway,
            // so it is hidden entirely for Shia users rather than shown disabled.
            if (settings.sect == Sect.SUNNI) {
                Group(stringResource(R.string.settings_group_madhab)) {
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

            Group(stringResource(R.string.settings_group_notifications)) {
                if (!PrayerAlarmScheduler.canScheduleExact(context)) {
                    ExactAlarmWarning(
                        onOpenSettings = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .setData(Uri.parse("package:${context.packageName}")),
                                )
                            }
                        },
                    )
                }
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
                    title = stringResource(R.string.settings_update_location),
                    subtitle = settings.cityName.ifBlank {
                        stringResource(R.string.location_set_generic)
                    },
                    onClick = onRefreshLocation,
                )
            }

            Group(stringResource(R.string.settings_group_about)) {
                SettingRow(
                    title = stringResource(R.string.about_version),
                    subtitle = versionName(context),
                )
                SettingRow(
                    title = stringResource(R.string.about_privacy),
                    subtitle = stringResource(R.string.about_privacy_desc),
                )
                SettingRow(
                    title = stringResource(R.string.about_charity),
                    subtitle = stringResource(R.string.about_charity_desc),
                )
            }
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
}

@Composable
private fun MethodPickerDialog(
    current: CalcMethod,
    sect: Sect,
    onDismiss: () -> Unit,
    onSelect: (CalcMethod) -> Unit,
) {
    // Jafari/Tehran are Shia conventions; the rest are Sunni. Showing all of them to
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

@Composable
private fun ExactAlarmWarning(onOpenSettings: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onOpenSettings)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.settings_exact_alarms_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.settings_exact_alarms_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
