package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * The two guards on [AppLocale], both of which exist because the thing they protect fails
 * silently.
 *
 * `app_language_tag` only works if every translation overrides it. If one does not, the
 * app carries on happily showing that translation's words with English number and date
 * formatting, and nothing anywhere fails — exactly the months-later failure mode these
 * tests convert into a red build. The second guard is blunter: `Locale.getDefault()` is
 * the device's language, it is the obvious thing to reach for, and reaching for it is the
 * bug. See AppLocale.kt for the four separate ways that went wrong.
 *
 * They read the repository's own sources and resource folders rather than anything
 * packaged, because the point is to catch the mistake at the moment it is made, on a plain
 * JVM, with no device and no Robolectric.
 */
class LocaleDisciplineTest {

    /** `values-ar`, `values-en-rGB`, `values-b+sr+Latn` — but not `values-night` or `values-v31`. */
    private val localeQualified = Regex("""^values-(b\+.+|[a-z]{2,3}(-r[A-Z]{2})?)$""")

    @Test
    fun `the default tag is a real locale`() {
        val declared = defaultStrings().tagOrNull()
        assertTrue("core/values/strings.xml declares no app_language_tag", declared != null)
        val locale = Locale.forLanguageTag(declared!!)
        assertTrue("app_language_tag '$declared' is not a usable language tag", locale.language.isNotEmpty())
        // Round-trips, so a typo like "en_GB" or "english" fails here rather than
        // silently degrading to the root locale at runtime.
        assertEquals(declared, locale.toLanguageTag())
    }

    @Test
    fun `every translation declares its own tag`() {
        val offenders = translatedStringFiles()
            .filter { it.tagOrNull() == null }
            .map { it.relativeTo(repoRoot).path }

        assertTrue(
            "These translations do not declare app_language_tag, so their words would be " +
                "shown with English number and date formatting. Add the string, or read " +
                "AppLocale.kt for why it is there. Offenders: $offenders",
            offenders.isEmpty(),
        )
    }

    @Test
    fun `every translation's tag matches the folder it is in`() {
        val mismatched = translatedStringFiles().mapNotNull { file ->
            val declared = file.tagOrNull() ?: return@mapNotNull null
            val folder = file.parentFile!!.name.removePrefix("values-")
            // values-en-rGB holds en-GB; values-ar holds ar or a variant of it.
            val expected = folder.replace("-r", "-")
            if (declared.equals(expected, ignoreCase = true) ||
                declared.startsWith("$expected-", ignoreCase = true)
            ) {
                null
            } else {
                "${file.relativeTo(repoRoot).path} is in values-$folder but declares '$declared'"
            }
        }

        assertTrue("Language tag does not match its folder: $mismatched", mismatched.isEmpty())
    }

    @Test
    fun `no shipped code formats from the device locale`() {
        val mainSources = "${File.separator}src${File.separator}main${File.separator}"
        val offenders = repoRoot.walkTopDown()
            .onEnter { it.name != "build" && it.name != ".git" }
            .filter { it.extension == "kt" && mainSources in it.path }
            .flatMap { file ->
                file.readLines().asSequence().mapIndexedNotNull { i, line ->
                    val code = line.substringBefore("//").trim()
                    if (code.startsWith("*") || !code.contains("Locale.getDefault()")) {
                        null
                    } else {
                        "${'$'}{file.relativeTo(repoRoot).path}:${'$'}{i + 1}"
                    }
                }
            }
            .toList()

        assertTrue(
            "Locale.getDefault() is the device's language. The app's own language is " +
                "AppLocale.of(context), and mixing the two is what put Arabic-Indic digits " +
                "inside English sentences and reordered a Hijri date. If the platform " +
                "genuinely demands the device locale somewhere, say so in a comment on the " +
                "same line and this check will pass. Offenders: ${'$'}offenders",
            offenders.isEmpty(),
        )
    }

    private fun defaultStrings() = File(repoRoot, "core/src/main/res/values/strings.xml")

    private fun translatedStringFiles(): List<File> = repoRoot.walkTopDown()
        .onEnter { it.name != "build" && it.name != ".git" }
        .filter { it.name == "strings.xml" && localeQualified.matches(it.parentFile!!.name) }
        .toList()

    private fun File.tagOrNull(): String? =
        Regex("""<string name="app_language_tag">([^<]+)</string>""")
            .find(readText())
            ?.groupValues
            ?.get(1)

    private val repoRoot: File
        get() = generateSequence(File("").absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
}
