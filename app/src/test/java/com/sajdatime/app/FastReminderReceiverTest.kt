package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.app.notify.shouldPostFastReminder
import com.sajdatime.core.FastKind
import com.sajdatime.core.FastReason
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * What the receiver decides when a Sunnah fast reminder arrives (HANDOVER 5.41): judged on the settings
 * of the moment it fires. Off by default, never for Shia, never on a stale day, never a code that
 * meets another family's.
 */
class FastReminderReceiverTest {

    private val zone = ZoneId.of("Europe/London")
    private val monday = LocalDate.parse("2026-10-26")
    private val eveningBefore = monday.minusDays(1).atTime(21, 30).atZone(zone).toInstant()
    private fun settings(sect: Sect = Sect.SUNNI, kinds: Set<FastKind> = setOf(FastKind.MONDAY_THURSDAY)) =
        AppSettings(sect = sect, fastReminders = kinds)

    @Test fun `off by default`() {
        assertTrue(AppSettings().fastReminders.isEmpty())
        assertFalse(shouldPostFastReminder(AppSettings(), monday, FastReason.MONDAY, eveningBefore, zone))
    }

    @Test fun `shown the evening before when chosen`() {
        assertTrue(shouldPostFastReminder(settings(), monday, FastReason.MONDAY, eveningBefore, zone))
    }

    @Test fun `never for a Shia user, even with every kind stored`() {
        assertFalse(shouldPostFastReminder(settings(Sect.SHIA, FastKind.entries.toSet()), monday, FastReason.MONDAY, eveningBefore, zone))
    }

    @Test fun `silent when that kind was switched off since it was laid down`() {
        assertFalse(shouldPostFastReminder(settings(kinds = setOf(FastKind.WHITE_DAYS)), monday, FastReason.MONDAY, eveningBefore, zone))
    }

    @Test fun `silent on the fast day itself or any other day`() {
        assertFalse(shouldPostFastReminder(settings(), monday, FastReason.MONDAY, monday.atTime(8, 0).atZone(zone).toInstant(), zone))
        assertFalse(shouldPostFastReminder(settings(), monday, FastReason.MONDAY, monday.minusDays(2).atTime(21, 0).atZone(zone).toInstant(), zone))
    }

    @Test fun `the fast band is 50000 plus the day and stays clear of every other family`() {
        assertEquals(50_001, PrayerAlarmScheduler.fastRequestCode(LocalDate.parse("2027-01-01")))
        assertEquals(50_366, PrayerAlarmScheduler.fastRequestCode(LocalDate.parse("2028-12-31")))
        assertTrue(PrayerAlarmScheduler.fastRequestCode(LocalDate.parse("2028-01-01")) > 40_000 + 3_665)
        assertEquals("com.sajdatime.app.action.FAST_REMINDER", PrayerAlarmScheduler.ACTION_FAST_REMINDER)
    }
}
