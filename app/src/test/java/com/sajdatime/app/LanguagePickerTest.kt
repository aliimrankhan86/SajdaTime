package com.sajdatime.app

import com.sajdatime.app.ui.settings.AppLanguage
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The picker, the system's list (`locales_config.xml`) and the manifest must agree, and English
 * must stay the default. A language is offered in the picker only if it has a `values-<tag>/`
 * folder in both :app and :core, so the picker can never offer words that are not there.
 * The Urdu, Turkish and Indonesian folders are Claude's translation of 8 Oct 2026, accepted by
 * the owner and NOT reviewed by a native speaker (the comment at the top of each file says so).
 * NoTranslationsYetTest was deleted in that commit for exactly this reason.
 */
class LanguagePickerTest {
    private fun read(path: String) = File("..", path).readText()

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
    fun `every language the picker offers has its words in both modules`() {
        for (language in AppLanguage.entries.filter { it != AppLanguage.ENGLISH }) {
            // Indonesian's folder is values-in: the framework matches on the legacy code.
            val folder = if (language.tag == "id") "in" else language.tag
            val app = File("..", "app/src/main/res/values-$folder/strings.xml")
            val core = File("..", "core/src/main/res/values-$folder/strings.xml")
            assertTrue("${language.tag}: missing $app", app.isFile)
            assertTrue("${language.tag}: missing $core", core.isFile)
            assertTrue(
                "${language.tag}: core must declare app_language_tag \"${language.tag}\"",
                core.readText().contains("""name="app_language_tag">${language.tag}<"""),
            )
        }
    }
}
