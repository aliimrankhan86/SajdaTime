package com.sajdatime.app.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.sajdatime.core.AppLocale
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Turns a typed place name into coordinates, for travellers and for anyone who declines
 * location permission.
 *
 * Two sources, in order:
 *  1. The phone's own geocoder. Nothing leaves the app, and most phones have it.
 *  2. Open-Meteo's free geocoding API, for phones with no geocoder backend.
 *
 * Only the place name is ever sent, and only when the first source comes up empty. No
 * device identifier, no coordinates, no history. Once resolved the coordinates are stored
 * locally and every prayer calculation from then on is offline.
 *
 * This previously read the coordinates out of Aladhan's timingsByAddress response. That
 * endpoint now returns a fixed placeholder (8.8889, 7.7778) for every address, so every
 * search silently resolved to a point in Nigeria while showing the user their own city
 * name. Any replacement here must be spot-checked against real cities, not just "does it
 * return something".
 *
 * ponytail: HttpURLConnection + org.json, both in the platform. One GET does not need a
 * networking stack.
 */
class CityLookup(private val context: Context) {

    data class Result(val city: String, val coordinates: Coordinates)

    suspend fun search(query: String): Result? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext null

        fromPlatformGeocoder(trimmed) ?: fromOpenMeteo(trimmed)
    }

    @Suppress("DEPRECATION")
    private fun fromPlatformGeocoder(query: String): Result? {
        if (!Geocoder.isPresent()) return null
        return runCatching {
            // The callback form arrived in API 33. The blocking call still works and this
            // is already off the main thread, so one code path covers every version.
            // AppLocale, matching the language= parameter the Open-Meteo fallback below
            // sends, so both halves of this lookup name a place the same way.
            Geocoder(context, AppLocale.of(context))
                .getFromLocationName(query, 1)
                ?.firstOrNull()
                ?.let { Result(city = it.describeAddress(query), coordinates = Coordinates(it.latitude, it.longitude)) }
        }.getOrNull()
    }

    private fun fromOpenMeteo(query: String): Result? {
        val encoded = URLEncoder.encode(query, "UTF-8")
        // language= was hardcoded to "en" while the platform geocoder above used the
        // device locale, so the same search returned "Cairo" or "القاهرة" depending only
        // on which of the two happened to answer. Both follow AppLocale now.
        val language = AppLocale.of(context).language
        val url = URL(
            "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$encoded&count=1&language=$language&format=json",
        )

        val body = runCatching { url.readTextWithTimeout() }.getOrNull() ?: return null
        return parseOpenMeteo(body, query)
    }

    /** A readable label, falling back to what the user typed rather than showing blank. */
    private fun Address.describeAddress(fallback: String): String = listOfNotNull(
        locality ?: subAdminArea ?: adminArea,
        countryName,
    ).joinToString(", ").ifBlank { fallback }

    private fun URL.readTextWithTimeout(): String {
        val connection = openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    internal companion object {
        private const val TIMEOUT_MS = 12_000

        /**
         * Kept separate from the request, and free of any Android type, so the parsing
         * can be tested without a network or a device.
         */
        internal fun parseOpenMeteo(body: String, fallbackName: String): Result? = runCatching {
            val results = JSONObject(body).optJSONArray("results") ?: return null
            if (results.length() == 0) return null
            val first = results.getJSONObject(0)
            Result(
                city = listOfNotNull(
                    first.optString("name").takeIf { it.isNotBlank() },
                    first.optString("country").takeIf { it.isNotBlank() },
                ).joinToString(", ").ifBlank { fallbackName },
                coordinates = Coordinates(
                    // getDouble throws when absent, which is what we want: no coordinates
                    // means no result, never a silent fallback to zero, zero.
                    latitude = first.getDouble("latitude"),
                    longitude = first.getDouble("longitude"),
                ),
            )
        }.getOrNull()
    }
}
