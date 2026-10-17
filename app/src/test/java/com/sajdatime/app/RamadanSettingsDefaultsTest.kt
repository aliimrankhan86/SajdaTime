package com.sajdatime.app

import com.sajdatime.app.data.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The defaults a new install gets for the Ramadan and Eid additions. They matter more than they
 * look: a person who never opens Settings must get the card and the greeting without asking, and a
 * dismissal must start out as "nothing dismissed", or the first Ramadan and the first Eid would be
 * swallowed by a zero that meant something.
 */
class RamadanSettingsDefaultsTest {

    @Test
    fun `a new install shows the Ramadan card and has dismissed nothing`() {
        val s = AppSettings()
        assertTrue(s.ramadanCardEnabled)
        assertEquals(0, s.ramadanNoticeDismissedYear)
        assertEquals(0, s.eidGreetingDismissedCode)
    }

    @Test
    fun `no real Eid code or Hijri year can equal the nothing dismissed value`() {
        // Codes are year times 100 plus month, so the smallest real one is about 130010.
        // Zero can only mean nothing has been dismissed.
        val smallestRealCode = 1300 * 100 + 10
        assertTrue(smallestRealCode > AppSettings().eidGreetingDismissedCode)
        assertTrue(1300 > AppSettings().ramadanNoticeDismissedYear)
    }
}
