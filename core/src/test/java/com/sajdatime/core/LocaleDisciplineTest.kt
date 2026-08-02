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

    /**
     * **The shipped app is left-to-right, and stays that way until it is written in a
     * right-to-left language.**
     *
     * The owner's instruction, 2 August 2026, after reviewing the RTL preview build for the
     * third time: *"I dont [want] RTL to be default because the text is not looking great in
     * RTL."* He is right, and the reason is not cosmetic. English words laid out
     * right-to-left are genuinely wrong — the closing full stop moves to the far end, a
     * leading number changes places with the sentence it belongs to — because the Unicode
     * algorithm is being told the paragraph is Arabic when every word in it is English.
     *
     * Nothing about that changes on the day `values-ar/` ships. On that day the words *are*
     * Arabic, the paragraph direction is correct, and the app should and will go
     * right-to-left on its own with no code change, because [AppLocale] reads the direction
     * out of the language the words are in. This test does not stand in the way of that; it
     * stands in the way of arriving there *early*, with the layout flipped and the words
     * still English, which is the only version the owner has ever been shown and the only
     * version that looks broken.
     *
     * ponytail: an explicit set rather than a lookup. The JDK's own
     * `ComponentOrientation.getOrientation` knows seven RTL languages and misses Pashto,
     * Sindhi, Uyghur, Kurdish and Divehi; ICU is not on a plain JVM test classpath. This list
     * does not have to be exhaustive — it has to cover the languages this app could plausibly
     * be translated into, and the failure it guards is a deliberate edit, not a typo.
     */
    @Test
    fun `the shipped app is left to right`() {
        val declared = defaultStrings().tagOrNull()!!
        val language = Locale.forLanguageTag(declared).language

        assertTrue(
            "app_language_tag is '$declared', which is a right-to-left language. That flips " +
                "the whole app — layout, digits and date order — so it must only ever be set " +
                "to a language the app is actually translated into. If a native-speaker " +
                "translation has genuinely shipped, this test is the one to change, in the " +
                "same commit, naming the reviewer. To preview right-to-left without shipping " +
                "it, run ./gradlew installRtl.",
            language !in rightToLeftLanguages,
        )
    }

    /**
     * A tag may only name a language the app has words in.
     *
     * Setting it to a language with no `values-<lang>/` behind it is the exact failure the
     * test above describes: the words keep falling back to English while the formatting, the
     * digits and the layout direction all switch. It is also indistinguishable from a
     * half-finished translation, which is the state CLAUDE.md forbids shipping.
     *
     * The default `values/` file is exempt because it *defines* the base language rather than
     * selecting one, and `src/rtl/` is exempt because that build type exists to produce
     * precisely this mismatch on purpose — it carries `applicationIdSuffix ".rtl"` and its own
     * `app_name`, so it installs beside the real app and can never be mistaken for it.
     *
     * This replaces the narrower check that used to live in `NoTranslationsYetTest`, which
     * only knew the single literal tag `ur` and would have passed for `ar`, `fa` or `he`.
     */
    @Test
    fun `no shipping source set points the app at a language it is not written in`() {
        val translated = translatedStringFiles()
            .map { it.parentFile!!.name.removePrefix("values-").substringBefore("-r").lowercase() }
            .toSet()

        val offenders = repoRoot.walkTopDown()
            .onEnter { it.name != "build" && it.name != ".git" }
            .filter { it.name == "strings.xml" && it.parentFile!!.name == "values" }
            .filterNot { it.path.contains("${File.separator}src${File.separator}rtl${File.separator}") }
            .filterNot { it.canonicalFile == defaultStrings().canonicalFile }
            .mapNotNull { file ->
                val tag = file.tagOrNull() ?: return@mapNotNull null
                val language = Locale.forLanguageTag(tag).language
                if (language in translated) {
                    null
                } else {
                    "${file.relativeTo(repoRoot).path} selects '$tag' but there is no values-$language/"
                }
            }
            .toList()

        assertTrue(
            "A shipping source set points app_language_tag at a language the app has no " +
                "words in. The words would stay English while the digits, dates and layout " +
                "direction all changed. Offenders: $offenders",
            offenders.isEmpty(),
        )
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

    /** See `the shipped app is left to right` for why this is a list and not a lookup. */
    private val rightToLeftLanguages = setOf(
        "ar", // Arabic
        "fa", // Persian
        "ur", // Urdu
        "ps", // Pashto
        "sd", // Sindhi
        "ug", // Uyghur
        "ku", "ckb", // Kurdish (Sorani)
        "dv", // Divehi
        "he", "iw", // Hebrew, and its withdrawn ISO code, which Locale still normalises to
        "yi", "ji", // Yiddish, likewise
        "syr", // Syriac
        "nqo", // N'Ko
    )

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
