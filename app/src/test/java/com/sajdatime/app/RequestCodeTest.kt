package com.sajdatime.app

import com.sajdatime.app.notify.PrayerAlarmScheduler
import com.sajdatime.core.PrayerSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Alarm identity. A PendingIntent is the same alarm when its action, component and request code
 * match, so a collision between a prayer and a suhoor reminder would make one silently replace the
 * other. The two families are kept apart twice over, by action and by number range, and this pins both,
 * across leap years where day 366 exists.
 */
class RequestCodeTest {

    private val prayers = PrayerSlot.entries.filter { it.isPrayer }

    private fun days(from: String, to: String): List<LocalDate> =
        generateSequence(LocalDate.parse(from)) { it.plusDays(1) }
            .takeWhile { !it.isAfter(LocalDate.parse(to)) }
            .toList()

    @Test
    fun `prayer codes stay below the suhoor range on every day of a leap and a common year`() {
        for (date in days("2027-01-01", "2028-12-31")) {
            for (slot in prayers) {
                val code = PrayerAlarmScheduler.prayerRequestCode(slot, date)
                assertTrue("$slot on $date is $code", code < PrayerAlarmScheduler.SUHOOR_REQUEST_BASE)
            }
        }
    }

    @Test
    fun `suhoor codes run 10001 to 10366 and a leap year's last day is the top`() {
        assertEquals(10_001, PrayerAlarmScheduler.suhoorRequestCode(LocalDate.parse("2027-01-01")))
        assertEquals(10_365, PrayerAlarmScheduler.suhoorRequestCode(LocalDate.parse("2027-12-31")))
        assertEquals(10_366, PrayerAlarmScheduler.suhoorRequestCode(LocalDate.parse("2028-12-31")))
    }

    @Test
    fun `no suhoor code equals any prayer code, on any day`() {
        val prayerCodes = days("2027-01-01", "2028-12-31").flatMap { date ->
            prayers.map { PrayerAlarmScheduler.prayerRequestCode(it, date) }
        }.toSet()
        for (date in days("2027-01-01", "2028-12-31")) {
            val code = PrayerAlarmScheduler.suhoorRequestCode(date)
            assertTrue("$date is $code", code !in prayerCodes)
        }
    }

    @Test
    fun `within one year every code is unique, so rescheduling replaces and never duplicates`() {
        for (year in listOf(2027, 2028)) {
            val dates = days("$year-01-01", "$year-12-31")
            val prayerCodes = dates.flatMap { d -> prayers.map { PrayerAlarmScheduler.prayerRequestCode(it, d) } }
            assertEquals(prayerCodes.size, prayerCodes.toSet().size)
            val suhoorCodes = dates.map { PrayerAlarmScheduler.suhoorRequestCode(it) }
            assertEquals(suhoorCodes.size, suhoorCodes.toSet().size)
        }
    }

    @Test
    fun `the horizon the scheduler cancels never wraps a year into a clash`() {
        // cancelAll walks yesterday to two days ahead. Around New Year that spans day 365 or 366 and
        // day 1 or 2, which must all still be distinct.
        for (start in listOf("2027-12-29", "2028-12-29")) {
            val window = days(start, LocalDate.parse(start).plusDays(6).toString())
            val codes = window.map { PrayerAlarmScheduler.suhoorRequestCode(it) }
            assertEquals(codes.size, codes.toSet().size)
        }
    }

    @Test
    fun `the two actions are different, so identity differs even if a number ever collided`() {
        assertNotEquals(PrayerAlarmScheduler.ACTION_PRAYER, PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
        assertEquals("com.sajdatime.app.action.SUHOOR_REMINDER", PrayerAlarmScheduler.ACTION_SUHOOR_REMINDER)
    }
}
