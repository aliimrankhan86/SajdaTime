package com.sajdatime.app

import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.Notifications
import com.sajdatime.core.PrayerSlot
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The notification sound (owner, 18 Oct 2026: "user can select adhan or another sound for an alarm or
 * notification"). Nobody's sound may change until they pick one, and a notification must keep
 * following the phone's silent, vibrate and Do Not Disturb settings, which is what he chose.
 */
class NotificationSoundTest {

    @Test
    fun `no sound chosen keeps every notification on the channel it has always used`() {
        assertEquals(Notifications.CHANNEL_PRAYERS, Notifications.notificationChannelId(""))
    }

    @Test
    fun `a chosen sound gets its own channel, never the alarm's`() {
        val sound = "android.resource://com.sajdatime.app/raw/adhan"
        val id = Notifications.notificationChannelId(sound)
        assertTrue(id.startsWith("prayer_notif_v"))
        assertNotEquals(Notifications.CHANNEL_PRAYERS, id)
        assertNotEquals(Notifications.alarmChannelId(sound, bypassesDnd = false), id)
        assertNotEquals(Notifications.notificationChannelId("content://media/internal/audio/media/7"), id)
    }

    @Test
    fun `the notification channel plays on the notification volume and never passes Do Not Disturb`() {
        val source = File("..", "app/src/main/java/com/sajdatime/app/notify/Notifications.kt").readText()
        val body = source.substringAfter("fun ensureNotificationChannel(").substringBefore("fun canPost(")
        assertTrue(body.contains("USAGE_NOTIFICATION"))
        assertTrue(body.contains("enableVibration(true)"))
        assertFalse("A notification follows the phone; only an Alarm with Play on silent may bypass", body.contains("setBypassDnd"))
        // One "Prayer times" in the phone's settings at a time: the original goes while a chosen sound's is in use.
        assertTrue(body.contains("deleteNotificationChannel(CHANNEL_PRAYERS)"))
    }

    @Test
    fun `the picker's Default row is the default for the sound being chosen, saved as blank`() {
        val source = File("..", "app/src/main/java/com/sajdatime/app/MainActivity.kt").readText()
        assertTrue(source.contains("EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(defaultType)"))
        assertTrue(source.contains("setNotificationSound(storedSound(uri, RingtoneManager.TYPE_NOTIFICATION))"))
        assertTrue(source.contains("setAlarmSound(storedSound(uri, RingtoneManager.TYPE_ALARM))"))
    }

    @Test
    fun `the notification sound row shows only while something is a notification`() {
        val allAlarm = AppSettings(alertFor = PrayerSlot.entries.filter { it.isPrayer }.associateWith { AlertStyle.ALARM })
        assertFalse(allAlarm.usesNotification)
        assertTrue(allAlarm.copy(suhoorReminderMinutes = 30, suhoorReminderStyle = AlertStyle.NOTIFICATION).usesNotification)
        assertTrue(AppSettings(alertFor = mapOf(PrayerSlot.FAJR to AlertStyle.NOTIFICATION)).usesNotification)
        assertFalse(AppSettings(alertFor = emptyMap()).usesNotification)
    }
}
