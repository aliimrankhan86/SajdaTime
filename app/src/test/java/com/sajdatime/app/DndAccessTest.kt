package com.sajdatime.app

import com.sajdatime.app.notify.Notifications
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Allow sound during Do Not Disturb" (Settings) opens Android's Do Not Disturb access list.
 * Android lists an app there only if its manifest declares ACCESS_NOTIFICATION_POLICY. 1.3.1
 * shipped the button without the permission, so on the owner's Samsung (10 Oct 2026) the list
 * opened and SajdaTime was not in it. These tests keep both halves of the fix in place.
 */
class DndAccessTest {
    private fun read(path: String) = File("..", path).readText()

    @Test
    fun `the manifest declares the permission that puts the app on the Do Not Disturb list`() {
        val manifest = read("app/src/main/AndroidManifest.xml")
        val declared = Regex("""<uses-permission\s+android:name="android\.permission\.ACCESS_NOTIFICATION_POLICY"\s*/>""")
        assertTrue(
            "AndroidManifest.xml must declare ACCESS_NOTIFICATION_POLICY, or the Do Not Disturb " +
                "button opens a list that never contains SajdaTime",
            declared.containsMatchIn(manifest),
        )
    }

    @Test
    fun `granting access moves the alarm to a channel that really bypasses Do Not Disturb`() {
        val sound = "content://settings/system/alarm_alert"
        val without = Notifications.alarmChannelId(sound, bypassesDnd = false)
        val with = Notifications.alarmChannelId(sound, bypassesDnd = true)
        // Android fixes a channel's bypass flag when the channel is created, so the grant has
        // to produce a new channel rather than re-use the old one.
        assertNotEquals(without, with)
        // Without access the id is exactly 1.3.1's, so nobody's existing channel changes until
        // they grant access.
        assertEquals("prayer_alarm_v" + sound.hashCode().toUInt().toString(16), without)
        // Both stay under the prefix the stale-channel clean-up looks for.
        assertTrue(with.startsWith("prayer_alarm_v"))
    }
}
