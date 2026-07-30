package com.sajdatime.app.data

import com.sajdatime.core.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Manual city lookup, used only when the user declines location permission.
 *
 * This is the single feature in SajdaTime that touches the network, and it sends only
 * the city name the user typed — no device identifier, no coordinates, no history. The
 * UI states this plainly before the request is made. Once resolved, the coordinates are
 * stored locally and every subsequent calculation is offline.
 *
 * ponytail: HttpURLConnection + org.json, both in the platform. A charity app does not
 * need Retrofit/OkHttp/Moshi for one GET request.
 */
class CityLookup {

    data class Result(val city: String, val coordinates: Coordinates)

    /** Resolves a free-text place name to coordinates. Returns null if not found. */
    suspend fun search(query: String): Result? = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext null
        val encoded = URLEncoder.encode(query.trim(), "UTF-8")
        val url = URL("https://api.aladhan.com/v1/timingsByAddress?address=$encoded")

        val body = runCatching { url.readTextWithTimeout() }.getOrNull() ?: return@withContext null

        runCatching {
            val meta = JSONObject(body)
                .getJSONObject("data")
                .getJSONObject("meta")
            val latitude = meta.getDouble("latitude")
            val longitude = meta.getDouble("longitude")
            Result(
                city = query.trim(),
                coordinates = Coordinates(latitude, longitude),
            )
        }.getOrNull()
    }

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

    private companion object {
        const val TIMEOUT_MS = 12_000
    }
}
