package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.alarmSoundForSect
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The built-in adhan (1.5.0): opt in, Sunni only, and a move to Shia falls back quietly to the
 * phone sound chosen before. Never the default.
 */
class BuiltInAdhanTest {

    private val phone = "content://media/internal/audio/media/42"
    private val adhan = BuiltInAdhan.ADHAN.uri("com.sajdatime.app")

    @Test
    fun `never the default sound`() {
        assertEquals("", AppSettings().alarmSoundUri)
        assertNull(BuiltInAdhan.of(AppSettings().alarmSoundUri))
    }

    @Test
    fun `the stored address names the raw file, whatever the build's package`() {
        assertEquals("android.resource://com.sajdatime.app/raw/adhan", adhan)
        assertEquals(BuiltInAdhan.ADHAN, BuiltInAdhan.of(BuiltInAdhan.ADHAN.uri("com.sajdatime.app.sideload")))
        assertNull(BuiltInAdhan.of(phone))
        assertNull(BuiltInAdhan.of(""))
        // The resource name must match the real file: this fails to compile if it is renamed.
        assertTrue(R.raw.adhan != 0)
    }

    @Test
    fun `offered to Sunni users only`() {
        assertTrue(BuiltInAdhan.offeredTo(Sect.SUNNI))
        assertFalse(BuiltInAdhan.offeredTo(Sect.SHIA))
    }

    @Test
    fun `moving to Shia falls back to the earlier phone sound, or the default`() {
        assertEquals(phone, alarmSoundForSect(Sect.SHIA, adhan, phone))
        assertEquals("", alarmSoundForSect(Sect.SHIA, adhan, ""))
    }

    @Test
    fun `any other sound, or staying Sunni, is left exactly as it was`() {
        assertEquals(adhan, alarmSoundForSect(Sect.SUNNI, adhan, phone))
        assertEquals(phone, alarmSoundForSect(Sect.SHIA, phone, "content://other"))
        assertEquals("", alarmSoundForSect(Sect.SHIA, "", phone))
    }
}
