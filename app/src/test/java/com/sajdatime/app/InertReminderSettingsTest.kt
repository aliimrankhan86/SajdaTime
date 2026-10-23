package com.sajdatime.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The two 1.6.1 reminders (jama'ah and fasting day) have planners in notify/ExtraReminderPlans.kt
 * that are kept but switched off, by Ali's decision (BUILD-INSTRUCTIONS section 3 Q2, DETAILED-PLAN
 * 8.8). Nothing may read their settings from a screen, and nothing but that file may name the
 * planners, so they cannot go live through the scheduler or a settings row by accident in 1.6.0.
 * Switching them on in 1.6.1 means changing this test in the same commit, on purpose.
 */
class InertReminderSettingsTest {

    private val sources = File(File("..").canonicalFile, "app/src/main/java")
        .walkTopDown().filter { it.extension == "kt" }.toList()
        .also { check(it.size > 20) { "Found only ${it.size} sources; is the working directory the app module?" } }

    @Test
    fun `no screen reads the inert reminder settings`() {
        // The ViewModel's setters too: a row could otherwise store the choice without reading it back.
        val setting = Regex("""\b(jamaahReminderMinutes|fastReminders|setJamaahReminderMinutes|setFastReminder)\b""")
        // SajdaViewModel.kt declares the setters; what must not happen is a screen calling them.
        val found = sources.filter { "/ui/" in it.path.replace('\\', '/') && it.name != "SajdaViewModel.kt" }
            .filter { setting.containsMatchIn(it.readText()) }
            .map { it.name }
        assertTrue("The 1.6.1 reminder settings stay off the screens: $found", found.isEmpty())
    }

    @Test
    fun `only their own file names the inert planners`() {
        val planner = Regex("""\b(JamaahReminderPlan|FastReminderPlan)\b""")
        val found = sources.filter { it.name != "ExtraReminderPlans.kt" && planner.containsMatchIn(it.readText()) }
            .map { it.name }
        assertTrue("The 1.6.1 planners must not be reachable from anything else: $found", found.isEmpty())
    }
}
