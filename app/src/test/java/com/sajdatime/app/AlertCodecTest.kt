package com.sajdatime.app

import com.sajdatime.app.data.AlertCodec
import com.sajdatime.app.data.AlertStyle
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the per-prayer alert preference, and in particular the upgrade path onto it.
 *
 * Both halves fail silently if they are wrong, which is the reason for the test. A decode
 * that quietly drops an entry turns a prayer alert off and says nothing; a migration that
 * misreads the two keys it replaced would give someone who had deliberately silenced four
 * prayers four unexpected alerts a day, the morning after an update they did not ask for.
 */
class AlertCodecTest {

    private val prayers = PrayerSlot.entries.filter { it.isPrayer }

    @Test
    fun `a round trip keeps every prayer and its own style`() {
        val original = mapOf(
            PrayerSlot.FAJR to AlertStyle.ALARM,
            PrayerSlot.DHUHR to AlertStyle.NOTIFICATION,
            PrayerSlot.ISHA to AlertStyle.ALARM,
        )
        assertEquals(original, AlertCodec.decode(AlertCodec.encode(original)))
    }

    @Test
    fun `encoding is stable, so an unchanged setting is not rewritten`() {
        val a = linkedMapOf(PrayerSlot.ISHA to AlertStyle.ALARM, PrayerSlot.FAJR to AlertStyle.ALARM)
        val b = linkedMapOf(PrayerSlot.FAJR to AlertStyle.ALARM, PrayerSlot.ISHA to AlertStyle.ALARM)
        assertEquals(AlertCodec.encode(a), AlertCodec.encode(b))
        assertEquals("FAJR:ALARM,ISHA:ALARM", AlertCodec.encode(a))
    }

    @Test
    fun `an empty string means silent, not missing`() {
        assertTrue(AlertCodec.decode("").isEmpty())
        assertEquals("", AlertCodec.encode(emptyMap()))
    }

    /**
     * The point is that nothing here throws. Preferences outlive the code that wrote them
     * — a renamed constant, a hand-edited file, or a downgrade from a later version all
     * arrive here, and losing one entry is survivable where a crash on the alarm thread is
     * not.
     */
    @Test
    fun `rubbish is dropped entry by entry rather than throwing`() {
        val decoded = AlertCodec.decode("FAJR:ALARM,NONSENSE:ALARM,DHUHR:SHOUTING,,ASR,ISHA:NOTIFICATION")
        assertEquals(
            mapOf(PrayerSlot.FAJR to AlertStyle.ALARM, PrayerSlot.ISHA to AlertStyle.NOTIFICATION),
            decoded,
        )
    }

    /** Sunrise is in PrayerSlot but is not a prayer, and must never acquire an alert. */
    @Test
    fun `sunrise is refused even if it is written into the preference`() {
        assertTrue(AlertCodec.decode("SUNRISE:ALARM").isEmpty())
        assertTrue(AlertCodec.migrate("SUNRISE,FAJR", "ALARM").keys.none { !it.isPrayer })
    }

    @Test
    fun `an install that never touched the old setting gets all five, quietly`() {
        val migrated = AlertCodec.migrate(null, null)
        assertEquals(prayers.toSet(), migrated.keys)
        assertTrue(migrated.values.all { it == AlertStyle.NOTIFICATION })
    }

    @Test
    fun `an install that chose alarm mode keeps alarm on every prayer it had on`() {
        val migrated = AlertCodec.migrate("FAJR,MAGHRIB", "ALARM")
        assertEquals(
            mapOf(PrayerSlot.FAJR to AlertStyle.ALARM, PrayerSlot.MAGHRIB to AlertStyle.ALARM),
            migrated,
        )
    }

    /**
     * The old preference stored "no prayers" as an empty string and "never set" as an
     * absent key. Conflating the two is the mistake that would turn five alerts back on
     * for the one user who had explicitly turned them all off.
     */
    @Test
    fun `an install that silenced everything stays silenced`() {
        assertTrue(AlertCodec.migrate("", "NOTIFICATION").isEmpty())
        assertEquals(5, AlertCodec.migrate(null, "NOTIFICATION").size)
    }
}
