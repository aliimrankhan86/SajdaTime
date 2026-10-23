package com.sajdatime.app

import com.sajdatime.app.ui.components.splitsAWord
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The rule behind WholeWords: a line may end between words, never inside one. */
class WholeWordsTest {
    @Test
    fun breakBetweenWordsIsFine() {
        // "Follow " | "phone": the first line ends after the space.
        assertFalse(splitsAWord("Follow phone", listOf(7)))
        assertFalse(splitsAWord("Settings", emptyList()))
    }

    @Test
    fun breakInsideAWordIsCaught() {
        assertTrue(splitsAWord("Settings", listOf(7))) // "Setting" | "s"
        assertTrue(splitsAWord("Telefona uy", listOf(7))) // "Telefon" | "a uy", as seen at 2.0
        assertTrue(splitsAWord("Pengaturan", listOf(9)))
        assertTrue(splitsAWord("فون کے مطابق", listOf(2))) // right to left letters count too
    }
}
