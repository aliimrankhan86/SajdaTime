package com.sajdatime.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the hard rule in CLAUDE.md: **never machine-translate the app.** Prayer and madhab
 * names are religious content, and every language needs a native speaker before it ships.
 *
 * This exists because the thing that used to discourage a translation was a side effect
 * rather than a guard. The `rtl` build type pinned itself to `ar-XB`, a reserved pseudolocale
 * no real phone can be set to, and the reasoning recorded at the time was that this stopped
 * the build type being "promoted into" an Arabic translation. It never did anything of the
 * sort — nothing about a pseudolocale prevents someone adding `values-ar/` to the main source
 * set, which is the move the rule actually forbids. When `rtl` moved to the real tag `ur` so
 * a human could read it (see `app/src/rtl/res/values/strings.xml`), the incidental protection
 * went, and this replaced it with a deliberate one that covers more than the old one did.
 *
 * If a genuine, native-speaker translation is ever ready, this test is the thing to delete —
 * consciously, in the same commit, with the reviewer named in the message.
 */
class NoTranslationsYetTest {

    /**
     * A `values-*` folder is a translation only when the qualifier is a *language*.
     * `values-night`, `values-land`, `values-v31` and `values-sw600dp` are all legitimate and
     * must not trip this. BCP-47 language qualifiers are `values-ar`, `values-ar-rEG`, or the
     * `values-b+sr+Latn` form.
     */
    private val languageQualifier = Regex("^([a-z]{2,3})(-r[A-Z]{2})?$|^b\\+.+")

    /** Not a language, despite matching the shape of one. */
    private val notLanguages = setOf("land", "port", "night", "car", "television", "watch")

    private fun resDirs(): List<File> {
        // Tests run with the module directory as the working directory, so the other
        // modules are siblings. Checked rather than assumed: the assertion below fails
        // loudly if this walk finds nothing, which is what would happen if that changed
        // and the test silently started passing for the wrong reason.
        val root = File("..").canonicalFile
        return listOf("app", "wear", "core")
            .map { File(root, "$it/src") }
            .filter { it.isDirectory }
            .flatMap { it.walkTopDown().filter { f -> f.isDirectory && f.name.startsWith("values-") } }
    }

    @Test
    fun `no language-qualified resource folder exists anywhere in the project`() {
        val translations = resDirs()
            .map { it to it.name.removePrefix("values-") }
            .filter { (_, qualifier) ->
                qualifier !in notLanguages && languageQualifier.matches(qualifier)
            }
            .map { (dir, _) -> dir.path }

        assertEquals(
            "Found a language-qualified resource folder. CLAUDE.md forbids machine " +
                "translation: prayer and madhab names are religious content and each " +
                "language needs a native speaker first. If this is a real reviewed " +
                "translation, delete this test in the same commit and name the reviewer.",
            emptyList<String>(),
            translations,
        )
    }

    @Test
    fun `the walk actually reaches the resource folders it is meant to police`() {
        // Without this, a wrong working directory would make the test above pass by
        // finding nothing at all — the most dangerous way for a guard to fail.
        val dirs = resDirs()
        assertTrue(
            "expected to find at least one values-* folder (e.g. values-night); " +
                "found none, so the test above is not actually checking anything",
            dirs.isNotEmpty(),
        )
    }

    @Test
    fun `the right-to-left preview locale is confined to the rtl build type`() {
        val root = File("..").canonicalFile
        val offenders = listOf("app", "wear", "core")
            .map { File(root, "$it/src") }
            .filter { it.isDirectory }
            .flatMap { it.walkTopDown().filter { f -> f.name == "strings.xml" } }
            .filter { it.readText().contains("<string name=\"app_language_tag\">ur</string>") }
            .map { it.path }
            .filterNot { it.contains("/src/rtl/") }

        assertEquals(
            "app_language_tag is pinned to the right-to-left preview locale outside the " +
                "rtl build type. That tag decides the language of the shipped app.",
            emptyList<String>(),
            offenders,
        )
    }
}
