package com.sajdatime.app

import com.sajdatime.app.widget.WidgetRenderer
import com.sajdatime.app.widget.WidgetRenderer.Ink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The widgets follow the app's Theme row (owner's request, 19 Oct 2026). Under System the launcher
 * resolves the layouts' own colours under the phone's dark mode; under Light or Dark the renderer pins
 * every colour from fixed copies, on the `_fixed` layouts. That second path holds three copies of the
 * same facts (the layout's colours, the renderer's list of them, the fixed colours and drawables), and
 * this test fails the build when any copy drifts, because a forgotten view would quietly keep following
 * the phone and show a light row on a dark widget.
 *
 * Read off disk, so the files are declared as test inputs in app/build.gradle.kts.
 */
class WidgetThemeTest {

    private val res = File(File("..").canonicalFile, "app/src/main/res")

    private fun read(path: String) = File(res, path).readText()

    private fun withoutComments(xml: String) = xml.replace(Regex("<!--.*?-->\\s*", RegexOption.DOT_MATCHES_ALL), "")

    private val layouts = mapOf(
        "widget_next_prayer" to R.layout.widget_next_prayer,
        "widget_next_prayer_wide" to R.layout.widget_next_prayer_wide,
        "widget_today" to R.layout.widget_today,
        "widget_today_large" to R.layout.widget_today_large,
        "widget_strip" to R.layout.widget_strip,
    )

    /** A drawable that carries a widget colour has a light and a dark copy; one that carries none needs none. */
    private fun hasCopies(drawable: String) = File(res, "drawable/${drawable}_light.xml").isFile

    private fun names(type: Class<*>): Map<Int, String> = type.fields.associate { it.getInt(null) to it.name }

    private val idNames = names(R.id::class.java)
    private val colourNames = names(R.color::class.java)
    private val drawableNames = names(R.drawable::class.java)

    private data class Painted(val id: String, val ink: Ink, val resource: String)

    /** Every coloured property in a layout file, as (view id, kind, resource name). */
    private fun declared(layout: String): Set<Painted> {
        val xml = withoutComments(read("layout/$layout.xml"))
        val out = mutableSetOf<Painted>()
        for (tag in Regex("""<[A-Za-z]+\b[^>]*>""", RegexOption.DOT_MATCHES_ALL).findAll(xml)) {
            val refs = Regex("""android:(textColor|tint|background|src)="@(color|drawable)/(widget_[a-z_]+)"""").findAll(tag.value)
                .map { it.groupValues[1] to it.groupValues[3] }
                // A glyph's src is a white shape tinted by the view, not a colour of its own.
                .filter { (attr, name) -> attr != "src" || hasCopies(name) }
                .toList()
            if (refs.isEmpty()) continue
            val id = Regex("""android:id="@\+id/([a-z_0-9]+)"""").find(tag.value)?.groupValues?.get(1)
            assertTrue("$layout: a coloured view has no id, so a chosen theme cannot reach it: ${tag.value}", id != null)
            for ((attr, name) in refs) {
                val ink = when (attr) {
                    "textColor" -> Ink.TEXT
                    "tint" -> Ink.TINT
                    "background" -> Ink.BACKGROUND
                    else -> Ink.IMAGE
                }
                out += Painted(id!!, ink, name)
            }
        }
        return out
    }

    private fun pinned(layout: Int): Set<Painted> = WidgetRenderer.PAINTS.getValue(layout).map {
        Painted(
            idNames.getValue(it.view),
            it.ink,
            if (it.ink == Ink.TEXT || it.ink == Ink.TINT) colourNames.getValue(it.resource) else drawableNames.getValue(it.resource),
        )
    }.toSet()

    @Test
    fun `the renderer pins exactly the colours each layout declares`() {
        for ((name, id) in layouts) {
            val declared = declared(name)
            assertTrue("$name declares no colours", declared.isNotEmpty())
            assertEquals("$name: the renderer's list differs from the layout", declared, pinned(id))
        }
    }

    /**
     * The renderer's two lookups, by name. A colour or drawable missing from them would throw (the widget
     * then stops updating under Light or Dark) or quietly keep following the phone, and a swapped pair
     * would draw the wrong theme; none of that fails to compile. Added after the independent review.
     */
    @Test
    fun `the renderer maps every pinned colour and drawable to its own light and dark copy`() {
        val pinned = WidgetRenderer.PAINTS.values.flatten()
        val colours = pinned.filter { it.ink == Ink.TEXT || it.ink == Ink.TINT }.map { colourNames.getValue(it.resource) }.toSet()
        val fixedColours = WidgetRenderer.FIXED_COLOURS.map { (k, v) ->
            colourNames.getValue(k) to (colourNames.getValue(v.first) to colourNames.getValue(v.second))
        }.toMap()
        assertEquals(colours, fixedColours.keys)
        for ((name, pair) in fixedColours) {
            val base = name.removePrefix("widget_")
            assertEquals(name, "widget_light_$base" to "widget_dark_$base", pair)
        }

        val drawables = pinned.filter { it.ink == Ink.BACKGROUND || it.ink == Ink.IMAGE }.map { drawableNames.getValue(it.resource) } +
            // Switched by the renderer after the paint, not declared once per layout: the highlighted row
            // and the three states of a day bar, and (step 8) the Next widget's four skies, the rimmed
            // pill, and the track and hairline on a sky.
            listOf(
                "widget_row_highlight", "widget_seg_done", "widget_seg_next", "widget_seg_todo",
                "widget_sky_dawn", "widget_sky_day", "widget_sky_evening", "widget_sky_night",
                "widget_pill_sky", "widget_seg_todo_sky", "widget_divider_sky",
            )
        val themed = drawables.filter { hasCopies(it) }.toSet()
        val fixedDrawables = WidgetRenderer.FIXED_DRAWABLES.map { (k, v) ->
            drawableNames.getValue(k) to (drawableNames.getValue(v.first) to drawableNames.getValue(v.second))
        }.toMap()
        assertEquals(themed, fixedDrawables.keys)
        for ((name, pair) in fixedDrawables) assertEquals(name, "${name}_light" to "${name}_dark", pair)
        // A drawable without copies must carry no widget colour, or it would keep following the phone.
        for (name in drawables.toSet() - themed) {
            assertTrue("$name has a widget colour but no fixed copies", !read("drawable/$name.xml").contains("@color/widget_"))
        }
    }

    @Test
    fun `every layout has its fixed twin, which is that layout in a frame and nothing else`() {
        for (name in layouts.keys) {
            val xml = withoutComments(read("layout/${name}_fixed.xml"))
            val tags = Regex("""<([A-Za-z]+)\b""").findAll(xml).map { it.groupValues[1] }.toList()
            assertEquals("${name}_fixed", listOf("FrameLayout", "include"), tags)
            assertTrue("${name}_fixed includes $name", xml.contains("""layout="@layout/$name""""))
        }
    }

    /** Each `name="hex"` widget colour in a values folder, without the fixed copies. */
    private fun colours(folder: String, prefix: String = "widget_"): Map<String, String> =
        Regex("""<color name="$prefix([a-z_]+)">(#[0-9A-Fa-f]+)</color>""").findAll(read("$folder/colors.xml"))
            .map { it.groupValues[1] to it.groupValues[2].uppercase() }
            .filterNot { (name, _) -> prefix == "widget_" && (name.startsWith("light_") || name.startsWith("dark_")) }
            .toMap()

    @Test
    fun `the fixed light and dark colours are the phone resolved ones, every one`() {
        assertEquals(colours("values"), colours("values", "widget_light_"))
        assertEquals(colours("values-night"), colours("values", "widget_dark_"))
    }

    @Test
    fun `every fixed drawable is its source with each colour renamed, and nothing else`() {
        val sources = res.resolve("drawable").listFiles()!!.map { it.name.removeSuffix(".xml") }
            .filter { it.startsWith("widget_") && !it.endsWith("_light") && !it.endsWith("_dark") }
        val themed = sources.filter { read("drawable/$it.xml").contains("@color/widget_") }
        assertEquals(15, themed.size)
        for (name in themed) {
            val source = withoutComments(read("drawable/$name.xml"))
            for (theme in listOf("light", "dark")) {
                val copy = withoutComments(read("drawable/${name}_$theme.xml"))
                assertEquals("${name}_$theme", source.replace("@color/widget_", "@color/widget_${theme}_"), copy)
            }
        }
    }
}
