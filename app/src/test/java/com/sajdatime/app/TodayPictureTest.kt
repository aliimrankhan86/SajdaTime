package com.sajdatime.app

import com.sajdatime.app.share.TodayPicture
import com.sajdatime.core.CalculationPrefs
import com.sajdatime.core.Coordinates
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** The 1.8.0 share picture's inputs (share/TodayPicture.kt). The drawing itself is checked on the emulator. */
class TodayPictureTest {

    private val london = Coordinates(51.5074, -0.1278)
    private val day = PrayerEngine.compute(london, LocalDate.parse("2027-02-05"), CalculationPrefs())

    @Test fun `six lines in order, every time the user sees`() {
        val lines = TodayPicture.lines(day, null)
        assertEquals(
            listOf(PrayerSlot.FAJR, PrayerSlot.SUNRISE, PrayerSlot.DHUHR, PrayerSlot.ASR, PrayerSlot.MAGHRIB, PrayerSlot.ISHA),
            lines.map { it.slot },
        )
        lines.forEach { assertEquals(day[it.slot], it.at) }
        assertTrue(lines.none { it.jumuah })
    }

    @Test fun `on a Friday with a Jumu'ah time Dhuhr's line is Jumu'ah at that time`() {
        val jumuah = day.date.atTime(LocalTime.of(13, 15)).atZone(ZoneId.of("Europe/London")).toInstant()
        val lines = TodayPicture.lines(day, jumuah)
        val dhuhr = lines.single { it.slot == PrayerSlot.DHUHR }
        assertTrue(dhuhr.jumuah)
        assertEquals(jumuah, dhuhr.at)
        assertEquals(6, lines.size)
    }

    @Test fun `no network, no new permission, no usage count in the picture code`() {
        val source = File(File("..").canonicalFile, "app/src/main/java/com/sajdatime/app/share/TodayPicture.kt").readText()
        for (word in listOf("UsageCounts", "java.net", "HttpURLConnection", "permission")) {
            assertFalse(word, word in source.substringAfter("object TodayPicture"))
        }
    }
}
