package com.sajdatime.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * "My prayers" records, it never judges (Ali, 16 Oct 2026; sign-off, 23 Oct 2026). Prose drifts
 * quietly, so this pins the words in every language the app ships, read off disk like
 * RamadanWordingTest, and pins where the record may and may not go.
 */
class TrackerWordingTest {

    private val root = File("..").canonicalFile
    private val languages = stringFolders

    /** Strings and plural items, comments removed. */
    private fun strings(folder: String): Map<String, String> {
        val text = File(root, "app/src/main/res/$folder/strings.xml").readText()
            .replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        val plain = Regex("""<string name="([a-z0-9_]+)"[^>]*>(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
        val plurals = Regex("""<plurals name="([a-z0-9_]+)">(.*?)</plurals>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(text).associate { it.groupValues[1] to it.groupValues[2] }
        return plain + plurals
    }

    private fun trackerKeys(map: Map<String, String>) =
        map.filterKeys { it.startsWith("tracker_") || it.startsWith("settings_tracker") || it == "settings_group_tracker" ||
                it == "nav_tracker" || it == "action_undo"
        }

    @Test
    fun `every language has every tracker string`() {
        val english = trackerKeys(strings("values")).keys
        assertTrue("only ${english.size} keys", english.size >= 30)
        for (lang in languages.drop(1)) assertEquals(lang, english, trackerKeys(strings(lang)).keys)
    }

    @Test
    fun `no judging word in English, and none of the words Ali ruled out`() {
        val english = trackerKeys(strings("values")).values.joinToString(" ").lowercase()
        // "owed" and "debt" for the fasts to make up count (25 Oct 2026). English only, on purpose: in Arabic
        // and Urdu قضا, and in Turkish kaza, is the plain word for making up a fast, not a label of guilt
        // (DETAILED-PLAN.md §9 wording notes), so those may use it. Indonesian uses "diganti", not utang (debt).
        for (word in listOf("missed", "excused", "qada", "sin", "reward", "lazy", "shame", "failed to pray", "broke", "lost", "owed", "debt")) {
            assertFalse("\"$word\" in the tracker strings", Regex("""\b${Regex.escape(word)}\b""").containsMatchIn(english))
        }
    }

    @Test
    fun `the words say the record stays with the person`() {
        val english = strings("values")
        assertTrue(english.getValue("settings_tracker_desc").contains("Only you can see it"))
        assertTrue(english.getValue("settings_tracker_save_desc").contains("Keep the file private"))
    }

    @Test
    fun `no dash and no semicolon in any language`() {
        for (lang in languages) {
            for ((key, text) in trackerKeys(strings(lang))) {
                assertFalse("$lang $key", text.contains('—') || text.contains('–') || text.contains(';') || text.contains('؛'))
            }
        }
    }

    @Test
    fun `the usage counts never mention the tracker`() {
        val counts = File(root, "app/src/main/java/com/sajdatime/app/data/UsageCounts.kt").readText().lowercase()
        for (word in listOf("tracker", "tick", "streak", "pause", "prayed")) assertFalse(word, counts.contains(word))
    }
}
