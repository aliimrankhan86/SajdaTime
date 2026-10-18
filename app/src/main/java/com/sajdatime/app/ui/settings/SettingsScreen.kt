package com.sajdatime.app.ui.settings

import com.sajdatime.app.ui.components.ctaIconButtonColors
import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.data.placeName
import com.sajdatime.app.ui.components.DialogTitle
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.selection.toggleable
import com.sajdatime.app.review.ReviewPrompt
import androidx.compose.ui.text.font.FontWeight
import android.content.Intent
import android.os.Build
import com.sajdatime.app.ui.components.PermissionCard
import com.sajdatime.app.ui.components.rememberGranted
import android.media.RingtoneManager
import androidx.annotation.StringRes
import androidx.core.net.toUri
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
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Contrast
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.outlined.MoreTime
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajdatime.app.R
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.components.UsageCountsConsentDialog
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.core.label
import com.sajdatime.core.labelRes
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.AppSettings
import kotlin.math.abs
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.ui.components.LocationSheet
import com.sajdatime.app.ui.components.MethodChoiceList
import com.sajdatime.app.ui.components.RadioRow
import com.sajdatime.app.ui.onboarding.madhabLabel
import com.sajdatime.app.ui.theme.ThemeChoice
import com.sajdatime.app.ui.theme.sajdaSurface
import java.time.LocalDate

/**
 * Which chooser is currently open, if any. Only one can be at a time.
 *
 * Not private, because the Times screen can ask for one by name — the "Different from your
 * mosque?" explainer and the far-north method notice both hand the user straight to the
 * chooser that answers them, rather than to the top of Settings with the row left to find.
 * See [SettingsScreen]'s `request` parameter.
 */
enum class SettingsChooser {
    SCHOOL, METHOD, ADJUSTMENTS, ALERTS, NOTIFICATION_SOUND, ALARM_SOUND, PRAYER_REMINDER, SUHOOR, LOCATION, LANGUAGE, DISCLAIMER,
}

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
    onSetRamadanCard: (Boolean) -> Unit,
    onSetSuhoorMinutes: (Int) -> Unit,
    onSetSuhoorStyle: (AlertStyle) -> Unit,
    onSetPrayerReminderMinutes: (Int) -> Unit,
    onSetPrayerReminder: (PrayerSlot, Boolean) -> Unit,
    onSetAlarmOnApproximateDays: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onPickBuiltInAdhan: (BuiltInAdhan) -> Unit,
    onPickNotificationSound: () -> Unit,
    onPickBuiltInNotificationAdhan: (BuiltInAdhan) -> Unit,
    onSetAlarmRespectsSilent: (Boolean) -> Unit,
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

    // Anything the system is withholding goes at the very top, above the settings
    // themselves. These are not preferences, they are problems, and burying them
    // inside the group they belong to meant nobody found them.
    val exactAllowed by rememberGranted { PrayerAlarmScheduler.canScheduleExact(it) }
    val dndAllowed by rememberGranted { Notifications.hasDndAccess(it) }
    val banners: @Composable () -> Unit = {
        // Before Android 12 there is no exact-alarm permission to ask for, so no row.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionCard(
                title = stringResource(R.string.settings_exact_alarms_title),
                body = stringResource(R.string.settings_exact_alarms_brief),
                details = stringResource(R.string.settings_exact_alarms_desc),
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
                onClick = { Notifications.openDndAccessSettings(context) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }

    val prayerTimes = SettingsSection(
        SettingsSectionId.PRAYER_TIMES,
        stringResource(R.string.settings_group_prayer_times),
        listOf(
            SettingsItem.Link(
                key = "location",
                icon = Icons.Outlined.LocationOn,
                title = stringResource(R.string.settings_location),
                subtitle = settings.placeName(LocalContext.current).ifBlank {
                    stringResource(R.string.location_set_generic)
                },
                onClick = { open = SettingsChooser.LOCATION },
            ),
            SettingsItem.Link(
                key = "school",
                icon = Icons.Outlined.Schedule,
                title = stringResource(R.string.settings_school),
                subtitle = schoolSummary(settings.sect, settings.madhab),
                onClick = { open = SettingsChooser.SCHOOL },
            ),
            SettingsItem.Link(
                key = "method",
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
            ),
            // Last in this group on purpose. The method and school rows above explain the
            // two divergences that have a reason; this one is the catch-all for whatever is
            // left, and offering it first would invite someone to nudge a number when the
            // honest fix was to pick the convention their mosque actually follows.
            SettingsItem.Link(
                key = "adjustments",
                icon = Icons.Outlined.MoreTime,
                title = stringResource(R.string.settings_adjustments),
                subtitle = adjustmentSummary(settings.adjustments, settings.hijriOffsetDays),
                onClick = { open = SettingsChooser.ADJUSTMENTS },
            ),
        ),
    )

    val appearance = SettingsSection(
        SettingsSectionId.APPEARANCE,
        stringResource(R.string.settings_group_appearance),
        buildList {
            add(
                SettingsItem.Theme(
                    title = stringResource(R.string.settings_theme),
                    current = settings.themeChoice,
                    onSelect = onSetThemeChoice,
                ),
            )
            if (AppLanguage.supported) {
                add(
                    SettingsItem.Link(
                        key = "language",
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.settings_language_title),
                        subtitle = AppLanguage.current(context).nativeName,
                        onClick = { open = SettingsChooser.LANGUAGE },
                    ),
                )
            }
        },
    )

    // The two sounds, each a row of its own under Prayer alerts (owner, 18 Oct 2026). The alarm sound
    // used to sit at the foot of the Prayer alerts dialog, below a fold with nothing showing there was
    // more, so he never found "From your phone". A row says what is chosen without opening anything.
    // Each appears only while some prayer (or suhoor) uses that style, like the alarm sound did.
    val notificationSound = SoundChoice(
        sect = settings.sect,
        uri = settings.notificationSoundUri,
        defaultLabel = R.string.settings_notification_sound_default,
        onPickPhone = onPickNotificationSound,
        onPickBuiltIn = onPickBuiltInNotificationAdhan,
    )
    val alarmSound = SoundChoice(
        sect = settings.sect,
        uri = settings.alarmSoundUri,
        defaultLabel = R.string.settings_alarm_sound_default,
        onPickPhone = onPickAlarmSound,
        onPickBuiltIn = onPickBuiltInAdhan,
        respectsSilent = settings.alarmRespectsSilent,
        onSetRespectsSilent = onSetAlarmRespectsSilent,
    )

    val reminders = SettingsSection(
        SettingsSectionId.REMINDERS,
        stringResource(R.string.settings_group_reminders),
        listOfNotNull(
            // One row, not two. "Which prayers" and "How you are told" were separate
            // choosers answering halves of the same question, and between them they could
            // not express "wake me for Fajr, a quiet notification for the rest" — which is
            // what a tester asked for. Off is now simply having no style.
            SettingsItem.Link(
                key = "alerts",
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.settings_alerts),
                subtitle = alertSummary(settings.alertFor),
                onClick = { open = SettingsChooser.ALERTS },
            ),
            SettingsItem.Link(
                key = "notification_sound",
                icon = Icons.Outlined.MusicNote,
                title = stringResource(R.string.settings_notification_sound),
                subtitle = soundSummary(notificationSound),
                onClick = { open = SettingsChooser.NOTIFICATION_SOUND },
            ).takeIf { settings.usesNotification },
            SettingsItem.Link(
                key = "alarm_sound",
                icon = Icons.Outlined.MusicNote,
                title = stringResource(R.string.settings_alarm_sound),
                subtitle = soundSummary(alarmSound),
                onClick = { open = SettingsChooser.ALARM_SOUND },
            ).takeIf { settings.usesAlarm },
            // Off until chosen (1.5.0): a quiet heads up a few minutes before the prayers picked.
            SettingsItem.Link(
                key = "prayer_reminder",
                icon = Icons.Outlined.Timer,
                title = stringResource(R.string.settings_prayer_reminder),
                subtitle = prayerReminderSummary(settings.prayerReminderMinutes, settings.prayerReminderFor),
                onClick = { open = SettingsChooser.PRAYER_REMINDER },
            ),
            // A plain on/off stays inline. Sending the user into a chooser to flip one
            // switch would be worse than the flat list this screen replaced.
            SettingsItem.Toggle(
                key = "ongoing_badge",
                icon = Icons.Outlined.PushPin,
                title = stringResource(R.string.settings_ongoing_badge),
                subtitle = stringResource(R.string.settings_ongoing_badge_desc),
                checked = settings.ongoingBadge,
                onCheckedChange = onSetOngoingBadge,
            ),
            // Off until chosen. A reminder before Fajr on the days of Ramadan only, on the Islamic
            // date the user has set, through the same alarm ladder and silent mode rules as a
            // prayer alert. The row says nothing about dates: the help line inside does.
            SettingsItem.Link(
                key = "suhoor",
                icon = Icons.Outlined.Alarm,
                title = stringResource(R.string.settings_suhoor),
                subtitle = suhoorSummary(settings.suhoorReminderMinutes, settings.suhoorReminderStyle),
                onClick = { open = SettingsChooser.SUHOOR },
            ),
            // On by default and removable here, because not everyone fasts and the card is
            // daily for a month.
            SettingsItem.Toggle(
                key = "ramadan_card",
                icon = Icons.Outlined.NightsStay,
                title = stringResource(R.string.settings_ramadan_card),
                subtitle = stringResource(R.string.settings_ramadan_card_desc),
                checked = settings.ramadanCardEnabled,
                onCheckedChange = onSetRamadanCard,
            ),
        ),
    )

    // Play requires a privacy policy reachable from inside the app, not only from
    // the Console field. Opening the hosted policy satisfies that.
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    // Not a prompt: a row the user chooses to open. It hands over to their own email app, so
    // nothing is stored or sent by SajdaTime, and nothing is attached to the message.
    val feedbackEmail = stringResource(R.string.feedback_email)
    val feedbackSubject = stringResource(R.string.feedback_subject)
    val shareMessage = stringResource(R.string.about_share_message)
    val about = SettingsSection(
        SettingsSectionId.ABOUT,
        stringResource(R.string.settings_group_about),
        listOf(
            SettingsItem.Link(
                key = "version",
                title = stringResource(R.string.about_version),
                subtitle = versionName(context),
            ),
            SettingsItem.Link(
                key = "disclaimer",
                title = stringResource(R.string.about_disclaimer),
                subtitle = stringResource(R.string.about_disclaimer_short),
                onClick = { open = SettingsChooser.DISCLAIMER },
            ),
            SettingsItem.Link(
                key = "privacy",
                title = stringResource(R.string.about_privacy),
                subtitle = stringResource(R.string.about_privacy_desc),
                onClick = {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, privacyUrl.toUri()))
                    }
                },
            ),
            SettingsItem.Link(
                key = "feedback",
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
            ),
            // A door, never an ask: the user chooses to go. Opens the store page, not the
            // In-App Review card, because Google says that card must not sit behind a button
            // (it may silently not show once the quota is spent). See review/ReviewPrompt.kt.
            SettingsItem.Link(
                key = "rate",
                title = stringResource(R.string.about_rate),
                subtitle = stringResource(R.string.about_rate_desc),
                onClick = { ReviewPrompt.openStorePage(context) },
            ),
            SettingsItem.Link(
                key = "share",
                title = stringResource(R.string.about_share),
                subtitle = stringResource(R.string.about_share_desc),
                onClick = { ReviewPrompt.shareApp(context, shareMessage) },
            ),
            SettingsItem.Toggle(
                key = "usage_counts",
                title = stringResource(R.string.settings_usage_counts),
                subtitle = stringResource(R.string.settings_usage_counts_desc),
                checked = settings.analyticsEnabled,
                onCheckedChange = { wanted ->
                    if (wanted) askingUsageConsent = true else onSetAnalytics(false)
                },
            ),
            SettingsItem.Link(
                key = "charity",
                title = stringResource(R.string.about_charity),
                subtitle = stringResource(R.string.about_charity_desc),
            ),
            SettingsItem.Link(
                key = "credits",
                title = stringResource(R.string.about_credits),
                subtitle = stringResource(R.string.about_credits_desc),
            ),
            SettingsItem.Link(
                key = "data",
                title = stringResource(R.string.about_data),
                subtitle = stringResource(R.string.about_data_desc),
            ),
            // A licence obligation, like the row above: the Shia adhan is CC BY-SA 4.0, which requires
            // the credit and a link to the licence. The Sunni one is CC0 and credited anyway. Shown to
            // every user, because both files ship in every copy of the app.
            SettingsItem.Link(
                key = "adhan",
                title = stringResource(R.string.about_adhan),
                subtitle = stringResource(R.string.about_adhan_desc),
                links = listOf(
                    stringResource(R.string.about_adhan_link_source) to ADHAN_SOURCE,
                    stringResource(R.string.about_adhan_link_shia) to SHIA_ADHAN_SOURCE,
                    stringResource(R.string.about_adhan_link_licence) to CC_BY_SA_4,
                ),
            ),
        ),
    )

    val sections = listOf(prayerTimes, appearance, reminders, about)
    val title = stringResource(R.string.title_settings)
    SettingsList(title, banners, sections)

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

        SettingsChooser.ADJUSTMENTS -> AdjustmentsDialog(
            adjustments = settings.adjustments,
            hijriOffsetDays = settings.hijriOffsetDays,
            onSetAdjustment = onSetAdjustment,
            onSetHijriOffset = onSetHijriOffset,
            onReset = onResetAdjustments,
            onDismiss = { open = null },
        )

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
            showAlarmOptions = settings.usesAlarm,
            alarmOnApproximate = settings.alarmOnApproximateDays,
            hasApproximateDays = rememberHasApproximateDays(settings),
            onSetAlert = onSetAlert,
            onSetAlarmOnApproximate = onSetAlarmOnApproximateDays,
            onDismiss = { open = null },
        )

        SettingsChooser.NOTIFICATION_SOUND -> SoundDialog(
            title = stringResource(R.string.settings_notification_sound),
            help = stringResource(R.string.settings_notification_sound_help),
            sound = notificationSound,
            onDismiss = { open = null },
        )

        SettingsChooser.ALARM_SOUND -> SoundDialog(
            title = stringResource(R.string.settings_alarm_sound),
            help = stringResource(R.string.settings_alarm_sound_help),
            sound = alarmSound,
            onDismiss = { open = null },
        )

        SettingsChooser.PRAYER_REMINDER -> PrayerReminderDialog(
            minutes = settings.prayerReminderMinutes,
            slots = settings.prayerReminderFor,
            onSetMinutes = onSetPrayerReminderMinutes,
            onSetSlot = onSetPrayerReminder,
            onDismiss = { open = null },
        )

        SettingsChooser.SUHOOR -> SuhoorDialog(
            minutes = settings.suhoorReminderMinutes,
            style = settings.suhoorReminderStyle,
            onSetMinutes = onSetSuhoorMinutes,
            onSetStyle = onSetSuhoorStyle,
            onDismiss = { open = null },
        )

        SettingsChooser.LOCATION -> LocationSheet(
            state = state,
            onDismiss = { open = null },
            onUseGps = onRefreshLocation,
            onSearchCity = onSearchCity,
        )

        SettingsChooser.LANGUAGE -> {
            val current = AppLanguage.current(context)
            ChooserDialog(title = stringResource(R.string.settings_language_title), onDismiss = { open = null }) {
                Column(Modifier.selectableGroup()) {
                    AppLanguage.entries.forEach { language ->
                        val available = language.isAvailable(context)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .selectable(
                                    selected = language == current,
                                    enabled = available,
                                    role = Role.RadioButton,
                                    onClick = {
                                        AppLanguage.apply(context, language)
                                        open = null
                                    },
                                ),
                        ) {
                            RadioButton(selected = language == current, onClick = null, enabled = available)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    language.nativeName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (available) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!available) {
                                    Text(
                                        stringResource(R.string.language_unavailable),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SettingsChooser.DISCLAIMER -> AlertDialog(
            onDismissRequest = { open = null },
            confirmButton = {
                TextButton(onClick = { open = null }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
            title = { DialogTitle(stringResource(R.string.disclaimer_title)) { open = null } },
            // Scrollable for the same reason every chooser on this screen is.
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.disclaimer_body))
                }
            },
        )
    }
}

// --- what the screen shows, kept apart from how it is drawn ------------------------------

/**
 * One row of the Settings screen as data. The screen builds these and a renderer draws
 * them, so a different look can be drawn from exactly the same rows, the same strings and
 * the same handlers. `key` names the row for a renderer that treats one specially (the
 * version line, say); it never changes what the row does.
 */
internal sealed interface SettingsItem {
    val key: String
    val title: String

    /** A row that opens something, or, with no [onClick], a line of information. */
    data class Link(
        override val key: String,
        override val title: String,
        val subtitle: String? = null,
        val icon: ImageVector? = null,
        val onClick: (() -> Unit)? = null,
        /** Web links shown under a folded row's text, as (label, address). */
        val links: List<Pair<String, String>> = emptyList(),
    ) : SettingsItem

    data class Toggle(
        override val key: String,
        override val title: String,
        val subtitle: String?,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val icon: ImageVector? = null,
    ) : SettingsItem

    data class Theme(
        override val title: String,
        val current: ThemeChoice,
        val onSelect: (ThemeChoice) -> Unit,
    ) : SettingsItem {
        override val key: String get() = "theme"
    }
}

// The built-in adhans' sources and licence (docs/licences/ADHAN.md). Not translatable, so not strings.
private const val ADHAN_SOURCE = "https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg"
private const val SHIA_ADHAN_SOURCE = "https://commons.wikimedia.org/wiki/File:AzaanMaahur.ogg"
private const val CC_BY_SA_4 = "https://creativecommons.org/licenses/by-sa/4.0/"

internal enum class SettingsSectionId { PRAYER_TIMES, APPEARANCE, REMINDERS, ABOUT }

internal class SettingsSection(
    val id: SettingsSectionId,
    val title: String,
    val items: List<SettingsItem>,
)

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

/** "10 minutes before · All five", "10 minutes before · 2 of 5", or just "Off". */
@Composable
private fun prayerReminderSummary(minutes: Int, slots: Set<PrayerSlot>): String {
    if (minutes <= 0 || slots.isEmpty()) return stringResource(R.string.suhoor_off)
    val which = if (slots.size == 5) {
        stringResource(R.string.settings_which_prayers_all)
    } else {
        stringResource(R.string.settings_which_prayers_some, slots.size)
    }
    return stringResource(R.string.settings_value_pair, prayerReminderChoiceLabel(minutes), which)
}

@Composable
private fun prayerReminderChoiceLabel(minutes: Int): String =
    if (minutes <= 0) {
        stringResource(R.string.suhoor_off)
    } else {
        pluralStringResource(R.plurals.prayer_reminder_minutes_before, minutes, minutes)
    }

/** "30 minutes before Fajr · Notification", or just "Off". Plain words, no date logic. */
@Composable
private fun suhoorSummary(minutes: Int, style: AlertStyle): String {
    if (minutes <= 0) return stringResource(R.string.suhoor_off)
    val styleLabel = stringResource(
        if (style == AlertStyle.ALARM) R.string.alert_style_alarm else R.string.alert_style_notification,
    )
    return stringResource(R.string.settings_value_pair, suhoorChoiceLabel(minutes), styleLabel)
}

@Composable
private fun suhoorChoiceLabel(minutes: Int): String =
    if (minutes <= 0) {
        stringResource(R.string.suhoor_off)
    } else {
        pluralStringResource(R.plurals.suhoor_minutes_before, minutes, minutes)
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
            HorizontalDivider(Modifier.padding(vertical = Space.Related))
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
        Spacer(Modifier.height(Space.Group))
        MethodChoiceList(sect = sect, current = current, onSelect = onSelect)
    }
}

/**
 * Every prayer, and how each one announces itself. Off, a quiet notification, or a full
 * alarm — chosen per prayer, because "wake me for Fajr, do not shout at me for Dhuhr" is
 * the request this screen exists to answer.
 *
 * The sounds are not here: since 18 Oct 2026 each is its own Settings row (see the rows above), because
 * at the foot of this dialog the alarm sound sat below a fold and the owner never found it.
 */
@Composable
private fun AlertsDialog(
    alertFor: Map<PrayerSlot, AlertStyle>,
    /** True when any prayer or the suhoor reminder is an alarm, so the approximate days switch applies. */
    showAlarmOptions: Boolean,
    alarmOnApproximate: Boolean,
    hasApproximateDays: Boolean,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetAlarmOnApproximate: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ChooserDialog(title = stringResource(R.string.settings_alerts), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_alerts_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Group))

        PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
            AlertChoiceRow(
                label = stringResource(slot.labelRes),
                selected = alertFor[slot],
                onSelect = { onSetAlert(slot, it) },
            )
        }

        // Only for the few thousand people on earth this can ever apply to. Everyone
        // below the polar circles has no approximated days, so the switch would be a
        // question about astronomy they will never meet — and this app is used by
        // people who have never opened a settings screen. See rememberHasApproximateDays.
        if (showAlarmOptions && hasApproximateDays) {
            HorizontalDivider(Modifier.padding(vertical = Space.Related))
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

/**
 * The heads up before a prayer: how long before, then which prayers. Always a quiet notification,
 * so there is no style or sound here; the prayer alerts keep those.
 */
@Composable
private fun PrayerReminderDialog(
    minutes: Int,
    slots: Set<PrayerSlot>,
    onSetMinutes: (Int) -> Unit,
    onSetSlot: (PrayerSlot, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ChooserDialog(title = stringResource(R.string.settings_prayer_reminder), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_prayer_reminder_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Group))
        Column(Modifier.selectableGroup()) {
            AppSettings.PRAYER_REMINDER_CHOICES.forEach { choice ->
                RadioRow(
                    label = prayerReminderChoiceLabel(choice),
                    selected = minutes == choice,
                    onSelect = { onSetMinutes(choice) },
                )
            }
        }
        if (minutes > 0) {
            HorizontalDivider(Modifier.padding(vertical = Space.Related))
            PrayerSlot.entries.filter { it.isPrayer }.forEach { slot ->
                SwitchRow(
                    title = stringResource(slot.labelRes),
                    checked = slot in slots,
                    onCheckedChange = { onSetSlot(slot, it) },
                    horizontalPadding = 0.dp,
                )
            }
        }
    }
}

/**
 * What a sound chooser needs, for the notification sound or the alarm sound: the school decides which
 * built-in adhan is offered. Only the alarm carries "Play on silent" ([onSetRespectsSilent] non null):
 * a notification always follows the phone's silent and Do Not Disturb settings (owner, 18 Oct 2026).
 */
private class SoundChoice(
    val sect: Sect,
    val uri: String,
    @StringRes val defaultLabel: Int,
    val onPickPhone: () -> Unit,
    val onPickBuiltIn: (BuiltInAdhan) -> Unit,
    val respectsSilent: Boolean = true,
    val onSetRespectsSilent: ((Boolean) -> Unit)? = null,
)

/** What a sound row says is chosen: "Adhan", the phone sound's own name, or the phone's default. */
@Composable
private fun soundSummary(sound: SoundChoice): String {
    val builtIn = BuiltInAdhan.of(sound.uri)
    val title = rememberRingtoneTitle(sound.uri.takeIf { builtIn == null }.orEmpty())
    return when {
        builtIn != null -> stringResource(builtIn.labelRes)
        else -> title ?: stringResource(sound.defaultLabel)
    }
}

/** One sound: a line on when it plays, then the choices. */
@Composable
private fun SoundDialog(title: String, help: String, sound: SoundChoice, onDismiss: () -> Unit) {
    ChooserDialog(title = title, onDismiss = onDismiss) {
        Text(
            text = help,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Group))
        SoundChoices(sound)
    }
}

/**
 * A sound, for notifications or alarms: the built-in adhan for the
 * user's school (1.5.0; Sunni and Shia each have their own, data/BuiltInAdhan.kt), then "From your
 * phone" (the system picker: alarms, ringtones and any audio the user has added), as one radio group.
 * Under the phone row, the tone chosen there, because naming it is the only confirmation the pick
 * stuck. Then, for the alarm only, "Play on silent".
 *
 * The 1.5.0 "No Shia adhan yet" notice that stood here was removed the same day, when a Shia adhan
 * with a clean licence was found, as its own rule required.
 */
@Composable
private fun SoundChoices(sound: SoundChoice) {
    val builtIn = BuiltInAdhan.of(sound.uri)
    val phoneTitle = rememberRingtoneTitle(sound.uri.takeIf { builtIn == null }.orEmpty())
        ?: stringResource(sound.defaultLabel)
    Column(Modifier.selectableGroup()) {
        BuiltInAdhan.forSect(sound.sect).forEach { adhan ->
            RadioRow(
                label = stringResource(adhan.labelRes),
                // Any built-in adhan counts as this row: each school has one, and a stored adhan from the
                // other school (not reachable today, setSect moves it) must not leave the group unticked.
                selected = builtIn != null,
                onSelect = { sound.onPickBuiltIn(adhan) },
            )
        }
        RadioRow(
            label = stringResource(R.string.settings_alarm_sound_phone),
            supporting = phoneTitle.takeIf { builtIn == null },
            selected = builtIn == null,
            onSelect = sound.onPickPhone,
        )
    }
    sound.onSetRespectsSilent?.let { PlayOnSilentRow(sound.respectsSilent, it) }
}

/**
 * "Play on silent", off by default: a phone on silent, vibrate or Do Not Disturb gets the notification
 * and vibration only, unless the user turns this on. Positive wording over the old "Stay quiet when your
 * phone is silent", which read as a double negative beside an Alarm choice (owner, 17 Oct 2026). Stored
 * inverted, as `alarmRespectsSilent`, so every existing user keeps the choice they made.
 */
@Composable
private fun PlayOnSilentRow(respectsSilent: Boolean, onSetRespectsSilent: (Boolean) -> Unit) {
    SwitchRow(
        title = stringResource(R.string.settings_alarm_play_on_silent),
        subtitle = stringResource(R.string.settings_alarm_play_on_silent_desc),
        checked = !respectsSilent,
        onCheckedChange = { onSetRespectsSilent(!it) },
        horizontalPadding = 0.dp,
    )
}

/**
 * The suhoor reminder: off, or 15, 30, 45 or 60 minutes before Fajr, and how it announces itself.
 *
 * It is a second heads up and not a ruling: suhoor ends at Fajr, and the reminder is simply earlier
 * than that by the amount the person picked. Its sound is the Notification sound or Alarm sound row,
 * which appears in Settings as soon as the style here is chosen (18 Oct 2026: one place for each sound).
 */
@Composable
private fun SuhoorDialog(
    minutes: Int,
    style: AlertStyle,
    onSetMinutes: (Int) -> Unit,
    onSetStyle: (AlertStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    ChooserDialog(title = stringResource(R.string.settings_suhoor), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_suhoor_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Space.Group))
        Column(Modifier.selectableGroup()) {
            AppSettings.SUHOOR_CHOICES.forEach { choice ->
                RadioRow(
                    label = suhoorChoiceLabel(choice),
                    selected = minutes == choice,
                    onSelect = { onSetMinutes(choice) },
                )
            }
        }
        if (minutes > 0) {
            HorizontalDivider(Modifier.padding(vertical = Space.Related))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.Related),
                verticalArrangement = Arrangement.spacedBy(Space.Related),
                modifier = Modifier.selectableGroup(),
            ) {
                ChoiceChip(
                    label = stringResource(R.string.alert_style_notification),
                    selected = style == AlertStyle.NOTIFICATION,
                    onClick = { onSetStyle(AlertStyle.NOTIFICATION) },
                )
                ChoiceChip(
                    label = stringResource(R.string.alert_style_alarm),
                    selected = style == AlertStyle.ALARM,
                    onClick = { onSetStyle(AlertStyle.ALARM) },
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
    Column(Modifier.padding(vertical = Space.Related)) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(Space.Related))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Space.Related),
            verticalArrangement = Arrangement.spacedBy(Space.Related),
            modifier = Modifier.selectableGroup(),
        ) {
            ChoiceChip(
                label = stringResource(R.string.alert_style_off),
                spokenAs = stringResource(R.string.alert_choice_a11y, stringResource(R.string.alert_style_off), label),
                selected = selected == null,
                onClick = { onSelect(null) },
            )
            ChoiceChip(
                label = stringResource(R.string.alert_style_notification),
                spokenAs = stringResource(R.string.alert_choice_a11y, stringResource(R.string.alert_style_notification), label),
                selected = selected == AlertStyle.NOTIFICATION,
                onClick = { onSelect(AlertStyle.NOTIFICATION) },
            )
            ChoiceChip(
                label = stringResource(R.string.alert_style_alarm),
                spokenAs = stringResource(R.string.alert_choice_a11y, stringResource(R.string.alert_style_alarm), label),
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
        title = { DialogTitle(title, onDismiss) },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) { content() } },
    )
}

// --- small building blocks -------------------------------------------------------------

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        // Same voice as the Settings section titles (SettingsList): Quiet Glass retired the
        // letterspaced green label, and two dialogs still had it (research F22).
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(vertical = Space.Related)
            .semantics { heading() },
    )
}

/**
 * One option in a small horizontal set: the per-prayer alert picker and the suhoor style.
 *
 * `selectable`, not `clickable`: a screen reader should announce "selected" on the active
 * chip, and a set of chips is one choice rather than three independent buttons.
 */
@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit, spokenAs: String? = null) {
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
            // Five rows of "Notification, radio button" were indistinguishable by ear; this
            // says "Alarm for Fajr" (research D4).
            .then(if (spokenAs != null) Modifier.semantics { contentDescription = spokenAs } else Modifier)
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
            // toggleable so a screen reader hears "on" or "off" (research D3).
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
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
 * Steppers rather than a text field or a slider. A field invites "12:45" into a box that
 * wants "5" and needs validation, an error state and a keyboard; a slider cannot be landed
 * on an exact minute with a thumb. Two buttons and a number can only ever produce a legal
 * value, need no keyboard at all, and are the easiest thing on this screen to hit — which
 * matters, because the person reaching for this feature is often reading a printed
 * timetable in one hand.
 */
@Composable
private fun AdjustmentsDialog(
    adjustments: Map<PrayerSlot, Int>,
    hijriOffsetDays: Int,
    onSetAdjustment: (PrayerSlot, Int) -> Unit,
    onSetHijriOffset: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    ChooserDialog(title = stringResource(R.string.settings_adjustments), onDismiss = onDismiss) {
        Text(
            text = stringResource(R.string.settings_adjustments_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Space.Group),
        )

        PrayerSlot.entries.forEach { slot ->
            val minutes = adjustments[slot] ?: 0
            StepperRow(
                label = slot.label(context),
                value = minuteLabel(minutes),
                canDecrease = minutes > -CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
                canIncrease = minutes < CalculationPrefs.MAX_ADJUSTMENT_MINUTES,
                onDecrease = { onSetAdjustment(slot, minutes - 1) },
                onIncrease = { onSetAdjustment(slot, minutes + 1) },
            )
        }

        HorizontalDivider(Modifier.padding(vertical = Space.Related))
        SectionLabel(stringResource(R.string.settings_hijri_offset))
        Text(
            text = stringResource(R.string.settings_hijri_offset_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = Space.Group),
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
            // Its own row, full width (so centred), a Section below the steppers it resets: the
            // owner asked for it wider and easier to press (18 Oct 2026). Full width is also the
            // DESIGN_SYSTEM 4.1 rule for a button alone on its line.
            Spacer(Modifier.height(Space.Section))
            CtaButton(
                onClick = {
                    onReset()
                    onSetHijriOffset(0)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_adjustments_reset))
            }
        }
    }
}

/**
 * One label with a minus, a value and a plus.
 *
 * The value carries the whole row's accessibility label because a screen reader landing on
 * a bare "+5 min" between two icon buttons has no way to know which prayer it belongs to.
 */
@Composable
private fun StepperRow(
    label: String,
    value: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Space.Tight),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(onClick = onDecrease, enabled = canDecrease, colors = ctaIconButtonColors()) {
            Icon(
                Icons.Outlined.Remove,
                contentDescription = stringResource(R.string.action_decrease, label),
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            // Wide enough for "-30 min" so the plus button does not shuffle sideways as
            // the number grows a digit or loses its sign.
            modifier = Modifier.width(64.dp),
        )
        FilledIconButton(onClick = onIncrease, enabled = canIncrease, colors = ctaIconButtonColors()) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = stringResource(R.string.action_increase, label),
            )
        }
    }
}

/** "+5 min", "-3 min", or a dash for no change. */
@Composable
private fun minuteLabel(minutes: Int): String = when {
    minutes == 0 -> stringResource(R.string.adjustment_none)
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
