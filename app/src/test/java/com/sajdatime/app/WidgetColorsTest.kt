package com.sajdatime.app

import androidx.compose.ui.graphics.Color
import com.sajdatime.app.ui.theme.DarkOnSurface
import com.sajdatime.app.ui.theme.DarkOnSurfaceVariant
import com.sajdatime.app.ui.theme.DarkOutlineVariant
import com.sajdatime.app.ui.theme.DarkPrimary
import com.sajdatime.app.ui.theme.DarkPrimaryContainer
import com.sajdatime.app.ui.theme.DarkSurface
import com.sajdatime.app.ui.theme.LightOnSurface
import com.sajdatime.app.ui.theme.LightOnSurfaceVariant
import com.sajdatime.app.ui.theme.LightOutlineVariant
import com.sajdatime.app.ui.theme.LightPrimary
import com.sajdatime.app.ui.theme.LightPrimaryContainer
import com.sajdatime.app.ui.theme.LightSurface
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The widget colours repeat Color.kt because a launcher inflates a RemoteViews layout and cannot reach
 * Compose. Two copies of a colour drift unless something notices, so this reads both resource files
 * off disk and fails the build if any widget colour differs from its Kotlin twin, or if a pair a
 * widget actually draws falls below WCAG AA in either theme.
 *
 * Read off disk, like the other guards, so the files are declared as task inputs in app/build.gradle.kts
 * (otherwise the build cache replays a green result after the file changed).
 */
class WidgetColorsTest {

    private val root = File("..").canonicalFile

    private fun colours(folder: String): Map<String, Int> {
        val file = File(root, "app/src/main/res/$folder/colors.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        return Regex("""<color name="(widget_[a-z_]+)">#([0-9A-Fa-f]{6})</color>""")
            .findAll(file.readText())
            .associate { it.groupValues[1] to (0xFF000000.toInt() or it.groupValues[2].toInt(16)) }
    }

    private fun argb(c: Color): Int =
        (0xFF shl 24) or
            (Math.round(c.red * 255f) shl 16) or
            (Math.round(c.green * 255f) shl 8) or
            Math.round(c.blue * 255f)

    private fun asColor(argb: Int) = Color(argb)

    private val light = mapOf(
        "widget_surface" to LightSurface,
        "widget_on_surface" to LightOnSurface,
        "widget_on_surface_variant" to LightOnSurfaceVariant,
        "widget_highlight" to LightPrimaryContainer,
        "widget_primary" to LightPrimary,
        "widget_outline_variant" to LightOutlineVariant,
        // The glossy finish's other stops (18 Oct 2026). Widget only, so no Color.kt twin: the
        // values are written here once and the pairs below hold them to AA.
        "widget_surface_top" to Color(0xFFFFFFFF),
        "widget_surface_bottom" to Color(0xFFEEF1EC),
        "widget_highlight_top" to Color(0xFFE6F3EC),
    )
    private val dark = mapOf(
        "widget_surface" to DarkSurface,
        "widget_on_surface" to DarkOnSurface,
        "widget_on_surface_variant" to DarkOnSurfaceVariant,
        "widget_highlight" to DarkPrimaryContainer,
        "widget_primary" to DarkPrimary,
        "widget_outline_variant" to DarkOutlineVariant,
        "widget_surface_top" to Color(0xFF252C29),
        "widget_surface_bottom" to Color(0xFF121514),
        "widget_highlight_top" to Color(0xFF18382D),
    )

    @Test
    fun `both themes define exactly the same widget colours`() {
        assertEquals(colours("values").keys, colours("values-night").keys)
        assertEquals(light.keys, colours("values").keys)
    }

    @Test
    fun `every light widget colour equals its Kotlin twin`() {
        val xml = colours("values")
        for ((name, kotlin) in light) assertEquals(name, argb(kotlin), xml.getValue(name))
    }

    @Test
    fun `every dark widget colour equals its Kotlin twin`() {
        val xml = colours("values-night")
        for ((name, kotlin) in dark) assertEquals(name, argb(kotlin), xml.getValue(name))
    }

    private data class Pair(val name: String, val foreground: String, val background: String, val minimum: Double)

    /**
     * Every foreground the widgets draw, on every background it sits on. The highlighted row keeps the
     * ordinary text colours (see values/colors.xml), so they are checked on it as well. The outline is
     * a decorative hairline and is held to a visible difference, not to text contrast.
     */
    private val pairs = listOf(
        Pair("text on surface", "widget_on_surface", "widget_surface", 4.5),
        Pair("secondary text on surface", "widget_on_surface_variant", "widget_surface", 4.5),
        Pair("countdown and next line on surface", "widget_primary", "widget_surface", 4.5),
        Pair("text on the highlighted row", "widget_on_surface", "widget_highlight", 4.5),
        Pair("secondary text on the highlighted row", "widget_on_surface_variant", "widget_highlight", 4.5),
        // The large Today widget's countdown card: the next prayer's name in green on the highlight.
        Pair("next prayer name on the countdown card", "widget_primary", "widget_highlight", 4.5),
        Pair("the highlighted row against the surface", "widget_highlight", "widget_surface", 1.1),
        Pair("the hairline against the surface", "widget_outline_variant", "widget_surface", 1.2),
        // The glossy finish: every ink against every stop of the gradient it can sit on.
        Pair("text on the top of the gradient", "widget_on_surface", "widget_surface_top", 4.5),
        Pair("secondary text on the top of the gradient", "widget_on_surface_variant", "widget_surface_top", 4.5),
        Pair("green on the top of the gradient", "widget_primary", "widget_surface_top", 4.5),
        Pair("text on the foot of the gradient", "widget_on_surface", "widget_surface_bottom", 4.5),
        Pair("secondary text on the foot of the gradient", "widget_on_surface_variant", "widget_surface_bottom", 4.5),
        Pair("green on the foot of the gradient", "widget_primary", "widget_surface_bottom", 4.5),
        Pair("text on the top of the countdown card", "widget_on_surface", "widget_highlight_top", 4.5),
        Pair("secondary text on the top of the countdown card", "widget_on_surface_variant", "widget_highlight_top", 4.5),
        Pair("green on the top of the countdown card", "widget_primary", "widget_highlight_top", 4.5),
        // The location chip: green pin, town and arrow on the highlight pill.
        Pair("the location chip", "widget_primary", "widget_highlight", 4.5),
    )

    /** An 8 digit (#AARRGGBB) widget colour, for the translucent sheen. */
    private fun translucent(folder: String, name: String): Long {
        val text = File(root, "app/src/main/res/$folder/colors.xml").readText()
        return Regex("""<color name="$name">#([0-9A-Fa-f]{8})</color>""").find(text)!!.groupValues[1].toLong(16)
    }

    /** [over] (#AARRGGBB) drawn on the opaque [under], as the launcher composites the sheen. */
    private fun composite(over: Long, under: Int): Color {
        val a = ((over shr 24) and 0xFF) / 255f
        fun ch(shift: Int) = (((over shr shift) and 0xFF) * a + ((under shr shift) and 0xFF) * (1 - a)) / 255f
        return Color(ch(16), ch(8), ch(0))
    }

    /** The top stop is the lightest point in dark and is under the sheen, so it is the worst case there. */
    private fun checkSheen(theme: String, folder: String) {
        val xml = colours(folder)
        val top = composite(translucent(folder, "widget_sheen"), xml.getValue("widget_surface_top"))
        val cardTop = composite(translucent(folder, "widget_sheen"), xml.getValue("widget_highlight_top"))
        for (ink in listOf("widget_on_surface", "widget_on_surface_variant", "widget_primary")) {
            val fg = asColor(xml.getValue(ink))
            val r1 = Wcag.contrast(fg, top)
            val r2 = Wcag.contrast(fg, cardTop)
            assertTrue("$theme $ink under the sheen: %.2f:1".format(r1), r1 >= 4.5)
            assertTrue("$theme $ink on the card under the sheen: %.2f:1".format(r2), r2 >= 4.5)
        }
    }

    @Test
    fun `light text clears AA under the gloss sheen`() = checkSheen("light", "values")

    @Test
    fun `dark text clears AA under the gloss sheen`() = checkSheen("dark", "values-night")

    private fun check(theme: String, folder: String) {
        val xml = colours(folder)
        val failures = pairs.mapNotNull { p ->
            val ratio = Wcag.contrast(asColor(xml.getValue(p.foreground)), asColor(xml.getValue(p.background)))
            if (ratio < p.minimum) "$theme ${p.name}: %.2f:1 (needs %.1f:1)".format(ratio, p.minimum) else null
        }
        assertTrue("Widget contrast failures:\n" + failures.joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun `light widget colours clear the contrast floors`() = check("light", "values")

    @Test
    fun `dark widget colours clear the contrast floors`() = check("dark", "values-night")
}
