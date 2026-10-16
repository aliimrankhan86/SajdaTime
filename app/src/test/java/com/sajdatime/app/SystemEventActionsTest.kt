package com.sajdatime.app

import com.sajdatime.app.notify.SystemEventReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The receiver that rebuilds the schedule has two lists that must say the same thing: the actions the
 * manifest delivers to it, and the actions its code acts on. An action in the manifest alone is
 * delivered and silently ignored, and one in the code alone is never delivered. Both are invisible in
 * use, so this holds them together, and pins the one added for the widgets (the phone's language).
 */
class SystemEventActionsTest {

    private val manifest: String = run {
        val file = File(File("..").canonicalFile, "app/src/main/AndroidManifest.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        file.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
    }

    private val filtered: Set<String> = run {
        val block = Regex("""<receiver\b[^>]*android:name="\.notify\.SystemEventReceiver"[^>]*>.*?</receiver>""", RegexOption.DOT_MATCHES_ALL)
            .find(manifest)?.value
        assertTrue("SystemEventReceiver is not declared", block != null)
        Regex("""<action\s+android:name="([^"]+)"""").findAll(block!!).map { it.groupValues[1] }.toSet()
    }

    @Test
    fun `the manifest filter and the code handle exactly the same actions`() {
        assertEquals(filtered, SystemEventReceiver.HANDLED_ACTIONS)
    }

    @Test
    fun `a change of the phone's language is one of them`() {
        assertTrue("android.intent.action.LOCALE_CHANGED" in SystemEventReceiver.HANDLED_ACTIONS)
    }

    @Test
    fun `the five that were there before the widgets are still there`() {
        val before = setOf(
            "android.intent.action.BOOT_COMPLETED",
            "android.intent.action.TIMEZONE_CHANGED",
            "android.intent.action.TIME_SET",
            "android.intent.action.MY_PACKAGE_REPLACED",
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
        assertTrue(SystemEventReceiver.HANDLED_ACTIONS.containsAll(before))
    }
}
