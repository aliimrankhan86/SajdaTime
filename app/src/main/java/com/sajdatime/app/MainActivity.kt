package com.sajdatime.app

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sajdatime.app.ui.SajdaViewModel
import com.sajdatime.app.ui.home.HomeScreen
import com.sajdatime.app.ui.onboarding.OnboardingScreen
import com.sajdatime.app.ui.settings.SettingsScreen
import com.sajdatime.app.ui.theme.SajdaTimeTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SajdaViewModel by viewModels()

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { viewModel.refreshLocation() }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Declining only silences alerts; times still display. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SajdaTimeTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                var showSettings by remember { mutableStateOf(false) }

                // Sharing the export is a user-visible side effect, so it fires once per
                // generated file and is then consumed.
                LaunchedEffect(state.exportedFile) {
                    state.exportedFile?.let { uri ->
                        sharePdf(uri)
                        viewModel.consumeExportedFile()
                    }
                }

                when {
                    state.loading -> Unit

                    !state.settings.onboardingComplete -> OnboardingScreen(
                        state = state,
                        onRequestLocationPermission = ::requestLocation,
                        onSearchCity = viewModel::searchCity,
                        onSelectSect = viewModel::setSect,
                        onSelectMadhab = viewModel::setMadhab,
                        onFinish = {
                            requestNotificationsIfNeeded()
                            viewModel.completeOnboarding()
                        },
                    )

                    showSettings -> SettingsScreen(
                        state = state,
                        // Without this, the system back gesture would close the app
                        // instead of returning to the prayer times.
                        onBack = { showSettings = false },
                        onSetSect = viewModel::setSect,
                        onSetMadhab = viewModel::setMadhab,
                        onSetMethod = viewModel::setMethod,
                        onSetNotify = viewModel::setNotify,
                        onSetOngoingBadge = viewModel::setOngoingBadge,
                        onRefreshLocation = ::requestLocation,
                    )

                    else -> HomeScreen(
                        state = state,
                        onOpenSettings = { showSettings = true },
                        onExport = viewModel::exportPdf,
                    )
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
        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun requestNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun sharePdf(uri: android.net.Uri) {
        val share = Intent(Intent.ACTION_SEND)
            .setType("application/pdf")
            .putExtra(Intent.EXTRA_STREAM, uri)
            // ClipData is what actually grants the share sheet permission to read the
            // file; without it the chooser cannot render a preview of the timetable.
            .apply { clipData = ClipData.newRawUri(getString(R.string.action_export_pdf), uri) }
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(share, getString(R.string.action_export_pdf)))
    }
}
