package com.sajdatime.app

import androidx.compose.ui.graphics.Color
import com.sajdatime.app.ui.theme.DarkAccent
import com.sajdatime.app.ui.theme.DarkAccentContainer
import com.sajdatime.app.ui.theme.DarkOnAccentContainer
import com.sajdatime.app.ui.theme.DarkWarnBottom
import com.sajdatime.app.ui.theme.DarkWarnTop
import com.sajdatime.app.ui.theme.LightWarnBottom
import com.sajdatime.app.ui.theme.LightWarnTop
import com.sajdatime.app.ui.theme.DarkInverseOnSurface
import com.sajdatime.app.ui.theme.DarkInverseSurface
import com.sajdatime.app.ui.theme.DarkOnSecondaryContainer
import com.sajdatime.app.ui.theme.DarkSecondaryContainer
import com.sajdatime.app.ui.theme.LightOnSecondaryContainer
import com.sajdatime.app.ui.theme.LightSecondaryContainer
import com.sajdatime.app.ui.theme.DarkSurfaceBright
import com.sajdatime.app.ui.theme.DarkSurfaceContainerHigh
import com.sajdatime.app.ui.theme.DarkSurfaceContainerLow
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
import com.sajdatime.app.ui.theme.GlassBarAlpha
import com.sajdatime.app.ui.theme.DarkSurfaceVariant
import com.sajdatime.app.ui.theme.LightAccent
import com.sajdatime.app.ui.theme.LightKiswah
import com.sajdatime.app.ui.theme.DarkKiswah
import com.sajdatime.app.ui.theme.DarkCta
import com.sajdatime.app.ui.theme.DarkHeroBottom
import com.sajdatime.app.ui.theme.LightCta
import com.sajdatime.app.ui.theme.OnCta
import com.sajdatime.app.ui.theme.DarkHeroSheen
import com.sajdatime.app.ui.theme.DarkHeroTop
import com.sajdatime.app.ui.theme.LightHeroEnd
import com.sajdatime.app.ui.theme.LightHeroSheen
import com.sajdatime.app.ui.theme.LightHeroMiddle
import com.sajdatime.app.ui.theme.LightHeroStart
import com.sajdatime.app.ui.theme.LightOnHero
import com.sajdatime.app.ui.theme.LightOnHeroLabel
import com.sajdatime.app.ui.theme.LightOnHeroSecondary
import com.sajdatime.app.ui.theme.LightAccentContainer
import com.sajdatime.app.ui.theme.LightOnAccentContainer
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
import com.sajdatime.app.ui.theme.skyColours
import com.sajdatime.core.SkyPeriod
import org.junit.Assert.assertEquals
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
        // The amber warning banner: its text, and its icon against its own container.
        Pair("light onAccentContainer/accentContainer", LightOnAccentContainer, LightAccentContainer, 4.5),
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
        // The Timetable's month heading is pinned on the sheet's own colour.
        Pair("light onSurfaceVariant/containerLow", LightOnSurfaceVariant, LightSurfaceContainerLow, 4.5),
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
        Pair("dark onAccentContainer/accentContainer", DarkOnAccentContainer, DarkAccentContainer, 4.5),
        Pair("dark primary/background", DarkPrimary, DarkBackground, 4.5),
        Pair("dark primary/surface", DarkPrimary, DarkSurface, 4.5),
        Pair("dark accent/surface", DarkAccent, DarkSurface, 4.5),
        Pair("dark onSurface/containerHigh", DarkOnSurface, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurfaceVariant/containerHigh", DarkOnSurfaceVariant, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurface/containerHighest", DarkOnSurface, DarkSurfaceContainerHighest, 4.5),
        Pair("dark primary/containerHigh", DarkPrimary, DarkSurfaceContainerHigh, 4.5),
        Pair("dark onSurface/surfaceBright", DarkOnSurface, DarkSurfaceBright, 4.5),
        Pair("dark onSurface/containerLow", DarkOnSurface, DarkSurfaceContainerLow, 4.5),
        Pair("dark onSurfaceVariant/containerLow", DarkOnSurfaceVariant, DarkSurfaceContainerLow, 4.5),
        Pair("dark inverseOnSurface/inverseSurface", DarkInverseOnSurface, DarkInverseSurface, 4.5),
        Pair("dark onSecondaryContainer/secondaryContainer", DarkOnSecondaryContainer, DarkSecondaryContainer, 4.5),
        // The dial's E, S and W are onSurfaceVariant on the aligned face (on the plain face they are
        // already covered above). Computed 24 Oct 2026: 5.13 and 6.52 (step 6, STEP-6-DESIGN 16.5).
        Pair("light onSurfaceVariant/dial face aligned", LightOnSurfaceVariant, LightPrimaryContainer, 4.5),
        Pair("dark onSurfaceVariant/dial face aligned", DarkOnSurfaceVariant, DarkPrimaryContainer, 4.5),
    )

    /** Icons, dividers and focus outlines only need 3:1 under WCAG 1.4.11. */
    // Not listed here on purpose: the My prayers ring for days to come and for prayers not due yet is
    // drawn at 38 percent. It is a disabled control, which WCAG 1.4.11 exempts from the 3:1 contrast
    // rule (STEP-1-DESIGN 3c). The tick and the ring in use are primary, covered by the pairs above.
    private val nonTextPairs = listOf(
        Pair("light outline/surface", LightOutline, LightSurface, 3.0),
        Pair("light outline/background", LightOutline, LightBackground, 3.0),
        Pair("dark outline/surface", DarkOutline, DarkSurface, 3.0),
        Pair("dark outline/background", DarkOutline, DarkBackground, 3.0),
        Pair("light accent/accentContainer", LightAccent, LightAccentContainer, 3.0),
        Pair("dark accent/accentContainer", DarkAccent, DarkAccentContainer, 3.0),
        // The Kaaba mark and the north wedge are onSurface drawn straight onto the Qibla
        // dial, and the dial face is not the surface — it is surfaceVariant, and it
        // becomes primaryContainer the moment you are facing the Qibla. Four pairs, not
        // one, because a mark that is legible until you point the right way is worse than
        // one that was never legible at all.
        Pair("light onSurface/dial face", LightOnSurface, LightSurfaceVariant, 3.0),
        Pair("dark onSurface/dial face", DarkOnSurface, DarkSurfaceVariant, 3.0),
        Pair("light onSurface/dial face aligned", LightOnSurface, LightPrimaryContainer, 3.0),
        Pair("dark onSurface/dial face aligned", DarkOnSurface, DarkPrimaryContainer, 3.0),
        // The needle, the N and the aligned bezel are primary, drawn on the two dial faces (step 6).
        // Computed 24 Oct 2026: 7.08 and 6.54 in light, 8.05 and 6.49 in dark.
        Pair("light primary/dial face", LightPrimary, LightSurfaceVariant, 3.0),
        Pair("dark primary/dial face", DarkPrimary, DarkSurfaceVariant, 3.0),
        Pair("light primary/dial face aligned", LightPrimary, LightPrimaryContainer, 3.0),
        Pair("dark primary/dial face aligned", DarkPrimary, DarkPrimaryContainer, 3.0),
        // The 60 ticks are `outline` on the face, and on the aligned face the moment you arrive (step 6,
        // STEP-6-DESIGN 15.3 and 16.5). Computed 24 Oct 2026: 3.69 and 5.63; aligned 3.41 and 4.54.
        Pair("light outline/dial face", LightOutline, LightSurfaceVariant, 3.0),
        Pair("dark outline/dial face", DarkOutline, DarkSurfaceVariant, 3.0),
        Pair("light outline/dial face aligned", LightOutline, LightPrimaryContainer, 3.0),
        Pair("dark outline/dial face aligned", DarkOutline, DarkPrimaryContainer, 3.0),
        // The Kaaba's hizam and door, in gold on the silhouette. **Two pairs, not four**,
        // and the difference is the whole reason gold was rejected once and is used now:
        // these shapes sit wholly inside the wall, so they are never drawn on the dial and
        // the dial's two faces are irrelevant to them. The background here is the mark's
        // own colour, which is onSurface — and onSurface flips from near-black to
        // near-white between the themes, which is why the two golds are not the same gold.
        // 4.5 rather than the 3.0 the rest of this list uses: the door is a 2.6-unit shape
        // at ~19dp on a watch, which is nearer fine detail than a divider.
        Pair("light kiswah/kaaba silhouette", LightKiswah, LightOnSurface, 4.5),
        Pair("dark kiswah/kaaba silhouette", DarkKiswah, DarkOnSurface, 4.5),
    )

    @Test
    fun `text colours meet WCAG AA`() = assertAll(textPairs)

    @Test
    fun `non-text colours meet WCAG AA`() = assertAll(nonTextPairs)

    /**
     * The light next-prayer card is drawn on a three-stop gradient, which is the only
     * gradient in the app and is allowed only because it is fixed rather than driven by
     * the time of day — so every stop can be checked against every colour drawn on it.
     * Nine assertions, because the countdown must be readable at the top of the card, at
     * the bottom, and everywhere between.
     */
    @Test
    fun `light hero gradient is readable at every stop`() {
        val stops = listOf(
            "start" to LightHeroStart,
            "middle" to LightHeroMiddle,
            "end" to LightHeroEnd,
        )
        val inks = listOf(
            "countdown" to LightOnHero,
            "label" to LightOnHeroLabel,
            "time" to LightOnHeroSecondary,
        )
        assertAll(
            stops.flatMap { (stopName, stop) ->
                inks.map { (inkName, ink) ->
                    Pair("light hero $inkName on $stopName", ink, stop, 4.5)
                }
            },
        )
    }

    /**
     * The call to action button (CtaButton): white text on the fill, AA in both themes, and the
     * fill itself 3:1 against every surface it sits on, so the button's shape is seen, not only
     * its words (WCAG 1.4.11).
     */
    @Test
    fun `call to action buttons are readable and stand out`() {
        assertAll(
            listOf(
                Pair("light CTA text", OnCta, LightCta, 4.5),
                Pair("dark CTA text", OnCta, DarkCta, 4.5),
                Pair("light CTA on surface", LightCta, LightSurface, 3.0),
                Pair("light CTA on dialog", LightCta, LightSurfaceContainerHigh, 3.0),
                Pair("dark CTA on background", DarkCta, DarkBackground, 3.0),
                Pair("dark CTA on surface", DarkCta, DarkSurface, 3.0),
                Pair("dark CTA on dialog", DarkCta, DarkSurfaceContainerHigh, 3.0),
            ),
        )
    }

    /**
     * The gloss (18 Oct 2026). The sheen is white over the top half of each card, so the top
     * stop is checked with the sheen composited on it, as well as every stop bare. In dark the
     * sheen lightens the stop under light text, which is the direction that can fail.
     */
    @Test
    fun `glossy hero cards are readable at every stop and under the sheen`() {
        fun over(top: Color, under: Color) = Color(
            red = top.red * top.alpha + under.red * (1 - top.alpha),
            green = top.green * top.alpha + under.green * (1 - top.alpha),
            blue = top.blue * top.alpha + under.blue * (1 - top.alpha),
        )
        val dark = listOf(
            "top" to DarkHeroTop,
            "middle" to DarkPrimaryContainer,
            "bottom" to DarkHeroBottom,
            "top under sheen" to over(DarkHeroSheen, DarkHeroTop),
        )
        val darkInks = listOf(
            "countdown" to DarkOnSurface,
            "label" to DarkOnPrimaryContainer,
            "time" to DarkSecondary,
        )
        val light = listOf("start under sheen" to over(LightHeroSheen, LightHeroStart))
        val lightInks = listOf(
            "countdown" to LightOnHero,
            "label" to LightOnHeroLabel,
            "time" to LightOnHeroSecondary,
        )
        assertAll(
            dark.flatMap { (stopName, stop) ->
                darkInks.map { (inkName, ink) -> Pair("dark hero $inkName on $stopName", ink, stop, 4.5) }
            } + light.flatMap { (stopName, stop) ->
                lightInks.map { (inkName, ink) -> Pair("light hero $inkName on $stopName", ink, stop, 4.5) }
            },
        )
    }

    /**
     * The amber notice's gloss (24 Oct 2026, STEP-3-DESIGN 19): the notice ink must hold AA at
     * every stop and under the sheen, and the accent (mark and button) 3:1 as a graphic.
     */
    @Test
    fun `glossy amber notice is readable at every stop and under the sheen`() {
        fun over(top: Color, under: Color) = Color(
            red = top.red * top.alpha + under.red * (1 - top.alpha),
            green = top.green * top.alpha + under.green * (1 - top.alpha),
            blue = top.blue * top.alpha + under.blue * (1 - top.alpha),
        )
        val lightStops = listOf(
            "top" to LightWarnTop,
            "bottom" to LightWarnBottom,
            "top under sheen" to over(LightHeroSheen, LightWarnTop),
        )
        val darkStops = listOf(
            "top" to DarkWarnTop,
            "bottom" to DarkWarnBottom,
            "top under sheen" to over(DarkHeroSheen, DarkWarnTop),
        )
        assertAll(
            lightStops.map { (name, stop) -> Pair("light notice ink on $name", LightOnAccentContainer, stop, 4.5) } +
                lightStops.map { (name, stop) -> Pair("light accent on $name", LightAccent, stop, 3.0) } +
                darkStops.map { (name, stop) -> Pair("dark notice ink on $name", DarkOnAccentContainer, stop, 4.5) } +
                darkStops.map { (name, stop) -> Pair("dark accent on $name", DarkAccent, stop, 3.0) },
        )
    }

    private fun skyTops(dark: Boolean) = SkyPeriod.entries.map { it to skyColours(it, dark) }

    /**
     * The four skies (1.6 step 5, STEP-5-DESIGN section 5): the city (onSurface), the date, arc and dots
     * (onSurfaceVariant) and the Hijri date, caret and glyph (primary) sit on every stop of every sky, in both
     * themes. 3 inks x 3 stops x 4 skies x 2 themes = 72, all at 4.5:1, which also clears the 3:1 the arc,
     * dots and glyph need. The light night top is the thin one (4.57:1 under onSurfaceVariant).
     */
    @Test
    fun `sky stops are readable under every ink`() {
        val pairs = mutableListOf<Pair>()
        for (dark in listOf(false, true)) {
            val theme = if (dark) "dark" else "light"
            val inks = if (dark) {
                listOf("onSurface" to DarkOnSurface, "onSurfaceVariant" to DarkOnSurfaceVariant, "primary" to DarkPrimary)
            } else {
                listOf("onSurface" to LightOnSurface, "onSurfaceVariant" to LightOnSurfaceVariant, "primary" to LightPrimary)
            }
            for ((period, sky) in skyTops(dark)) {
                for ((stopName, stop) in listOf("top" to sky.top, "mid" to sky.mid, "foot" to sky.foot)) {
                    for ((inkName, ink) in inks) {
                        pairs += Pair("$theme $inkName on $period $stopName", ink, stop, 4.5)
                    }
                }
            }
        }
        assertEquals(72, pairs.size)
        assertAll(pairs)
    }

    /** Each sky fades into the page: the foot stop is the background, so no seam shows under the card. */
    @Test
    fun `sky feet equal the page`() {
        var checked = 0
        for ((dark, page) in listOf(false to LightBackground, true to DarkBackground)) {
            for ((period, sky) in skyTops(dark)) {
                assertEquals("${if (dark) "dark" else "light"} $period foot", page, sky.foot)
                checked++
            }
        }
        assertEquals(8, checked)
    }

    /**
     * The status bar icons follow the theme (dark icons on light, white on dark), and the sky now runs under
     * the bar, so black on every light top and white on every dark top must hold 3:1. 8 pairs.
     */
    @Test
    fun `status bar icons hold on every sky top`() {
        val pairs = listOf(false, true).flatMap { dark ->
            skyTops(dark).map { (period, sky) ->
                Pair("${if (dark) "white" else "black"} icons on ${if (dark) "dark" else "light"} $period top", if (dark) Color.White else Color.Black, sky.top, 3.0)
            }
        }
        assertEquals(8, pairs.size)
        assertAll(pairs)
    }

    /**
     * The floating tab bar is see-through (GlassBarAlpha), so what is behind its labels is
     * whatever has scrolled underneath. Composited over the two extremes, pure black and pure
     * white, every colour drawn straight on the bar must still hold AA. The selected pill is
     * opaque secondaryContainer and is covered by the pair above.
     */
    @Test
    fun `glass tab bar labels hold over the worst backdrop`() {
        fun over(top: Color, alpha: Float, under: Color) = Color(
            red = top.red * alpha + under.red * (1 - alpha),
            green = top.green * alpha + under.green * (1 - alpha),
            blue = top.blue * alpha + under.blue * (1 - alpha),
        )
        val pairs = listOf(Color.Black, Color.White).flatMap { under ->
            val u = if (under == Color.Black) "black" else "white"
            val light = over(LightSurface, GlassBarAlpha, under)
            val dark = over(DarkSurface, GlassBarAlpha, under)
            listOf(
                Pair("light bar label over $u", LightOnSurfaceVariant, light, 4.5),
                Pair("light bar selected label over $u", LightOnSurface, light, 4.5),
                Pair("dark bar label over $u", DarkOnSurfaceVariant, dark, 4.5),
                Pair("dark bar selected label over $u", DarkOnSurface, dark, 4.5),
            )
        }
        assertAll(pairs)
    }

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

    private fun contrast(a: Color, b: Color): Double = Wcag.contrast(a, b)
}
