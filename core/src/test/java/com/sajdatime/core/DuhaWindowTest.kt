package com.sajdatime.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class DuhaWindowTest {
    private fun t(s: String) = Instant.parse(s)

    @Test
    fun `starts 20 minutes after sunrise and ends 15 before Dhuhr, by hand`() {
        val w = DuhaWindow.of(t("2026-01-10T08:02:00Z"), t("2026-01-10T12:08:00Z"))!!
        assertEquals(t("2026-01-10T08:22:00Z"), w.start)
        assertEquals(t("2026-01-10T11:53:00Z"), w.end)
    }

    @Test
    fun `seconds are cut in the safe direction`() {
        val w = DuhaWindow.of(t("2026-01-10T08:02:30Z"), t("2026-01-10T12:08:30Z"))!!
        assertEquals(t("2026-01-10T08:23:00Z"), w.start)
        assertEquals(t("2026-01-10T11:53:00Z"), w.end)
    }

    @Test
    fun `nothing when a time is missing, approximated, or the window is empty`() {
        assertNull(DuhaWindow.of(null, t("2026-01-10T12:00:00Z")))
        assertNull(DuhaWindow.of(t("2026-01-10T08:00:00Z"), null))
        assertNull(DuhaWindow.of(t("2026-01-10T08:00:00Z"), t("2026-01-10T12:00:00Z"), approximated = true))
        assertNull(DuhaWindow.of(t("2026-01-10T11:50:00Z"), t("2026-01-10T12:00:00Z")))
    }
}
