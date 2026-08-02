package com.sajdatime.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import com.sajdatime.core.AppLocale
import com.sajdatime.core.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Approximate location, read on demand while the app is in the foreground.
 *
 * ponytail: platform LocationManager rather than Play Services FusedLocationProvider.
 * One fewer proprietary dependency, works on de-Googled devices, and coarse accuracy is
 * all prayer calculation needs (a few km shifts times by well under a minute).
 */
class LocationRepository(private val context: Context) {

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Returns the device's current approximate position, or null if permission is
     * missing, location is switched off, or no fix arrives within [timeoutMs].
     */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(timeoutMs: Long = 10_000): Coordinates? {
        if (!hasPermission()) return null
        val manager = ContextCompat.getSystemService(context, LocationManager::class.java)
            ?: return null

        lastKnown(manager)?.let { return it.toCoordinates() }

        // No cached fix, so ask for a single one. Never a continuous subscription.
        val fresh = withTimeoutOrNull(timeoutMs) { requestSingleFix(manager) }
        return fresh?.toCoordinates()
    }

    /**
     * Best-effort city name for display. Purely cosmetic — an empty result never blocks
     * prayer calculation, which only needs the coordinates.
     */
    suspend fun cityName(coordinates: Coordinates): String = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext ""
        runCatching {
            @Suppress("DEPRECATION")
            // AppLocale rather than the device locale, so the city name is in the same
            // language as everything around it. On an Arabic phone the device locale gave
            // "سلاو، المملكة المتحدة" as the only non-English text on an English screen,
            // and the Open-Meteo fallback in CityLookup already asks for English, so the
            // two halves of the same feature disagreed. Reversible: if translations land
            // and someone would rather see their town in their own script regardless of
            // app language, this is the line to change.
            Geocoder(context, AppLocale.of(context))
                .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                ?.firstOrNull()
                // Shared with CityLookup so a place found automatically and the same place
                // found by searching are named identically. See PlaceName.kt for why the
                // old chain showed a tester his county.
                ?.placeLabel()
                .orEmpty()
        }.getOrDefault("")
    }

    @SuppressLint("MissingPermission")
    private fun lastKnown(manager: LocationManager): Location? =
        listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .mapNotNull { provider ->
                runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
            }
            // Ignore stale fixes: a week-old position could be a different country.
            .filter { System.currentTimeMillis() - it.time < STALE_FIX_MS }
            .maxByOrNull { it.time }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(manager: LocationManager): Location? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                    LocationManager.NETWORK_PROVIDER
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                    LocationManager.GPS_PROVIDER
                else -> null
            }
            if (provider == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val signal = CancellationSignal()
                continuation.invokeOnCancellation { signal.cancel() }
                manager.getCurrentLocation(
                    provider,
                    signal,
                    context.mainExecutor,
                ) { location -> if (continuation.isActive) continuation.resume(location) }
            } else {
                @Suppress("DEPRECATION")
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        manager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(location)
                    }

                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) = Unit
                    override fun onProviderEnabled(p: String) = Unit
                    override fun onProviderDisabled(p: String) = Unit
                }
                continuation.invokeOnCancellation { manager.removeUpdates(listener) }
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(provider, listener, context.mainLooper)
            }
        }

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    private companion object {
        const val STALE_FIX_MS = 24L * 60 * 60 * 1000
    }
}
