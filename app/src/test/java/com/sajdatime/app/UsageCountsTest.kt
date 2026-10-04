package com.sajdatime.app

import com.sajdatime.app.data.Permission
import com.sajdatime.app.data.Screen
import com.sajdatime.app.data.SetupStep
import com.sajdatime.app.data.UsageCounts
import com.sajdatime.app.data.UsageSink
import com.sajdatime.app.ui.onboarding.Step
import com.sajdatime.app.ui.onboarding.setupStep
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the rules in docs/ANALYTICS_PLAN.md for the optional usage counts. The privacy policy,
 * the consent text and the Play Data safety form all promise these things, so a regression here
 * is a broken promise, not just a bug.
 *
 * Two kinds of test. The first group drives [UsageCounts] against a recording sink, so no
 * Firebase is needed. The second reads the manifest and resource files off disk, because the
 * manifest is where "off until the user agrees" is actually enforced, and a library or a merge
 * can quietly undo it. Those files are declared as test inputs in app/build.gradle.kts — if you
 * add another file to read, declare it there or this task goes UP-TO-DATE after the edit
 * (docs/HANDOVER.md §15 lesson 84).
 */
class UsageCountsTest {

    private class Recording : UsageSink {
        val calls = mutableListOf<String>()
        val events = mutableListOf<Pair<String, Map<String, String>>>()
        override fun setCollection(enabled: Boolean) { calls += "collection=$enabled" }
        override fun reset() { calls += "reset" }
        override fun event(name: String, params: Map<String, String>) {
            calls += "event:$name"
            events += name to params
        }
    }

    private fun counts(allowed: Boolean = true) = Recording().let { it to UsageCounts(it, allowed) }

    // ---- off means off ---------------------------------------------------------------

    @Test
    fun `a user who never opted in causes no call at all`() {
        val (sink, usage) = counts()
        usage.apply(false)
        usage.apply(false)
        usage.screen(Screen.TIMES)
        usage.setupStep(SetupStep.SECT)
        usage.permission(Permission.LOCATION, true)
        assertEquals(emptyList<String>(), sink.calls)
    }

    @Test
    fun `a build that is not allowed to send never calls the sink even if opted in`() {
        val (sink, usage) = counts(allowed = false)
        usage.apply(true)
        usage.screen(Screen.QIBLA)
        usage.setupStep(SetupStep.FINISH)
        assertEquals(emptyList<String>(), sink.calls)
    }

    @Test
    fun `collection is switched on before the first event`() {
        val (sink, usage) = counts()
        usage.apply(true)
        usage.setupStep(SetupStep.PERMISSION)
        assertEquals(listOf("collection=true", "event:setup_step"), sink.calls)
    }

    @Test
    fun `re-applying the same choice does not repeat the call`() {
        val (sink, usage) = counts()
        usage.apply(true)
        usage.apply(true)
        assertEquals(listOf("collection=true"), sink.calls)
    }

    @Test
    fun `turning it off clears Firebase's data and stops everything`() {
        val (sink, usage) = counts()
        usage.apply(true)
        usage.screen(Screen.TIMES)
        usage.apply(false)
        usage.screen(Screen.QIBLA)
        usage.setupStep(SetupStep.SECT)
        usage.permission(Permission.NOTIFICATIONS, false)
        assertEquals(
            listOf("collection=true", "event:screen_view", "collection=false", "reset"),
            sink.calls,
        )
    }

    @Test
    fun `opting in again after opting out starts clean`() {
        val (sink, usage) = counts()
        usage.apply(true)
        usage.screen(Screen.TIMES)
        usage.setupStep(SetupStep.SECT)
        usage.apply(false)
        usage.apply(true)
        usage.screen(Screen.TIMES)
        usage.setupStep(SetupStep.SECT)
        // The de-duplication memory was cleared, so these are sent again rather than swallowed.
        assertEquals(4, sink.events.size)
    }

    // ---- only a fixed, closed set of events ------------------------------------------

    @Test
    fun `repeats are not sent twice`() {
        val (sink, usage) = counts()
        usage.apply(true)
        usage.screen(Screen.TIMES)
        usage.screen(Screen.TIMES)
        usage.screen(Screen.QIBLA)
        usage.setupStep(SetupStep.SECT)
        usage.setupStep(SetupStep.SECT)
        assertEquals(listOf("times", "qibla"), sink.events.filter { it.first == "screen_view" }
            .map { it.second.getValue("screen_name") })
        assertEquals(1, sink.events.count { it.first == "setup_step" })
    }

    @Test
    fun `only the three approved event names and their parameters can ever be sent`() {
        val (sink, usage) = counts()
        usage.apply(true)
        Screen.entries.forEach(usage::screen)
        SetupStep.entries.forEach(usage::setupStep)
        Permission.entries.forEach { usage.permission(it, true); usage.permission(it, false) }

        val allowedShape = mapOf(
            "screen_view" to setOf("screen_name"),
            "setup_step" to setOf("step"),
            "permission_result" to setOf("permission", "granted"),
        )
        assertTrue(sink.events.isNotEmpty())
        sink.events.forEach { (name, params) ->
            assertEquals("Unapproved event $name", true, name in allowedShape)
            assertEquals("Unapproved parameters on $name", allowedShape.getValue(name), params.keys)
        }
        // Values come only from the enums' fixed words, or a plain true or false.
        val words = (Screen.entries.map { it.wire } + SetupStep.entries.map { it.wire } +
            Permission.entries.map { it.wire } + listOf("true", "false")).toSet()
        sink.events.flatMap { it.second.values }.forEach { assertTrue("Unexpected value $it", it in words) }
    }

    // ---- never anything that reveals the sect ----------------------------------------

    @Test
    fun `no setup step is shown to one sect only`() {
        assertEquals(
            setOf("permission", "sect", "method", "confirm", "finish"),
            SetupStep.entries.map { it.wire }.toSet(),
        )
        // The madhab screen is Sunni only, so it must report nothing.
        assertEquals(null, Step.MADHAB.setupStep())
    }

    @Test
    fun `the reported path is identical for a Sunni and a Shia user`() {
        val sunni = listOf(Step.WELCOME, Step.CONSENT, Step.PERMISSION, Step.SECT, Step.MADHAB, Step.METHOD, Step.CONFIRM)
        val shia = sunni - Step.MADHAB
        assertEquals(sunni.mapNotNull { it.setupStep() }, shia.mapNotNull { it.setupStep() })
    }

    @Test
    fun `nothing before consent is reported`() {
        assertEquals(null, Step.WELCOME.setupStep())
        assertEquals(null, Step.CONSENT.setupStep())
    }

    // ---- the manifest and resources, read from disk ----------------------------------

    private val root = File("..").canonicalFile

    private fun read(path: String) = File(root, path).also {
        assertTrue("Expected to find $path, has it moved?", it.isFile)
    }.readText()

    private val manifest by lazy { read("app/src/main/AndroidManifest.xml") }

    @Test
    fun `the manifest keeps collection off until the user agrees`() {
        assertTrue(manifest.contains("""android:name="firebase_analytics_collection_enabled" android:value="false""""))
        assertTrue(manifest.contains("""android:name="google_analytics_adid_collection_enabled" android:value="false""""))
        assertTrue(manifest.contains("""android:name="google_analytics_automatic_screen_reporting_enabled" android:value="false""""))
        // The "deactivated" key would block the opt in entirely. The name appears in a comment
        // explaining why, so check for it as an actual attribute.
        assertFalse(manifest.contains("""android:name="firebase_analytics_collection_deactivated""""))
    }

    @Test
    fun `the advertising permissions are removed from the manifest`() {
        listOf(
            "com.google.android.gms.permission.AD_ID",
            "android.permission.ACCESS_ADSERVICES_AD_ID",
            "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
        ).forEach { permission ->
            assertTrue(
                "$permission must stay removed with tools:node=\"remove\" (Play Advertising ID answer is No)",
                Regex("""<uses-permission\s+android:name="${Regex.escape(permission)}"\s+tools:node="remove"\s*/>""")
                    .containsMatchIn(manifest),
            )
        }
    }

    @Test
    fun `only the shipped release build is allowed to send`() {
        fun allowed(dir: String) = read("app/src/$dir/res/values/bools.xml")
            .let { Regex("""<bool name="analytics_allowed">(true|false)</bool>""").find(it)!!.groupValues[1] }
        assertEquals("true", allowed("main"))
        listOf("debug", "rtl", "sideload").forEach {
            assertEquals("$it must never send usage counts", "false", allowed(it))
        }
    }

    private fun config(dir: String) = read("app/src/$dir/res/values/firebase_config.xml")
    private fun value(xml: String, name: String) =
        Regex("""<string name="$name"[^>]*>([^<]*)</string>""").find(xml)?.groupValues?.get(1)

    private val appIdShape = Regex("""1:\d{6,}:android:[0-9a-f]{20,}""")

    @Test
    fun `the shipped Firebase ids are real, well formed and consistent`() {
        val main = config("main")
        val appId = value(main, "google_app_id")!!
        val sender = value(main, "gcm_defaultSenderId")!!
        val key = value(main, "google_api_key")!!
        val project = value(main, "project_id")!!
        assertFalse("A release must not carry placeholder ids", main.contains("PLACEHOLDER"))
        assertTrue("app id shape: $appId", appIdShape.matches(appId))
        // The middle part of the app id IS the project number, which is the sender id. A
        // mismatch means ids copied from two different projects.
        assertEquals(appId.split(":")[1], sender)
        assertTrue("api key shape", Regex("""AIza[0-9A-Za-z_-]{35}""").matches(key))
        assertTrue("project id shape: $project", Regex("""[a-z][a-z0-9-]{4,28}[a-z0-9]""").matches(project))
    }

    @Test
    fun `the sideload build reports to the test app, never the real one`() {
        val realId = value(config("main"), "google_app_id")!!
        val testId = value(config("sideload"), "google_app_id")!!
        assertTrue("sideload app id shape: $testId", appIdShape.matches(testId))
        assertEquals("same project", realId.split(":")[1], testId.split(":")[1])
        assertFalse("sideload must use a different app id from the shipped app", realId == testId)
    }

    @Test
    fun `the consent text states the facts and avoids the forbidden words`() {
        val strings = read("app/src/main/res/values/strings.xml")
        val consent = Regex("""<string name="(consent_[a-z_]+|settings_usage_counts[a-z_]*)">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .findAll(strings).map { it.groupValues[2] }.toList()
        assertTrue("consent strings are missing", consent.size >= 6)
        val body = Regex("""<string name="consent_body">(.*?)</string>""", RegexOption.DOT_MATCHES_ALL)
            .find(strings)!!.groupValues[1]
        listOf("Google Analytics", "random ID", "outside the UK", "faith", "GPS", "your prayer settings", "switch it off")
            .forEach { assertTrue("consent_body no longer says \"$it\"", body.contains(it)) }
        consent.forEach {
            assertFalse("The word tracking is not used in this app's copy", it.contains("track", ignoreCase = true))
            assertFalse("The dua request belongs in the disclaimer and nowhere else", it.contains("dua", ignoreCase = true))
        }
    }

    @Test
    fun `the feedback row mails the address the privacy policy gives`() {
        val strings = read("app/src/main/res/values/strings.xml")
        val email = Regex("""<string name="feedback_email"[^>]*>(.*?)</string>""").find(strings)!!.groupValues[1]
        assertTrue("feedback address must be in privacy.html", read("docs/privacy.html").contains("mailto:$email"))
    }
}
