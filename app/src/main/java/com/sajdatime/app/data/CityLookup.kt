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
 *  1. The phone's own geocoder, which most phones have. Android documents it as possibly
 *     network backed, and on phones with Google Play services Google answers it, so the
 *     typed name may reach Google. Until 19 Oct 2026 this line said nothing left the app,
 *     the same false claim found on 16 Oct 2026 and corrected in docs/privacy.html.
 *  2. Open-Meteo's free geocoding API, for phones with no geocoder backend.
 *
 * Open-Meteo is sent the place name and the app's language, and only when the first source
 * comes up empty. No device identifier, no coordinates, no history. Once resolved the
 * coordinates are stored locally and every prayer calculation from then on is offline.
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
            // Wrapped: below Android 13 a language picked in the app lives only in AppLocale.wrap
            // (19 Oct 2026); the bare application context would answer with the phone's language.
            Geocoder(context, AppLocale.of(AppLocale.wrap(context)))
                .getFromLocationName(query, 1)
                ?.firstOrNull()
                ?.let { address ->
                    // Same range check as every other pair that enters the app from outside.
                    Coordinates.orNull(address.latitude, address.longitude)?.let { coordinates ->
                        Result(city = address.describeAddress(query).capped(), coordinates = coordinates)
                    }
                }
        }.getOrNull()
    }

    private fun fromOpenMeteo(query: String): Result? {
        val encoded = URLEncoder.encode(query, "UTF-8")
        // language= was hardcoded to "en" while the platform geocoder above used the
        // device locale, so the same search returned "Cairo" or "القاهرة" depending only
        // on which of the two happened to answer. Both follow AppLocale now.
        // Wrapped: below Android 13 a language picked in the app lives only in AppLocale.wrap
        // (19 Oct 2026); the bare application context would answer with the phone's language.
        val language = AppLocale.of(AppLocale.wrap(context)).language
        val url = URL(
            "https://geocoding-api.open-meteo.com/v1/search" +
                "?name=$encoded&count=1&language=$language&format=json",
        )

        val body = runCatching { url.readTextWithTimeout() }.getOrNull() ?: return null
        return parseOpenMeteo(body, query)
    }

    /** A readable label, falling back to what the user typed rather than showing blank. */
    private fun Address.describeAddress(fallback: String): String = placeLabel(fallback)

    private fun URL.readTextWithTimeout(): String {
        val connection = openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.requestMethod = "GET"
            // One fixed HTTPS host. A redirect is not expected, so one is treated as a
            // failure rather than followed to somewhere the app never chose.
            connection.instanceFollowRedirects = false
            // A plain name instead of Android's default User-Agent, which carries the phone model and
            // Android version ("Dalvik/2.1.0 (Linux; U; Android 14; SM-S918B ...)"). privacy.html promises
            // Open-Meteo gets the place name and the app's language and no device information
            // (independent security review, 17 Oct 2026, HANDOVER §5.24).
            connection.setRequestProperty("User-Agent", "SajdaTime")
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("HTTP ${connection.responseCode}")
            }
            connection.inputStream.bufferedReader().use { readCapped(it, MAX_BODY_CHARS) }
        } finally {
            connection.disconnect()
        }
    }

    internal companion object {
        private const val TIMEOUT_MS = 12_000

        /**
         * A `count=1` answer is well under a kilobyte. Anything past this is not a city, and
         * reading it all into memory is the only thing an unbounded response could do to us.
         */
        internal const val MAX_BODY_CHARS = 64 * 1024

        /** A place name longer than this is not a place name. Also bounds the PDF masthead. */
        internal const val MAX_CITY_CHARS = 80

        internal fun String.capped(): String = take(MAX_CITY_CHARS)

        /** Reads at most [max] characters, and fails (rather than truncates) on more. */
        internal fun readCapped(reader: java.io.Reader, max: Int): String {
            val out = StringBuilder()
            val buffer = CharArray(4096)
            while (true) {
                val read = reader.read(buffer)
                if (read < 0) break
                out.append(buffer, 0, read)
                if (out.length > max) throw IOException("response too large")
            }
            return out.toString()
        }

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
                ).joinToString(", ").ifBlank { fallbackName }.capped(),
                // getDouble throws when absent, which is what we want: no coordinates means
                // no result, never a silent fallback to zero, zero. orNull then rejects a
                // pair outside -90..90 and -180..180 (and NaN) like every other writer the
                // app does not control, so a bad answer is a miss, not a stored location.
                coordinates = Coordinates.orNull(
                    first.getDouble("latitude"),
                    first.getDouble("longitude"),
                ) ?: return null,
            )
        }.getOrNull()
    }
}
