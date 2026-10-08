package com.sajdatime.app.ui.settings

import android.content.Intent
import android.os.Build
import com.sajdatime.app.ui.components.PermissionCard
import com.sajdatime.app.ui.components.rememberGranted
import android.media.RingtoneManager
import android.text.format.DateFormat
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.ui.components.UsageCountsConsentDialog
import com.sajdatime.core.AdjustmentFit
import com.sajdatime.core.AppLocale
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.label
import com.sajdatime.core.labelRes
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.AppSettings
import kotlin.math.abs
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.TimeFormat
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.MethodChoiceList
import com.sajdatime.app.ui.components.RadioRow
import com.sajdatime.app.ui.onboarding.madhabLabel
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.ui.theme.sajdaSurface
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Which chooser is currently open, if any. Only one can be at a time.
 *
 * Not private, because the Times screen can ask for one by name — the "Different from your
 * mosque?" explainer and the far-north method notice both hand the user straight to the
 * chooser that answers them, rather than to the top of Settings with the row left to find.
 * See [SettingsScreen]'s `request` parameter.
 */
enum class SettingsChooser { SCHOOL, METHOD, ADJUSTMENTS, ALERTS, LOCATION, LANGUAGE, DISCLAIMER }

@Composable
fun SettingsScreen(
    state: UiState,
    /**
     * A chooser another screen asked to have opened, consumed once. Held by the scaffold
     * rather than here so it survives the tab switch that delivers it; cleared through
     * [onRequestHandled] so rotating the phone afterwards does not reopen it.
     */
    request: SettingsChooser?,
    onRequestHandled: () -> Unit,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetAdjustment: (PrayerSlot, Int) -> Unit,
    onSetHijriOffset: (Int) -> Unit,
    onResetAdjustments: () -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onSetAlarmRespectsSilent: (Boolean) -> Unit,
    onSetAlarmOnApproximateDays: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onSetThemeChoice: (ThemeChoice) -> Unit,
    onSetAnalytics: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var open by rememberSaveable { mutableStateOf<SettingsChooser?>(null) }
    // Turning usage counts ON never flips the switch directly: it opens the full consent text
    // and only a Yes there records the choice. Turning it OFF is a single tap.
    var askingUsageConsent by rememberSaveable { mutableStateOf(false) }
    val settings = state.settings

    // Keyed on the request, so a second identical request after the first was closed still
    // opens it (the scaffold nulls it in between, which is what makes the key change).
    LaunchedEffect(request) {
        if (request != null) {
            open = request
            onRequestHandled()
        }
    }

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
        val exactAllowed by rememberGranted { PrayerAlarmScheduler.canScheduleExact(it) }
        val dndAllowed by rememberGranted { Notifications.hasDndAccess(it) }
        // Before Android 12 there is no exact-alarm permission to ask for, so no row.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionCard(
                title = stringResource(R.string.settings_exact_alarms_title),
                body = stringResource(R.string.settings_exact_alarms_desc),
                granted = exactAllowed,
                onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        if (settings.usesAlarm) {
            PermissionCard(
                title = stringResource(R.string.settings_dnd_title),
                body = stringResource(R.string.settings_dnd_desc),
                granted = dndAllowed,
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(SystemSettings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),
                        )
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        Group(stringResource(R.string.settings_group_prayer_times)) {
            SettingRow(
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.settings_location),
                subtitle = settings.cityName.ifBlank {
                    stringResource(R.string.location_set_generic)
                },
                onClick = { open = SettingsChooser.LOCATION },
            )
            SettingRow(
                icon = Icons.Outlined.Schedule,
                title = stringResource(R.string.settings_school),
                subtitle = schoolSummary(settings.sect, settings.madhab),
                onClick = { open = SettingsChooser.SCHOOL },
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
                onClick = { open = SettingsChooser.METHOD },
            )
            // Last in this group on purpose. The method and school rows above explain the
            // two divergences that have a reason; this one is the catch-all for whatever is
            // left, and offering it first would invite someone to nudge a number when the
            // honest fix was to pick the convention their mosque actually follows.
            SettingRow(
                icon = Icons.Outlined.MoreTime,
                title = stringResource(R.string.settings_adjustments),
                subtitle = adjustmentSummary(settings.adjustments, settings.hijriOffsetDays),
                onClick = { open = SettingsChooser.ADJUSTMENTS },
            )
        }

        Group(stringResource(R.string.settings_group_appearance)) {
            // First in the group, above Theme: a person who cannot read the app's current
            // language looks for the globe, so it must be somewhere the eye lands.
            SettingRow(
                icon = Icons.Outlined.Language,
                title = stringResource(R.string.settings_language_title),
                subtitle = AppLanguage.chosen(context)?.nativeName
                    ?: "${stringResource(R.string.language_phone)} · ${AppLanguage.showing(context).nativeName}",
                onClick = { open = SettingsChooser.LANGUAGE },
            )
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
                onClick = { open = SettingsChooser.ALERTS },
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
            // Only when the app is in a translated language: the English text is the owner's
            // own writing, the translations were drafted with AI help.
            if (AppLocale.of(context).language != "en") {
                SettingRow(
                    title = stringResource(R.string.about_translation),
                    subtitle = stringResource(R.string.about_translation_note),
                )
            }
            SettingRow(
                title = stringResource(R.string.about_disclaimer),
                subtitle = stringResource(R.string.about_disclaimer_short),
                onClick = { open = SettingsChooser.DISCLAIMER },
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
            // Not a prompt: a row the user chooses to open. It hands over to their own email app, so
            // nothing is stored or sent by SajdaTime, and nothing is attached to the message.
            val feedbackEmail = stringResource(R.string.feedback_email)
            val feedbackSubject = stringResource(R.string.feedback_subject)
            SettingRow(
                title = stringResource(R.string.about_feedback),
                subtitle = stringResource(R.string.about_feedback_desc),
                onClick = {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO, "mailto:$feedbackEmail".toUri())
                                .putExtra(Intent.EXTRA_SUBJECT, feedbackSubject),
                        )
                    }
                },
            )
            SwitchRow(
                title = stringResource(R.string.settings_usage_counts),
                subtitle = stringResource(R.string.settings_usage_counts_desc),
                checked = settings.analyticsEnabled,
                onCheckedChange = { wanted ->
                    if (wanted) askingUsageConsent = true else onSetAnalytics(false)
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

    if (askingUsageConsent) {
        UsageCountsConsentDialog(
            onYes = {
                askingUsageConsent = false
                onSetAnalytics(true)
            },
            onNo = { askingUsageConsent = false },
        )
    }

    when (open) {
        null -> Unit

        SettingsChooser.ADJUSTMENTS -> {
            // Today's times as the engine produces them *before* any correction. Typing "10:15"
            // is fitted against these, because the stored correction is relative to them;
            // fitting against the corrected times would count the old correction twice.
            val uncorrected = settings.calculationPrefs.copy(adjustments = emptyMap())
            val calculated = remember(settings.coordinates, uncorrected) {
                settings.coordinates?.let { PrayerEngine.compute(it, LocalDate.now(), uncorrected).times }
            }
            AdjustmentsDialog(
                adjustments = settings.adjustments,
                hijriOffsetDays = settings.hijriOffsetDays,
                calculated = calculated,
                onSetAdjustment = onSetAdjustment,
                onSetHijriOffset = onSetHijriOffset,
                onReset = onResetAdjustments,
                // One state variable holds the open chooser, so this closes this dialog as it
                // opens the method list: the user is moved on, not stacked.
                onChooseMethod = { open = SettingsChooser.METHOD },
                onDismiss = { open = null },
            )
        }

        SettingsChooser.SCHOOL -> SchoolDialog(
            sect = settings.sect,
            madhab = settings.madhab,
            onSelectSect = onSetSect,
            onSelectMadhab = onSetMadhab,
            onDismiss = { open = null },
        )

        SettingsChooser.METHOD -> MethodPickerDialog(
            current = settings.method,
            sect = settings.sect,
            onDismiss = { open = null },
            onSelect = {
                onSetMethod(it)
                open = null
            },
        )

        SettingsChooser.ALERTS -> AlertsDialog(
            alertFor = settings.alertFor,
            alarmSoundUri = settings.alarmSoundUri,
            respectsSilent = settings.alarmRespectsSilent,
            alarmOnApproximate = settings.alarmOnApproximateDays,
            hasApproximateDays = rememberHasApproximateDays(settings),
            onSetAlert = onSetAlert,
            onSetRespectsSilent = onSetAlarmRespectsSilent,
            onSetAlarmOnApproximate = onSetAlarmOnApproximateDays,
            onPickAlarmSound = onPickAlarmSound,
            onDismiss = { open = null },
        )

        SettingsChooser.LOCATION -> LocationSheet(
            state = state,
            onDismiss = { open = null },
            onUseGps = onRefreshLocation,
            onSearchCity = onSearchCity,
        )

        SettingsChooser.LANGUAGE -> LanguageChooserDialog(onDismiss = { open = null })

        SettingsChooser.DISCLAIMER -> AlertDialog(
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
    ChooserDialog(title = stringResource(R.string.settings_method), onDismiss = onDismiss) {
        // A list of institution names is meaningless to most users, and picking the wrong one
        // moves Isha by over an hour at UK latitudes. This says what the choice is for and
        // where to get the answer — the mosque — rather than leaving them to guess. The list
        // itself is shared with the first-run step, so each entry also says who it is for.
        Text(
            text = stringResource(R.string.settings_method_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        MethodChoiceList(sect = sect, current = current, onSelect = onSelect)
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
    alarmOnApproximate: Boolean,
    hasApproximateDays: Boolean,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetRespectsSilent: (Boolean) -> Unit,
    onSetAlarmOnApproximate: (Boolean) -> Unit,
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
            // Only for the few thousand people on earth this can ever apply to. Everyone
            // below the polar circles has no approximated days, so the switch would be a
            // question about astronomy they will never meet — and this app is used by
            // people who have never opened a settings screen. See rememberHasApproximateDays.
            if (hasApproximateDays) {
                SwitchRow(
                    title = stringResource(R.string.settings_alarm_on_approximate),
                    subtitle = stringResource(R.string.settings_alarm_on_approximate_desc),
                    checked = alarmOnApproximate,
                    onCheckedChange = onSetAlarmOnApproximate,
                    horizontalPadding = 0.dp,
                )
            }
        }
    }
}

/**
 * Whether this location has any day in the coming year that must be projected from another
 * latitude — which is the only condition under which the alarm-on-approximate-days switch
 * means anything.
 *
 * A full 365-day sweep rather than a latitude threshold or a solstice probe. Both of those
 * were considered: a threshold would have to be method-dependent, because the reference
 * latitude is (45 under the Islamic Fiqh Council, 60 under Moonsighting Committee), and a
 * two-solstice probe assumes the worst day of the year is a solstice, which is *nearly*
 * true but not something worth asserting when the exact answer is this cheap. The engine is
 * pure arithmetic, the sweep is a few milliseconds, it runs behind `remember` keyed on the
 * only two inputs that can change it, and it is computed only when the dialog is open.
 *
 * ponytail: no threshold constant, no test pinning a physical claim, no way to be subtly
 * wrong at one latitude in one hemisphere. Ask the engine the actual question.
 */
@Composable
private fun rememberHasApproximateDays(settings: AppSettings): Boolean {
    val coordinates = settings.coordinates
    val prefs = settings.calculationPrefs
    return remember(coordinates, prefs) { settings.hasApproximateDays(LocalDate.now()) }
}

/** The year-long sweep behind [rememberHasApproximateDays], separated so it can be tested. */
internal fun AppSettings.hasApproximateDays(from: LocalDate): Boolean {
    val coordinates = coordinates ?: return false
    return runCatching {
        PrayerEngine.computeRange(coordinates, from, DAYS_IN_YEAR, calculationPrefs)
            .any { it.approximated }
    }.getOrDefault(false)
}

/** 366, not 365: a leap year must not be the one that hides a day. */
private const val DAYS_IN_YEAR = 366

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

/**
 * "Match your mosque": the per-prayer corrections and the Hijri date shift, in one place.
 *
 * They are one dialog rather than two rows because from where the user is standing they
 * answer one question — *my mosque says something different* — and splitting them would
 * make someone decide, before they had any information, whether their disagreement was
 * about minutes or about days.
 *
 * Two ways in, because the person reaching for this is often reading a printed timetable in
 * one hand. Plus and minus steppers can only ever produce a legal value and need no
 * keyboard, and they stay. But a twenty minute gap is twenty presses, so the number is also
 * tappable: pick the time the mosque's board shows and [AdjustmentFit] works out the minutes.
 * A time dial rather than a text field, so "12:45" can never be typed into a box that wants
 * "5". It is the same earlier reasoning applied to the new control, not a reversal of it.
 *
 * The limit (30 minutes) did not move, and that is the point of [LimitNote]. A gap that big
 * is a different calculation method or a board that shows the congregation, never a bigger
 * offset. Before this the plus button simply went grey with the only explanation a line of
 * static text above the list, nowhere near where the user was stuck. See docs/HANDOVER.md §5.17.
 */
@Composable
private fun AdjustmentsDialog(
    adjustments: Map<PrayerSlot, Int>,
    hijriOffsetDays: Int,
    /** Today's uncorrected times, or null before there is a location to calculate for. */
    calculated: Map<PrayerSlot, Instant>?,
    onSetAdjustment: (PrayerSlot, Int) -> Unit,
    onSetHijriOffset: (Int) -> Unit,
    onReset: () -> Unit,
    onChooseMethod: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    // Which prayer's time dial is open, and which prayer's typed time was too far away.
    // Saveable so rotating the phone mid-entry does not throw the user back to the list.
    var picking by rememberSaveable { mutableStateOf<PrayerSlot?>(null) }
    var tooFar by rememberSaveable { mutableStateOf<PrayerSlot?>(null) }

    ChooserDialog(title = stringResource(R.string.settings_adjustments), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_adjustments_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        PrayerSlot.entries.forEach { slot ->
            val minutes = adjustments[slot] ?: 0
            val base = calculated?.get(slot)
            val label = slot.label(context)
            StepperRow(
                label = label,
                value = minuteLabel(minutes),
                canDecrease = minutes > -CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
                canIncrease = minutes < CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
                onDecrease = {
                    tooFar = null
                    onSetAdjustment(slot, minutes - 1)
                },
                onIncrease = {
                    tooFar = null
                    onSetAdjustment(slot, minutes + 1)
                },
                // The time the screen now shows, only once there is a correction to check.
                shows = if (base != null && minutes != 0) {
                    stringResource(
                        R.string.adjustment_now_shows,
                        TimeFormat.clock(context, base.plusSeconds(60L * minutes), zone),
                    )
                } else {
                    null
                },
                valueDescription = stringResource(R.string.adjustment_pick_time_description, label),
                onValueClick = if (base != null) ({ picking = slot }) else null,
            )
            if (abs(minutes) >= CalculationPrefs.MAX_ADJUSTMENT_MINUTES || tooFar == slot) {
                LimitNote(onChooseMethod)
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        SectionLabel(stringResource(R.string.settings_hijri_offset))
        Text(
            text = stringResource(R.string.settings_hijri_offset_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        StepperRow(
            label = stringResource(R.string.settings_hijri_offset_label),
            value = dayLabel(hijriOffsetDays),
            canDecrease = hijriOffsetDays > -CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            canIncrease = hijriOffsetDays < CalculationPrefs.MAX_HIJRI_OFFSET_DAYS,
            onDecrease = { onSetHijriOffset(hijriOffsetDays - 1) },
            onIncrease = { onSetHijriOffset(hijriOffsetDays + 1) },
        )

        if (adjustments.isNotEmpty() || hijriOffsetDays != 0) {
            TextButton(
                onClick = {
                    tooFar = null
                    onReset()
                    onSetHijriOffset(0)
                },
            ) {
                Text(stringResource(R.string.settings_adjustments_reset))
            }
        }
    }

    val pickingSlot = picking
    val pickingBase = pickingSlot?.let { calculated?.get(it) }
    if (pickingSlot != null && pickingBase != null) {
        TimeEntryDialog(
            title = stringResource(R.string.adjustment_pick_time_title, pickingSlot.label(context)),
            // Opens on what the screen shows now, so a small change is a small move on the dial.
            initial = pickingBase
                .plusSeconds(60L * (adjustments[pickingSlot] ?: 0))
                .atZone(zone)
                .toLocalTime(),
            // Same rule as every clock in the app: the device's 12/24 setting (TimeFormat.clock).
            is24Hour = DateFormat.is24HourFormat(context.applicationContext),
            onConfirm = { time ->
                picking = null
                when (val fit = AdjustmentFit.fit(pickingBase, time, zone)) {
                    is AdjustmentFit.Result.Within -> {
                        tooFar = null
                        onSetAdjustment(pickingSlot, fit.minutes)
                    }
                    // Nothing is stored. The note under the row says why and where to go next.
                    AdjustmentFit.Result.TooFar -> tooFar = pickingSlot
                }
            },
            onDismiss = { picking = null },
        )
    }
}

/**
 * Shown under a prayer that has reached the limit, or whose typed time was past it.
 *
 * Visible without a tap on purpose: a disabled button receives no taps, so an explanation
 * that waited for one would never appear. Announced politely to a screen reader because
 * "nothing happened" is the worst thing to leave someone with when they cannot see that the
 * button went grey.
 *
 * It names no method and declares no time right or wrong, per §5.17. It says what a gap
 * this size usually is and offers the one door that deals with it.
 */
@Composable
private fun LimitNote(onChooseMethod: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    ) {
        Text(
            text = stringResource(
                R.string.adjustment_limit_note,
                CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TextButton(onClick = onChooseMethod) {
            Text(stringResource(R.string.adjustment_choose_method))
        }
    }
}

/**
 * The time dial. Scrollable, because the dial is tall and a raised system font size makes
 * the dialog taller than a phone in landscape.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeEntryDialog(
    title: String,
    initial: LocalTime,
    is24Hour: Boolean,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initial.hour, initial.minute, is24Hour)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.action_set))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/**
 * One label with a minus, a value and a plus.
 *
 * The value carries the whole row's accessibility label because a screen reader landing on
 * a bare "+5 min" between two icon buttons has no way to know which prayer it belongs to.
 *
 * When [onValueClick] is given the value is also a button, drawn in the accent colour and
 * underlined so it reads as tappable, and at least 48dp tall to be easy to hit.
 */
@Composable
private fun StepperRow(
    label: String,
    value: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    shows: String? = null,
    valueDescription: String? = null,
    onValueClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (shows != null) {
                Text(
                    text = shows,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        FilledTonalIconButton(onClick = onDecrease, enabled = canDecrease) {
            Icon(
                Icons.Outlined.Remove,
                contentDescription = stringResource(R.string.action_decrease, label),
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.let {
                if (onValueClick != null) it.copy(textDecoration = TextDecoration.Underline) else it
            },
            color = if (onValueClick != null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            // Wide enough for "-30 min" so the plus button does not shuffle sideways as
            // the number grows a digit or loses its sign.
            modifier = Modifier
                .width(64.dp)
                .then(
                    if (onValueClick != null) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                role = Role.Button,
                                onClickLabel = valueDescription,
                                onClick = onValueClick,
                            )
                            .minimumInteractiveComponentSize()
                    } else {
                        Modifier
                    },
                ),
        )
        FilledTonalIconButton(onClick = onIncrease, enabled = canIncrease) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(R.string.action_increase, label),
            )
        }
    }
}

/** "+5 min", "-3 min", or "0 min" for no change. (Not a dash: it is underlined and tappable.) */
@Composable
private fun minuteLabel(minutes: Int): String = when {
    minutes == 0 -> stringResource(R.string.adjustment_zero_minutes)
    else -> stringResource(R.string.adjustment_minutes, minutes)
}

/** "+1 day", "-2 days", or a dash. Plural-aware, because Arabic and Urdu are not English. */
@Composable
private fun dayLabel(days: Int): String = when {
    days == 0 -> stringResource(R.string.adjustment_none)
    else -> pluralStringResource(R.plurals.adjustment_days, abs(days), days)
}

/** The settings-row summary: what is set, without opening the dialog. */
@Composable
private fun adjustmentSummary(adjustments: Map<PrayerSlot, Int>, hijriOffsetDays: Int): String {
    val parts = buildList {
        val count = adjustments.count { it.value != 0 }
        if (count > 0) add(pluralStringResource(R.plurals.adjustment_summary_times, count, count))
        if (hijriOffsetDays != 0) {
            add(
                stringResource(
                    R.string.adjustment_summary_hijri,
                    pluralStringResource(R.plurals.adjustment_days, abs(hijriOffsetDays), hijriOffsetDays),
                ),
            )
        }
    }
    return if (parts.isEmpty()) stringResource(R.string.settings_adjustments_none)
    else parts.joinToString(stringResource(R.string.list_separator))
}
