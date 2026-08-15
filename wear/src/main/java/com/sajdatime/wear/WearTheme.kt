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
// Kept in step with the phone's dark palette in app/ui/theme/Color.kt by hand. The two
// modules cannot share a Compose theme — Wear Compose has its own ColorScheme with a
// different set of roles — so a colour changed on the phone has to be changed here too,
// and a watch that looks like a different app is the symptom of forgetting.
private val Primary = Color(0xFF4FC48F)
private val OnPrimary = Color(0xFF062117)
private val Accent = Color(0xFFE8B14A)
private val OnAccent = Color(0xFF3A2A00)
// Pure black, not the phone's #101312: on the OLED panels every Wear device ships with,
// black pixels are switched off, which is both the deepest contrast available and the
// cheapest thing to draw on a battery this small.
private val Background = Color(0xFF000000)
private val OnSurface = Color(0xFFE7EAE8)
private val OnSurfaceVariant = Color(0xFFA9B2AE)

/**
 * The gold of the Kaaba's hizam and door on the Qibla dial. The phone's `DarkKiswah` —
 * this module cannot import it, so the value is duplicated here like every other colour in
 * this file, and must be changed in both places.
 *
 * Deliberately not [Accent]. That amber means "the system is withholding something", and
 * the watch draws both on the same screen family. It is also the *dark* gold rather than
 * the phone's bright one, because the watch only has a dark theme: the silhouette is
 * `onSurface`, which here is near-white, and a bright gold on a white cube is 1.7:1.
 */
internal val Kiswah = Color(0xFF8A5807)

private val SajdaWearColors = ColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Color(0xFF143028),
    onPrimaryContainer = Color(0xFF8FD9B8),
    secondary = Color(0xFFB8C4BE),
    onSecondary = Color(0xFF1F352C),
    secondaryContainer = Color(0xFF1E4034),
    onSecondaryContainer = Color(0xFF8FD9B8),
    tertiary = Accent,
    onTertiary = OnAccent,
    tertiaryContainer = Color(0xFF2A2113),
    onTertiaryContainer = Color(0xFFF3E4C5),
    // The Qibla dial is drawn from surfaceContainer, which is where the stray lilac
    // was coming through.
    surfaceContainerLow = Color(0xFF141817),
    surfaceContainer = Color(0xFF171B1A),
    surfaceContainerHigh = Color(0xFF1E2321),
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Color(0xFF8B948F),
    outlineVariant = Color(0xFF2A312E),
    background = Background,
    onBackground = OnSurface,
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

@Composable
fun SajdaWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = SajdaWearColors, content = content)
}
