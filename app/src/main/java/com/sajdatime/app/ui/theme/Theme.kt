package com.sajdatime.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightAccent,
    onTertiary = LightOnAccent,
    tertiaryContainer = LightAccentContainer,
    onTertiaryContainer = LightOnAccentContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
)

private val DarkColors = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkAccent,
    onTertiary = DarkOnAccent,
    tertiaryContainer = DarkAccentContainer,
    onTertiaryContainer = DarkOnAccentContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
)

/**
 * Which scheme to draw in.
 *
 * [SYSTEM] is the default and is what almost everyone should stay on. It is listed first
 * for that reason. When the OS expresses no preference — which is what
 * `isSystemInDarkTheme()` reports on older devices and on any device where the user has
 * never chosen — it resolves to **light**, so light is effectively the default.
 *
 * The other two exist because following the system is not always what a person wants: a
 * phone set to dark all day is still held in bright sun, and someone reading Fajr times in
 * a dark room does not want a white screen because their phone is on the light setting.
 */
enum class ThemeChoice { SYSTEM, LIGHT, DARK }

/**
 * True when the dark scheme is in force, whether that came from the system or from the
 * user's own choice.
 *
 * Components need this because the two schemes do not just swap colours: **light separates
 * surfaces with a shadow, dark with a hairline border and no shadow at all.** A shadow on
 * a dark surface is invisible, and a border in light adds a hard line the design does not
 * want. `isSystemInDarkTheme()` is the wrong thing to read once the user can override it.
 */
val LocalDarkTheme = staticCompositionLocalOf { false }

/**
 * ponytail: no dynamic colour (Material You). A wallpaper-derived palette would break
 * the verified contrast guarantees and the deliberate green/gold identity. Revisit only
 * if users ask for it, and re-run the contrast test against the generated scheme.
 */
@Composable
fun SajdaTimeTheme(
    choice: ThemeChoice = ThemeChoice.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (choice) {
        ThemeChoice.SYSTEM -> isSystemInDarkTheme()
        ThemeChoice.LIGHT -> false
        ThemeChoice.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colors,
            typography = SajdaTypography,
            content = content,
        )
    }
}

/**
 * The one card treatment, so every bounded surface in the app separates itself from the
 * page the same way.
 *
 * Light gets a level-1 shadow and no border; dark gets a hairline `outlineVariant` border
 * and no shadow. Written once here rather than branched at six call sites, because the
 * failure mode of getting it wrong — a card that is invisible in one theme — is silent.
 */
@Composable
fun Modifier.sajdaSurface(
    shape: Shape,
    color: Color = MaterialTheme.colorScheme.surface,
): Modifier = sajdaSurface(shape, SolidColor(color))

@Composable
fun Modifier.sajdaSurface(shape: Shape, brush: Brush): Modifier {
    val dark = LocalDarkTheme.current
    val outline = MaterialTheme.colorScheme.outlineVariant
    return this
        .then(if (dark) Modifier else Modifier.shadow(2.dp, shape, clip = false))
        .clip(shape)
        .background(brush)
        .then(if (dark) Modifier.border(1.dp, outline, shape) else Modifier)
}

/**
 * The gold for the Kaaba's hizam and door on the Qibla dial.
 *
 * A function rather than a colour-scheme role because Material 3 has no slot for it and
 * inventing one would imply it is semantic; it is depictive. Read through [LocalDarkTheme]
 * and not `isSystemInDarkTheme()`, for the same reason `heroStyle` is: the user can override
 * the system, and a mark that picks its gold from the phone's setting rather than the app's
 * would be invisible on exactly the screen the override exists to fix.
 */
@Composable
fun kiswahGold(): Color = if (LocalDarkTheme.current) DarkKiswah else LightKiswah

/**
 * The next-prayer card's fill, and the three text colours that sit on it.
 *
 * Light keeps the design's mint-to-sand gradient; dark is a flat `primaryContainer`. All
 * three light stops are asserted against all three of these text colours in
 * `ColorContrastTest`, which is the only reason a gradient is allowed here at all.
 */
data class HeroStyle(
    val brush: Brush,
    val label: Color,
    val prominent: Color,
    val secondary: Color,
)

@Composable
fun heroStyle(): HeroStyle {
    val scheme = MaterialTheme.colorScheme
    return if (LocalDarkTheme.current) {
        HeroStyle(
            brush = SolidColor(scheme.primaryContainer),
            label = scheme.onPrimaryContainer,
            prominent = scheme.onSurface,
            secondary = scheme.secondary,
        )
    } else {
        HeroStyle(
            // ponytail: vertical, not the design's 155°. The card is four times wider than
            // it is tall, so a 25°-off-vertical sweep and a vertical one differ by a few
            // pixels at the corners and by nothing anyone can see.
            brush = Brush.verticalGradient(
                listOf(LightHeroStart, LightHeroMiddle, LightHeroEnd),
            ),
            label = LightOnHeroLabel,
            prominent = LightOnHero,
            secondary = LightOnHeroSecondary,
        )
    }
}
