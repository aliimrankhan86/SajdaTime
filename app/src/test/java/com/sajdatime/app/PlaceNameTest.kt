package com.sajdatime.app

import com.sajdatime.app.data.placeLabel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bug a tester reported was "it shows my county, not my town", so the assertions that
 * matter are about *ordering* — which field wins when several are present, and which is
 * reached when the town-sized ones are missing.
 */
class PlaceNameTest {

    private fun label(
        locality: String? = null,
        subLocality: String? = null,
        featureName: String? = null,
        subAdminArea: String? = null,
        adminArea: String? = null,
        countryName: String? = null,
        fallback: String = "",
    ) = placeLabel(locality, subLocality, featureName, subAdminArea, adminArea, countryName, fallback)

    @Test
    fun `the town wins when the geocoder knows it`() {
        assertEquals(
            "Slough, United Kingdom",
            label(
                locality = "Slough",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
            ),
        )
    }

    @Test
    fun `the reported bug - a coarse fix with no locality no longer falls through to the county`() {
        // This is the exact shape a coarse fix returns when the fuzzed point lands outside
        // any town polygon: no locality, but a district the user would still recognise.
        assertEquals(
            "Langley, United Kingdom",
            label(
                subLocality = "Langley",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
            ),
        )
    }

    @Test
    fun `the county is still used when nothing more specific exists`() {
        // Not a regression — a county beats showing the user nothing at all.
        assertEquals(
            "Berkshire, United Kingdom",
            label(subAdminArea = "Berkshire", adminArea = "England", countryName = "United Kingdom"),
        )
    }

    @Test
    fun `a house number is never shown as a place`() {
        // featureName is only usually a place. "37, United Kingdom" would be worse than
        // the county this whole change exists to avoid.
        assertEquals(
            "Berkshire, United Kingdom",
            label(featureName = "37", subAdminArea = "Berkshire", countryName = "United Kingdom"),
        )
    }

    @Test
    fun `a point of interest never outranks a real administrative area`() {
        // Regression, found by running it rather than reading it. featureName sat third in
        // the chain, ahead of the county, and the emulator's geocoder answered a UK fix
        // with a hotel: the home screen read "Townhouse Hotel, United Kingdom". A POI is
        // worse than the county the change exists to remove, and on an app that asks only
        // for an approximate position, naming a building is its own kind of wrong.
        assertEquals(
            "Berkshire, United Kingdom",
            label(
                featureName = "Townhouse Hotel",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
            ),
        )
    }

    @Test
    fun `a named feature is shown when there is no administrative area at all`() {
        // Mid-ocean or open desert: a name beats no name. This is the only case featureName
        // is still consulted for.
        assertEquals("Snowdonia, United Kingdom", label(featureName = "Snowdonia", countryName = "United Kingdom"))
    }

    @Test
    fun `the country is not repeated when it is all the geocoder resolved`() {
        assertEquals("Singapore", label(locality = "Singapore", countryName = "Singapore"))
        assertEquals("United Kingdom", label(countryName = "United Kingdom"))
    }

    @Test
    fun `blank and whitespace-only fields are skipped rather than shown`() {
        // A geocoder returning "" for locality used to produce ", United Kingdom".
        assertEquals("Slough, United Kingdom", label(locality = "  ", subLocality = "Slough", countryName = "United Kingdom"))
    }

    @Test
    fun `the fallback is used when the geocoder resolved nothing`() {
        // CityLookup falls back to what the user typed rather than showing a blank row.
        assertEquals("Timbuktu", label(fallback = "Timbuktu"))
        assertEquals("", label())
    }
}
