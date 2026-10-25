package com.sajdatime.app

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified
import com.sajdatime.app.ui.theme.GlassBarAlpha
import com.sajdatime.app.ui.theme.PrayerTimeTextStyle
import com.sajdatime.app.ui.theme.SajdaTypography
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The design system in docs/design-system/ and the code may never disagree (owner, 25 Oct 2026: "it should
 * be checked and contains all the details so any future session cannot drift away from it"). The token
 * files are what another AI, and later the iPhone app, read; this test is what keeps them true.
 *
 * Both directions are checked: a value changed on one side only fails, and so does a colour, gap, timing
 * or type style added to the code without its token. The fix is always the same commit: change the Kotlin,
 * change the JSON, say why in docs/design-system/CHANGELOG.md.
 *
 * Colours, gaps and timings are read from the source, as DesignRulesTest reads it, because their files
 * are one value per line and the token file names the Kotlin symbol. Type is read from the compiled
 * SajdaTypography, so a style built in an unusual way still has its real size checked.
 */
class DesignTokensTest {

    private val root = File("..").canonicalFile
    private val theme = File(root, "app/src/main/java/com/sajdatime/app/ui/theme")
    private val tokens = File(root, "docs/design-system/tokens")

    private fun json(name: String) = JSONObject(File(tokens, name).readText())

    /** Every object anywhere under [node] that names a Kotlin symbol, as symbol to object. */
    private fun withKotlin(node: Any?, into: MutableList<JSONObject> = mutableListOf()): List<JSONObject> {
        when (node) {
            is JSONObject -> {
                if (node.has("kotlin")) into += node
                node.keys().forEach { withKotlin(node.get(it), into) }
            }
            is JSONArray -> for (i in 0 until node.length()) withKotlin(node.get(i), into)
        }
        return into
    }

    /** Kotlin writes 0xAARRGGBB; the tokens write #RRGGBB, or #RRGGBBAA when translucent. */
    private fun tokenHex(argb: String): String {
        val a = argb.uppercase()
        return if (a.startsWith("FF")) "#${a.substring(2)}" else "#${a.substring(2)}${a.substring(0, 2)}"
    }

    @Test
    fun `every colour in Color kt is a token with the same value, and nothing else is`() {
        val code = Regex("""^val (\w+) = Color\(0x([0-9A-Fa-f]{8})\)""", RegexOption.MULTILINE)
            .findAll(File(theme, "Color.kt").readText())
            .associate { it.groupValues[1] to tokenHex(it.groupValues[2]) }
        assertTrue("Read only ${code.size} colours from Color.kt; has its format changed?", code.size > 80)

        val doc = withKotlin(json("color.json")).filter { it.optString("\$value").startsWith("#") }
        val docNames = doc.map { it.getString("kotlin") }
        val twice = docNames.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue("Listed more than once in color.json: $twice", twice.isEmpty())

        val missing = code.keys - docNames.toSet()
        assertTrue("In Color.kt but not in docs/design-system/tokens/color.json: $missing", missing.isEmpty())
        val stale = docNames.toSet() - code.keys
        assertTrue("In color.json but no longer in Color.kt: $stale", stale.isEmpty())
        val wrong = doc.filter { it.getString("\$value").uppercase() != code[it.getString("kotlin")] }
            .map { "${it.getString("kotlin")}: doc ${it.getString("\$value")}, code ${code[it.getString("kotlin")]}" }
        assertTrue("color.json disagrees with Color.kt: $wrong", wrong.isEmpty())

        assertEquals(
            "glassBarAlpha in color.json",
            GlassBarAlpha.toDouble(),
            json("color.json").getJSONObject("glassBarAlpha").getDouble("\$value"),
            1e-6,
        )
    }

    /**
     * The roles table says which Material slot each colour fills. A wrong slot in the doc would send an iPhone
     * build, or the next Android screen, to the wrong colour, so the doc is held to Theme.kt's two schemes.
     */
    @Test
    fun `every Material role in the doc is wired that way in Theme kt, and every wired role is in the doc`() {
        val source = File(theme, "Theme.kt").readText()
        fun scheme(builder: String): Map<String, String> {
            val start = source.indexOf("$builder(")
            assertTrue("$builder not found in Theme.kt", start >= 0)
            val body = source.substring(start, source.indexOf("\n)", start))
            return Regex("""(\w+) = (\w+),""").findAll(body).associate { it.groupValues[1] to it.groupValues[2] }
        }
        val wired = mapOf("light" to scheme("lightColorScheme"), "dark" to scheme("darkColorScheme"))
        val roles = json("color.json").getJSONObject("roles")
        for (themeName in listOf("light", "dark")) {
            val inDoc = roles.keys().asSequence().associate { key ->
                val token = roles.getJSONObject(key)
                token.getString("m3Role") to token.getJSONObject(themeName).getString("kotlin")
            }
            assertEquals("Material roles in color.json ($themeName) against Theme.kt", wired.getValue(themeName), inDoc)
        }
    }

    @Test
    fun `the spacing scale in the doc is Space in Spacing kt`() {
        val code = Regex("""val (\w+) = (\d+)\.dp""").findAll(File(theme, "Spacing.kt").readText())
            .associate { "Space.${it.groupValues[1]}" to it.groupValues[2].toInt() }
        val space = json("spacing.json").getJSONObject("space")
        val doc = space.keys().asSequence().associate {
            space.getJSONObject(it).getString("kotlin") to space.getJSONObject(it).getInt("\$value")
        }
        assertEquals("spacing.json space against Spacing.kt", code, doc)
    }

    @Test
    fun `the durations in the doc are Motion in Motion kt`() {
        val code = Regex("""const val (\w+) = (\d+)""").findAll(File(theme, "Motion.kt").readText())
            .associate { "Motion.${it.groupValues[1]}" to it.groupValues[2].toInt() }
        val duration = json("motion.json").getJSONObject("duration")
        val doc = duration.keys().asSequence().associate {
            duration.getJSONObject(it).getString("kotlin") to duration.getJSONObject(it).getInt("\$value")
        }
        assertEquals("motion.json duration against Motion.kt", code, doc)
        assertTrue("Motion.kt no longer uses FastOutSlowInEasing; update motion.json easing",
            File(theme, "Motion.kt").readText().contains("FastOutSlowInEasing"))
    }

    private fun assertStyle(name: String, expected: JSONObject, actual: TextStyle) {
        assertEquals("$name size", expected.getDouble("size"), actual.fontSize.value.toDouble(), 1e-3)
        if (expected.isNull("lineHeight")) {
            assertTrue("$name lineHeight: doc says unset, code sets ${actual.lineHeight}", actual.lineHeight.isUnspecified)
        } else {
            assertEquals("$name lineHeight", expected.getDouble("lineHeight"), actual.lineHeight.value.toDouble(), 1e-3)
        }
        assertEquals("$name weight", expected.getInt("weight"), actual.fontWeight?.weight)
        val spacing = if (actual.letterSpacing.isUnspecified) 0.0 else actual.letterSpacing.value.toDouble()
        assertEquals("$name letterSpacing", expected.getDouble("letterSpacing"), spacing, 1e-3)
        assertEquals("$name tabular", expected.getBoolean("tabular"), actual.fontFeatureSettings == "tnum")
    }

    @Test
    fun `the type scale in the doc is SajdaTypography, style by style`() {
        val t = SajdaTypography
        val all = mapOf(
            "displayLarge" to t.displayLarge, "displayMedium" to t.displayMedium, "displaySmall" to t.displaySmall,
            "headlineLarge" to t.headlineLarge, "headlineMedium" to t.headlineMedium, "headlineSmall" to t.headlineSmall,
            "titleLarge" to t.titleLarge, "titleMedium" to t.titleMedium, "titleSmall" to t.titleSmall,
            "bodyLarge" to t.bodyLarge, "bodyMedium" to t.bodyMedium, "bodySmall" to t.bodySmall,
            "labelLarge" to t.labelLarge, "labelMedium" to t.labelMedium, "labelSmall" to t.labelSmall,
        )
        val type = json("typography.json")
        val styles = type.getJSONObject("styles")
        val unset = type.getJSONObject("unsetMaterialRoles").getJSONArray("roles").let { a -> (0 until a.length()).map { a.getString(it) } }

        // Which roles Type.kt actually defines, read from its source: the doc's two lists must split them exactly.
        val defined = Regex("""^\s{4}(\w+) = TextStyle\(""", RegexOption.MULTILINE)
            .findAll(File(theme, "Type.kt").readText()).map { it.groupValues[1] }.toSet()
        assertEquals("typography.json styles against the roles Type.kt defines", defined, styles.keys().asSequence().toSet())
        assertEquals("typography.json unsetMaterialRoles", all.keys - defined, unset.toSet())

        styles.keys().forEach { assertStyle(it, styles.getJSONObject(it), all.getValue(it)) }
        assertStyle("prayerTime", type.getJSONObject("derived").getJSONObject("prayerTime"), PrayerTimeTextStyle)

        val floor = 14.0
        val small = styles.keys().asSequence().filter { styles.getJSONObject(it).getDouble("size") < floor }.toList()
        assertTrue("Phone app type floor is 14sp: $small", small.isEmpty())
    }

    /** Android resources write #RRGGBB or #AARRGGBB; the tokens write #RRGGBB or #RRGGBBAA. */
    private fun resColours(path: String) = Regex("""<color name="(\w+)">#([0-9A-Fa-f]{6,8})</color>""")
        .findAll(File(root, path).readText())
        .associate { it.groupValues[1] to it.groupValues[2].uppercase().let { h -> if (h.length == 6) "#$h" else tokenHex(h) } }

    /**
     * The widgets' System colours (values/ and values-night/) are in the token file, so an iPhone widget
     * can be built from it. The widget_light_ and widget_dark_ fixed copies are WidgetThemeTest's job.
     */
    @Test
    fun `the widget colours in the doc are the widget colour resources`() {
        val light = resColours("app/src/main/res/values/colors.xml")
        val dark = resColours("app/src/main/res/values-night/colors.xml")
        val widget = json("color.json").getJSONObject("widget")
        val doc = widget.keys().asSequence().filter { !it.startsWith("$") }.toSet()
        assertEquals("color.json widget against values-night/colors.xml", dark.keys, doc)
        val wrong = doc.flatMap { key ->
            val token = widget.getJSONObject(key)
            listOfNotNull(
                "$key light".takeIf { token.getJSONObject("light").getString("\$value").uppercase() != light[key] },
                "$key dark".takeIf { token.getJSONObject("dark").getString("\$value").uppercase() != dark[key] },
            )
        }
        assertTrue("color.json widget disagrees with the colour resources: $wrong", wrong.isEmpty())
    }

    /**
     * WearTheme.kt is the phone's dark palette copied by hand, because the two modules cannot share a
     * Compose theme. Until 25 Oct 2026 nothing checked the copy. Now every watch role must equal the doc,
     * and the doc's phone role, except the background (pure black for OLED, on purpose).
     */
    @Test
    fun `the watch palette is the phone's dark palette, and the doc says so`() {
        val source = File(root, "wear/src/main/java/com/sajdatime/wear/WearTheme.kt").readText()
        val named = Regex("""val (\w+) = Color\(0x([0-9A-Fa-f]{8})\)""").findAll(source)
            .associate { it.groupValues[1] to tokenHex(it.groupValues[2]) }
        val start = source.indexOf("ColorScheme(")
        val block = source.substring(start, source.indexOf("\n)", start))
        val code = Regex("""(\w+) = (?:Color\(0x([0-9A-Fa-f]{8})\)|(\w+)),""").findAll(block).associate { m ->
            m.groupValues[1] to (m.groupValues[2].takeIf { it.isNotEmpty() }?.let(::tokenHex) ?: named.getValue(m.groupValues[3]))
        } + ("kiswah" to named.getValue("Kiswah"))

        val colour = json("color.json")
        val wear = colour.getJSONObject("wear")
        val doc = wear.keys().asSequence().filter { !it.startsWith("$") }
            .associate { it to wear.getJSONObject(it).getString("\$value").uppercase() }
        assertEquals("color.json wear against WearTheme.kt", code, doc)

        val roles = colour.getJSONObject("roles")
        val byM3 = roles.keys().asSequence().associate { key ->
            roles.getJSONObject(key).getString("m3Role") to roles.getJSONObject(key).getJSONObject("dark").getString("\$value").uppercase()
        }
        val kiswah = colour.getJSONObject("brand").getJSONObject("kiswah").getJSONObject("dark").getString("\$value").uppercase()
        val drift = doc.filterKeys { it != "background" }.filter { (role, value) ->
            value != (if (role == "kiswah") kiswah else byM3[role])
        }
        assertTrue("The watch differs from the phone's dark palette (change both, or record why): $drift", drift.isEmpty())
        assertEquals("watch background", "#000000", doc["background"])
    }

    /** The tile's own three colours are a recorded gap (color.json wearTile); held so they cannot move silently. */
    @Test
    fun `the watch tile colours are the ones the doc records`() {
        val source = File(root, "wear/src/main/java/com/sajdatime/wear/NextPrayerTileService.kt").readText()
        val code = Regex("""const val (COLOUR_\w+) = 0x([0-9A-Fa-f]{8})\.toInt\(\)""").findAll(source)
            .associate { it.groupValues[1] to tokenHex(it.groupValues[2]) }
        val tile = json("color.json").getJSONObject("wearTile")
        val doc = tile.keys().asSequence().filter { !it.startsWith("$") }
            .associate { it to tile.getJSONObject(it).getString("\$value").uppercase() }
        assertEquals("color.json wearTile against NextPrayerTileService.kt", code, doc)
    }

    /** Every colour value anywhere in color.json, as written there (upper case). */
    private fun allColourValues(node: Any?, into: MutableSet<String> = mutableSetOf()): Set<String> {
        when (node) {
            is JSONObject -> node.keys().forEach { key ->
                val v = node.get(key)
                if (key == "\$value" && v is String && v.startsWith("#")) into += v.uppercase() else allColourValues(v, into)
            }
            is JSONArray -> for (i in 0 until node.length()) allColourValues(node.get(i), into)
        }
        return into
    }

    /** Icon and PDF colours are literals in their own files; each file must still hold its value. */
    @Test
    fun `asset colours in the doc are still in their files`() {
        val assets = json("color.json").getJSONObject("assets")
        val missing = assets.keys().asSequence().filter { !it.startsWith("$") }.flatMap { key ->
            val token = assets.getJSONObject(key)
            val hex = token.getString("\$value").removePrefix("#")
            val files = token.getJSONArray("files")
            (0 until files.length()).map { files.getString(it) }
                .filter { !File(root, it).readText().uppercase().contains(hex.uppercase()) }
                .map { "$key not in $it" }
        }.toList()
        assertTrue("color.json assets out of date: $missing", missing.isEmpty())
    }

    /**
     * A hex written in a page must be a token, so prose cannot quote a colour the code no longer has. The
     * pages explain; the token file holds the value.
     */
    @Test
    fun `every colour quoted in the pages is a token`() {
        val known = allColourValues(json("color.json"))
        val quoted = File(root, "docs/design-system").walkTopDown().filter { it.extension == "md" }.flatMap { file ->
            Regex("""#[0-9A-Fa-f]{6}(?:[0-9A-Fa-f]{2})?(?![0-9A-Za-z])""").findAll(file.readText())
                .map { it.value.uppercase() }.filter { it !in known }.map { "${file.name}: $it" }
        }.toSet()
        assertTrue("Quoted in docs/design-system but not in color.json: $quoted", quoted.isEmpty())
    }

    /** actionLabelStyle() is composable, so its one line is read rather than called. */
    @Test
    fun `the action label in the doc is actionLabelStyle`() {
        val cta = File(root, "app/src/main/java/com/sajdatime/app/ui/components/CtaButton.kt").readText()
        val size = Regex("""fun actionLabelStyle\(\): TextStyle =\s*MaterialTheme\.typography\.labelLarge\.copy\(fontSize = (\d+)\.sp""")
            .find(cta)?.groupValues?.get(1)?.toDouble()
        val doc = json("typography.json").getJSONObject("derived").getJSONObject("actionLabel")
        assertEquals("actionLabel size against CtaButton.kt actionLabelStyle()", doc.getDouble("size"), size ?: -1.0, 1e-3)
        val labelLarge = json("typography.json").getJSONObject("styles").getJSONObject("labelLarge")
        assertEquals("actionLabel weight is labelLarge's", labelLarge.getInt("weight"), doc.getInt("weight"))
    }

    /**
     * Every file of phone UI code is named somewhere in docs/design-system/, so a new screen, sheet or
     * component cannot be added without its spec being written (the same idea as the button inventory in
     * DESIGN_SYSTEM.md section 4.1, widened to every UI file).
     */
    @Test
    fun `every phone UI file is described in the design system`() {
        val docs = File(root, "docs/design-system").walkTopDown().filter { it.extension == "md" }
            .joinToString("\n") { it.readText() }
        val ui = File(root, "app/src/main/java/com/sajdatime/app/ui").walkTopDown()
            .filter { it.extension == "kt" }.map { it.name }.toList()
        assertTrue("Found only ${ui.size} UI files", ui.size > 20)
        val undocumented = ui.filter { "`$it`" !in docs }.sorted()
        assertTrue("Describe these in docs/design-system/ (name them in backticks): $undocumented", undocumented.isEmpty())
    }
}
