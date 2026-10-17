package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import com.sajdatime.app.data.BuiltInAdhan
import com.sajdatime.app.data.alarmSoundForSect
import com.sajdatime.core.Sect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The built-in adhans (1.5.0): opt in, never the default, one per school, and a chosen adhan follows
 * the school when it changes.
 */
class BuiltInAdhanTest {

    private val pkg = "com.sajdatime.app"
    private val phone = "content://media/internal/audio/media/42"
    private val sunni = BuiltInAdhan.ADHAN.uri(pkg)
    private val shia = BuiltInAdhan.SHIA_ADHAN.uri(pkg)

    @Test
    fun `never the default sound`() {
        assertEquals("", AppSettings().alarmSoundUri)
        assertNull(BuiltInAdhan.of(AppSettings().alarmSoundUri))
    }

    @Test
    fun `the stored address names the raw file, whatever the build's package`() {
        assertEquals("android.resource://com.sajdatime.app/raw/adhan", sunni)
        assertEquals("android.resource://com.sajdatime.app/raw/adhan_shia", shia)
        assertEquals(BuiltInAdhan.SHIA_ADHAN, BuiltInAdhan.of(BuiltInAdhan.SHIA_ADHAN.uri("com.sajdatime.app.sideload")))
        assertEquals(BuiltInAdhan.ADHAN, BuiltInAdhan.of(sunni))
        assertNull(BuiltInAdhan.of(phone))
        assertNull(BuiltInAdhan.of(""))
        // The resource names must match the real files: this fails to compile if one is renamed.
        assertTrue(R.raw.adhan != 0 && R.raw.adhan_shia != 0)
    }

    @Test
    fun `each school is offered exactly its own adhan`() {
        assertEquals(listOf(BuiltInAdhan.ADHAN), BuiltInAdhan.forSect(Sect.SUNNI))
        assertEquals(listOf(BuiltInAdhan.SHIA_ADHAN), BuiltInAdhan.forSect(Sect.SHIA))
    }

    @Test
    fun `a chosen adhan follows the school`() {
        assertEquals(shia, alarmSoundForSect(Sect.SHIA, sunni, phone, pkg))
        assertEquals(sunni, alarmSoundForSect(Sect.SUNNI, shia, phone, pkg))
    }

    @Test
    fun `any other sound, or the same school, is left exactly as it was`() {
        assertEquals(sunni, alarmSoundForSect(Sect.SUNNI, sunni, phone, pkg))
        assertEquals(shia, alarmSoundForSect(Sect.SHIA, shia, phone, pkg))
        assertEquals(phone, alarmSoundForSect(Sect.SHIA, phone, "content://other", pkg))
        assertEquals("", alarmSoundForSect(Sect.SHIA, "", phone, pkg))
    }
}
