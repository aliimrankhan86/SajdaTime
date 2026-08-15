package com.sajdatime.app

import com.sajdatime.app.data.placeLabel
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bug a tester reported was "it shows my county, not my town", so the assertions that
 * matter are about *ordering* — which field wins when several are present, and which is
 * reached when the town-sized ones are missing.
 *
 * The field values in the UK cases are copied from a real probe of Google's geocoder on
 * 15 Aug 2026 (HANDOVER §10), not invented: in Britain `locality` comes back null even for
 * the exact centre of a town, and the town survives only in the address line.
 */
class PlaceNameTest {

    private fun label(
        locality: String? = null,
        subLocality: String? = null,
        featureName: String? = null,
        subAdminArea: String? = null,
        adminArea: String? = null,
        countryName: String? = null,
        thoroughfare: String? = null,
        addressLine: String? = null,
        fallback: String = "",
    ) = placeLabel(
        locality, subLocality, featureName, subAdminArea, adminArea, countryName,
        thoroughfare, addressLine, fallback,
    )

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
    fun `the reported bug - the centre of Slough is Slough, not Berkshire`() {
        // Verbatim from the probe at 51.5105, -0.5950. No locality, no sub-locality, a bus
        // stop for a feature, and the county in subAdminArea. The old chain showed
        // "Berkshire, United Kingdom" here — for a user standing on the High Street.
        assertEquals(
            "Slough, United Kingdom",
            label(
                featureName = "Wellington Street (Stop C)",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
                addressLine = "Wellington Street (Stop C), Slough SL1 1XU, UK",
            ),
        )
    }

    @Test
    fun `the coarse fix that started it - a rugby club on the Slough border`() {
        // The fudged coarse position the emulator actually handed the app for Slough.
        assertEquals(
            "Slough, United Kingdom",
            label(
                featureName = "Slough Rugby Football Club",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
                addressLine = "Slough Rugby Football Club, Slough SL3 7LT, UK",
            ),
        )
    }

    @Test
    fun `the town beats the district, the same way locality beats subLocality abroad`() {
        // Bradford, Manningham: subLocality is present, and the old chain would have shown
        // "Manningham". A mosque timetable is titled by the town.
        assertEquals(
            "Bradford, United Kingdom",
            label(
                subLocality = "Manningham",
                featureName = "5",
                subAdminArea = "West Yorkshire",
                adminArea = "England",
                countryName = "United Kingdom",
                thoroughfare = "Welbury Drive",
                addressLine = "5 Welbury Dr, Manningham, Bradford BD8 7QH, UK",
            ),
        )
        // And Karachi, where locality is filled and already wins over Gulshan-e-Iqbal.
        assertEquals(
            "Karachi, Pakistan",
            label(
                locality = "Karachi",
                subLocality = "Gulshan-e-Iqbal",
                featureName = "A 168",
                subAdminArea = "Karachi City",
                adminArea = "Sindh",
                countryName = "Pakistan",
                addressLine = "A 168, Block 5 Gulshan-e-Iqbal, Karachi, 75300, Pakistan",
            ),
        )
    }

    @Test
    fun `London and Birmingham stop reading as their metropolitan counties`() {
        assertEquals(
            "London, United Kingdom",
            label(
                featureName = "58",
                subAdminArea = "Greater London",
                adminArea = "England",
                countryName = "United Kingdom",
                thoroughfare = "Turner Street",
                addressLine = "58 Turner St, London E1 2AB, UK",
            ),
        )
        assertEquals(
            "Birmingham, United Kingdom",
            label(
                featureName = "53",
                subAdminArea = "West Midlands",
                adminArea = "England",
                countryName = "United Kingdom",
                thoroughfare = "Evelyn Road",
                addressLine = "53 Evelyn Rd, Birmingham B11 3JH, UK",
            ),
        )
    }

    @Test
    fun `a partial postcode and a missing postcode are both handled`() {
        assertEquals(
            "Reading, United Kingdom",
            label(
                featureName = "Ashampstead Road",
                subAdminArea = "Berkshire",
                countryName = "United Kingdom",
                thoroughfare = "Ashampstead Road",
                addressLine = "Ashampstead Rd, Reading, UK",
            ),
        )
        assertEquals(
            "Slough, United Kingdom",
            label(
                featureName = "SL3",
                countryName = "United Kingdom",
                addressLine = "Upton Court Rd, Slough SL3, UK",
            ),
        )
    }

    @Test
    fun `a district is still used when there is no town anywhere`() {
        // No locality, a two-part address line that could equally be a POI, and a
        // sub-locality: the district is the most specific thing that is safely a place.
        assertEquals(
            "Datchet, United Kingdom",
            label(
                subLocality = "Datchet",
                featureName = "Datchet",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
                addressLine = "Datchet, UK",
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
        // for an approximate position, naming a building is its own kind of wrong. The
        // two-part address line must not rescue it either.
        assertEquals(
            "Berkshire, United Kingdom",
            label(
                featureName = "Townhouse Hotel",
                subAdminArea = "Berkshire",
                adminArea = "England",
                countryName = "United Kingdom",
                addressLine = "Townhouse Hotel, United Kingdom",
            ),
        )
    }

    @Test
    fun `the street never becomes the town`() {
        // A line whose part before the country is the street itself.
        assertEquals(
            "Berkshire, United Kingdom",
            label(
                featureName = "12",
                subAdminArea = "Berkshire",
                countryName = "United Kingdom",
                thoroughfare = "High Street",
                addressLine = "12, High Street, UK",
            ),
        )
    }

    @Test
    fun `a state or province abbreviation is never shown as a town`() {
        // A rural fix in a country that formats "<Town> <STATE> <zip>": with no locality
        // the parse would leave "ON" or "CA". The region name is the honest answer.
        assertEquals(
            "Ontario, Canada",
            label(
                featureName = "Concession Road 4",
                adminArea = "Ontario",
                countryName = "Canada",
                thoroughfare = "Concession Road 4",
                addressLine = "Concession Rd 4, ON K0A 1L0, Canada",
            ),
        )
    }

    @Test
    fun `a Turkish district-slash-province pair is cut back to the district`() {
        // Copied verbatim from the owner's Galaxy S23 Ultra in Alanya, 15 Aug 2026 — the
        // probe that disproved "locality is only ever null in the UK". Turkey writes
        // "<postcode> <District>/<Province>", so the whole part is "Alanya/Antalya" and
        // Antalya is a province the size of the Berkshire this feature exists to remove.
        assertEquals(
            "Alanya, Türkiye",
            label(
                subLocality = "Türkler",
                featureName = "23/2",
                subAdminArea = "Alanya",
                adminArea = "Antalya",
                countryName = "Türkiye",
                thoroughfare = "Fatih 6. Cadde",
                addressLine = "Türkler, Fatih 6. Cadde 23/2, 07410 Alanya/Antalya, Türkiye",
            ),
        )
    }

    @Test
    fun `an Arabic-comma address line parses the same way`() {
        assertEquals(
            "سلاو, المملكة المتحدة",
            label(
                featureName = "5",
                subAdminArea = "بيركشير",
                countryName = "المملكة المتحدة",
                addressLine = "5 شارع ويليام، سلاو SL1 1GZ، المملكة المتحدة",
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
