package com.sajdatime.app

import com.sajdatime.app.data.CityLookup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards the geocoding response parsing.
 *
 * Context: the previous implementation read coordinates from a field that upstream
 * quietly turned into a fixed placeholder, so every city search resolved to the same
 * point in Nigeria while the UI showed the user their own city name. Nothing failed
 * loudly. These tests pin the shape of the response actually relied on, and the last one
 * pins the behaviour that would have made the original fault visible: the name shown is
 * the one that came back with the coordinates, never the text the user typed.
 */
class CityLookupParseTest {

    // A trimmed but faithful Open-Meteo response.
    private val lahore = """
        {"results":[{"id":1172451,"name":"Lahore","latitude":31.558,"longitude":74.35071,
        "country":"Pakistan","admin1":"Punjab"}],"generationtime_ms":0.5}
    """.trimIndent()

    @Test
    fun `coordinates come from the response, not from anywhere else`() {
        val result = CityLookup.parseOpenMeteo(lahore, "Lahore")!!
        assertEquals(31.558, result.coordinates.latitude, 0.0001)
        assertEquals(74.35071, result.coordinates.longitude, 0.0001)
    }

    @Test
    fun `the displayed name is the resolved place, not the typed text`() {
        // Typing "lahor" and getting back "Lahore, Pakistan" is the point: if the lookup
        // ever resolves somewhere else, the user can see that it did.
        val result = CityLookup.parseOpenMeteo(lahore, "lahor")!!
        assertEquals("Lahore, Pakistan", result.city)
    }

    @Test
    fun `an empty result set is a miss, not a default location`() {
        assertNull(CityLookup.parseOpenMeteo("""{"generationtime_ms":0.3}""", "Nowhereville"))
        assertNull(CityLookup.parseOpenMeteo("""{"results":[]}""", "Nowhereville"))
    }

    @Test
    fun `malformed json is a miss rather than a crash`() {
        assertNull(CityLookup.parseOpenMeteo("not json at all", "Lahore"))
        assertNull(CityLookup.parseOpenMeteo("", "Lahore"))
    }

    @Test
    fun `a result missing coordinates is rejected outright`() {
        // Better to tell the user nothing was found than to place them at zero, zero.
        val noCoords = """{"results":[{"name":"Lahore","country":"Pakistan"}]}"""
        assertNull(CityLookup.parseOpenMeteo(noCoords, "Lahore"))
    }

}
