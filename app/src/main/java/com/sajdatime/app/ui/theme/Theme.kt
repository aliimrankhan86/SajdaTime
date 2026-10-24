package com.sajdatime.app.ui.theme

import com.sajdatime.core.SkyPeriod
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
 * The next-prayer card's fill, its gloss, and the three text colours that sit on it.
 *
 * Light keeps the design's mint-to-sand gradient; dark is a fixed green gradient in the widgets'
 * countdown card colours (since 18 Oct 2026; flat before). Every stop of both, and the top stop
 * under the sheen, is asserted against all three text colours in `ColorContrastTest`, which is
 * the only reason a gradient is allowed here at all.
 */
data class HeroStyle(
    val brush: Brush,
    val label: Color,
    val prominent: Color,
    val secondary: Color,
    /** White laid over the top half of the card, fading out by the middle: the gloss. */
    val sheen: Color,
    /** The rim at the top of the card, and where it fades to at the bottom. */
    val rim: Color,
    val rimBottom: Color,
    /** The shadow's tint, or transparent for none (dark: a shadow does not show on black). */
    val glow: Color,
)

@Composable
fun heroStyle(): HeroStyle {
    val scheme = MaterialTheme.colorScheme
    return if (LocalDarkTheme.current) {
        HeroStyle(
            // The widgets' countdown card (owner, 18 Oct 2026: "similar to the widget style
            // glossy look"): primaryContainer in the middle, a step lighter at the top and a
            // step deeper at the bottom. All three stops, and the top under the sheen, are
            // checked in ColorContrastTest.
            brush = Brush.verticalGradient(
                listOf(DarkHeroTop, scheme.primaryContainer, DarkHeroBottom),
            ),
            label = scheme.onPrimaryContainer,
            prominent = scheme.onSurface,
            secondary = scheme.secondary,
            sheen = DarkHeroSheen,
            rim = DarkHeroRim,
            rimBottom = DarkHeroRimBottom,
            glow = Color.Transparent,
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
            sheen = LightHeroSheen,
            rim = LightHeroRim,
            rimBottom = LightHeroRimBottom,
            glow = LightHeroGlow,
        )
    }
}

/** One sky: three fixed stops, top to foot. The foot is the page background. */
data class SkyColours(val top: Color, val mid: Color, val foot: Color)

/** Pure, so ColorContrastTest can read every stop without composing anything. */
fun skyColours(period: SkyPeriod, dark: Boolean): SkyColours = if (dark) {
    when (period) {
        SkyPeriod.DAWN -> SkyColours(DarkSkyDawnTop, DarkSkyDawnMid, DarkSkyFoot)
        SkyPeriod.DAY -> SkyColours(DarkSkyDayTop, DarkSkyDayMid, DarkSkyFoot)
        SkyPeriod.EVENING -> SkyColours(DarkSkyEveningTop, DarkSkyEveningMid, DarkSkyFoot)
        SkyPeriod.NIGHT -> SkyColours(DarkSkyNightTop, DarkSkyNightMid, DarkSkyFoot)
    }
} else {
    when (period) {
        SkyPeriod.DAWN -> SkyColours(LightSkyDawnTop, LightSkyDawnMid, LightSkyFoot)
        SkyPeriod.DAY -> SkyColours(LightSkyDayTop, LightSkyDayMid, LightSkyFoot)
        SkyPeriod.EVENING -> SkyColours(LightSkyEveningTop, LightSkyEveningMid, LightSkyFoot)
        SkyPeriod.NIGHT -> SkyColours(LightSkyNightTop, LightSkyNightMid, LightSkyFoot)
    }
}

/** The sky for [period] in the current theme, as [heroStyle] reads [LocalDarkTheme]. */
@Composable
fun skyStyle(period: SkyPeriod): SkyColours = skyColours(period, LocalDarkTheme.current)

/**
 * The warning notice's panel: the hero card's gloss in amber (24 Oct 2026). Quiet notices stay
 * flat, because the dark CTA on them is already at 2.32:1 and a sheen would take it to 1.64:1.
 */
data class GlossStyle(
    val brush: Brush,
    val sheen: Color,
    val rim: Color,
    val rimBottom: Color,
)

@Composable
fun warningGloss(): GlossStyle {
    val scheme = MaterialTheme.colorScheme
    return if (LocalDarkTheme.current) {
        GlossStyle(
            brush = Brush.verticalGradient(listOf(DarkWarnTop, scheme.tertiaryContainer, DarkWarnBottom)),
            sheen = DarkHeroSheen,
            rim = DarkHeroRim,
            rimBottom = DarkHeroRimBottom,
        )
    } else {
        GlossStyle(
            brush = Brush.verticalGradient(listOf(LightWarnTop, scheme.tertiaryContainer, LightWarnBottom)),
            sheen = LightHeroSheen,
            rim = LightHeroRim,
            rimBottom = LightHeroRimBottom,
        )
    }
}
