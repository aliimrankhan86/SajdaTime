package com.sajdatime.app

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the hard rule in CLAUDE.md — **the religious disclaimer must never be removed,
 * softened, or buried** — and the specification of *what it has to say* in
 * `docs/HANDOVER.md` §5.15.
 *
 * This exists because the disclaimer is prose, spread across four files in four different
 * formats, and prose is exactly what a later edit quietly erodes. Nothing else in the build
 * connects them: the app would compile, ship and pass review with the warranty sentence
 * deleted and the dua request moved to the top.
 *
 * It reads the files off disk rather than through `R.string`, for the same reason
 * `NoTranslationsYetTest` does — the published web page and the Play listing are not
 * resources, and the point is to check the *copies* have not drifted apart.
 *
 * **What it is not.** It matches a handful of load-bearing phrases, not whole sentences, so
 * the wording stays free to improve. It cannot judge whether a rewrite still *means* the same
 * thing. If you are rewording deliberately and a phrase here is genuinely wrong for the new
 * text, change the phrase in the same commit and say why — but change all four copies, which
 * is the thing this test is really here to make you do.
 */
class DisclaimerContentTest {

    private val root = File("..").canonicalFile

    private fun read(path: String) = File(root, path).also {
        // A silently-missing file would make every assertion below vacuously pass, which is
        // the failure mode a guard must not have.
        assertTrue("Expected to find $path — has it moved?", it.isFile)
    }.readText()

    private val phoneStrings by lazy { read("app/src/main/res/values/strings.xml") }

    /** The `disclaimer_body` resource, raw, with its literal `\n\n` paragraph breaks intact. */
    private val disclaimer by lazy {
        Regex("""<string name="disclaimer_body">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(phoneStrings)!!
            .groupValues[1]
    }

    private fun assertContains(haystack: String, needle: String, where: String) =
        assertTrue(
            "$where no longer contains \"$needle\". See docs/HANDOVER.md §5.15 — the " +
                "disclaimer has to establish four things, and all four copies change together.",
            haystack.contains(needle, ignoreCase = true),
        )

    // Point 1: these are a calculation, not a timetable from an authority.
    // Point 2: the calculation can be wrong, and how.
    // Point 3: a disagreement with the mosque is settled in the mosque's favour.
    // Point 4: free, as it is, no warranty.
    @Test
    fun `the in-app disclaimer still makes all four points`() {
        assertContains(disclaimer, "not a religious authority", "disclaimer_body")
        assertContains(disclaimer, "calculat", "disclaimer_body (point 1: where times come from)")
        assertContains(disclaimer, "artificial intelligence", "disclaimer_body (point 2: how it fails)")
        assertContains(disclaimer, "wrong clock", "disclaimer_body (point 2: how it fails)")
        assertContains(disclaimer, "follow your mosque", "disclaimer_body (point 3: who wins)")
        assertContains(disclaimer, "no warranty", "disclaimer_body (point 4: no warranty)")
    }

    /**
     * The dua request is asked once, on one screen, at the end. CLAUDE.md puts it here "and
     * nowhere else", and it was briefly a second card in About until that was undone.
     */
    @Test
    fun `the dua request is the last paragraph and appears exactly once`() {
        val paragraphs = disclaimer.split("\\n\\n").filter { it.isNotBlank() }
        assertTrue(
            "The disclaimer's last paragraph is no longer the dua request. It belongs there " +
                "and nowhere else — CLAUDE.md.",
            paragraphs.last().contains("duas"),
        )
        assertTrue(
            "\"duas\" appears more than once in the disclaimer. The user is asked once.",
            disclaimer.split("duas").size == 2,
        )
    }

    /**
     * The two points that survive the shortening for the wrist, plus the length tripwire.
     *
     * **The length check is necessary, not sufficient, and that is the whole lesson.** This
     * string is the second-to-last item in a bottom-anchored list, so its top rises into the
     * cap of the circle where the watch face clock is painted; at four lines the first line is
     * printed straight through. Wrapping is by word, so a *shorter* string can still be taller
     * — 88 characters fitted three lines, 85 needed four, 78 fitted. Passing this test is not
     * evidence the layout is intact. Run `./tools/wear-verify.sh` and look at the captures.
     */
    @Test
    fun `the watch disclaimer keeps its two points and its length budget`() {
        val wear = Regex("""<string name="wear_disclaimer">(.*?)</string>""")
            .find(read("wear/src/main/res/values/strings.xml"))!!
            .groupValues[1]

        assertContains(wear, "not a religious authority", "wear_disclaimer")
        assertContains(wear, "calculat", "wear_disclaimer (point 1)")
        assertContains(wear, "follow your mosque", "wear_disclaimer (point 3)")
        assertTrue(
            "wear_disclaimer is ${wear.length} characters. Over ~88 it has always wrapped to " +
                "four lines and gone under the watch face clock. This check cannot prove three " +
                "lines — only ./tools/wear-verify.sh can — but it can prove you did not read " +
                "the comment above the string.",
            wear.length <= 88,
        )
    }

    /**
     * The published copies. These are what someone reads without installing anything, and the
     * privacy page is the URL Google Play itself links to.
     */
    @Test
    fun `the published copies still carry the disclaimer`() {
        val privacy = read("docs/privacy.html")
        assertContains(privacy, """id="disclaimer"""", "docs/privacy.html")
        assertContains(privacy, "not a religious authority", "docs/privacy.html")
        assertContains(privacy, "calculat", "docs/privacy.html (point 1)")
        assertContains(privacy, "artificial intelligence", "docs/privacy.html (point 2)")
        assertContains(privacy, "follow your mosque", "docs/privacy.html (point 3)")
        assertContains(privacy, "no warranty", "docs/privacy.html (point 4)")

        // The dua request is asked in the app, once. A web page that repeats it asks twice.
        assertTrue(
            "docs/privacy.html now repeats the dua request. It belongs on the first-run " +
                "screen and nowhere else — CLAUDE.md.",
            !privacy.contains("duas", ignoreCase = true),
        )

        val index = read("docs/index.html")
        assertContains(index, "privacy.html#disclaimer", "docs/index.html")
        assertContains(index, "follow your mosque", "docs/index.html")

        val listing = read("docs/store/LISTING.md")
        assertContains(listing, "not a religious authority", "docs/store/LISTING.md")
        assertContains(listing, "follow your mosque", "docs/store/LISTING.md (point 3)")
        assertContains(listing, "no warranty", "docs/store/LISTING.md (point 4)")

        // The README is the first thing a visitor to the public GitHub repo reads, and it was
        // carrying a sixth copy that nobody had counted — missing "follow your mosque" and the
        // no-warranty sentence, which are two of the four points. Found by audit on 2 Aug 2026,
        // not by this test, because this test did not know the file existed.
        val readme = read("README.md")
        assertContains(readme, "not a religious authority", "README.md")
        assertContains(readme, "follow your mosque", "README.md (point 3)")
        assertContains(readme, "no warranty", "README.md (point 4)")
        assertTrue(
            "README.md now repeats the dua request. It belongs on the first-run screen and " +
                "nowhere else — CLAUDE.md.",
            !readme.contains("duas", ignoreCase = true),
        )
    }
}
