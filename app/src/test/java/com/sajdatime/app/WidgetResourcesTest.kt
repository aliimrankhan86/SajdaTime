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

    private val layouts = listOf("layout/widget_next_prayer.xml", "layout/widget_today.xml")
    private val providers = listOf("xml/widget_next_prayer_info.xml", "xml/widget_today_info.xml")
    private val languages = listOf("values", "values-ur", "values-tr", "values-in")

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
        val next = read("layout/widget_next_prayer.xml")
        for (id in listOf("widget_root", "widget_label", "widget_name", "widget_time", "widget_sublabel", "widget_countdown")) {
            assertTrue("next prayer lacks $id", next.contains("""@+id/$id""""))
        }
        val today = read("layout/widget_today.xml")
        val ids = mutableListOf("widget_root", "widget_next_line", "widget_countdown", "widget_footer")
        for (slot in listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")) {
            ids += listOf("widget_row_$slot", "widget_name_$slot", "widget_label_$slot", "widget_time_$slot")
        }
        for (id in ids) assertTrue("today lacks $id", today.contains("""@+id/$id""""))
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
        assertTrue(next.contains("""android:targetCellWidth="2"""") && next.contains("""android:targetCellHeight="2""""))
        val today = read("xml/widget_today_info.xml")
        assertTrue(today.contains("""android:targetCellWidth="4"""") && today.contains("""android:targetCellHeight="3""""))
        // The Today widget must never be resizable below the height its six rows need.
        assertTrue(today.contains("""android:minResizeHeight="180dp""""))
    }

    @Test
    fun `every colour the widget files use is defined in both themes`() {
        val files = layouts + listOf("drawable/widget_background.xml", "drawable/widget_row_highlight.xml", "drawable/widget_row_plain.xml")
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
        assertEquals(15, names.size)
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
