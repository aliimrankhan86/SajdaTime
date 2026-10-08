package com.sajdatime.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Replaces `NoTranslationsYetTest`, which forbade every translation until a native speaker had
 * reviewed it. That rule is still CLAUDE.md's rule, with **one scoped exception**, and this test
 * is the exception's fence.
 *
 * On 5 Oct 2026 the owner waived the native-speaker review for Urdu, Turkish and Indonesian
 * only (he reads Urdu; nobody on the project can check the other two), asked the assistant to do
 * the translations, and approved the safeguards in `docs/translation/BRIEF.md`. The three
 * languages were confirmed in his 7 Oct brief. Reviewer on record: **none, owner's waiver**.
 *
 * So any *other* language folder still fails the build. Adding Arabic, Bengali or French is a new
 * decision that needs its own owner sign-off, recorded in CLAUDE.md and here, not a quiet edit.
 */
class TranslationScopeTest {

    /** Android resource qualifiers of the three approved languages. Indonesian is `in`, not `id`. */
    private val approved = setOf("in", "tr", "ur")

    private val languageQualifier = Regex("^([a-z]{2,3})(-r[A-Z]{2})?$|^b\\+.+")
    private val notLanguages = setOf("land", "port", "night", "car", "television", "watch")

    private fun valuesDirs(): List<File> {
        val root = File("..").canonicalFile
        return listOf("app", "wear", "core")
            .map { File(root, "$it/src") }
            .filter { it.isDirectory }
            .flatMap { it.walkTopDown().filter { f -> f.isDirectory && f.name.startsWith("values-") } }
    }

    @Test
    fun `only the three approved languages have a translation folder`() {
        val unapproved = valuesDirs()
            .map { it to it.name.removePrefix("values-") }
            .filter { (_, q) -> q !in notLanguages && languageQualifier.matches(q) && q.substringBefore("-r") !in approved }
            .map { (dir, _) -> dir.path }

        assertEquals(
            "Found a translation folder for a language the owner has not approved. The only " +
                "approved languages are Indonesian (in), Turkish (tr) and Urdu (ur), under the " +
                "owner's 5 Oct 2026 waiver (docs/translation/BRIEF.md). A new language needs a " +
                "new owner decision, recorded in CLAUDE.md and in this test.",
            emptyList<String>(),
            unapproved,
        )
    }

    @Test
    fun `the walk actually reaches the resource folders it is meant to police`() {
        // A wrong working directory would make the test above pass by finding nothing.
        assertTrue("found no values-* folder at all, so this guard checks nothing", valuesDirs().isNotEmpty())
    }
}
