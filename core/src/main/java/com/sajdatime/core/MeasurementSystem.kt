package com.sajdatime.core

/**
 * Distance in miles or km, by the phone's region (Ali, 23 Oct 2026, Q12). The four regions are CLDR's own
 * measurement data (US, LR and MM use the US system; GB uses the UK system, which is miles for distance), written
 * as a list so every Android version and a future iPhone give the same answer. No setting.
 */
object MeasurementSystem {
    const val KM_PER_MILE = 1.609344
    private val milesRegions = setOf("US", "GB", "LR", "MM")
    fun usesMiles(country: String): Boolean = country.uppercase() in milesRegions
    fun milesFromKm(km: Double): Double = km / KM_PER_MILE
}
