package com.sajdatime.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

/**
 * The watch's colours, taken from the phone app's dark theme.
 *
 * Wear Compose ships its own default scheme, and leaving it in place made the watch look
 * like a different app: lilac dials and containers next to the phone's green. Watch
 * screens are always drawn on black, so only the dark palette is needed here.
 */
private val Primary = Color(0xFF7FD1AE)
private val OnPrimary = Color(0xFF003825)
private val Accent = Color(0xFFE3B341)
private val OnAccent = Color(0xFF3A2A00)
private val Background = Color(0xFF000000)
private val OnSurface = Color(0xFFE8EEEA)
private val OnSurfaceVariant = Color(0xFFB3C2BA)

private val SajdaWearColors = ColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF1D4A3A),
    onPrimaryContainer = Color(0xFFA8EBCB),
    secondary = Color(0xFFB4CCC0),
    onSecondary = Color(0xFF1F352C),
    secondaryContainer = Color(0xFF2A473B),
    onSecondaryContainer = Color(0xFFCDE8DA),
    tertiary = Accent,
    onTertiary = OnAccent,
    tertiaryContainer = Color(0xFF5A4300),
    onTertiaryContainer = Color(0xFFF5DFA6),
    // The Qibla dial is drawn from surfaceContainer, which is where the stray lilac
    // was coming through.
    surfaceContainerLow = Color(0xFF151D19),
    surfaceContainer = Color(0xFF19221E),
    surfaceContainerHigh = Color(0xFF232D28),
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Color(0xFF8A9A92),
    outlineVariant = Color(0xFF40514A),
    background = Background,
    onBackground = OnSurface,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Composable
fun SajdaWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SajdaWearColors, content = content)
}
