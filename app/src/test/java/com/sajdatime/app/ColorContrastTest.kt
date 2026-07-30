package com.sajdatime.app

import androidx.compose.ui.graphics.Color
import com.sajdatime.app.ui.theme.DarkAccent
import com.sajdatime.app.ui.theme.DarkInverseOnSurface
import com.sajdatime.app.ui.theme.DarkInverseSurface
import com.sajdatime.app.ui.theme.DarkOnSecondaryContainer
import com.sajdatime.app.ui.theme.DarkSecondaryContainer
import com.sajdatime.app.ui.theme.LightOnSecondaryContainer
import com.sajdatime.app.ui.theme.LightSecondaryContainer
import com.sajdatime.app.ui.theme.DarkSurfaceBright
import com.sajdatime.app.ui.theme.DarkSurfaceContainerHigh
import com.sajdatime.app.ui.theme.DarkSurfaceContainerHighest
import com.sajdatime.app.ui.theme.LightInverseOnSurface
import com.sajdatime.app.ui.theme.LightInverseSurface
import com.sajdatime.app.ui.theme.LightSurfaceContainerHigh
import com.sajdatime.app.ui.theme.LightSurfaceContainerHighest
import com.sajdatime.app.ui.theme.LightSurfaceContainerLow
import com.sajdatime.app.ui.theme.DarkBackground
import com.sajdatime.app.ui.theme.DarkOnAccent
import com.sajdatime.app.ui.theme.DarkOnBackground
import com.sajdatime.app.ui.theme.DarkOnPrimary
import com.sajdatime.app.ui.theme.DarkOnPrimaryContainer
import com.sajdatime.app.ui.theme.DarkOnSecondary
import com.sajdatime.app.ui.theme.DarkOnSurface
import com.sajdatime.app.ui.theme.DarkOnSurfaceVariant
import com.sajdatime.app.ui.theme.DarkOutline
import com.sajdatime.app.ui.theme.DarkPrimary
import com.sajdatime.app.ui.theme.DarkPrimaryContainer
import com.sajdatime.app.ui.theme.DarkSecondary
import com.sajdatime.app.ui.theme.DarkSurface
import com.sajdatime.app.ui.theme.DarkSurfaceVariant
import com.sajdatime.app.ui.theme.LightAccent
import com.sajdatime.app.ui.theme.LightBackground
import com.sajdatime.app.ui.theme.LightOnAccent
import com.sajdatime.app.ui.theme.LightOnBackground
import com.sajdatime.app.ui.theme.LightOnPrimary
import com.sajdatime.app.ui.theme.LightOnPrimaryContainer
import com.sajdatime.app.ui.theme.LightOnSecondary
import com.sajdatime.app.ui.theme.LightOnSurface
import com.sajdatime.app.ui.theme.LightOnSurfaceVariant
import com.sajdatime.app.ui.theme.LightOutline
import com.sajdatime.app.ui.theme.LightPrimary
import com.sajdatime.app.ui.theme.LightPrimaryContainer
import com.sajdatime.app.ui.theme.LightSecondary
import com.sajdatime.app.ui.theme.LightSurface
import com.sajdatime.app.ui.theme.LightSurfaceVariant
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Locks the palette to WCAG 2.1 AA. Editing a colour below the threshold fails the build
 * rather than quietly shipping unreadable text.
 *
 * AA thresholds: 4.5:1 for body text, 3:1 for large text and non-text UI boundaries.
 */
class ColorContrastTest {

    private data class Pair(
        val name: String,
        val foreground: Color,
        val background: Color,
        val minimum: Double,
    )

    private val textPairs = listOf(
        // Light
        Pair("light onBackground/background", LightOnBackground, LightBackground, 4.5),
        Pair("light onSurface/surface", LightOnSurface, LightSurface, 4.5),
        Pair("light onSurfaceVariant/surface", LightOnSurfaceVariant, LightSurface, 4.5),
        Pair("light onSurfaceVariant/surfaceVariant", LightOnSurfaceVariant, LightSurfaceVariant, 4.5),
        Pair("light onSurfaceVariant/background", LightOnSurfaceVariant, LightBackground, 4.5),
        Pair("light onPrimary/primary", LightOnPrimary, LightPrimary, 4.5),
        Pair("light onPrimaryContainer/primaryContainer", LightOnPrimaryContainer, LightPrimaryContainer, 4.5),
        Pair("light onSecondary/secondary", LightOnSecondary, LightSecondary, 4.5),
        Pair("light onAccent/accent", LightOnAccent, LightAccent, 4.5),
        // The section headings in Settings are primary-on-background.
        Pair("light primary/background", LightPrimary, LightBackground, 4.5),
        Pair("light primary/surface", LightPrimary, LightSurface, 4.5),
        Pair("light accent/surface", LightAccent, LightSurface, 4.5),
        // Dialogs, bottom sheets and menus draw from the tonal container roles.
        Pair("light onSurface/containerHigh", LightOnSurface, LightSurfaceContainerHigh, 4.5),
        Pair("light onSurfaceVariant/containerHigh", LightOnSurfaceVariant, LightSurfaceContainerHigh, 4.5),
        Pair("light onSurface/containerHighest", LightOnSurface, LightSurfaceContainerHighest, 4.5),
        Pair("light primary/containerHigh", LightPrimary, LightSurfaceContainerHigh, 4.5),
        Pair("light onSurface/containerLow", LightOnSurface, LightSurfaceContainerLow, 4.5),
        Pair("light inverseOnSurface/inverseSurface", LightInverseOnSurface, LightInverseSurface, 4.5),
        // The navigation bar's selected pill.
        Pair("light onSecondaryContainer/secondaryContainer", LightOnSecondaryContainer, LightSecondaryContainer, 4.5),

        // Dark
        Pair("dark onBackground/background", DarkOnBackground, DarkBackground, 4.5),
        Pair("dark onSurface/surface", DarkOnSurface, DarkSurface, 4.5),
        Pair("dark onSurfaceVariant/surface", DarkOnSurfaceVariant, DarkSurface, 4.5),
        Pair("dark onSurfaceVariant/surfaceVariant", DarkOnSurfaceVariant, DarkSurfaceVariant, 4.5),
        Pair("dark onSurfaceVariant/background", DarkOnSurfaceVariant, DarkBackground, 4.5),
        Pair("dark onPrimary/primary", DarkOnPrimary, DarkPrimary, 4.5),
        Pair("dark onPrimaryContainer/primaryContainer", DarkOnPrimaryContainer, DarkPrimaryContainer, 4.5),
        Pair("dark onSecondary/secondary", DarkOnSecondary, DarkSecondary, 4.5),
        Pair("dark onAccent/accent", DarkOnAccent, DarkAccent, 4.5),
        Pair("dark primary/background", DarkPrimary, DarkBackground, 4.5),
        Pair("dark primary/surface", DarkPrimary, DarkSurface, 4.5),
        Pair("dark accent/surface", DarkAccent, DarkSurface, 4.5),
        Pair("dark onSurface/containerHigh", DarkOnSurface, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurfaceVariant/containerHigh", DarkOnSurfaceVariant, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurface/containerHighest", DarkOnSurface, DarkSurfaceContainerHighest, 4.5),
        Pair("dark primary/containerHigh", DarkPrimary, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurface/surfaceBright", DarkOnSurface, DarkSurfaceBright, 4.5),
        Pair("dark inverseOnSurface/inverseSurface", DarkInverseOnSurface, DarkInverseSurface, 4.5),
        Pair("dark onSecondaryContainer/secondaryContainer", DarkOnSecondaryContainer, DarkSecondaryContainer, 4.5),
    )

    /** Icons, dividers and focus outlines only need 3:1 under WCAG 1.4.11. */
    private val nonTextPairs = listOf(
        Pair("light outline/surface", LightOutline, LightSurface, 3.0),
        Pair("light outline/background", LightOutline, LightBackground, 3.0),
        Pair("dark outline/surface", DarkOutline, DarkSurface, 3.0),
        Pair("dark outline/background", DarkOutline, DarkBackground, 3.0),
    )

    @Test
    fun `text colours meet WCAG AA`() = assertAll(textPairs)

    @Test
    fun `non-text colours meet WCAG AA`() = assertAll(nonTextPairs)

    /**
     * The next-prayer row is highlighted with primaryContainer. It must be visibly
     * distinct from the plain surface behind it, otherwise "which prayer is next" is
     * carried by nothing at all for low-vision users.
     */
    @Test
    fun `highlighted row is distinguishable from the surface`() {
        val light = contrast(LightPrimaryContainer, LightSurface)
        val dark = contrast(DarkPrimaryContainer, DarkSurface)
        assertTrue("light highlight too subtle: $light", light >= 1.2)
        assertTrue("dark highlight too subtle: $dark", dark >= 1.2)
    }

    private fun assertAll(pairs: List<Pair>) {
        val failures = pairs.mapNotNull { pair ->
            val ratio = contrast(pair.foreground, pair.background)
            if (ratio < pair.minimum) {
                "%s: %.2f:1 (needs %.1f:1)".format(pair.name, ratio, pair.minimum)
            } else {
                null
            }
        }
        assertTrue(
            "Contrast failures:\n" + failures.joinToString("\n"),
            failures.isEmpty(),
        )
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    /** WCAG 2.1 relative luminance. */
    private fun relativeLuminance(color: Color): Double {
        fun channel(value: Float): Double {
            val c = value.toDouble()
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(color.red) +
            0.7152 * channel(color.green) +
            0.0722 * channel(color.blue)
    }
}
