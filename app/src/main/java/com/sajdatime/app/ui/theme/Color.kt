package com.sajdatime.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * SajdaTime palette.
 *
 * Deep green (traditionally associated with Islam) as the primary, warm gold as the
 * single accent, warm-neutral paper surfaces. No decorative colour: every hue here
 * carries meaning, which keeps the interface calm and keeps contrast easy to control.
 *
 * Every foreground/background pair used in the UI is asserted against WCAG 2.1 AA in
 * ColorContrastTest — if a colour is edited below AA, the build fails.
 */

// --- Light ---------------------------------------------------------------------------
val LightPrimary = Color(0xFF14624B)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFD7EAE0)
val LightOnPrimaryContainer = Color(0xFF05271C)
val LightSecondary = Color(0xFF3F5B50)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightAccent = Color(0xFF8A5200)
val LightOnAccent = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFFBFAF7)
val LightOnBackground = Color(0xFF12211C)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF12211C)
val LightSurfaceVariant = Color(0xFFEDF1EE)
val LightOnSurfaceVariant = Color(0xFF43524B)
val LightOutline = Color(0xFF6B7A73)
val LightOutlineVariant = Color(0xFFC9D2CD)
val LightError = Color(0xFFB3261E)
val LightOnError = Color(0xFFFFFFFF)

// Tonal surfaces. Material 3 draws dialogs, bottom sheets, menus and elevated cards from
// these roles — leaving them unset makes those components fall back to Material's default
// purple-tinted baseline, which is jarring next to the green identity.
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF7F6F2)
val LightSurfaceContainer = Color(0xFFF2F2ED)
val LightSurfaceContainerHigh = Color(0xFFEBEDE8)
val LightSurfaceContainerHighest = Color(0xFFE5E8E3)
val LightSurfaceBright = Color(0xFFFBFAF7)
val LightSurfaceDim = Color(0xFFDCDED9)
val LightInverseSurface = Color(0xFF2A322E)
val LightInverseOnSurface = Color(0xFFF0F2EE)
val LightInversePrimary = Color(0xFF7FD1AE)

// --- Dark ----------------------------------------------------------------------------
val DarkPrimary = Color(0xFF7FD1AE)
val DarkOnPrimary = Color(0xFF003825)
val DarkPrimaryContainer = Color(0xFF1D4A3A)
val DarkOnPrimaryContainer = Color(0xFFA8EBCB)
val DarkSecondary = Color(0xFFB4CCC0)
val DarkOnSecondary = Color(0xFF1F352C)
val DarkAccent = Color(0xFFE3B341)
val DarkOnAccent = Color(0xFF3A2A00)
val DarkBackground = Color(0xFF0E1512)
val DarkOnBackground = Color(0xFFE8EEEA)
val DarkSurface = Color(0xFF18211D)
val DarkOnSurface = Color(0xFFE8EEEA)
val DarkSurfaceVariant = Color(0xFF222D28)
val DarkOnSurfaceVariant = Color(0xFFB3C2BA)
val DarkOutline = Color(0xFF8A9A92)
val DarkOutlineVariant = Color(0xFF40514A)
val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)

val DarkSurfaceContainerLowest = Color(0xFF090F0C)
val DarkSurfaceContainerLow = Color(0xFF151D19)
val DarkSurfaceContainer = Color(0xFF19221E)
val DarkSurfaceContainerHigh = Color(0xFF232D28)
val DarkSurfaceContainerHighest = Color(0xFF2E3833)
val DarkSurfaceBright = Color(0xFF343E39)
val DarkSurfaceDim = Color(0xFF0E1512)
val DarkInverseSurface = Color(0xFFE8EEEA)
val DarkInverseOnSurface = Color(0xFF1B2420)
val DarkInversePrimary = Color(0xFF14624B)
