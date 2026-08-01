package com.sajdatime.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sajdatime.app.R
import com.sajdatime.core.CalcMethod
import com.sajdatime.core.Madhab
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.Sect
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.pdf.PrayerPdfExporter
import com.sajdatime.app.ui.home.HomeScreen
import com.sajdatime.app.ui.qibla.QiblaScreen
import com.sajdatime.app.ui.settings.SettingsScreen
import com.sajdatime.app.ui.theme.ThemeChoice

/** Top-level destinations. Three is comfortably inside the five-item guidance. */
enum class Destination(
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    TIMES(R.string.nav_times, Icons.Filled.Schedule, Icons.Outlined.Schedule),
    QIBLA(R.string.nav_qibla, Icons.Filled.Explore, Icons.Outlined.Explore),
    SETTINGS(R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings),
}

@Composable
fun MainScaffold(
    state: UiState,
    onExport: (PrayerPdfExporter.Range) -> Unit,
    onSetSect: (Sect) -> Unit,
    onSetMadhab: (Madhab) -> Unit,
    onSetMethod: (CalcMethod) -> Unit,
    onSetAlert: (PrayerSlot, AlertStyle?) -> Unit,
    onSetOngoingBadge: (Boolean) -> Unit,
    onSetAlarmRespectsSilent: (Boolean) -> Unit,
    onPickAlarmSound: () -> Unit,
    onRefreshLocation: () -> Unit,
    onSearchCity: (String) -> Unit,
    onQiblaVisible: (Boolean) -> Unit,
    onSetThemeChoice: (ThemeChoice) -> Unit,
    onDismissExactAlarmNotice: () -> Unit,
) {
    // Saveable, not remember. With a plain remember, rotating the phone rebuilt the
    // composition from scratch and dropped the user back on Times — so anyone holding
    // the Qibla compass and turning the phone sideways to line it up lost the screen
    // they were using, which is precisely when they were using it.
    var destination by rememberSaveable { mutableStateOf(Destination.TIMES) }

    // The magnetometer only runs while the Qibla tab is actually on screen.
    DisposableEffect(destination) {
        onQiblaVisible(destination == Destination.QIBLA)
        onDispose { onQiblaVisible(false) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                Destination.entries.forEach { item ->
                    val selected = destination == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { destination = item },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        // Always labelled: icon-only navigation is guesswork for anyone
                        // who has not used the app before.
                        label = { Text(stringResource(item.labelRes)) },
                        alwaysShowLabel = true,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AnimatedContent(
                targetState = destination,
                // A crossfade, not a slide: tabs are siblings, so directional motion
                // would imply a hierarchy that is not there.
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "destination",
            ) { current ->
                when (current) {
                    Destination.TIMES -> HomeScreen(
                        state = state,
                        onExport = onExport,
                        onChangeLocation = onRefreshLocation,
                        onSearchCity = onSearchCity,
                        onDismissExactAlarmNotice = onDismissExactAlarmNotice,
                    )

                    Destination.QIBLA -> QiblaScreen(state = state)

                    Destination.SETTINGS -> SettingsScreen(
                        state = state,
                        onSetSect = onSetSect,
                        onSetMadhab = onSetMadhab,
                        onSetMethod = onSetMethod,
                        onSetAlert = onSetAlert,
                        onSetOngoingBadge = onSetOngoingBadge,
                        onSetAlarmRespectsSilent = onSetAlarmRespectsSilent,
                        onPickAlarmSound = onPickAlarmSound,
                        onRefreshLocation = onRefreshLocation,
                        onSearchCity = onSearchCity,
                        onSetThemeChoice = onSetThemeChoice,
                    )
                }
            }
        }
    }
}
