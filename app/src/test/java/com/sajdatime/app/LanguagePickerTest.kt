package com.sajdatime.app

import com.sajdatime.app.ui.settings.AppLanguage
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The picker, the system's list (`locales_config.xml`) and the manifest must agree, and English
 * must stay the default. Offering a language is not shipping it: NoTranslationsYetTest still
 * forbids any `values-<lang>/` until a native speaker has reviewed it.
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
}
