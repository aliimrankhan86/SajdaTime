package com.sajdatime.app

import com.sajdatime.app.notify.AlertCheck
import org.junit.Assert.assertEquals
import org.junit.Test

/** The Alert check row's count: only checks that apply and fail are counted. */
class AlertCheckTest {
    @Test
    fun `everything fine counts nothing`() {
        assertEquals(0, AlertCheck(notifications = true, exactAlarms = true, battery = true, dnd = true).failing)
    }

    @Test
    fun `checks that do not apply are never counted`() {
        assertEquals(0, AlertCheck(notifications = true, exactAlarms = null, battery = true, dnd = null).failing)
    }

    @Test
    fun `each failing check counts once`() {
        assertEquals(1, AlertCheck(notifications = false, exactAlarms = null, battery = true, dnd = null).failing)
        assertEquals(4, AlertCheck(notifications = false, exactAlarms = false, battery = false, dnd = false).failing)
    }
}
