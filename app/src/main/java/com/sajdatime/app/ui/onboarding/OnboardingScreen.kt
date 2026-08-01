package com.sajdatime.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.Madhab
import com.sajdatime.core.Sect
import com.sajdatime.app.ui.LocationProblem
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.ui.components.ProgressRow
import com.sajdatime.app.ui.components.SectionHeading

private enum class Step { WELCOME, PERMISSION, SECT, MADHAB, CONFIRM }

@Composable
fun OnboardingScreen(
    state: UiState,
    onRequestLocationPermission: () -> Unit,
    onSearchCity: (String) -> Unit,
    onUseDefaultLocation: () -> Unit,
    onSelectSect: (Sect) -> Unit,
    onSelectMadhab: (Madhab) -> Unit,
    onFinish: () -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(Step.WELCOME) }

    Scaffold { padding ->
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                // Forward moves left, back moves right — spatial continuity.
                val forward = targetState.ordinal > initialState.ordinal
                val offset = if (forward) 1 else -1
                (slideInHorizontally { it / 6 * offset } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it / 6 * offset } + fadeOut()) using SizeTransform(clip = false)
            },
            label = "onboarding-step",
            modifier = Modifier.padding(padding),
        ) { current ->
            when (current) {
                Step.WELCOME -> WelcomeStep(onNext = { step = Step.PERMISSION })

                Step.PERMISSION -> PermissionStep(
                    state = state,
                    onAllow = onRequestLocationPermission,
                    onSearchCity = onSearchCity,
                    onUseDefaultLocation = onUseDefaultLocation,
                    onNext = { step = Step.SECT },
                )

                // Selecting only selects. Every step is left by pressing the button, never
                // by tapping a card. These two used to advance the moment a card was
                // touched, which threw the user forward before they had decided and left
                // Sect with no button on screen at all — a tester reported not knowing
                // whether to tap or to press Next, and he was right. See HANDOVER §15.
                Step.SECT -> SectStep(
                    selected = state.settings.sect,
                    onSelect = onSelectSect,
                    onNext = {
                        step = if (state.settings.sect == Sect.SUNNI) Step.MADHAB else Step.CONFIRM
                    },
                    onBack = { step = Step.PERMISSION },
                )

                Step.MADHAB -> MadhabStep(
                    selected = state.settings.madhab,
                    onSelect = onSelectMadhab,
                    onNext = { step = Step.CONFIRM },
                    onBack = { step = Step.SECT },
                )

                Step.CONFIRM -> ConfirmStep(state = state, onFinish = onFinish)
            }
        }
    }
}

@Composable
private fun StepScaffold(
    title: String,
    body: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // imePadding *outside* verticalScroll, so the keyboard shrinks the scrolling
            // viewport rather than being drawn over the end of it. The other order
            // compiles, looks identical in a preview, and leaves the last control
            // permanently under the keyboard. enableEdgeToEdge means adjustResize in the
            // manifest no longer moves the window on its own, so without this the city
            // field on the location step is typed into blind. See HANDOVER §15.
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(28.dp))
        content()
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val bismillahSpoken = stringResource(R.string.bismillah_a11y)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.bismillah_arabic),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            // Screen readers should announce the meaning, not attempt the Arabic glyphs
            // in the user's own locale voice.
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = bismillahSpoken
            },
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.welcome_tagline),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_begin))
        }
    }
}

@Composable
private fun PermissionStep(
    state: UiState,
    onAllow: () -> Unit,
    onSearchCity: (String) -> Unit,
    onUseDefaultLocation: () -> Unit,
    onNext: () -> Unit,
) {
    var explaining by rememberSaveable { mutableStateOf(false) }
    var city by rememberSaveable { mutableStateOf("") }
    var searchingCity by rememberSaveable { mutableStateOf(false) }
    val located = state.settings.coordinates != null
    val keyboard = LocalSoftwareKeyboardController.current

    // The search has finished when the view model stops resolving. Keyed on that flag
    // alone, so the effect can only act on the true -> false edge and never on the gap
    // between the tap and the coroutine starting.
    LaunchedEffect(state.resolvingLocation) {
        if (searchingCity && !state.resolvingLocation) searchingCity = false
    }

    val findCity = {
        keyboard?.hide()
        searchingCity = true
        onSearchCity(city)
    }

    if (explaining) {
        AlertDialog(
            onDismissRequest = { explaining = false },
            confirmButton = {
                TextButton(onClick = { explaining = false }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
            title = { Text(stringResource(R.string.permission_why_title)) },
            text = { Text(stringResource(R.string.permission_why_body)) },
        )
    }

    StepScaffold(
        title = stringResource(R.string.permission_title),
        body = stringResource(R.string.permission_body),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onAllow,
                enabled = !state.resolvingLocation,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            ) {
                Icon(Icons.Outlined.LocationOn, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.action_allow_location))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = { explaining = true },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.permission_why_title),
                )
            }
        }

        // Without this the buttons simply grey out while a fix is awaited, which reads as
        // a frozen screen. Location can legitimately take several seconds indoors.
        if (state.resolvingLocation && !searchingCity) {
            Spacer(Modifier.height(20.dp))
            ProgressRow(stringResource(R.string.location_finding))
        }

        if (located && !state.resolvingLocation) {
            Spacer(Modifier.height(20.dp))
            LocatedRow(cityName = state.settings.cityName)
        }

        if (state.problem == LocationProblem.NO_FIX) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.location_no_fix),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        // Offered until a location exists, and gated on nothing else.
        //
        // This used to appear only while `problem` was PERMISSION_DENIED or NO_FIX, which
        // made the whole block — field, button, error message and the Makkah escape —
        // disappear the instant "Find city" was pressed, because searching sets
        // `problem` to null and a failure sets it to CITY_NOT_FOUND. Neither is in that
        // pair. A user who declined location and mistyped a city was left on a screen
        // with a disabled Continue and no way forward at all, and the "we could not find
        // that place" message they needed was inside the block that had just vanished.
        // Reproduced on device before the fix, and again after. See HANDOVER §10.
        //
        // Showing it up front also answers T3's fourth point: manual entry was always
        // there and nobody could find it.
        if (!located) {
            Spacer(Modifier.height(28.dp))
            SectionHeading(stringResource(R.string.location_sheet_search_heading))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.city_fallback_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            val notFound = state.problem == LocationProblem.CITY_NOT_FOUND
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(stringResource(R.string.city_field_label)) },
                supportingText = {
                    Text(
                        text = stringResource(
                            if (notFound) R.string.city_not_found else R.string.city_field_helper,
                        ),
                    )
                },
                isError = notFound,
                singleLine = true,
                // The keyboard's own search key is the first thing a user reaches for,
                // and without this it only closed the keyboard.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (city.isNotBlank()) findCity() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = findCity,
                enabled = city.isNotBlank() && !state.resolvingLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.action_find_city))
            }

            // Said next to the field it belongs to, and in its own words. The one spinner
            // this screen had said "Finding your location…" beside the GPS button, which
            // is the wrong sentence in the wrong place for a typed-in city.
            if (searchingCity && state.resolvingLocation) {
                Spacer(Modifier.height(12.dp))
                ProgressRow(stringResource(R.string.location_looking_up))
            }

            // Nobody should be stuck at setup. If neither route works, Makkah keeps the
            // app usable and the main screen says clearly that this is what happened.
            Spacer(Modifier.height(20.dp))
            TextButton(
                onClick = onUseDefaultLocation,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.action_use_makkah))
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNext,
            enabled = located,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun LocatedRow(cityName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = cityName.ifBlank { stringResource(R.string.location_set_generic) },
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * Back on the left, Continue on the right, on every step that has a choice.
 *
 * One shape, used everywhere, because the tester complaint was not "this button is wrong"
 * but "I cannot tell whether to tap or to press something". Two steps answered that
 * differently from the other three, and one of them showed no button at all.
 */
@Composable
private fun StepButtons(onBack: () -> Unit, onNext: () -> Unit) {
    Spacer(Modifier.height(8.dp))
    Row {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_back))
        }
        Spacer(Modifier.width(12.dp))
        Button(
            onClick = onNext,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_continue))
        }
    }
}

@Composable
private fun SectStep(
    selected: Sect,
    onSelect: (Sect) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    StepScaffold(
        title = stringResource(R.string.sect_title),
        body = stringResource(R.string.sect_body),
    ) {
        ChoiceCard(
            title = stringResource(R.string.sect_sunni),
            subtitle = stringResource(R.string.sect_sunni_desc),
            selected = selected == Sect.SUNNI,
            onClick = { onSelect(Sect.SUNNI) },
        )
        Spacer(Modifier.height(12.dp))
        ChoiceCard(
            title = stringResource(R.string.sect_shia),
            subtitle = stringResource(R.string.sect_shia_desc),
            selected = selected == Sect.SHIA,
            onClick = { onSelect(Sect.SHIA) },
        )
        StepButtons(onBack = onBack, onNext = onNext)
    }
}

@Composable
private fun MadhabStep(
    selected: Madhab,
    onSelect: (Madhab) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    StepScaffold(
        title = stringResource(R.string.madhab_title),
        body = stringResource(R.string.madhab_body),
    ) {
        Madhab.entries.forEach { madhab ->
            ChoiceCard(
                title = madhabLabel(madhab),
                subtitle = stringResource(
                    if (madhab == Madhab.HANAFI) R.string.madhab_hanafi_desc
                    else R.string.madhab_standard_desc,
                ),
                selected = selected == madhab,
                onClick = { onSelect(madhab) },
            )
            Spacer(Modifier.height(12.dp))
        }
        // Skip used to sit here next to Back. It was removed rather than relabelled: one of
        // the four is always already selected, so Skip and Continue did the same thing while
        // looking like a choice between keeping and discarding an answer.
        StepButtons(onBack = onBack, onNext = onNext)
    }
}

@Composable
private fun ConfirmStep(state: UiState, onFinish: () -> Unit) {
    val context = LocalContext.current
    // Recomposed when the step is re-entered after the system screen returns, so the
    // offer disappears once the permission has actually been granted rather than
    // continuing to ask for something the user has already done.
    var exactAllowed by remember { mutableStateOf(PrayerAlarmScheduler.canScheduleExact(context)) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        exactAllowed = PrayerAlarmScheduler.canScheduleExact(context)
    }

    StepScaffold(
        title = stringResource(R.string.confirm_title),
        body = stringResource(R.string.confirm_body),
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                SummaryRow(
                    stringResource(R.string.label_location),
                    state.settings.cityName.ifBlank {
                        stringResource(R.string.location_set_generic)
                    },
                )
                Spacer(Modifier.height(12.dp))
                SummaryRow(
                    stringResource(R.string.label_school),
                    when (state.settings.sect) {
                        // settings_value_pair, not string concatenation: Settings already
                        // pairs these two the same way, and the separator belongs to the
                        // translator rather than to this line.
                        Sect.SUNNI -> stringResource(
                            R.string.settings_value_pair,
                            stringResource(R.string.sect_sunni),
                            madhabLabel(state.settings.madhab),
                        )
                        Sect.SHIA -> stringResource(R.string.sect_shia)
                    },
                )
            }
        }

        // Asked once, here, because a prayer alert that arrives late is not a prayer
        // alert. From Android 13 "Alarms & reminders" is *denied by default*, and without
        // it the app can only fall back to an alarm the system is documented as being
        // free to move — Google publishes no upper bound on how late that can be. The
        // banners on Times and Settings were the only mention of this, and a banner about
        // a setting is not the same as being asked.
        //
        // Deliberately not a blocker: Finish is right underneath, unconditional, and the
        // app works without it. See HANDOVER §10.
        if (!exactAllowed) {
            Spacer(Modifier.height(24.dp))
            SectionHeading(stringResource(R.string.confirm_exact_alarm_title))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.confirm_exact_alarm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
            ) {
                Text(stringResource(R.string.settings_exact_alarms_title))
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_finish))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ChoiceCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { /* role and selection are conveyed by the check icon + text */ },
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.state_selected),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
internal fun madhabLabel(madhab: Madhab): String = stringResource(
    when (madhab) {
        Madhab.HANAFI -> R.string.madhab_hanafi
        Madhab.SHAFII -> R.string.madhab_shafii
        Madhab.MALIKI -> R.string.madhab_maliki
        Madhab.HANBALI -> R.string.madhab_hanbali
    },
)
