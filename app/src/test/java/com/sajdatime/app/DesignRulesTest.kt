package com.sajdatime.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The design rules the owner has had to repeat, made into failures instead of reminders
 * (18 Oct 2026: "I already told you this many many times ... record and remember this").
 * docs/DESIGN_SYSTEM.md section 2 and section 4 say why. Reading the source is crude, but
 * each rule here is about what is written, and a crude check that runs on every build beats
 * a careful one nobody runs.
 */
class DesignRulesTest {

    private val sources = File(File("..").canonicalFile, "app/src/main/java")
        .walkTopDown().filter { it.extension == "kt" }.toList()
        .also { check(it.size > 20) { "Found only ${it.size} sources; is the working directory the app module?" } }

    private fun offenders(pattern: Regex, allowed: Set<String> = emptySet()) = sources
        .filter { it.name !in allowed && pattern.containsMatchIn(it.readText()) }
        .map { it.name }

    /**
     * Every call to action is a CtaButton: solid green, white bold text (owner, 18 Oct 2026, after
     * outlined and then tonal buttons were both too easy to miss). Material's outlined and tonal
     * buttons are banned outright, and a bare Material Button only where a colour other than the
     * call to action green is meant: the amber notice and permission buttons, and CtaButton itself.
     */
    @Test
    fun `calls to action are solid CtaButtons`() {
        val banned = offenders(Regex("""\b(OutlinedButton|FilledTonalButton)\s*\("""))
        assertTrue("Use CtaButton, not an outlined or tonal button: $banned", banned.isEmpty())
        val bare = offenders(
            Regex("""(?<![A-Za-z])Button\s*\("""),
            allowed = setOf("CtaButton.kt", "Notice.kt", "PermissionAction.kt"),
        )
        assertTrue("Use CtaButton, not a bare Material Button: $bare", bare.isEmpty())
    }

    /**
     * Every dialog the user can simply leave shows a close X at the top right (DialogTitle).
     * The exceptions must be answered, never closed: the first run disclaimer (MainActivity)
     * and the usage counts question, where an X would be an answer nobody gave.
     */
    @Test
    fun `dismissable dialogs carry the close X`() {
        val exempt = setOf("MainActivity.kt", "UsageCountsConsent.kt")
        // Per file: at least as many DialogTitle calls as AlertDialog calls. Counting, not a
        // text window, so a long confirmButton block cannot hide the title from the check.
        val found = sources.filter { it.name !in exempt }.mapNotNull { file ->
            val text = file.readText()
            val dialogs = Regex("""\bAlertDialog\(""").findAll(text).count()
            val titles = Regex("""\bDialogTitle\(""").findAll(text).count()
            if (dialogs > titles) "${file.name} ($dialogs dialogs, $titles with the X)" else null
        }
        assertTrue("These dialogs have no DialogTitle close X: $found", found.isEmpty())
    }

    /** Spacing between text and its buttons comes from the scale, not from eyeballed numbers. */
    @Test
    fun `notices and explainers use the spacing scale`() {
        val files = sources.filter { it.name == "Notice.kt" }
        assertTrue(files.isNotEmpty())
        val text = files.single().readText()
        assertTrue("Notice.kt must space with Space.*", "Space.Group" in text && "Space.Tight" in text)
        val stray = Regex("""Spacer\(Modifier\.height\(\d+\.dp\)\)""").findAll(text).map { it.value }.toList()
        assertTrue("Notice.kt has gaps outside the scale: $stray", stray.isEmpty())
    }
}
