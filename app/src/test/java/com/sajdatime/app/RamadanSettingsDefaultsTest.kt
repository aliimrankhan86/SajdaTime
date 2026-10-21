package com.sajdatime.app

import com.sajdatime.app.data.AlertStyle
import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.notify.Notifications
import com.sajdatime.app.notify.suhoorSoundUri
import com.sajdatime.core.Sect
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The defaults a new install gets for the Ramadan and Eid additions. They matter more than they
 * look: a person who never opens Settings must get the card and the greeting without asking, and a
 * dismissal must start out as "nothing dismissed", or the first Ramadan and the first Eid would be
 * swallowed by a zero that meant something.
 */
class RamadanSettingsDefaultsTest {

    @Test
    fun `a new install shows the Ramadan card and has dismissed nothing`() {
        val s = AppSettings()
        assertTrue(s.ramadanCardEnabled)
        assertEquals(0, s.ramadanNoticeDismissedYear)
        assertEquals(0, s.eidGreetingDismissedCode)
    }

    @Test
    fun `no real Eid code or Hijri year can equal the nothing dismissed value`() {
        // Codes are year times 100 plus month, so the smallest real one is about 130010.
        // Zero can only mean nothing has been dismissed.
        val smallestRealCode = 1300 * 100 + 10
        assertTrue(smallestRealCode > AppSettings().eidGreetingDismissedCode)
        assertTrue(1300 > AppSettings().ramadanNoticeDismissedYear)
    }

    @Test
    fun `the suhoor reminder is on by default, an hour before Fajr, as a quiet notification without the adhan`() {
        // Owner, 21 Oct 2026: on by default, 60 minutes, Notification, never the adhan unless chosen.
        val s = AppSettings()
        assertEquals(60, s.suhoorReminderMinutes)
        assertTrue(s.suhoorReminderMinutes in AppSettings.SUHOOR_CHOICES)
        assertEquals(AlertStyle.NOTIFICATION, s.suhoorReminderStyle)
        assertFalse(s.suhoorUsesAdhan)
    }

    @Test
    fun `an install that never chose reads 60, and a stored Off stays off`() {
        val source = File("..", "app/src/main/java/com/sajdatime/app/data/SettingsRepository.kt").readText()
        // Unset key: the default, so 1.4.0 users who never opened the reminder get it too.
        assertTrue(source.contains("(this[Keys.SUHOOR_MINUTES] ?: AppSettings.SUHOOR_DEFAULT_MINUTES)"))
        // Choosing Off writes 0, which is a choice in the list, so it is read back as Off, not as the default.
        assertTrue(source.contains("it[Keys.SUHOOR_MINUTES] = minutes.takeIf { m -> m in AppSettings.SUHOOR_CHOICES } ?: 0"))
        assertTrue(0 in AppSettings.SUHOOR_CHOICES)
    }

    @Test
    fun `the suhoor reminder never plays the prayer sounds, and plays the school's adhan only when chosen`() {
        val pkg = "com.sajdatime.app"
        val adhanForPrayers = AppSettings(
            alarmSoundUri = BuiltInAdhan.ADHAN.uri(pkg),
            notificationSoundUri = BuiltInAdhan.ADHAN.uri(pkg),
        )
        assertEquals("", suhoorSoundUri(adhanForPrayers, pkg))
        assertEquals(BuiltInAdhan.ADHAN.uri(pkg), suhoorSoundUri(AppSettings(sect = Sect.SUNNI, suhoorUsesAdhan = true), pkg))
        assertEquals(BuiltInAdhan.SHIA_ADHAN.uri(pkg), suhoorSoundUri(AppSettings(sect = Sect.SHIA, suhoorUsesAdhan = true), pkg))
    }

    @Test
    fun `the suhoor reminder's channel is its own, so it never deletes a prayer channel`() {
        val ids = listOf(
            Notifications.suhoorChannelId(AlertStyle.NOTIFICATION, "", bypassesDnd = false),
            Notifications.suhoorChannelId(AlertStyle.ALARM, "", bypassesDnd = false),
            Notifications.suhoorChannelId(AlertStyle.ALARM, "", bypassesDnd = true),
            Notifications.suhoorChannelId(AlertStyle.NOTIFICATION, BuiltInAdhan.ADHAN.uri("p"), bypassesDnd = false),
        )
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach {
            assertTrue(it.startsWith("suhoor_v"))
            assertNotEquals(Notifications.CHANNEL_PRAYERS, it)
            assertFalse(it.startsWith("prayer_"))
        }
        // A notification never passes Do Not Disturb, access or not.
        assertEquals(
            Notifications.suhoorChannelId(AlertStyle.NOTIFICATION, "", bypassesDnd = false),
            Notifications.suhoorChannelId(AlertStyle.NOTIFICATION, "", bypassesDnd = true),
        )
    }

    @Test
    fun `a person whose only alarm is the suhoor reminder can still reach Play on silent`() {
        // The Alarm sound row, home of the switch, shows only while a prayer is an Alarm (review, 21 Oct 2026).
        val suhoorOnlyAlarm = AppSettings(
            alertFor = emptyMap(),
            suhoorReminderMinutes = 60,
            suhoorReminderStyle = AlertStyle.ALARM,
        )
        assertFalse(suhoorOnlyAlarm.prayersUseAlarm)
        assertTrue(suhoorOnlyAlarm.usesAlarm)
        val source = File("..", "app/src/main/java/com/sajdatime/app/ui/settings/SettingsScreen.kt").readText()
        val dialog = source.substringAfter("private fun SuhoorDialog(").substringBefore("\n}\n")
        assertTrue(dialog.contains("if (style == AlertStyle.ALARM) PlayOnSilentRow(respectsSilent, onSetRespectsSilent)"))
    }
}
