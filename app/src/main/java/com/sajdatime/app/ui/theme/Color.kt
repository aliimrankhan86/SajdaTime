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
// Used by the navigation bar's selected pill and by chips. Left unset it falls back to
// Material's lilac baseline, which looks like a different app pasted into the tab bar.
val LightSecondaryContainer = Color(0xFFD3E4DA)
val LightOnSecondaryContainer = Color(0xFF12211C)
val LightAccent = Color(0xFF8A5200)
val LightOnAccent = Color(0xFFFFFFFF)
// The banner behind "the system is withholding something you asked for". Amber, not red:
// prayer alerts still arrive, they are just at the mercy of the scheduler, and an error
// colour would say the app is broken when it is not.
val LightAccentContainer = Color(0xFFFBEEDA)
val LightOnAccentContainer = Color(0xFF3A2A00)
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
//
// These are the tokens from the SajdaTime dark design system (docs/DESIGN_SYSTEM.md).
// The surfaces are near-neutral rather than green-tinted: on an OLED panel a tinted dark
// grey reads as a colour cast, and the green then has to shout to be seen as an accent.
// Neutral surfaces let a single brighter green carry the whole identity.
//
// Two deliberate departures from the design document, both for contrast:
//
//  - The design's `outline` (#3A4441) is 1.7:1 against the surface. It is drawn as a
//    hairline card border in a mockup, which is fine, but Material spends `outline` on
//    chevrons, tick marks and chip borders, and WCAG 1.4.11 wants 3:1 for those. #3A4441
//    is therefore `outlineVariant` here (decorative only) and `outline` takes the grey
//    the design uses for its facing tick and muted icons.
//  - The design's token table states 3.1:1 for that colour. It is 1.85:1 against the
//    design's own background. The table is wrong, not the renderer.
val DarkPrimary = Color(0xFF4FC48F)
val DarkOnPrimary = Color(0xFF062117)
val DarkPrimaryContainer = Color(0xFF143028)
val DarkOnPrimaryContainer = Color(0xFF8FD9B8)
val DarkSecondary = Color(0xFFB8C4BE)
val DarkOnSecondary = Color(0xFF1F352C)
// The navigation bar's selected pill, matching the design's active tab.
val DarkSecondaryContainer = Color(0xFF1E4034)
val DarkOnSecondaryContainer = Color(0xFF8FD9B8)
val DarkAccent = Color(0xFFE8B14A)
val DarkOnAccent = Color(0xFF3A2A00)
val DarkAccentContainer = Color(0xFF2A2113)
val DarkOnAccentContainer = Color(0xFFF3E4C5)
val DarkBackground = Color(0xFF101312)
val DarkOnBackground = Color(0xFFE7EAE8)
val DarkSurface = Color(0xFF171B1A)
val DarkOnSurface = Color(0xFFE7EAE8)
// The Qibla dial face.
val DarkSurfaceVariant = Color(0xFF161A19)
val DarkOnSurfaceVariant = Color(0xFFA9B2AE)
val DarkOutline = Color(0xFF8B948F)
val DarkOutlineVariant = Color(0xFF2A312E)
val DarkError = Color(0xFFF2B8B5)
val DarkOnError = Color(0xFF601410)

val DarkSurfaceContainerLowest = Color(0xFF0B0D0C)
val DarkSurfaceContainerLow = Color(0xFF141817)
val DarkSurfaceContainer = Color(0xFF171B1A)
val DarkSurfaceContainerHigh = Color(0xFF1E2321)
val DarkSurfaceContainerHighest = Color(0xFF262C29)
val DarkSurfaceBright = Color(0xFF2E3532)
val DarkSurfaceDim = Color(0xFF101312)
val DarkInverseSurface = Color(0xFFE7EAE8)
val DarkInverseOnSurface = Color(0xFF171B1A)
val DarkInversePrimary = Color(0xFF14624B)
