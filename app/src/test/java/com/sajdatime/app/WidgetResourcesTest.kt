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
    private val stripLayouts = listOf("layout/widget_strip.xml")
    private val layouts = nextLayouts + todayLayouts + stripLayouts
    private val providers = listOf("xml/widget_next_prayer_info.xml", "xml/widget_today_info.xml", "xml/widget_today_strip_info.xml")
    private val languages = stringFolders

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
                        // "center" is direction neutral, so it is as right in Urdu as in English: the strip's
                        // columns centre their text in a match_parent view (step 8). Only the strip may.
                        tag.contains("""android:textAlignment="viewStart"""") || tag.contains("""android:textAlignment="viewEnd"""") ||
                            (layout in stripLayouts && tag.contains("""android:textAlignment="center"""")) ||
                            // "gravity" follows the view's gravity, which WidgetRenderer.nextViews writes (start, or centred in
                            // right to left on the small Next card, 4 Nov 2026), so it is as right in Urdu as in English.
                            (layout == "layout/widget_next_prayer.xml" && tag.contains("""android:textAlignment="gravity"""")),
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
        // The location chip, on every layout (owner's request, 18 Oct 2026).
        val chip = listOf("widget_place_chip", "widget_place")
        val expected = mapOf(
            "layout/widget_next_prayer.xml" to hero + chip,
            "layout/widget_next_prayer_wide.xml" to hero + rows + chip,
            "layout/widget_today.xml" to listOf("widget_message", "widget_footer") + rows + slots.map { "widget_row_underline_$it" } + chip,
            "layout/widget_today_large.xml" to listOf("widget_message", "widget_footer", "widget_header", "widget_date", "widget_hijri", "widget_hero") + hero + rows + chip,
            // The strip: five columns (no sunrise, no label), the large text row, the message, the chip.
            "layout/widget_strip.xml" to listOf("widget_message", "widget_strip_next", "widget_glyph", "widget_name", "widget_time", "widget_sublabel") +
                slots.filter { it != "sunrise" }.flatMap { listOf("widget_row_$it", "widget_glyph_$it", "widget_name_$it", "widget_time_$it") } + chip,
        )
        for ((layout, ids) in expected) {
            val xml = read(layout)
            for (id in listOf("widget_root") + ids) assertTrue("$layout lacks $id", xml.contains("""@+id/$id""""))
            if (layout == "layout/widget_today.xml") {
                for (slot in slots) {
                    val row = Regex("""<FrameLayout\b[^>]*android:id="@\+id/widget_row_$slot"[^>]*>(.*?)</FrameLayout>""", RegexOption.DOT_MATCHES_ALL)
                        .find(xml)?.groupValues?.get(1)
                    assertTrue("$layout: $slot needs an overlay container", row != null)
                    val bar = Regex("""<ImageView\b[^>]*android:id="@\+id/widget_row_underline_$slot"[^>]*/>""", RegexOption.DOT_MATCHES_ALL)
                        .find(row!!)?.value.orEmpty()
                    for (attribute in listOf(
                        "layout_width=\"match_parent\"", "layout_height=\"4dp\"", "layout_gravity=\"bottom\"",
                        "src=\"@drawable/widget_underline\"", "visibility=\"gone\"", "importantForAccessibility=\"no\"", "layout_marginBottom=\"-4dp\"",
                    )) assertTrue("$layout: $slot underline lacks $attribute", bar.contains("android:$attribute"))
                    // Without these the bar goes back over the text (seen on a Redmi, 4 Nov 2026) or is clipped away.
                    val rowTag = Regex("""<FrameLayout\b[^>]*android:id="@\+id/widget_row_$slot"[^>]*>""").find(xml)?.value.orEmpty()
                    assertTrue("$layout: $slot row must not clip to padding", rowTag.contains("android:clipToPadding=\"false\""))
                }
            }
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
            for (glyph in glyphs) {
                if (glyph.value.contains("""android:src="@drawable/widget_mark"""")) {
                    assertFalse("$layout: the app mark must keep its own colours", glyph.value.contains("android:tint"))
                    continue
                }
                assertTrue("$layout: ${glyph.value}", glyph.value.contains(Regex("""android:tint="@color/widget_[a-z_]+"""")))
            }
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
            "xml/widget_today_strip_info.xml" to Triple("widget_strip", "widget_preview_strip", "widget_strip_desc"),
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
        for (name in listOf("widget_preview_next", "widget_preview_today", "widget_preview_strip")) {
            val size = File(res, "drawable-nodpi/$name.png").length()
            assertTrue("$name is $size bytes", size in 1..30_000)
        }
    }

    @Test
    fun `the sizes are the ones the layouts were designed for`() {
        val next = read("xml/widget_next_prayer_info.xml")
        assertTrue(next.contains("""android:targetCellWidth="3"""") && next.contains("""android:targetCellHeight="1""""))
        // Neither widget may be resized below the size its content was drawn for.
        assertTrue(next.contains("""android:minResizeWidth="180dp"""") && next.contains("""android:minResizeHeight="100dp""""))
        val today = read("xml/widget_today_info.xml")
        // Default 4 by 3 (Ali, 4 Nov 2026): a new widget opens as the compact card with no spare height. The 4 by 4
        // default made the emulator's launcher open a tall 3 by 5 slot with an empty band under the rows.
        assertTrue(today.contains("""android:targetCellWidth="4"""") && today.contains("""android:targetCellHeight="3""""))
        // The Today widget must never be resizable below the height its six rows need. 290dp since 4 Nov 2026 (Ali asked for
        // the compact card to be taller, so its rows can sit with even gaps); WidgetRenderer.COMPACT_MIN_DP is the same figure.
        assertTrue(today.contains("""android:minResizeHeight="290dp""""))
        // The strip is one cell tall everywhere. 40dp and not 60: a launcher that sizes by 70n minus 30 would
        // round a 60dp minimum up to two cells (STEP-8-DESIGN section 3).
        val strip = read("xml/widget_today_strip_info.xml")
        assertTrue(strip.contains("""android:targetCellWidth="4"""") && strip.contains("""android:targetCellHeight="1""""))
        assertTrue(strip.contains("""android:minHeight="40dp"""") && strip.contains("""android:minResizeHeight="40dp""""))
    }

    /**
     * The four skies and their fixed copies are widget_background with the body colours changed and
     * nothing else: one gradient with a centre stop at 55 percent, the sheen, the rim. A sky that lost its
     * sheen or rim would be the one widget out of the family.
     */
    @Test
    fun `each sky is the widget surface with its own three stops, the same sheen and the same rim`() {
        fun shape(xml: String) = Regex("""<(layer-list|item|shape|gradient|solid|stroke|corners)\b""").findAll(withoutComments(xml)).map { it.groupValues[1] }.toList()
        val surface = shape(read("drawable/widget_background.xml"))
        for (sky in listOf("dawn", "day", "evening", "night")) {
            for (suffix in listOf("", "_light", "_dark")) {
                val name = "widget_sky_$sky$suffix"
                val xml = withoutComments(read("drawable/$name.xml"))
                assertEquals(name, surface, shape(xml))
                assertTrue("$name body centre stop", xml.contains("""android:centerY="0.55""""))
                assertTrue("$name sheen", xml.contains("""android:centerY="0.45""""))
                val prefix = suffix.removePrefix("_").let { if (it.isEmpty()) "widget_" else "widget_${it}_" }
                for (stop in listOf("sky_${sky}_top", "sky_${sky}_mid", "sky_foot", "sheen", "edge")) {
                    assertTrue("$name lacks $prefix$stop", xml.contains("@color/$prefix$stop"))
                }
            }
        }
    }

    @Test
    fun `every colour the widget files use is defined in both themes`() {
        val drawables = listOf(
            "widget_background", "widget_row_highlight", "widget_strip_current", "widget_row_plain", "widget_hero_background", "widget_divider", "widget_chip",
            "widget_seg_done", "widget_seg_next", "widget_seg_todo", "widget_underline",
            // Step 8: the Next prayer widget's skies, and what has to stay visible on them.
            "widget_sky_dawn", "widget_sky_day", "widget_sky_evening", "widget_sky_night",
            "widget_pill_sky", "widget_seg_todo_sky", "widget_divider_sky",
            "widget_strip_dawn", "widget_strip_day", "widget_strip_evening", "widget_strip_night",
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
        assertEquals(16, names.size)
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

    // Ali, 4 Nov 2026: the thinnest strip shows the next prayer alone, because five columns want 45 to 49.5dp and the floor is 40.
    // A source guard (the renderer needs a device to run): drop any of these and a 40dp strip is cut again.
    @Test
    fun thin_strip_shows_the_next_prayer_alone_under_the_five_column_height() {
        val source = File(root, "app/src/main/java/com/sajdatime/app/widget/WidgetRenderer.kt").readText()
        assertTrue("strip must switch to the next prayer alone under stripFiveHeight",
            source.contains("size.height < stripFiveHeight(context)"))
        assertTrue("stripFiveHeight must be 50dp (56 in the tall scripts)",
            source.contains("STRIP_FIVE_HEIGHT_DP = 50f") && source.contains("56f"))
        assertTrue("the first strip entry (all Android below 12 gets) must be the five column one",
            source.contains("SizeF(250f, five), SizeF(250f, 40f)"))
        assertTrue("the no location branch must write widget_strip_row visible",
            source.contains("setViewVisibility(R.id.widget_strip_row, View.VISIBLE)"))
    }
}
