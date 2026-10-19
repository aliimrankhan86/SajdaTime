package com.sajdatime.wear

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The receiver that redraws the tile on a time zone or clock change has two lists that must say the
 * same thing: the actions the manifest delivers and the actions the code acts on. A mismatch is
 * invisible in use (the tile just shows the old zone's time for up to an hour), so this holds them
 * together. Same pattern as the phone's SystemEventActionsTest.
 */
class ClockChangeActionsTest {

    private val filtered: Set<String> = run {
        val file = File(File("..").canonicalFile, "wear/src/main/AndroidManifest.xml")
        assertTrue("Expected to find ${file.path}", file.isFile)
        val manifest = file.readText().replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")
        val block = Regex("""<receiver\b[^>]*android:name="\.ClockChangeReceiver"[^>]*>.*?</receiver>""", RegexOption.DOT_MATCHES_ALL)
            .find(manifest)?.value
        assertTrue("ClockChangeReceiver is not declared", block != null)
        Regex("""<action\s+android:name="([^"]+)"""").findAll(block!!).map { it.groupValues[1] }.toSet()
    }

    @Test
    fun `the manifest filter and the code handle exactly the same actions`() {
        assertEquals(filtered, ClockChangeReceiver.HANDLED_ACTIONS)
    }

    @Test
    fun `a time zone change is one of them`() {
        assertTrue("android.intent.action.TIMEZONE_CHANGED" in ClockChangeReceiver.HANDLED_ACTIONS)
    }
}
