package com.sajdatime.app

import com.sajdatime.app.widget.WidgetRefreshScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The widgets are declared in the manifest and add no permission. Read off disk, and declared as a task
 * input in app/build.gradle.kts like the other manifest guards.
 *
 * The permission list is embedded on purpose. A widget needs none, and a new permission is a Play
 * Console declaration, a privacy page change and a conversation with the owner, so it must fail a test
 * and be updated deliberately. The baseline is the app's own manifest as it stood at 1.3.2.
 */
class WidgetManifestTest {

    private val manifest: String = run {
        val file = File(File("..").canonicalFile, "app/src/main/AndroidManifest.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        file.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
    }

    private fun receiver(name: String): String {
        val match = Regex("""<receiver\b[^>]*android:name="\.widget\.$name"[^>]*>.*?</receiver>""", RegexOption.DOT_MATCHES_ALL)
            .find(manifest)
        assertTrue("$name is not declared as a receiver", match != null)
        return match!!.value
    }

    @Test
    fun `both widget providers are declared with the update action and their provider file`() {
        val expected = mapOf(
            "NextPrayerWidgetProvider" to "widget_next_prayer_info",
            "TodayWidgetProvider" to "widget_today_info",
        )
        for ((name, xml) in expected) {
            val block = receiver(name)
            assertTrue("$name: no update action", block.contains("""android.appwidget.action.APPWIDGET_UPDATE"""))
            assertTrue("$name: no provider meta data", block.contains("""android:name="android.appwidget.provider""""))
            assertTrue("$name: wrong provider file", block.contains("""android:resource="@xml/$xml""""))
            assertTrue("$name: exported is not stated", block.contains("android:exported="))
        }
    }

    @Test
    fun `the redraw receiver is declared, listens for its own action only and is not exported`() {
        val block = receiver("WidgetRefreshReceiver")
        assertTrue("must not be exported", block.contains("""android:exported="false""""))
        val actions = Regex("""<action\s+android:name="([^"]+)"""").findAll(block).map { it.groupValues[1] }.toList()
        assertEquals(listOf(WidgetRefreshScheduler.ACTION_REFRESH), actions)
    }

    @Test
    fun `the picker labels are the widget label strings`() {
        assertTrue(receiver("NextPrayerWidgetProvider").contains("""android:label="@string/widget_next_label""""))
        assertTrue(receiver("TodayWidgetProvider").contains("""android:label="@string/widget_today_label""""))
    }

    @Test
    fun `the manifest asks for the permissions it asked for at 1_3_2 and no others`() {
        val declared = Regex("""<uses-permission\s+android:name="([^"]+)"([^>]*)/>""")
            .findAll(manifest)
            .filterNot { it.groupValues[2].contains("""tools:node="remove"""") }
            .map { it.groupValues[1] }
            .toSet()
        val baseline = setOf(
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.INTERNET",
            "android.permission.POST_NOTIFICATIONS",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.SCHEDULE_EXACT_ALARM",
            "android.permission.ACCESS_NOTIFICATION_POLICY",
        )
        assertEquals(baseline, declared)
    }
}
