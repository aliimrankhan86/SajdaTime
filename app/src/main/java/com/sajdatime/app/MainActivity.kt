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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

                when {
                    state.loading -> Unit

                    !state.settings.onboardingComplete -> OnboardingScreen(
                        state = state,
                        onRequestLocationPermission = ::requestLocation,
                        onSearchCity = viewModel::searchCity,
                        onUseDefaultLocation = viewModel::useDefaultLocation,
                        onSelectSect = viewModel::setSect,
                        onSelectMadhab = viewModel::setMadhab,
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
                            onSetNotify = viewModel::setNotify,
                            onSetOngoingBadge = viewModel::setOngoingBadge,
                            onSetAlertStyle = viewModel::setAlertStyle,
                            onPickAlarmSound = {
                                openAlarmSoundPicker(state.settings.alarmSoundUri)
                            },
                            onRefreshLocation = ::requestLocation,
                            onSearchCity = viewModel::searchCity,
                            onQiblaVisible = viewModel::setQiblaVisible,
                            onSetThemeChoice = viewModel::setThemeChoice,
                            onDismissExactAlarmNotice = viewModel::dismissExactAlarmNotice,
                        )

                        // Shown once, immediately after setup. The app is a convenience,
                        // not a religious authority, and saying so should not be buried.
                        if (!state.settings.disclaimerSeen) {
                            AlertDialog(
                                onDismissRequest = { },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.markDisclaimerSeen() }) {
                                        Text(stringResource(R.string.action_understood))
                                    }
                                },
                                title = { Text(stringResource(R.string.disclaimer_title)) },
                                // Scrollable. AlertDialog does not scroll its body for
                                // you, and this text is now five paragraphs — at a large
                                // system font size the last of them, the dua request, is
                                // exactly what would fall off the bottom unread.
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
