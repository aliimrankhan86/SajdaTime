package com.sajdatime.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The jama'ah reminder has a planner in notify/ExtraReminderPlans.kt that is kept but switched off, by
 * Ali's decision (BUILD-INSTRUCTIONS section 3 Q2, DETAILED-PLAN 8.8). Nothing may read its setting from
 * a screen, and nothing but that file may name the planner, so it cannot go live by accident. The Sunnah
 * fast reminder was switched on in 1.6.0 (HANDOVER 5.41): its planner is named by the scheduler only,
 * its setting by Settings. Switching the jama'ah one on means changing this test in the same commit, on purpose.
 */
class InertReminderSettingsTest {

    private val sources = File(File("..").canonicalFile, "app/src/main/java")
        .walkTopDown().filter { it.extension == "kt" }.toList()
        .also { check(it.size > 20) { "Found only ${it.size} sources; is the working directory the app module?" } }

    @Test
    fun `no screen reads the inert reminder settings`() {
        // The ViewModel's setters too: a row could otherwise store the choice without reading it back.
        val setting = Regex("""\b(jamaahReminderMinutes|setJamaahReminderMinutes)\b""")
        // SajdaViewModel.kt declares the setters; what must not happen is a screen calling them.
        val found = sources.filter { "/ui/" in it.path.replace('\\', '/') && it.name != "SajdaViewModel.kt" }
            .filter { setting.containsMatchIn(it.readText()) }
            .map { it.name }
        assertTrue("The jama'ah reminder setting stays off the screens: $found", found.isEmpty())
    }

    @Test
    fun `only its own file names the jamaah planner`() {
        val planner = Regex("""\bJamaahReminderPlan\b""")
        val found = sources.filter { it.name != "ExtraReminderPlans.kt" && planner.containsMatchIn(it.readText()) }
            .map { it.name }
        assertTrue("The jama'ah planner must not be reachable from anything else: $found", found.isEmpty())
    }

    @Test
    fun `the fast planner is named by the scheduler only`() {
        val planner = Regex("""\bFastReminderPlan\b""")
        val found = sources.filter { it.name !in setOf("ExtraReminderPlans.kt", "PrayerAlarmScheduler.kt") && planner.containsMatchIn(it.readText()) }
            .map { it.name }
        assertTrue("Only the scheduler lays down fast reminders: $found", found.isEmpty())
    }
}
