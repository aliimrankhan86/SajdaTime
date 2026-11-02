package com.sajdatime.app

import com.sajdatime.app.ui.components.weekRingDp
import org.junit.Assert.assertEquals
import org.junit.Test

/** The week card's rings are 36dp when seven plus six 4dp gaps fit (276dp), else 32dp (STEP-1-DESIGN 9.4). */
class WeekRingSizeTest {
    @Test
    fun `36 at 276 and wider, 32 below`() {
        assertEquals(36f, weekRingDp(276f), 0f)
        assertEquals(36f, weekRingDp(340f), 0f)
        assertEquals(32f, weekRingDp(275.9f), 0f)
        assertEquals(32f, weekRingDp(256f), 0f)
    }
}
