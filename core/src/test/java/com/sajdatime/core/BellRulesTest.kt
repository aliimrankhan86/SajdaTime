package com.sajdatime.core

import com.sajdatime.core.PrayerSlot.ASR
import com.sajdatime.core.PrayerSlot.DHUHR
import com.sajdatime.core.PrayerSlot.FAJR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The bell's off, on and restore rules (STEP-3-DESIGN §5, §14; Ali's Q6). "N" and "A" stand for the two styles. */
class BellRulesTest {

    private val five = PrayerSlot.entries.filter { it.isPrayer }
    private val start = BellState(
        alertFor = five.associateWith { "N" } + (FAJR to "A"),
        lastStyle = emptyMap(),
        reminderFor = five.toSet(),
        bellRemovedReminder = emptySet(),
    )

    @Test fun `off then on restores the alarm and the reminder`() {
        val (off, removed) = BellRules.off(start, FAJR, reminderMinutes = 10)
        assertTrue(removed)
        assertFalse(FAJR in off.alertFor)
        assertFalse(FAJR in off.reminderFor)
        val back = BellRules.on(off, FAJR, default = "N")
        assertEquals(start.alertFor, back.alertFor)
        assertEquals(start.reminderFor, back.reminderFor)
        assertEquals(emptySet<PrayerSlot>(), back.bellRemovedReminder)
    }

    @Test fun `with no memory, on is a notification`() {
        val state = start.copy(alertFor = start.alertFor - DHUHR)
        assertEquals("N", BellRules.on(state, DHUHR, default = "N").alertFor[DHUHR])
    }

    @Test fun `minutes at 0, the reminder still moves but the message says nothing about it`() {
        val (off, removed) = BellRules.off(start, ASR, reminderMinutes = 0)
        assertFalse(removed)
        assertTrue(ASR in off.bellRemovedReminder)
        assertTrue(ASR in BellRules.on(off, ASR, "N").reminderFor)
    }

    @Test fun `a prayer not in the reminder list gets none on restore`() {
        val state = start.copy(reminderFor = start.reminderFor - DHUHR)
        val (off, removed) = BellRules.off(state, DHUHR, reminderMinutes = 10)
        assertFalse(removed)
        assertFalse(DHUHR in BellRules.on(off, DHUHR, "N").reminderFor)
    }

    @Test fun `ticking the reminder dialog clears the bell's memory, so restore adds nothing twice`() {
        val (off, _) = BellRules.off(start, DHUHR, 10)
        val unticked = BellRules.setReminderFromSettings(off, DHUHR, on = false)
        assertFalse(DHUHR in BellRules.on(unticked, DHUHR, "N").reminderFor)
    }

    @Test fun `Settings Off remembers the style and leaves the reminder, then the bell restores that style`() {
        val off = BellRules.setFromSettings(start, FAJR, null)
        assertTrue(FAJR in off.reminderFor)
        assertEquals("A", BellRules.on(off, FAJR, "N").alertFor[FAJR])
    }

    @Test fun `bell off then Settings on gives the reminder back too`() {
        val (off, _) = BellRules.off(start, ASR, 10)
        val back = BellRules.setFromSettings(off, ASR, "A")
        assertEquals("A", back.alertFor[ASR])
        assertTrue(ASR in back.reminderFor)
        assertFalse(ASR in back.bellRemovedReminder)
    }

    @Test fun `off on an off prayer and on on an on prayer change nothing`() {
        val (off, _) = BellRules.off(start, DHUHR, 10)
        assertEquals(off to false, BellRules.off(off, DHUHR, 10))
        assertEquals(start, BellRules.on(start, DHUHR, "N"))
    }

    @Test fun `the four messages`() {
        assertEquals(BellMessage.OFF, BellRules.message(DHUHR, false, true))
        assertEquals(BellMessage.OFF_AND_REMINDER, BellRules.message(DHUHR, true, true))
        assertEquals(BellMessage.OFF_FAJR_RAMADAN, BellRules.message(FAJR, false, true))
        assertEquals(BellMessage.OFF_AND_REMINDER_FAJR_RAMADAN, BellRules.message(FAJR, true, true))
        assertEquals(BellMessage.OFF, BellRules.message(FAJR, false, false))
    }

    @Test fun `the permission door is chosen before asking`() {
        assertEquals(PermissionDoor.NONE, BellRules.permissionDoor(false, true, false, true))
        assertEquals(PermissionDoor.ASK, BellRules.permissionDoor(true, true, false, askedBefore = false))
        assertEquals(PermissionDoor.ASK, BellRules.permissionDoor(true, true, rationale = true, askedBefore = true))
        assertEquals(PermissionDoor.OPEN_SETTINGS, BellRules.permissionDoor(true, true, false, true))
        assertEquals(PermissionDoor.OPEN_SETTINGS, BellRules.permissionDoor(true, systemCanAsk = false, false, false))
    }
}
