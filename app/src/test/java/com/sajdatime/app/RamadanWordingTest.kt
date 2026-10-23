package com.sajdatime.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The words around Ramadan are a promise: the app reports Fajr and Maghrib as calculated and calls them
 * the end of suhoor and the time of iftar, adds no margin of its own, gives no rulings, and sends the
 * reader to their mosque wherever a timetable differs. Prose erodes quietly, so this pins the shape.
 *
 * Read off disk like EidWordingTest, because the point is to check every language file, and declared
 * as a task input in app/build.gradle.kts (the `values*` glob) so editing a string re-runs it.
 *
 * The English phrases are the four commitments in the plan (A11). The checks that are not about English
 * words run on all four languages: the same keys exist, the same placeholders sit in them, and none of
 * them carries a dash, a semicolon or an unnumbered placeholder.
 */
class RamadanWordingTest {

    private val root = File("..").canonicalFile
    private val languages = listOf("values", "values-ur", "values-tr", "values-in", "values-ar")

    private fun strings(folder: String): Map<String, String> {
        val file = File(root, "app/src/main/res/$folder/strings.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        // Comments removed first: they hold example phrases such as "stop eating" on purpose.
        val text = file.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        return Regex("""<string name="([a-z0-9_]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(text)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    private val english = strings("values")

    /** Every string that belongs to the Ramadan pack, the suhoor reminder, the Eid greeting or the widgets. */
    private val keys: List<String> = english.keys.filter {
        it.startsWith("ramadan_") || it.startsWith("suhoor_") || it.startsWith("notif_suhoor_") ||
            it.startsWith("settings_suhoor") || it.startsWith("settings_ramadan") || it.startsWith("widget_") ||
            it.startsWith("pdf_ramadan") || it.startsWith("pdf_period_ramadan") || it.startsWith("export_ramadan") ||
            it.startsWith("eid_")
    }.sorted()

    private fun placeholders(text: String): List<String> =
        Regex("""%\d+\$[sd]""").findAll(text).map { it.value }.sorted().toList()

    @Test
    fun `the set is the size it should be, so a renamed prefix cannot make the checks vacuous`() {
        assertTrue("only ${keys.size} keys matched", keys.size >= 55)
        // 13 since 18 Oct 2026: widget_update_location, the location chip's spoken label.
        assertEquals(13, keys.count { it.startsWith("widget_") })
    }

    @Test
    fun `the info note says as calculated, does not add, follow your mosque and Islamic date`() {
        val note = english.filterKeys { it.startsWith("ramadan_info_") }.values.joinToString(" ").lowercase()
        for (phrase in listOf("as calculated", "does not add", "follow your mosque", "islamic date")) {
            assertTrue("the info note lacks \"$phrase\"", note.contains(phrase))
        }
    }

    @Test
    fun `imsak appears only in the one sentence that says timetables print it`() {
        val mentions = english.filter { it.value.contains("imsak", ignoreCase = true) }
        assertEquals(listOf("ramadan_info_margin"), mentions.keys.toList())
        val sentence = mentions.getValue("ramadan_info_margin")
        assertTrue(sentence.contains("print", ignoreCase = true))
        assertTrue("it must say SajdaTime adds no margin", sentence.contains("does not add", ignoreCase = true))
        assertTrue("no string may be named for an imsak time", english.keys.none { it.contains("imsak") })
    }

    @Test
    fun `no string gives a ruling or tells the reader what they may or must do`() {
        val banned = Regex(
            """\b(you may|you must|you should|stop eating|valid|invalid|permitted|forbidden|halal|haram|allowed)\b""",
            RegexOption.IGNORE_CASE,
        )
        for (key in keys) {
            val hit = banned.find(english.getValue(key))
            assertTrue("$key says \"${hit?.value}\"", hit == null)
        }
    }

    @Test
    fun `every explanation that could differ from a mosque sends the reader to the mosque`() {
        for (key in listOf("ramadan_info_mosque", "pdf_ramadan_note", "eid_fine_print")) {
            assertTrue("$key does not mention the mosque", english.getValue(key).contains("mosque", ignoreCase = true))
        }
        assertTrue(english.getValue("ramadan_month_ends_today").contains("mosque", ignoreCase = true))
    }

    @Test
    fun `the eve notice names what mosques do and what the app does`() {
        assertTrue(english.getValue("ramadan_eve_title_before").contains("calendar", ignoreCase = true))
        assertTrue(english.getValue("ramadan_eve_body").contains("mosques announce", ignoreCase = true))
    }

    @Test
    fun `the widget descriptions no longer say they follow the phone's dark mode`() {
        // True until 19 Oct 2026. The widgets now follow the app's Theme row (WidgetThemeTest), as the
        // app does, so the sentence that warned of the difference was removed in every language.
        for (key in listOf("widget_next_desc", "widget_today_desc")) {
            assertFalse("$key", english.getValue(key).contains("dark mode", ignoreCase = true))
        }
    }

    @Test
    fun `the widgets say Suhoor ends and Iftar, the same two words as the card`() {
        assertEquals(english.getValue("ramadan_suhoor_ends"), english.getValue("widget_suhoor_ends"))
        assertEquals(english.getValue("ramadan_iftar"), english.getValue("widget_iftar"))
    }

    @Test
    fun `every language has every key in the set and none is blank`() {
        for (folder in languages) {
            val s = strings(folder)
            for (key in keys) {
                val value = s[key]
                assertNotNull("$folder is missing $key", value)
                assertTrue("$folder $key is blank", value!!.isNotBlank())
            }
        }
    }

    @Test
    fun `every translation carries exactly the placeholders of the English`() {
        for (folder in languages.drop(1)) {
            val s = strings(folder)
            for (key in keys) {
                assertEquals("$folder $key", placeholders(english.getValue(key)), placeholders(s.getValue(key)))
            }
        }
    }

    @Test
    fun `placeholders are numbered, in every language`() {
        val bare = Regex("""%[sd]""")
        for (folder in languages) {
            val s = strings(folder)
            for (key in keys) {
                assertFalse("$folder $key has an unnumbered placeholder", bare.containsMatchIn(s.getValue(key)))
            }
        }
    }

    @Test
    fun `no em dash, no en dash and no semicolon in any of them, in any language`() {
        for (folder in languages) {
            val s = strings(folder)
            for (key in keys) {
                // Entities such as &amp; end in a semicolon and are not prose.
                val prose = s.getValue(key).replace(Regex("""&[a-z#0-9]+;"""), "")
                assertFalse("$folder $key has a dash", prose.contains('—') || prose.contains('–'))
                assertFalse("$folder $key has a semicolon", prose.contains(';'))
            }
        }
    }
}
