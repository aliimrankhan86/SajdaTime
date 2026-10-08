package com.sajdatime.app

import com.sajdatime.app.ui.settings.AppLanguage
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The picker, the system's list (`locales_config.xml`), the manifest and the resource folders
 * must agree, and "follow the phone" must stay the default. Which languages may exist at all is
 * fenced by [TranslationScopeTest].
 */
class LanguagePickerTest {
    private fun read(path: String) = File("..", path).readText()

    /** The language tag a picker entry uses -> the Android resource qualifier of its folder. */
    private fun qualifier(tag: String) = if (tag == "id") "in" else tag

    @Test
    fun `the picker offers English first, then Urdu, Turkish and Indonesian`() {
        assertEquals(listOf("en", "ur", "tr", "id"), AppLanguage.entries.map { it.tag })
    }

    @Test
    fun `the system list matches the picker and the manifest points at it`() {
        val tags = Regex("""<locale android:name="([^"]+)"""")
            .findAll(read("app/src/main/res/xml/locales_config.xml")).map { it.groupValues[1] }.toList()
        assertEquals(AppLanguage.entries.map { it.tag }, tags)
        assertTrue(read("app/src/main/AndroidManifest.xml").contains("""android:localeConfig="@xml/locales_config""""))
    }

    @Test
    fun `every language the picker offers other than English has its words in all three modules`() {
        for (language in AppLanguage.entries.filter { it.tag != "en" }) {
            for (module in listOf("app", "core", "wear")) {
                val folder = "$module/src/main/res/values-${qualifier(language.tag)}/strings.xml"
                assertTrue("The picker offers ${language.tag} but $folder is missing", File("..", folder).isFile)
            }
        }
    }

    @Test
    fun `Indonesian is the same language under its old and new codes`() {
        // Locale.getLanguage() answers "in" or "id" depending on the Android version. Comparing
        // strings made Indonesian "unavailable" on exactly the phones that have it.
        assertTrue(AppLanguage.sameLanguage("id", "in"))
        assertTrue(AppLanguage.sameLanguage("in-ID", "id"))
        assertTrue(AppLanguage.sameLanguage("ur-PK", "ur"))
        assertFalse(AppLanguage.sameLanguage("tr", "ur"))
    }
}
