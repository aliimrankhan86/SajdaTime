package com.sajdatime.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The Eid greeting has a shape the owner asked for on 16 Oct 2026: the main text is a warm greeting
 * that suits everyone, and the caveat (this date is our calculation, sorry if you celebrate on
 * another day, follow your mosque) lives in small print under it. Prose erodes quietly, so this pins
 * the shape in every language, not the exact words.
 *
 * Read off disk like DisclaimerContentTest, because the point is to check every language file.
 */
class EidWordingTest {

    private val root = File("..").canonicalFile

    private val languages = listOf("values", "values-ur", "values-tr", "values-in")
    private val names = listOf("eid_fitr_title", "eid_fitr_body", "eid_adha_title", "eid_adha_body", "eid_fine_print")

    private fun strings(folder: String): Map<String, String> {
        val file = File(root, "app/src/main/res/$folder/strings.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        return Regex("""<string name="([a-z0-9_]+)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(file.readText())
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    /** One word per language for each of the three jobs the fine print must do. */
    private data class FinePrintWords(val calculation: String, val apology: String, val mosque: String)

    private val finePrintWords = mapOf(
        "values" to FinePrintWords("calculation", "Sorry", "mosque"),
        "values-ur" to FinePrintWords("حساب", "معذرت", "مسجد"),
        "values-tr" to FinePrintWords("hesab", "kusura", "camin"),
        "values-in" to FinePrintWords("perhitungan", "maaf", "masjid"),
    )

    @Test
    fun `every language has all five Eid strings and none is blank`() {
        for (folder in languages) {
            val s = strings(folder)
            for (name in names) {
                val value = s[name]
                assertNotNull("$folder is missing $name", value)
                assertTrue("$folder $name is blank", value!!.isNotBlank())
            }
        }
    }

    @Test
    fun `the fine print says it is our calculation, apologises and defers to the mosque, in every language`() {
        for (folder in languages) {
            val text = strings(folder).getValue("eid_fine_print")
            val words = finePrintWords.getValue(folder)
            assertTrue("$folder fine print lacks the calculation", text.contains(words.calculation, ignoreCase = true))
            assertTrue("$folder fine print lacks the apology", text.contains(words.apology, ignoreCase = true))
            assertTrue("$folder fine print lacks the mosque", text.contains(words.mosque, ignoreCase = true))
        }
    }

    @Test
    fun `the greeting itself carries no caveat, so it suits everyone`() {
        for (folder in languages) {
            val s = strings(folder)
            val words = finePrintWords.getValue(folder)
            for (name in listOf("eid_fitr_title", "eid_fitr_body", "eid_adha_title", "eid_adha_body")) {
                val text = s.getValue(name)
                assertFalse("$folder $name mentions the calculation", text.contains(words.calculation, ignoreCase = true))
                assertFalse("$folder $name mentions the mosque", text.contains(words.mosque, ignoreCase = true))
                assertFalse("$folder $name apologises", text.contains(words.apology, ignoreCase = true))
            }
        }
    }

    @Test
    fun `the English titles are the plain greeting for each Eid`() {
        val s = strings("values")
        assertEquals("Eid al-Fitr Mubarak", s["eid_fitr_title"])
        assertEquals("Eid al-Adha Mubarak", s["eid_adha_title"])
    }

    @Test
    fun `no Eid string has an em dash or a format placeholder`() {
        for (folder in languages) {
            val s = strings(folder)
            for (name in names) {
                val text = s.getValue(name)
                assertFalse("$folder $name has an em dash", text.contains('—'))
                assertFalse("$folder $name has a placeholder, but the card passes none", text.contains("%"))
            }
        }
    }
}
