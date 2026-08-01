package com.sajdatime.app.ui.settings

import android.content.Intent
import android.media.RingtoneManager
import androidx.core.net.toUri
import android.provider.Settings as SystemSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.labelRes
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.onboarding.madhabLabel
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.ui.theme.sajdaSurface

/** Which chooser is currently open, if any. Only one can be at a time. */
private enum class Chooser { SCHOOL, METHOD, ALERTS, LOCATION, DISCLAIMER }

@Composable
fun SettingsScreen(
    state: UiState,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onSetAlarmRespectsSilent: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onSetThemeChoice: (ThemeChoice) -> Unit,
) {
    val context = LocalContext.current
    var open by rememberSaveable { mutableStateOf<Chooser?>(null) }
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

        // Anything the system is withholding goes at the very top, above the settings
        // themselves. These are not preferences, they are problems, and burying them
        // inside the group they belong to meant nobody found them.
        if (!PrayerAlarmScheduler.canScheduleExact(context)) {
            WarningRow(
                title = stringResource(R.string.settings_exact_alarms_title),
                body = stringResource(R.string.settings_exact_alarms_desc),
                onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
            )
        }
        if (settings.usesAlarm && !Notifications.hasDndAccess(context)) {
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

        Group(stringResource(R.string.settings_group_prayer_times)) {
            SettingRow(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.settings_location),
                subtitle = settings.cityName.ifBlank {
                    stringResource(R.string.location_set_generic)
                },
                onClick = { open = Chooser.LOCATION },
            )
            SettingRow(
                icon = Icons.Outlined.Schedule,
                title = stringResource(R.string.settings_school),
                subtitle = schoolSummary(settings.sect, settings.madhab),
                onClick = { open = Chooser.SCHOOL },
            )
            SettingRow(
                icon = Icons.Outlined.Tune,
                title = stringResource(R.string.settings_method),
                subtitle = if (settings.method == CalcMethod.AUTO) {
                    stringResource(
                        R.string.settings_method_auto,
                        stringResource(PrayerEngine.resolveMethod(settings.calculationPrefs).labelRes),
                    )
                } else {
                    stringResource(settings.method.labelRes)
                },
                onClick = { open = Chooser.METHOD },
            )
        }

        Group(stringResource(R.string.settings_group_appearance)) {
            ThemeRow(current = settings.themeChoice, onSelect = onSetThemeChoice)
        }

        Group(stringResource(R.string.settings_group_reminders)) {
            // One row, not two. "Which prayers" and "How you are told" were separate
            // choosers answering halves of the same question, and between them they could
            // not express "wake me for Fajr, a quiet notification for the rest" — which is
            // what a tester asked for. Off is now simply having no style.
            SettingRow(
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.settings_alerts),
                subtitle = alertSummary(settings.alertFor),
                onClick = { open = Chooser.ALERTS },
            )
            // A plain on/off stays inline. Sending the user into a chooser to flip one
            // switch would be worse than the flat list this screen replaced.
            SwitchRow(
                icon = Icons.Outlined.PushPin,
                title = stringResource(R.string.settings_ongoing_badge),
                subtitle = stringResource(R.string.settings_ongoing_badge_desc),
                checked = settings.ongoingBadge,
                onCheckedChange = onSetOngoingBadge,
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
                onClick = { open = Chooser.DISCLAIMER },
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
        }
    }

    when (open) {
        null -> Unit

        Chooser.SCHOOL -> SchoolDialog(
            sect = settings.sect,
            madhab = settings.madhab,
            onSelectSect = onSetSect,
            onSelectMadhab = onSetMadhab,
            onDismiss = { open = null },
        )

        Chooser.METHOD -> MethodPickerDialog(
            current = settings.method,
            sect = settings.sect,
            onDismiss = { open = null },
            onSelect = {
                onSetMethod(it)
                open = null
            },
        )

        Chooser.ALERTS -> AlertsDialog(
            alertFor = settings.alertFor,
            alarmSoundUri = settings.alarmSoundUri,
            respectsSilent = settings.alarmRespectsSilent,
            onSetAlert = onSetAlert,
            onSetRespectsSilent = onSetAlarmRespectsSilent,
            onPickAlarmSound = onPickAlarmSound,
            onDismiss = { open = null },
        )

        Chooser.LOCATION -> LocationSheet(
            state = state,
            onDismiss = { open = null },
            onUseGps = onRefreshLocation,
            onSearchCity = onSearchCity,
        )

        Chooser.DISCLAIMER -> AlertDialog(
            onDismissRequest = { open = null },
            confirmButton = {
                TextButton(onClick = { open = null }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
            title = { Text(stringResource(R.string.disclaimer_title)) },
            // Scrollable for the same reason every chooser on this screen is.
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.disclaimer_body))
                }
            },
        )
    }
}

// --- summaries shown under each row ------------------------------------------------------

/** "Sunni · Hanafi", or just "Shia" — the Jafari school fixes the Asr rule anyway. */
@Composable
private fun schoolSummary(sect: Sect, madhab: Madhab): String = when (sect) {
    Sect.SUNNI -> stringResource(
        R.string.settings_value_pair,
        stringResource(R.string.sect_sunni),
        madhabLabel(madhab),
    )

    Sect.SHIA -> stringResource(R.string.sect_shia)
}

/** "All five · Notification", "2 of 5 · Mixed", or just "None". */
@Composable
private fun alertSummary(alertFor: Map<PrayerSlot, AlertStyle>): String {
    if (alertFor.isEmpty()) return stringResource(R.string.settings_which_prayers_none)
    val count = when (alertFor.size) {
        5 -> stringResource(R.string.settings_which_prayers_all)
        else -> stringResource(R.string.settings_which_prayers_some, alertFor.size)
    }
    val styles = alertFor.values.toSet()
    val style = stringResource(
        when {
            styles.size > 1 -> R.string.alert_style_mixed
            styles.single() == AlertStyle.ALARM -> R.string.alert_style_alarm
            else -> R.string.alert_style_notification
        },
    )
    return stringResource(R.string.settings_value_pair, count, style)
}

// --- choosers ----------------------------------------------------------------------------

/**
 * Sect and madhab together, because they are one decision to the user. Madhab only
 * changes the Asr rule, which the Jafari school fixes anyway, so it is hidden entirely
 * for Shia users rather than shown disabled.
 */
@Composable
private fun SchoolDialog(
    sect: Sect,
    madhab: Madhab,
    onSelectSect: (Sect) -> Unit,
    onSelectMadhab: (Madhab) -> Unit,
    onDismiss: () -> Unit,
) {
    ChooserDialog(title = stringResource(R.string.settings_school), onDismiss = onDismiss) {
        Column(Modifier.selectableGroup()) {
            Sect.entries.forEach { entry ->
                RadioRow(
                    label = stringResource(
                        if (entry == Sect.SUNNI) R.string.sect_sunni else R.string.sect_shia,
                    ),
                    supporting = stringResource(
                        if (entry == Sect.SUNNI) R.string.sect_sunni_desc else R.string.sect_shia_desc,
                    ),
                    selected = sect == entry,
                    onSelect = { onSelectSect(entry) },
                )
            }
        }

        if (sect == Sect.SUNNI) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionLabel(stringResource(R.string.settings_madhab))
            Column(Modifier.selectableGroup()) {
                Madhab.entries.forEach { entry ->
                    RadioRow(
                        label = madhabLabel(entry),
                        supporting = stringResource(
                            if (entry == Madhab.HANAFI) R.string.madhab_hanafi_desc
                            else R.string.madhab_standard_desc,
                        ),
                        selected = madhab == entry,
                        onSelect = { onSelectMadhab(entry) },
                    )
                }
            }
        }
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

    ChooserDialog(title = stringResource(R.string.settings_method), onDismiss = onDismiss) {
        // A list of institution names is meaningless to most users, and picking the wrong one
        // moves Isha by over an hour at UK latitudes. This says what the choice is for and
        // where to get the answer — the mosque — rather than leaving them to guess.
        Text(
            text = stringResource(R.string.settings_method_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Column(Modifier.selectableGroup()) {
            available.forEach { method ->
                RadioRow(
                    label = stringResource(method.labelRes),
                    selected = current == method,
                    onSelect = { onSelect(method) },
                )
            }
        }
    }
}

/**
 * Every prayer, and how each one announces itself. Off, a quiet notification, or a full
 * alarm — chosen per prayer, because "wake me for Fajr, do not shout at me for Dhuhr" is
 * the request this screen exists to answer.
 *
 * The alarm sound and the silent-mode switch appear only once at least one prayer is set
 * to Alarm, rather than sitting greyed out for the majority who never leave the default.
 */
@Composable
private fun AlertsDialog(
    alertFor: Map<PrayerSlot, AlertStyle>,
    alarmSoundUri: String,
    respectsSilent: Boolean,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetRespectsSilent: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onDismiss: () -> Unit,
) {
    ChooserDialog(title = stringResource(R.string.settings_alerts), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_alerts_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))

        PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
            AlertChoiceRow(
                label = stringResource(slot.labelRes),
                selected = alertFor[slot],
                onSelect = { onSetAlert(slot, it) },
            )
        }

        if (alertFor.containsValue(AlertStyle.ALARM)) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SettingRow(
                title = stringResource(R.string.settings_alarm_sound),
                // Naming the chosen tone is the only confirmation the user gets that the
                // pick stuck. The generic hint stays until something is chosen.
                subtitle = rememberRingtoneTitle(alarmSoundUri)
                    ?: stringResource(R.string.settings_alarm_sound_desc),
                onClick = onPickAlarmSound,
                horizontalPadding = 0.dp,
            )
            SwitchRow(
                title = stringResource(R.string.settings_alarm_respect_silent),
                subtitle = stringResource(R.string.settings_alarm_respect_silent_desc),
                checked = respectsSilent,
                onCheckedChange = onSetRespectsSilent,
                horizontalPadding = 0.dp,
            )
        }
    }
}

/**
 * One prayer, three mutually exclusive answers.
 *
 * Chips in a FlowRow rather than a Row of radio buttons: three labelled radios do not fit
 * across a phone beside a prayer name at a raised system font size, and the one that would
 * be clipped is "Alarm" — the whole reason anyone opens this.
 */
@Composable
private fun AlertChoiceRow(
    label: String,
    selected: AlertStyle?,
    onSelect: (AlertStyle?) -> Unit,
) {
    Column(Modifier.padding(vertical = 10.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.selectableGroup(),
        ) {
            ChoiceChip(
                label = stringResource(R.string.alert_style_off),
                selected = selected == null,
                onClick = { onSelect(null) },
            )
            ChoiceChip(
                label = stringResource(R.string.alert_style_notification),
                selected = selected == AlertStyle.NOTIFICATION,
                onClick = { onSelect(AlertStyle.NOTIFICATION) },
            )
            ChoiceChip(
                label = stringResource(R.string.alert_style_alarm),
                selected = selected == AlertStyle.ALARM,
                onClick = { onSelect(AlertStyle.ALARM) },
            )
        }
    }
}

/**
 * The one dialog shape every chooser uses. Scrollable, because the method list is long
 * and a raised system font size makes even the short ones taller than a phone.
 */
@Composable
private fun ChooserDialog(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
        },
        title = { Text(title) },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { content() } },
    )
}

// --- small building blocks -------------------------------------------------------------

/**
 * A titled group of rows, drawn as one bounded card.
 *
 * The rows used to run edge to edge under a coloured heading, which left the screen as a
 * single unbroken column and made "where does Prayer times end and Reminders begin"
 * something you worked out by reading. A border is faster than reading.
 */
@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Spacer(Modifier.height(24.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
        color = scheme.primary,
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .semantics { heading() },
    )
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(20.dp)),
    ) {
        content()
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .semantics { heading() },
    )
}

/**
 * Light, dark, or follow the phone.
 *
 * Three chips rather than a chooser dialog, because unlike every other setting on this
 * screen the result is visible the instant it is tapped — sending the user into a dialog
 * to see a change that happens behind the dialog would be perverse.
 *
 * "Follow the phone" is first and is the default. On a device that expresses no dark
 * preference it resolves to light, which is what the plain-English label promises.
 */
@Composable
private fun ThemeRow(current: ThemeChoice, onSelect: (ThemeChoice) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val labels = mapOf(
        ThemeChoice.SYSTEM to R.string.theme_system,
        ThemeChoice.LIGHT to R.string.theme_light,
        ThemeChoice.DARK to R.string.theme_dark,
    )

    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Outlined.Contrast,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { heading() },
            )
        }
        Spacer(Modifier.height(10.dp))
        // A FlowRow, not a Row: at a large font size three chips no longer fit across a
        // phone, and a fixed Row would clip the third one — which is "Dark", the whole
        // reason anyone opens this.
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(start = 38.dp)
                .selectableGroup(),
        ) {
            ThemeChoice.entries.forEach { choice ->
                ChoiceChip(
                    label = stringResource(labels.getValue(choice)),
                    selected = current == choice,
                    onClick = { onSelect(choice) },
                )
            }
        }
    }
}

/**
 * One option in a small horizontal set. Used by the theme picker and by the per-prayer
 * alert picker, which is the only reason it is not still inlined in the theme row.
 *
 * `selectable`, not `clickable`: a screen reader should announce "selected" on the active
 * chip, and a set of chips is one choice rather than three independent buttons.
 */
@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) scheme.onSecondaryContainer else scheme.onSurfaceVariant,
        modifier = Modifier
            // Grows the touch target to the 48dp minimum without growing the chip itself.
            // A chip this size draws at about 40dp, which reads fine and misses the
            // accessibility floor — the two are separate measurements and only one of
            // them is visible.
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .background(if (selected) scheme.secondaryContainer else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (selected) scheme.primary else scheme.outline,
                shape = RoundedCornerShape(50),
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

/** Same amber treatment as the banners on Times, so a warning looks like a warning. */
@Composable
private fun WarningRow(title: String, body: String, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .sajdaSurface(RoundedCornerShape(16.dp), scheme.tertiaryContainer)
            .border(1.dp, scheme.tertiary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = scheme.tertiary,
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.onTertiaryContainer,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onTertiaryContainer,
            )
        }
    }
}

/**
 * A settings row. Anything tappable carries a chevron, so "this opens something" never
 * has to be guessed at from the text alone.
 */
@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp,
    onClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
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
            .padding(horizontal = horizontalPadding, vertical = 12.dp),
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
        }
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
        if (onClick != null) {
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(14.dp),
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
            .padding(vertical = 8.dp),
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
    icon: ImageVector? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .heightIn(min = 56.dp)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
    ) {
        // Same 22dp icon and 16dp gutter as SettingRow, so titles in a group line up
        // whether the row ends in a chevron or a switch.
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
        }
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
