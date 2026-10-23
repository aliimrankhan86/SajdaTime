package com.sajdatime.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Only sounds the alarm channel can play are kept from the picker (HANDOVER §5.24). */
class AlarmSoundSchemeTest {
    @Test
    fun `media, settings and app resources are kept`() {
        assertTrue(isAcceptedAlarmSound("content"))
        assertTrue(isAcceptedAlarmSound("android.resource"))
    }

    @Test
    fun `files, the web and nonsense are refused`() {
        listOf("file", "http", "https", "javascript", "", null).forEach { assertFalse(it.toString(), isAcceptedAlarmSound(it)) }
    }
}
