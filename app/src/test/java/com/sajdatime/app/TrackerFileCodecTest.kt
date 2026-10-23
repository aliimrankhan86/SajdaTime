package com.sajdatime.app

import com.sajdatime.app.data.TrackerData
import com.sajdatime.app.data.TrackerFileCodec
import com.sajdatime.core.DayRecord
import com.sajdatime.core.PauseRange
import com.sajdatime.core.PrayerSlot
import com.sajdatime.core.TRACKED_SLOTS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** The prayer record's save file (TrackerFileCodec). org.json is on the test classpath (libs.json). */
class TrackerFileCodecTest {

    private fun d(s: String) = LocalDate.parse(s)
    private val sample = TrackerData(
        records = mapOf(
            d("2027-02-08") to DayRecord(TRACKED_SLOTS.toSet()),
            d("2027-02-09") to DayRecord(setOf(PrayerSlot.FAJR, PrayerSlot.ISHA)),
        ),
        pauses = listOf(PauseRange(d("2027-02-10"), d("2027-02-12"))),
    )

    @Test fun `round trip keeps every tick and pause`() {
        val back = TrackerFileCodec.decode(TrackerFileCodec.encode(sample, d("2027-02-20")))
        assertEquals(sample.records, back.records)
        assertEquals(sample.pauses, back.pauses)
    }

    @Test fun `the file names prayers and holds nothing but dates and ticks`() {
        val text = TrackerFileCodec.encode(sample, d("2027-02-20"))
        assertTrue(text.contains("\"FAJR\""))
        for (word in listOf("latitude", "longitude", "city", "sect", "method")) assertFalse(word, text.contains(word, ignoreCase = true))
    }

    @Test fun `an open pause is closed on the day the file was saved`() {
        val open = sample.copy(pauses = listOf(PauseRange(d("2027-02-15"), null)))
        val back = TrackerFileCodec.decode(TrackerFileCodec.encode(open, d("2027-02-20")))
        assertEquals(listOf(PauseRange(d("2027-02-15"), d("2027-02-20"))), back.pauses)
    }

    @Test fun `a newer version is refused as too new`() {
        val text = """{"kind":"sajdatime-prayer-tracker","version":2,"days":{}}"""
        val e = runCatching { TrackerFileCodec.decode(text) }.exceptionOrNull() as TrackerFileCodec.Unreadable
        assertTrue(e.tooNew)
    }

    @Test fun `a file that is not ours, or not JSON, is refused`() {
        for (text in listOf("""{"kind":"something-else","version":1}""", "not json", "", """{"kind":"sajdatime-prayer-tracker"}""")) {
            val e = runCatching { TrackerFileCodec.decode(text) }.exceptionOrNull()
            assertTrue(text, e is TrackerFileCodec.Unreadable && !e.tooNew)
        }
    }

    @Test fun `unknown prayer names and bad dates are dropped one by one`() {
        val text = """{"kind":"sajdatime-prayer-tracker","version":1,"days":{"2027-02-08":["FAJR","SUNRISE","TAHAJJUD"],"bad-date":["ISHA"]}}"""
        assertEquals(mapOf(d("2027-02-08") to DayRecord(setOf(PrayerSlot.FAJR))), TrackerFileCodec.decode(text).records)
    }

    @Test fun `file name carries the date`() =
        assertEquals("sajdatime-prayers-2027-02-20.json", TrackerFileCodec.fileName(d("2027-02-20")))
}
