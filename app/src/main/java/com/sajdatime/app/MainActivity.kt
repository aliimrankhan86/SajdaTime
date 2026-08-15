package com.sajdatime.app

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajdatime.core.AppLocale
import com.sajdatime.app.ui.ExportEvent
import com.sajdatime.app.ui.MainScaffold
import com.sajdatime.app.ui.SajdaViewModel
import com.sajdatime.app.ui.onboarding.OnboardingScreen
import com.sajdatime.app.ui.theme.SajdaTimeTheme

class MainActivity : ComponentActivity() {

    /**
     * Every screen in the app renders through this Activity, so pinning its configuration
     * to the app's own language covers the whole UI in one place. See AppLocale.kt for why
     * that is necessary at all, and for what it deliberately does not do.
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    private val viewModel: SajdaViewModel by viewModels()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.refreshLocation() }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Declining only silences alerts; times still display. */ }

    /**
     * The system ringtone picker. It already lists alarms, ringtones and any audio the
     * user has added themselves, so there is no need to bundle an adhan recording —
     * which would also raise licensing questions and inflate a charity app's download.
     */
    private val alarmSoundPicker = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uri = result.data
            ?.getParcelableExtraCompat<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        viewModel.setAlarmSound(uri?.toString().orEmpty())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            // Read outside the theme, not inside it: the choice decides which scheme the
            // theme builds, so it cannot come from within the theme's own content.
            SajdaTimeTheme(choice = state.settings.themeChoice) {

                // The export result is a user-visible side effect, so it fires once per
                // attempt and is then consumed. Failures are reported too: a tap that
                // silently does nothing is worse than an error.
                LaunchedEffect(state.exportEvent) {
                    when (val event = state.exportEvent) {
                        null -> Unit
                        is ExportEvent.Share -> sharePdf(event.uri)
                        is ExportEvent.Saved -> toast(getString(R.string.export_saved, event.fileName))
                        ExportEvent.Failed -> toast(getString(R.string.export_failed))
                    }
                    if (state.exportEvent != null) viewModel.consumeExportEvent()
                }

                PreviewBuildFrame {
                    when {
                        state.loading -> Unit

                        !state.settings.onboardingComplete -> OnboardingScreen(
                            state = state,
                            onRequestLocationPermission = ::requestLocation,
                            onSearchCity = viewModel::searchCity,
                            onUseDefaultLocation = viewModel::useDefaultLocation,
                            onSelectSect = viewModel::setSect,
                            onSelectMadhab = viewModel::setMadhab,
                            onSelectMethod = viewModel::setMethod,
                            onFinish = {
                                requestNotificationsIfNeeded()
                                viewModel.completeOnboarding()
                            },
                        )

                        else -> {
                            MainScaffold(
                                state = state,
                                onExport = viewModel::exportPdf,
                                onSetSect = viewModel::setSect,
                                onSetMadhab = viewModel::setMadhab,
                                onSetMethod = viewModel::setMethod,
                                onSetAlert = viewModel::setAlert,
                                onSetAdjustment = viewModel::setAdjustment,
                                onSetHijriOffset = viewModel::setHijriOffsetDays,
                                onResetAdjustments = viewModel::resetAdjustments,
                                onSetOngoingBadge = viewModel::setOngoingBadge,
                                onSetAlarmRespectsSilent = viewModel::setAlarmRespectsSilent,
                                onSetAlarmOnApproximateDays = viewModel::setAlarmOnApproximateDays,
                                onPickAlarmSound = {
                                    openAlarmSoundPicker(state.settings.alarmSoundUri)
                                },
                                onRefreshLocation = ::requestLocation,
                                onSearchCity = viewModel::searchCity,
                                onQiblaVisible = viewModel::setQiblaVisible,
                                onSetThemeChoice = viewModel::setThemeChoice,
                                onDismissExactAlarmNotice = viewModel::dismissExactAlarmNotice,
                                onDismissMethodNotice = viewModel::dismissMethodNotice,
                            )

                            // Shown once, immediately after setup. The app is a convenience,
                            // not a religious authority, and saying so should not be buried.
                            if (!state.settings.disclaimerSeen) {
                                AlertDialog(
                                    onDismissRequest = { },
                                    // A filled, full-width button, not a text link. This is the
                                    // one control on the one screen every user must pass
                                    // through, and as a small tinted word in the corner it did
                                    // not read as pressable — a tester said so. 56dp clears the
                                    // 48dp minimum touch target with room for shaky hands.
                                    confirmButton = {
                                        Button(
                                            onClick = { viewModel.markDisclaimerSeen() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 56.dp),
                                        ) {
                                            Text(stringResource(R.string.action_understood))
                                        }
                                    },
                                    title = { Text(stringResource(R.string.disclaimer_title)) },
                                    // Scrollable. AlertDialog does not scroll its body for
                                    // you, and this text is now seven paragraphs — at a large
                                    // system font size the last of them, the dua request, is
                                    // exactly what would fall off the bottom unread.
                                    //
                                    // Seven, not five, since the Isha and congregation-time
                                    // paragraphs were added. Only four fit on a 1080x1920
                                    // screen at default font, so the dua is three swipes down
                                    // and the scroll is the only thing keeping it reachable.
                                    // Verified by screenshot in RTL — see HANDOVER §10. Do not
                                    // lengthen this further without re-checking that the dua
                                    // still arrives, and do not remove the scroll.
                                    text = {
                                        Column(Modifier.verticalScroll(rememberScrollState())) {
                                            Text(stringResource(R.string.disclaimer_body))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Coordinates may be stale after travel, and alarms may have been cleared by the
        // system, so a foreground visit always refreshes both.
        if (viewModel.state.value.settings.coordinates != null) viewModel.refreshLocation()
    }

    private fun requestLocation() {
        locationPermission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openAlarmSoundPicker(current: String) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settings_alarm_sound))
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                current.takeIf { it.isNotBlank() }?.let(Uri::parse)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
            )
        }
        runCatching { alarmSoundPicker.launch(intent) }
    }

    private fun toast(message: String) =
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()

    private fun sharePdf(uri: Uri) {
        val share = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            // ClipData is what actually grants the share sheet permission to read the
            // file; without it the chooser cannot render a preview of the timetable.
            .apply { clipData = ClipData.newRawUri(getString(R.string.action_save_timetable), uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, getString(R.string.action_save_timetable)))
    }
}

@Suppress("DEPRECATION")
private inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(name: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(name, T::class.java)
    } else {
        getParcelableExtra(name)
    }

/**
 * Wraps the whole UI in a build-type banner — or, in every build a user can install, in
 * nothing at all.
 *
 * `preview_build_notice` is empty in `main` and overridden only in `src/rtl/`, the
 * developer-only build that forces the app right-to-left with its English words left in
 * place (see that file). Every time the owner has seen that build on an emulator he has,
 * correctly, reported the app as broken — four times by 15 Aug 2026 — because English laid
 * out right-to-left *is* broken and nothing on the screen said "this is a layout test".
 * Now something does, on every screen, in a colour nothing else in the app uses for a
 * band. The check is a resource, not `BuildConfig`, for the same reason `app_language_tag`
 * is: it is one string, it needs no build feature, and the shipping build pays a single
 * `isBlank()` for it.
 *
 * The status-bar inset is taken by the banner and consumed before the content, so the
 * scaffold underneath does not add it a second time. That branch is only compiled into a
 * layout when the notice is non-blank; the shipping build lays out exactly as before.
 */
@Composable
private fun PreviewBuildFrame(content: @Composable () -> Unit) {
    val notice = stringResource(R.string.preview_build_notice)
    if (notice.isBlank()) {
        content()
        return
    }
    Column(Modifier.fillMaxSize()) {
        Text(
            text = notice,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Box(
            Modifier
                .weight(1f)
                .consumeWindowInsets(WindowInsets.statusBars),
        ) { content() }
    }
}
