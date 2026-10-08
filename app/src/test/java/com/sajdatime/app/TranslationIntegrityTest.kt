package com.sajdatime.app

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

/**
 * Mechanical integrity of the three translations, against the English source.
 *
 * It cannot judge whether a translation is *good*. It catches what breaks the app or changes its
 * meaning without anything failing: a missing key (the string silently shows in English), a lost
 * or renumbered placeholder (a crash or a wrong number), a wrong set of plural forms, an
 * unescaped apostrophe (a build error on one machine and not another), a changed bullet or
 * paragraph count, a direction-mark character (the app isolates names itself), the wrong
 * `app_language_tag` (right words, wrong number format and layout direction), a prayer name that
 * is not the agreed one, and a method name that has been translated when the owner's safeguard
 * says it must not be. The same checks run from the command line as `tools/check-translation.py`,
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
    )

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
                        t.items.forEach { (qty, text) ->
                            if (placeholders(s.items[qty] ?: base) != placeholders(text)) problems += "$where[$qty] placeholders differ"
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
        val marks = Regex("[‎‏‪-‮⁦-⁩؜]")
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
    fun `the disclaimer keeps seven paragraphs with the dua request last, and the dua appears nowhere else`() {
        val sourceParagraphs = ((load(file("app", "values")).first["disclaimer_body"] as Res.Str).text).split("\\n\\n")
        assertEquals("the English disclaimer should have seven paragraphs", 7, sourceParagraphs.size)
        for ((q, lang) in languages) {
            val disclaimer = (load(file("app", "values-$q")).first["disclaimer_body"] as Res.Str).text
            val paragraphs = disclaimer.split("\\n\\n")
            assertEquals("values-$q disclaimer_body paragraph count", 7, paragraphs.size)
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
    fun `the translated app tells people it was translated with AI help and that the policy is in English`() {
        for (q in languages.keys) {
            val app = load(file("app", "values-$q")).first
            for (key in listOf("about_translation", "about_translation_note", "consent_policy_english")) {
                assertTrue("app/values-$q must define $key", (app[key] as? Res.Str)?.text?.isNotBlank() == true)
            }
        }
    }
}
