package com.sajdatime.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The widget resource files, read off disk (and declared as task inputs in app/build.gradle.kts).
 *
 * These pin the traps that cannot be seen in a screenshot or a unit test of the logic:
 *  - a widget layout is inflated by the LAUNCHER, so android:text="@string/..." would resolve in the
 *    phone's language and not the app's. Every word has to be set from code. That is the one mistake
 *    that works perfectly on a developer's phone and is wrong on a user's;
 *  - the system's own periodic update uses a waking alarm, so updatePeriodMillis stays 0;
 *  - a screen reader should hear one sentence for the whole widget, so no child is exposed to it;
 *  - text must follow the layout direction, so every text view says which edge it aligns to.
 */
class WidgetResourcesTest {

    private val root = File("..").canonicalFile
    private val res = File(root, "app/src/main/res")

    private fun read(path: String): String {
        val file = File(res, path)
        assertTrue("Expected to find ${file.path}", file.isFile)
        return file.readText()
    }

    private val nextLayouts = listOf("layout/widget_next_prayer.xml", "layout/widget_next_prayer_wide.xml")
    private val todayLayouts = listOf("layout/widget_today.xml", "layout/widget_today_large.xml")
    private val layouts = nextLayouts + todayLayouts
    private val providers = listOf("xml/widget_next_prayer_info.xml", "xml/widget_today_info.xml")
    private val languages = listOf("values", "values-ur", "values-tr", "values-in", "values-ar")

    private fun withoutComments(xml: String) = xml.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")

    @Test
    fun `no widget layout sets any text, so no word resolves in the launcher's language`() {
        for (layout in layouts) {
            val xml = withoutComments(read(layout))
            assertFalse("$layout has android:text", xml.contains(Regex("""android:text\s*=""")))
            assertFalse("$layout has android:hint", xml.contains(Regex("""android:hint\s*=""")))
            assertFalse("$layout has android:contentDescription", xml.contains("android:contentDescription"))
        }
    }

    /**
     * RemoteViews can inflate only a short list of classes, and a launcher shows "Problem loading
     * widget" for the whole widget if one other is used. A plain View is the classic one, and the first
     * version of the Today layout used it for its divider. Found by rendering the layout under
     * Robolectric, not by reading it: nothing on the JVM, in lint or in the build objects to it.
     * The list is the one in the RemoteViews documentation, trimmed to what could plausibly be added.
     */
    @Test
    fun `layouts use only classes a RemoteViews may inflate`() {
        val allowed = setOf(
            "LinearLayout", "FrameLayout", "RelativeLayout", "GridLayout", "TextView", "Chronometer",
            "ImageView", "ImageButton", "Button", "ProgressBar", "TextClock", "AnalogClock", "ViewFlipper",
            "ListView", "GridView", "StackView", "AdapterViewFlipper", "ViewStub",
        )
        for (layout in layouts) {
            val used = Regex("""<([A-Za-z.]+)\b""").findAll(withoutComments(read(layout))).map { it.groupValues[1] }.toSet()
            assertEquals("$layout uses a class RemoteViews cannot inflate", emptySet<String>(), used - allowed)
        }
    }

    @Test
    fun `every text view and chronometer is hidden from accessibility and says which edge it aligns to`() {
        for (layout in layouts) {
            val xml = withoutComments(read(layout))
            val views = Regex("""<(TextView|Chronometer)\b[^>]*>""", RegexOption.DOT_MATCHES_ALL).findAll(xml).toList()
            assertTrue("$layout has no text views", views.isNotEmpty())
            for (view in views) {
                val tag = view.value
                assertTrue("$layout: $tag is exposed to TalkBack", tag.contains("""android:importantForAccessibility="no""""))
                if (tag.startsWith("<TextView")) {
                    assertTrue(
                        "$layout: $tag has no textAlignment",
                        tag.contains("""android:textAlignment="viewStart"""") || tag.contains("""android:textAlignment="viewEnd""""),
                    )
                }
            }
        }
    }

    @Test
    fun `each layout has the ids the renderer sets`() {
        val slots = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
        val rows = slots.flatMap { listOf("widget_row_$it", "widget_glyph_$it", "widget_name_$it", "widget_label_$it", "widget_time_$it") }
        // The countdown block, in both Next layouts and in the large Today card (WidgetRenderer.fillHero).
        val hero = listOf(
            "widget_glyph", "widget_name", "widget_time", "widget_sublabel", "widget_countdown", "widget_hero_time", "widget_progress",
        ) + (1..5).map { "widget_seg_$it" }
        val expected = mapOf(
            "layout/widget_next_prayer.xml" to hero,
            "layout/widget_next_prayer_wide.xml" to hero + rows,
            "layout/widget_today.xml" to listOf("widget_message", "widget_footer") + rows,
            "layout/widget_today_large.xml" to listOf("widget_message", "widget_footer", "widget_header", "widget_date", "widget_hijri", "widget_place", "widget_hero") + hero + rows,
        )
        for ((layout, ids) in expected) {
            val xml = read(layout)
            for (id in listOf("widget_root") + ids) assertTrue("$layout lacks $id", xml.contains("""@+id/$id""""))
        }
    }

    /**
     * Glyphs are white drawables tinted by their ImageView. A tint set from code would be baked in at
     * the update and go stale when the phone changes theme (lesson 130), so every one is in the layout.
     */
    @Test
    fun `every glyph is tinted in the layout from a widget colour`() {
        for (layout in layouts) {
            val glyphs = Regex("""<ImageView\b[^>]*widget_glyph[^>]*>""", RegexOption.DOT_MATCHES_ALL).findAll(withoutComments(read(layout))).toList()
            for (glyph in glyphs) assertTrue("$layout: ${glyph.value}", glyph.value.contains(Regex("""android:tint="@color/widget_[a-z_]+"""")))
        }
    }

    @Test
    fun `the providers never ask the system for periodic updates`() {
        for (provider in providers) {
            assertTrue(provider, read(provider).contains("""android:updatePeriodMillis="0""""))
        }
    }

    @Test
    fun `each provider points at its own layout, an existing preview image and its description`() {
        val pairs = mapOf(
            "xml/widget_next_prayer_info.xml" to Triple("widget_next_prayer", "widget_preview_next", "widget_next_desc"),
            "xml/widget_today_info.xml" to Triple("widget_today", "widget_preview_today", "widget_today_desc"),
        )
        val strings = read("values/strings.xml")
        for ((provider, expected) in pairs) {
            val xml = withoutComments(read(provider))
            val (layout, preview, description) = expected
            assertTrue("$provider initialLayout", xml.contains("""android:initialLayout="@layout/$layout""""))
            assertTrue("$provider previewLayout", xml.contains("""android:previewLayout="@layout/$layout""""))
            assertTrue("$provider previewImage", xml.contains("""android:previewImage="@drawable/$preview""""))
            assertTrue("$provider description", xml.contains("""android:description="@string/$description""""))
            assertTrue("$provider shows no home screen category", xml.contains("""android:widgetCategory="home_screen""""))
            assertTrue("$preview.png is missing", File(res, "drawable-nodpi/$preview.png").isFile)
            assertTrue("$description is not a string", strings.contains("""name="$description""""))
        }
    }

    @Test
    fun `the preview images are small, the first raster assets in the app`() {
        for (name in listOf("widget_preview_next", "widget_preview_today")) {
            val size = File(res, "drawable-nodpi/$name.png").length()
            assertTrue("$name is $size bytes", size in 1..30_000)
        }
    }

    @Test
    fun `the sizes are the ones the layouts were designed for`() {
        val next = read("xml/widget_next_prayer_info.xml")
        assertTrue(next.contains("""android:targetCellWidth="3"""") && next.contains("""android:targetCellHeight="2""""))
        // Neither widget may be resized below the size its content was drawn for.
        assertTrue(next.contains("""android:minResizeWidth="180dp"""") && next.contains("""android:minResizeHeight="110dp""""))
        val today = read("xml/widget_today_info.xml")
        // Placed at 4 by 4 so a new widget opens on the large layout (18 Oct 2026).
        assertTrue(today.contains("""android:targetCellWidth="4"""") && today.contains("""android:targetCellHeight="4""""))
        // The Today widget must never be resizable below the height its six rows need.
        assertTrue(today.contains("""android:minResizeHeight="180dp""""))
    }

    @Test
    fun `every colour the widget files use is defined in both themes`() {
        val drawables = listOf(
            "widget_background", "widget_row_highlight", "widget_row_plain", "widget_hero_background", "widget_divider",
            "widget_seg_done", "widget_seg_next", "widget_seg_todo",
        )
        val files = layouts + drawables.map { "drawable/$it.xml" }
        val used = files.flatMap { Regex("""@color/(widget_[a-z_]+)""").findAll(read(it)).map { m -> m.groupValues[1] }.toList() }.toSet()
        assertTrue(used.isNotEmpty())
        for (folder in listOf("values", "values-night")) {
            val defined = Regex("""<color name="(widget_[a-z_]+)">""").findAll(read("$folder/colors.xml")).map { it.groupValues[1] }.toSet()
            assertEquals("missing in $folder", emptySet<String>(), used - defined)
        }
    }

    @Test
    fun `every widget string exists in all four languages`() {
        val names = Regex("""<string name="(widget_[a-z0-9_]+)"""").findAll(read("values/strings.xml")).map { it.groupValues[1] }.toList()
        assertEquals(12, names.size)
        for (language in languages) {
            val strings = read("$language/strings.xml")
            for (name in names) assertTrue("$language lacks $name", strings.contains("""name="$name""""))
        }
    }

    @Test
    fun `no widget string has an em dash`() {
        for (language in languages) {
            val lines = read("$language/strings.xml").lines().filter { it.contains("name=\"widget_") }
            for (line in lines) assertFalse("$language: $line", line.contains('—'))
        }
    }
}
