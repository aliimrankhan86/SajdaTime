package com.sajdatime.app

import com.sajdatime.app.ui.settings.AppLanguage
import java.io.File
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The picker, the system's list (`locales_config.xml`) and the manifest must agree, and English
 * must stay the default. A language is offered in the picker only if it has a `values-<tag>/`
 * folder in both :app and :core, so the picker can never offer words that are not there.
 * The Urdu, Turkish and Indonesian folders are Claude's translation of 8 Oct 2026. On 9 Oct 2026
 * the owner decided they need no native speaker review, so the app must not say they do.
 * NoTranslationsYetTest was deleted when they were added.
 *
 * The last three tests exist because 1.3.1 passed every test here and still could not switch
 * language on a real phone: Play installs only the phone's own language unless the bundle is
 * told otherwise (app/build.gradle.kts, `bundle.language.enableSplit`).
 */
class LanguagePickerTest {
    private fun read(path: String) = File("..", path).readText()

    @Test
    fun `the picker offers English first, then Urdu, Turkish, Indonesian and Arabic`() {
        assertEquals(listOf("en", "ur", "tr", "id", "ar"), AppLanguage.entries.map { it.tag })
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
                // The tag or a variant of it: Arabic is ar-u-nu-latn, to pin Western digits.
                Regex("""name="app_language_tag">${language.tag}(-[^<]*)?<""").containsMatchIn(core.readText()),
            )
        }
    }

    @Test
    fun `every language is packaged into the app, not split out by Play`() {
        // With splits on, Play installs only the languages the PHONE is set to, so an English
        // phone never receives the Urdu, Turkish or Indonesian words and the picker greys them
        // out. That is what the owner saw on his Samsung with 1.3.1 on 10 Oct 2026.
        val gradle = read("app/build.gradle.kts")
        val block = Regex("""bundle\s*\{\s*language\s*\{\s*enableSplit\s*=\s*false\s*\}\s*\}""")
        assertTrue(
            "app/build.gradle.kts must keep bundle { language { enableSplit = false } }, " +
                "or the in-app language picker stops working on every Play install",
            block.containsMatchIn(gradle.lines().filterNot { it.trim().startsWith("//") }.joinToString("\n")),
        )
    }

    @Test
    fun `Indonesian is recognised under its legacy code`() {
        // Android's Locale.getLanguage() reports Indonesian as "in", never "id".
        assertEquals("id", AppLanguage.normalise("in"))
        assertEquals("id", AppLanguage.normalise("ID"))
        assertEquals("ur", AppLanguage.normalise("ur"))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromLocale(Locale("in")))
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromLocale(Locale.forLanguageTag("id")))
        assertEquals(AppLanguage.URDU, AppLanguage.fromLocale(Locale.forLanguageTag("ur-PK")))
        assertEquals(AppLanguage.TURKISH, AppLanguage.fromLocale(Locale.forLanguageTag("tr")))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocale(Locale.forLanguageTag("fr")))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromLocale(null))
    }

    @Test
    fun `the picker never tells users a language is waiting for review`() {
        // Owner's decision, 9 Oct 2026: no native speaker review, so no such message.
        val text = Regex("""<string name="language_unavailable">([^<]*)</string>""")
            .find(read("app/src/main/res/values/strings.xml"))?.groupValues?.get(1)
        assertTrue("language_unavailable is missing", text != null)
        assertFalse("language_unavailable still mentions a review: $text", text!!.contains("review", ignoreCase = true))
        assertFalse("language_unavailable still mentions native speakers: $text", text.contains("native", ignoreCase = true))
    }
}
