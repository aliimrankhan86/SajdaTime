package com.sajdatime.app.ui.onboarding

import com.sajdatime.app.ui.components.CtaButton
import com.sajdatime.app.ui.settings.LanguageChooserDialog
import com.sajdatime.app.ui.settings.AppLanguage
import androidx.compose.material.icons.outlined.Language
import com.sajdatime.app.data.placeName
import android.content.pm.PackageManager
import android.Manifest
import androidx.core.content.ContextCompat
import com.sajdatime.app.ui.theme.Space
import com.sajdatime.app.ui.components.DialogTitle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.foundation.selection.selectableGroup
import androidx.activity.compose.BackHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.selection.selectable
import android.os.Build
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.sajdatime.app.ui.components.PermissionButton
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.Sect
import com.sajdatime.core.labelRes
import com.sajdatime.app.ui.LocationProblem
import com.sajdatime.app.ui.UiState
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.data.SetupStep
import com.sajdatime.app.ui.components.MethodChoiceList
import com.sajdatime.app.ui.components.UsageCountsConsentBody
import com.sajdatime.app.ui.components.ProgressRow
import com.sajdatime.app.ui.components.SectionHeading

/**
 * Internal, not private, so a test can prove the mapping below never depends on the user's sect.
 *
 * CONSENT sits just before CONFIRM since 17 Oct 2026 (owner's decision, research F1). It used to
 * be the second screen, so a new user met a privacy decision before seeing one prayer time. The
 * steps passed before it are counted after a Yes instead ([stepsBeforeConsent]); the disclaimer
 * still comes after CONFIRM, so consent never follows it. Order here is the order on screen.
 */
internal enum class Step { WELCOME, PERMISSION, SECT, MADHAB, METHOD, CONSENT, CONFIRM }

/**
 * The counted steps every path passes before CONSENT, in order, reported once the user says yes.
 * Fixed rather than remembered: every path goes through all three, MADHAB counts as nothing on
 * either path, so the list cannot reveal anything the live path would not have.
 */
internal val stepsBeforeConsent = listOf(Step.PERMISSION, Step.SECT, Step.METHOD).mapNotNull { it.setupStep() }

/**
 * Which usage-count step, if any, an onboarding step reports. **MADHAB maps to nothing, on
 * purpose**: only Sunni users are shown it, so reporting it would reveal the user's sect. The
 * same goes for any step only one sect sees. WELCOME and CONSENT come before the user has agreed
 * to anything. See docs/ANALYTICS_PLAN.md section 4.3, and `UsageCountsTest`.
 */
internal fun Step.setupStep(): SetupStep? = when (this) {
    Step.WELCOME, Step.CONSENT, Step.MADHAB -> null
    Step.PERMISSION -> SetupStep.PERMISSION
    Step.SECT -> SetupStep.SECT
    Step.METHOD -> SetupStep.METHOD
    Step.CONFIRM -> SetupStep.CONFIRM
}

@Composable
fun OnboardingScreen(
    state: UiState,
    onRequestLocationPermission: () -> Unit,
    onSearchCity: (String) -> Unit,
    onUseDefaultLocation: () -> Unit,
    onSelectSect: (Sect) -> Unit,
    onSelectMadhab: (Madhab) -> Unit,
    onSelectMethod: (CalcMethod) -> Unit,
    onFinish: () -> Unit,
    onSetAnalytics: (Boolean) -> Unit,
    onSetupStep: (SetupStep) -> Unit,
) {
    var step by rememberSaveable { mutableStateOf(Step.WELCOME) }

    // System Back steps back through setup instead of closing the app (research review, 17 Oct
    // 2026). Welcome has nowhere to go back to, so there Back leaves as usual.
    BackHandler(enabled = step != Step.WELCOME) {
        step = when (step) {
            Step.WELCOME, Step.PERMISSION -> Step.WELCOME
            Step.SECT -> Step.PERMISSION
            Step.MADHAB -> Step.SECT
            Step.METHOD -> if (state.settings.sect == Sect.SUNNI) Step.MADHAB else Step.SECT
            Step.CONSENT -> Step.METHOD
            // Consent is skipped going forward when the answer is already yes, so likewise back.
            Step.CONFIRM -> if (state.settings.analyticsEnabled) Step.METHOD else Step.CONSENT
        }
    }

    // Usage counts, opt-in only (a no-op otherwise). Once per step per run: see UsageCounts.
    LaunchedEffect(step) { step.setupStep()?.let(onSetupStep) }

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
                // Skipped when the choice is already yes (for example after the process was
                // killed mid-setup), so nobody is asked to consent twice.
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
                        step = if (state.settings.sect == Sect.SUNNI) Step.MADHAB else Step.METHOD
                    },
                    onBack = { step = Step.PERMISSION },
                )

                Step.MADHAB -> MadhabStep(
                    selected = state.settings.madhab,
                    onSelect = onSelectMadhab,
                    onNext = { step = Step.METHOD },
                    onBack = { step = Step.SECT },
                )

                // Asked of everyone, once, with "not sure" preselected. Until 15 Aug 2026
                // the method was never mentioned at setup, so a user who did not open
                // Settings could not reach it — and measured with the app's own engine the
                // default is 10-20 minutes from local practice across North America and
                // the Gulf, and 8 minutes late on Fajr in Indonesia, none of which the
                // far-north banner covers. One tap costs the unsure user nothing; the
                // concept is now something they have seen once. See HANDOVER §10.
                Step.METHOD -> MethodStep(
                    sect = state.settings.sect,
                    selected = state.settings.method,
                    onSelect = onSelectMethod,
                    // Skipped when the choice is already yes (for example after the process was
                    // killed mid-setup), so nobody is asked to consent twice.
                    onNext = { step = if (state.settings.analyticsEnabled) Step.CONFIRM else Step.CONSENT },
                    onBack = {
                        step = if (state.settings.sect == Sect.SUNNI) Step.MADHAB else Step.SECT
                    },
                )

                Step.CONSENT -> ConsentStep(
                    onYes = {
                        // Collection is on before this returns (setAnalyticsEnabled), so the
                        // replay below is sent; UsageCounts still sends each step once per run.
                        onSetAnalytics(true)
                        stepsBeforeConsent.forEach(onSetupStep)
                        step = Step.CONFIRM
                    },
                    onNo = {
                        onSetAnalytics(false)
                        step = Step.CONFIRM
                    },
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
    leading: (@Composable () -> Unit)? = null,
    /**
     * At the foot of the screen when there is room, and simply after the content when there is
     * not (large text, or the keyboard up), so it never squeezes the step or covers the city box.
     * Pinning it outside the scroll was tried first and took a quarter of the screen at 2x text.
     */
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    // imePadding *outside* verticalScroll, so the keyboard shrinks the scrolling viewport
    // rather than being drawn over the end of it. The other order compiles, looks identical in
    // a preview, and leaves the last control permanently under the keyboard. enableEdgeToEdge
    // means adjustResize in the manifest no longer moves the window on its own, so without this
    // the city field on the location step is typed into blind. See HANDOVER §15.
    BoxWithConstraints(Modifier.fillMaxSize().imePadding()) {
        val viewport = maxHeight
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .heightIn(min = viewport)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Column {
                    if (leading != null) {
                        leading()
                        Spacer(Modifier.height(Space.Group))
                    }
                    Text(text = title, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(Space.Paragraph))
                    // The consent step carries its own, longer text inside [content], so it passes none.
                    if (body.isNotEmpty()) {
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(Space.Section))
                    }
                    content()
                }
                if (footer != null) {
                    Column(Modifier.padding(top = Space.Section)) { footer() }
                }
            }
        }
    }
}

@Composable
private fun ConsentStep(onYes: () -> Unit, onNo: () -> Unit) {
    StepScaffold(
        title = stringResource(R.string.consent_title),
        body = "",
    ) {
        UsageCountsConsentBody(onYes = onYes, onNo = onNo)
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val bismillahSpoken = stringResource(R.string.bismillah_a11y)
    val context = LocalContext.current
    var pickingLanguage by rememberSaveable { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
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
            Spacer(Modifier.height(Space.Section))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.Paragraph))
            Text(
                text = stringResource(R.string.welcome_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Space.Section))
            CtaButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.action_begin))
            }
        }
        // A person who cannot read this screen must be able to change it before the first question,
        // not after (8 Oct 2026 branch). The owner chose this look from screenshots on 19 Oct 2026:
        // a globe with the language's own name, in the top corner, out of the way of Begin. Rejected:
        // a bare globe icon (easy to miss, a new kind of control) and a "Language" button under Begin
        // (the word is in English). The globe carries the spoken label for screen readers.
        CtaButton(
            onClick = { pickingLanguage = true },
            compact = true,
            modifier = Modifier.align(Alignment.TopEnd).padding(Space.Group),
        ) {
            Icon(
                Icons.Outlined.Language,
                contentDescription = stringResource(R.string.settings_language_title),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(Space.Related))
            Text(AppLanguage.showing(context).nativeName)
        }
    }
    if (pickingLanguage) LanguageChooserDialog(onDismiss = { pickingLanguage = false })
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
            title = { DialogTitle(stringResource(R.string.action_how_location_used)) { explaining = false } },
            // The full detail for both routes, one tap away (owner, 18 Oct 2026: a short setup
            // screen). The city box carries a one line summary of the second part, always visible.
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.permission_why_body))
                    Spacer(Modifier.height(Space.Paragraph))
                    Text(stringResource(R.string.city_lookup_detail))
                }
            },
        )
    }

    // 18 Oct 2026, the owner did not like this screen. It opened on 66 words about the city
    // lookup and two greyed out buttons, so the one choice it asks for was hard to see. Now:
    // a pin, one line, one big Allow button, and the detail behind a second button. Find city appears
    // once there is something to find, and Continue once there is somewhere to continue
    // with, so nothing on the screen is ever a dead grey button.
    StepScaffold(
        title = stringResource(R.string.permission_title),
        body = stringResource(R.string.permission_body),
        leading = { LocationBadge() },
        // Nobody should be stuck at setup. If neither route works, Makkah keeps the app usable
        // and the main screen says clearly that this is what happened. At the foot of the
        // screen, under a line saying what it means (owner, 18 Oct 2026, DESIGN_SYSTEM §9): as
        // a solid button beside Allow it looked as good a choice as Allow, and a person in
        // London who took it would get times hours out. It says "Skip", so it moves on; it used
        // to tick Makkah and stay, leaving the user under the same Allow button (research F3).
        footer = if (located) null else {
            {
                Text(
                    text = stringResource(R.string.setup_makkah_note),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Space.Related))
                CtaButton(
                    onClick = {
                        onUseDefaultLocation()
                        onNext()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_use_makkah))
                }
            }
        },
    ) {
        CtaButton(
            onClick = onAllow,
            enabled = !state.resolvingLocation,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
        ) {
            Icon(Icons.Outlined.LocationOn, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_allow_location))
        }
        // Solid like every action (owner, 18 Oct 2026), Related below the button it explains.
        Spacer(Modifier.height(Space.Related))
        CtaButton(
            onClick = { explaining = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.action_how_location_used))
        }

        // Without this the buttons simply grey out while a fix is awaited, which reads as
        // a frozen screen. Location can legitimately take several seconds indoors.
        if (state.resolvingLocation && !searchingCity) {
            Spacer(Modifier.height(Space.Related))
            ProgressRow(stringResource(R.string.location_finding))
        }

        if (located && !state.resolvingLocation) {
            Spacer(Modifier.height(Space.Group))
            LocatedRow(cityName = state.settings.placeName(LocalContext.current))
        }

        // Denying used to change nothing visible at all (research F7).
        if (state.problem == LocationProblem.NO_FIX || state.problem == LocationProblem.PERMISSION_DENIED) {
            Spacer(Modifier.height(Space.Group))
            Text(
                text = stringResource(
                    if (state.problem == LocationProblem.NO_FIX) R.string.location_no_fix else R.string.location_permission_denied,
                ),
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
            Spacer(Modifier.height(Space.Section))
            SectionHeading(stringResource(R.string.location_sheet_search_heading))
            Spacer(Modifier.height(Space.Related))
            val notFound = state.problem == LocationProblem.CITY_NOT_FOUND || state.problem == LocationProblem.OFFLINE
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text(stringResource(R.string.city_field_label)) },
                supportingText = {
                    Column {
                        Text(
                            text = stringResource(
                                if (state.problem == LocationProblem.OFFLINE) R.string.city_lookup_offline else if (notFound) R.string.city_not_found else R.string.city_field_helper,
                            ),
                        )
                        // What happens to a typed name, in one line, always on screen before anyone
                        // can type, so nobody searches without being told (independent review,
                        // 18 Oct 2026). Inside the field's own hint area so it lines up with the
                        // example and is read with the field. The detail (the phone's place lookup,
                        // usually Google, else once to a free service) is behind "How your location
                        // is used". Rejected: the 66 word paragraph above the field (it buried the
                        // screen's one choice) and showing it only once typing starts (text jumping
                        // in under the cursor; the owner found it confusing). Its colour stays
                        // neutral when the field shows an error, so it never reads as the error.
                        Text(
                            text = stringResource(R.string.city_lookup_note),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                isError = notFound,
                singleLine = true,
                // The keyboard's own search key is the first thing a user reaches for,
                // and without this it only closed the keyboard.
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (city.isNotBlank()) findCity() }),
                modifier = Modifier.fillMaxWidth(),
            )
            if (city.isNotBlank()) {
                Spacer(Modifier.height(Space.Related))
                CtaButton(
                    onClick = findCity,
                    enabled = !state.resolvingLocation,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                    Spacer(Modifier.width(Space.Related))
                    Text(stringResource(R.string.action_find_city))
                }
            }

            // Said next to the field it belongs to, and in its own words. The one spinner
            // this screen had said "Finding your location…" beside the GPS button, which
            // is the wrong sentence in the wrong place for a typed-in city.
            if (searchingCity && state.resolvingLocation) {
                Spacer(Modifier.height(Space.Related))
                ProgressRow(stringResource(R.string.location_looking_up))
            }

        }

        if (located) {
            Spacer(Modifier.height(Space.Section))
            CtaButton(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
            ) {
                Text(stringResource(R.string.action_continue))
            }
        }
    }
}

/** The setup location step's picture: a pin on a soft green disc. Decorative, so TalkBack skips it. */
@Composable
private fun LocationBadge() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
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
    Spacer(Modifier.height(Space.Section))
    Row {
        CtaButton(
            onClick = onBack,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
        ) {
            Text(stringResource(R.string.action_back))
        }
        // Two buttons side by side: Related (DESIGN_SYSTEM 2).
        Spacer(Modifier.width(Space.Related))
        CtaButton(
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
        Column(Modifier.selectableGroup()) {
        ChoiceCard(
            title = stringResource(R.string.sect_sunni),
            subtitle = stringResource(R.string.sect_sunni_desc),
            selected = selected == Sect.SUNNI,
            onClick = { onSelect(Sect.SUNNI) },
        )
        Spacer(Modifier.height(Space.Paragraph))
        ChoiceCard(
            title = stringResource(R.string.sect_shia),
            subtitle = stringResource(R.string.sect_shia_desc),
            selected = selected == Sect.SHIA,
            onClick = { onSelect(Sect.SHIA) },
        )
        }
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
        Column(Modifier.selectableGroup()) {
        Madhab.entries.forEachIndexed { i, madhab ->
            // Between the cards only: after the last one, StepButtons brings its own section gap.
            if (i > 0) Spacer(Modifier.height(Space.Paragraph))
            ChoiceCard(
                title = madhabLabel(madhab),
                subtitle = stringResource(
                    if (madhab == Madhab.HANAFI) R.string.madhab_hanafi_desc
                    else R.string.madhab_standard_desc,
                ),
                selected = selected == madhab,
                onClick = { onSelect(madhab) },
            )
        }
        }
        // Skip used to sit here next to Back. It was removed rather than relabelled: one of
        // the four is always already selected, so Skip and Continue did the same thing while
        // looking like a choice between keeping and discarding an answer.
        StepButtons(onBack = onBack, onNext = onNext)
    }
}

@Composable
private fun MethodStep(
    sect: Sect,
    selected: CalcMethod,
    onSelect: (CalcMethod) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    StepScaffold(
        title = stringResource(R.string.method_step_title),
        body = stringResource(R.string.method_step_body),
    ) {
        // The same list Settings shows, so what was chosen here is what is found there.
        MethodChoiceList(sect = sect, current = selected, onSelect = onSelect)
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
                    state.settings.placeName(LocalContext.current).ifBlank {
                        stringResource(R.string.location_set_generic)
                    },
                )
                Spacer(Modifier.height(Space.Paragraph))
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
                Spacer(Modifier.height(Space.Paragraph))
                SummaryRow(
                    stringResource(R.string.settings_method),
                    // Same wording as the Settings row, so "Automatic · Muslim World League"
                    // reads identically in both places.
                    if (state.settings.method == CalcMethod.AUTO) {
                        stringResource(
                            R.string.settings_method_auto,
                            stringResource(PrayerEngine.resolveMethod(state.settings.calculationPrefs).labelRes),
                        )
                    } else {
                        stringResource(state.settings.method.labelRes)
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
        //
        // Android 12 and later only: before that there is no such permission, and this block
        // told a third of the installed base about a setting their phone does not have
        // (research F5). Settings has carried the same gate since the start.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(Modifier.height(Space.Section))
            SectionHeading(stringResource(R.string.confirm_exact_alarm_title))
            Spacer(Modifier.height(Space.Tight))
            Text(
                text = stringResource(R.string.confirm_exact_alarm_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Space.Related))
            PermissionButton(
                label = stringResource(R.string.settings_exact_alarms_title),
                granted = exactAllowed,
                onClick = { PrayerAlarmScheduler.requestExactAlarmPermission(context) },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Android 13+ asks about notifications the moment Finish is tapped, and every prayer
        // alert depends on the answer. Said here, before the system box appears (research F6).
        // Only when the system box will actually appear: not once notifications are already allowed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Spacer(Modifier.height(Space.Section))
            Text(
                text = stringResource(R.string.confirm_notifications_hint, stringResource(R.string.action_finish)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(Space.Section))
        CtaButton(
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
        Spacer(Modifier.height(Space.Tight))
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
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
        // A radio choice, so a screen reader says "selected" as a state, once, instead of
        // reading the word "Selected" out of the tick as part of the text (research D5).
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardDefaults.shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(Space.Tight))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
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
