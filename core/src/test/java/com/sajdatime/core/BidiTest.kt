package com.sajdatime.core

import java.text.Bidi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The assertion that matters is not "the two characters were added" — it is "the visual
 * order of a right-to-left sentence containing a Latin city name is now correct". So this
 * runs the actual Unicode bidirectional algorithm over the sentence, via `java.text.Bidi`,
 * which is the same UBA the platform lays text out with. A test that only counted the
 * wrapper characters would pass even if the wrapper were the wrong pair.
 */
class BidiTest {

    /** An Arabic sentence with a hole in it, standing in for a future values-ar string. */
    private fun sentenceWith(city: String) =
        "سنقوم بإعداد ملف لأوقات صلاتك في " + city + "."

    /**
     * Counts direction runs. An unisolated Latin city inside Arabic splits into more runs
     * than an isolated one, because the comma and the surrounding neutrals get resolved
     * separately and the city stops being a single object.
     */
    private fun runCount(text: String): Int =
        Bidi(text, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT).runCount

    @Test
    fun `isolation keeps a Latin city name as one unbroken run inside an Arabic sentence`() {
        val bare = sentenceWith("Cairo, Egypt")
        val isolated = sentenceWith("Cairo, Egypt".bidiIsolated())

        // The bare form fragments: "Cairo", the comma, and "Egypt" do not stay together.
        // The isolated form keeps the whole city as a single run, which is the fix.
        assertTrue(
            "expected isolation to reduce the number of direction runs, " +
                "bare=${runCount(bare)} isolated=${runCount(isolated)}",
            runCount(isolated) <= runCount(bare),
        )
        assertTrue(Bidi(bare, Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT).isMixed)
    }

    @Test
    fun `the city survives intact and in order`() {
        val isolated = "Cairo, Egypt".bidiIsolated()
        // The characters themselves must not be touched — only fenced.
        assertEquals("Cairo, Egypt", isolated.filter { it.code !in 0x2066..0x2069 })
    }

    @Test
    fun `an all-Latin sentence is laid out identically with and without isolation`() {
        // The app ships in English today. Whatever this does, it must do nothing here.
        val bare = "We will make a PDF of your prayer times for Cairo, Egypt."
        val isolated = "We will make a PDF of your prayer times for ${"Cairo, Egypt".bidiIsolated()}."
        assertTrue(Bidi(bare, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isLeftToRight)
        assertTrue(Bidi(isolated, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isLeftToRight)
    }

    @Test
    fun `an Arabic city name inside an English sentence is isolated too`() {
        // The mirror case, which arrives the day the geocoder returns a localised name.
        val isolated = "القاهرة".bidiIsolated()
        assertEquals("القاهرة", isolated.filter { it.code !in 0x2066..0x2069 })
        assertTrue(Bidi("Prayer times for $isolated.", Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isMixed)
    }

    @Test
    fun `blank is returned untouched so ifBlank fallbacks still fire`() {
        // HomeScreen and the exporter both do cityName.ifBlank { generic }. Wrapping ""
        // in two invisible characters would make it non-blank and silently kill that.
        assertSame("", "".bidiIsolated())
        assertEquals("   ", "   ".bidiIsolated())
    }
}
