package com.sajdatime.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Mechanical integrity of the four translations, against the English source.
 *
 * Written on the 8 Oct 2026 branch for Urdu, Turkish and Indonesian; brought into 1.5.0 on
 * 19 Oct 2026 with Arabic added (Arabic did not exist on 8 Oct). Its AI translation note check
 * was dropped: the owner decided on 19 Oct 2026 that the app shows no such note (and on 9 Oct
 * that it never tells users a translation awaits review).
 *
 * It cannot judge whether a translation is *good*. It catches what breaks the app or changes its
 * meaning without anything failing: a missing key (the string silently shows in English), a lost
 * or renumbered placeholder (a crash or a wrong number), a wrong set of plural forms, an
 * unescaped apostrophe (a build error on one machine and not another), a changed bullet or
 * paragraph count, a direction-mark character (the app isolates names itself), the wrong
 * `app_language_tag` (right words, wrong number format and layout direction), and a prayer name that
 * is not the agreed one. The same checks run from the command line as `tools/check-translation.py`,
 * which is what translators use while they work.
 *
 * The dua request is guarded too: the last of the disclaimer's seven paragraphs, and mentioned
 * nowhere else in the language (CLAUDE.md: asked once, never nagged).
 */
class TranslationIntegrityTest {

    private val root = File("..").canonicalFile
    private val modules = listOf("app", "core", "wear")

    /** qualifier -> (language tag, plural categories, the dua word, prayer names in core order). */
    private data class Lang(
        val tag: String,
        val plurals: Set<String>,
        val duaWord: String,
        val prayers: List<String>,
    )

    private val languages = mapOf(
        // The tags are the bare language codes the shipped 1.3.1 files declare (and picked on an
        // emulator): "id", not "id-ID". Turkish Fajr is "Sabah", not Diyanet's "İmsak": that was the
        // choice that shipped, and a reviewer judged both defensible (docs/translation/review-tr-TR.md).
        "in" to Lang("id", setOf("other"), "doa", listOf("Subuh", "Terbit", "Zuhur", "Asar", "Magrib", "Isya")),
        "tr" to Lang("tr", setOf("one", "other"), "dua", listOf("Sabah", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")),
        "ur" to Lang("ur", setOf("one", "other"), "دعا", listOf("فجر", "طلوع آفتاب", "ظہر", "عصر", "مغرب", "عشاء")),
        // Arabic (17 Oct 2026): its tag keeps Western digits (values-ar/strings.xml says why), and
        // Arabic has all six CLDR plural categories. "دعا" is the stem of دعائك, the dua request.
        "ar" to Lang(
            "ar-u-nu-latn",
            setOf("zero", "one", "two", "few", "many", "other"),
            "دعا",
            listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء"),
        ),
        // Bengali (30 Oct 2026): Western digits like Arabic; the spellings Bangladesh's mosques print.
        "bn" to Lang("bn-u-nu-latn", setOf("one", "other"), "দোয়া", listOf("ফজর", "সূর্যোদয়", "যোহর", "আসর", "মাগরিব", "এশা")),
    )

    @Test
    fun `every translation folder on disk is checked here`() {
        // A new values-<lang>/ with no entry above would skip every check in this file.
        assertEquals(stringFolders.drop(1).map { it.removePrefix("values-") }.toSet(), languages.keys)
    }

    private sealed interface Res {
        data class Str(val text: String) : Res
        data class Plural(val items: Map<String, String>) : Res
        data class Arr(val items: List<String>) : Res
    }

    private fun file(module: String, folder: String) = File(root, "$module/src/main/res/$folder/strings.xml")

    private fun load(f: File): Pair<Map<String, Res>, Set<String>> {
        assertTrue("Expected ${f.path} to exist", f.isFile)
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(f)
        val out = LinkedHashMap<String, Res>()
        val notTranslatable = mutableSetOf<String>()
        val children = doc.documentElement.childNodes
        for (i in 0 until children.length) {
            val el = children.item(i) as? Element ?: continue
            val name = el.getAttribute("name")
            if (el.getAttribute("translatable") == "false") notTranslatable += name
            out[name] = when (el.tagName) {
                "string" -> Res.Str(el.textContent)
                "plurals" -> Res.Plural(items(el).associate { it.getAttribute("quantity") to it.textContent })
                else -> Res.Arr(items(el).map { it.textContent })
            }
        }
        return out to notTranslatable
    }

    private fun items(el: Element): List<Element> =
        (0 until el.childNodes.length).mapNotNull { el.childNodes.item(it) as? Element }

    private val format = Regex("""%(?:\d+\$)?[-+#0 ,(]*\d*(?:\.\d+)?[sdfxXc%]""")
    private fun placeholders(s: String) = format.findAll(s).map { it.value }.sorted().toList()
    private fun count(s: String, needle: String) = s.split(needle).size - 1

    private inline fun forEachTranslation(block: (q: String, lang: Lang, module: String, src: Map<String, Res>, tr: Map<String, Res>, skip: Set<String>) -> Unit) {
        for ((q, lang) in languages) for (m in modules) {
            val (src, skip) = load(file(m, "values"))
            val (tr, _) = load(file(m, "values-$q"))
            block(q, lang, m, src, tr, skip)
        }
    }

    @Test
    fun `every approved language has a file in all three modules with exactly the source keys`() {
        val problems = mutableListOf<String>()
        forEachTranslation { q, _, m, src, tr, skip ->
            val need = src.keys - skip - "app_language_tag"
            val have = tr.keys - "app_language_tag"
            (need - have).sorted().takeIf { it.isNotEmpty() }?.let { problems += "$m/values-$q is missing $it" }
            (have - need).sorted().takeIf { it.isNotEmpty() }?.let { problems += "$m/values-$q has keys not in the source: $it" }
        }
        assertEquals("Translation keys differ from the English source", emptyList<String>(), problems)
    }

    @Test
    fun `SajdaTime is never translated or transliterated`() {
        // Owner, 30 Oct 2026: the name stays "SajdaTime" for every user, because that is the name on
        // Google Play and the one people search. So every string that names it in English names it the
        // same way, in Latin letters, in every language: never সাজদাটাইম, سجدہ ٹائم or "Sajda Time".
        // Cannot see a transliteration added beside the Latin name, or in a string whose English has none.
        fun names(r: Res) = when (r) {
            is Res.Str -> count(r.text, "SajdaTime")
            is Res.Plural -> r.items.values.maxOfOrNull { count(it, "SajdaTime") } ?: 0
            is Res.Arr -> r.items.sumOf { count(it, "SajdaTime") }
        }
        val problems = mutableListOf<String>()
        forEachTranslation { q, _, m, src, tr, _ ->
            for ((key, res) in src) {
                val want = names(res)
                val got = names(tr[key] ?: continue)
                if (want > 0 && got != want) problems += "$m/values-$q $key names SajdaTime $got times, English $want"
            }
        }
        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `placeholders, plural forms, line breaks, bullets and list lengths match the source`() {
        val problems = mutableListOf<String>()
        forEachTranslation { q, lang, m, src, tr, skip ->
            for (name in (src.keys - skip - "app_language_tag")) {
                val s = src[name] ?: continue
                val t = tr[name] ?: continue
                val where = "$m/values-$q:$name"
                when {
                    s is Res.Str && t is Res.Str -> {
                        if (placeholders(s.text) != placeholders(t.text)) problems += "$where placeholders ${placeholders(s.text)} -> ${placeholders(t.text)}"
                        if (count(s.text, "\\n") != count(t.text, "\\n")) problems += "$where line breaks ${count(s.text, "\\n")} -> ${count(t.text, "\\n")}"
                        if (count(s.text, "•") != count(t.text, "•")) problems += "$where bullets differ"
                        if (t.text.isBlank() && s.text.isNotBlank()) problems += "$where is empty"
                    }
                    s is Res.Plural && t is Res.Plural -> {
                        if (t.items.keys != lang.plurals) problems += "$where plural forms ${t.items.keys}, ${lang.tag} needs ${lang.plurals}"
                        val base = s.items["other"] ?: s.items.values.first()
                        // A form may drop the number where the grammar carries it (Arabic "صلاتان", two
                        // prayers, has no %d), but never add, renumber or retype one.
                        t.items.forEach { (qty, text) ->
                            val allowed = placeholders(s.items[qty] ?: base)
                            if (!allowed.containsAll(placeholders(text))) problems += "$where[$qty] placeholders ${placeholders(text)} not in $allowed"
                            if (qty == "other" && placeholders(text) != allowed) problems += "$where[other] placeholders differ"
                        }
                    }
                    s is Res.Arr && t is Res.Arr -> if (s.items.size != t.items.size) problems += "$where has ${t.items.size} items, source ${s.items.size}"
                    else -> problems += "$where changed kind"
                }
            }
        }
        assertEquals("Translations changed something that is code, not prose", emptyList<String>(), problems)
    }

    @Test
    fun `each translation declares its own language tag in core, and app and wear agree if they declare one`() {
        for ((q, lang) in languages) {
            val core = load(file("core", "values-$q")).first["app_language_tag"] as? Res.Str
            assertEquals("core/values-$q app_language_tag", lang.tag, core?.text)
            for (m in listOf("app", "wear")) {
                val other = load(file(m, "values-$q")).first["app_language_tag"] as? Res.Str
                if (other != null) assertEquals("$m/values-$q app_language_tag must agree with core", lang.tag, other.text)
            }
        }
    }

    @Test
    fun `no apostrophe is left unescaped and no direction marks are smuggled in`() {
        // Embeddings, overrides and bare marks. Isolates (U+2066 to U+2069) are allowed: 1.5.0 wraps
        // the Latin names and licence in the adhan credits with them on purpose (about_adhan_desc).
        val marks = Regex("[\u200E\u200F\u202A-\u202E\u061C]")
        val problems = mutableListOf<String>()
        for (q in languages.keys) for (m in modules) {
            val raw = file(m, "values-$q").readText()
            val noComments = raw.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
            if (Regex("""(?<!\\)'""").containsMatchIn(noComments)) problems += "$m/values-$q has an unescaped apostrophe"
            if (marks.containsMatchIn(raw)) problems += "$m/values-$q contains a direction mark. The app isolates names itself; remove it."
        }
        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `prayer names are the agreed ones`() {
        val keys = listOf("prayer_fajr", "prayer_sunrise", "prayer_dhuhr", "prayer_asr", "prayer_maghrib", "prayer_isha")
        for ((q, lang) in languages) {
            val core = load(file("core", "values-$q")).first
            val got = keys.map { (core[it] as? Res.Str)?.text }
            assertEquals("core/values-$q prayer names (Indonesian: Kemenag, Turkish: Diyanet, Urdu: standard; see docs/translation/BRIEF.md)", lang.prayers, got)
        }
    }

    @Test
    fun `the disclaimer keeps the English paragraphs with the dua request last, and the dua appears nowhere else`() {
        val sourceParagraphs = ((load(file("app", "values")).first["disclaimer_body"] as Res.Str).text).split("\\n\\n")
        for ((q, lang) in languages) {
            val disclaimer = (load(file("app", "values-$q")).first["disclaimer_body"] as Res.Str).text
            val paragraphs = disclaimer.split("\\n\\n")
            assertEquals("values-$q disclaimer_body paragraph count", sourceParagraphs.size, paragraphs.size)
            assertTrue("values-$q: the last paragraph must be the dua request (it should contain \"${lang.duaWord}\")", lang.duaWord in paragraphs.last())
            // Paragraphs, not occurrences: Indonesian writes the plural "doa-doa", which is one phrase.
            assertEquals(
                "values-$q: only the last paragraph of the disclaimer may mention \"${lang.duaWord}\"",
                1,
                paragraphs.count { lang.duaWord in it },
            )
            // Everywhere else in the language, the request is not repeated.
            for (m in modules) for ((name, res) in load(file(m, "values-$q")).first) {
                if (m == "app" && name == "disclaimer_body") continue
                val texts = when (res) {
                    is Res.Str -> listOf(res.text)
                    is Res.Plural -> res.items.values.toList()
                    is Res.Arr -> res.items
                }
                assertTrue("values-$q $m:$name repeats the dua request", texts.none { lang.duaWord in it })
            }
        }
    }

    @Test
    fun `the translated consent says the privacy policy is in English`() {
        for (q in languages.keys) {
            val app = load(file("app", "values-$q")).first
            assertTrue("app/values-$q must define consent_policy_english", (app["consent_policy_english"] as? Res.Str)?.text?.isNotBlank() == true)
        }
    }
}
