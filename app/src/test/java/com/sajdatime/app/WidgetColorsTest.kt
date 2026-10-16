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
    )
    private val dark = mapOf(
        "widget_surface" to DarkSurface,
        "widget_on_surface" to DarkOnSurface,
        "widget_on_surface_variant" to DarkOnSurfaceVariant,
        "widget_highlight" to DarkPrimaryContainer,
        "widget_primary" to DarkPrimary,
        "widget_outline_variant" to DarkOutlineVariant,
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
        Pair("the highlighted row against the surface", "widget_highlight", "widget_surface", 1.1),
        Pair("the hairline against the surface", "widget_outline_variant", "widget_surface", 1.2),
    )

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
