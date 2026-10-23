package com.sajdatime.wear

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sajdatime.core.Coordinates
import com.sajdatime.core.DayPrayerTimes
import com.sajdatime.core.Madhab
import com.sajdatime.core.NextPrayer
import com.sajdatime.core.PrayerEngine
import com.sajdatime.core.QiblaEngine
import com.sajdatime.core.Sect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WearUiState(
    val settings: WearSettings = WearSettings(),
    val today: DayPrayerTimes? = null,
    val next: NextPrayer? = null,
    val now: Instant = Instant.now(),
    val qiblaBearing: Double? = null,
    val heading: Double? = null,
    val needsLocation: Boolean = false,
    /** A fix has been asked for and has not arrived yet, and there is no place to show meanwhile. */
    val locating: Boolean = false,
    /** A fix from this watch is on its way. Shown on the Location screen, which has a place to show. */
    val findingWatchFix: Boolean = false,
    /**
     * The watch's own location was wanted and could not be had: not allowed, switched off, or no fix.
     * The Location screen says so under "This watch", the one place it can be acted on.
     */
    val watchLocationUnavailable: Boolean = false,
)

class WearViewModel(application: Application) : AndroidViewModel(application) {

    private val store = WearSettingsStore(application)
    private val _state = MutableStateFlow(WearUiState())
    val state: StateFlow<WearUiState> = _state.asStateFlow()

    private var compassJob: Job? = null

    /** In-flight single-shot location request, cancelled if another is asked for. */
    private var currentFix: CancellationSignal? = null

    init {
        viewModelScope.launch {
            store.settings.collect { settings ->
                _state.update { it.copy(settings = settings) }
                recalculate()
            }
        }
        viewModelScope.launch {
            while (true) {
                val now = Instant.now()
                _state.update { it.copy(now = now) }
                val snapshot = _state.value
                val prayerPassed = snapshot.next?.let { !now.isBefore(it.at) } == true
                // The day rolls over between midnight and Fajr, when no prayer passes and
                // so nothing else here fires. Same bug as the phone, same fix — see
                // SajdaViewModel.startClock for what it looked like on a running device.
                val dayChanged = snapshot.today
                    ?.let { it.date != LocalDate.now(ZoneId.systemDefault()) } == true
                if (prayerPassed || dayChanged) recalculate()
                delay(1_000)
            }
        }
        refreshLocation()
    }

    /**
     * Watches have their own location, either onboard GPS or a fix handed over by the
     * paired phone. Either way it arrives through the platform LocationManager, so the
     * watch does not depend on the phone app being open.
     */
    fun refreshLocation() {
        // Read from the store, not from the state: on launch the state still holds the defaults,
        // which would ask the hardware for a fix the stored choice is about to throw away.
        viewModelScope.launch {
            val settings = store.settings.first()
            if (acceptsWatchFix(settings.locationSource, isRealPlace(settings.phoneCoordinates))) {
                requestWatchFix()
            } else {
                // The place comes from the phone or is Makkah: no GPS, no battery, nothing to wait for.
                currentFix?.cancel()
                currentFix = null
                _state.update {
                    it.copy(
                        locating = false,
                        findingWatchFix = false,
                        watchLocationUnavailable = false,
                        needsLocation = settings.coordinates == null,
                    )
                }
            }
        }
    }

    /** True if location access is allowed. The Location screen asks for it before choosing this watch. */
    fun watchLocationAllowed(): Boolean = ContextCompat.checkSelfPermission(
        getApplication(),
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun requestWatchFix() {
        val context = getApplication<Application>()

        if (!watchLocationAllowed()) {
            // A synced location from the phone still works, so this is only a problem
            // when the watch has neither.
            unavailable()
            return
        }

        val manager = ContextCompat.getSystemService(context, LocationManager::class.java) ?: return

        val cached: Location? = PROVIDERS
            .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }

        if (cached != null) {
            // An earlier request still running could otherwise answer null later and report the
            // watch's location as unavailable after this one was adopted.
            currentFix?.cancel()
            currentFix = null
            adopt(cached)
            return
        }

        // A watch that has never held a fix has no last known location at all, so asking
        // for the cache alone leaves it stuck on the setup prompt forever. Ask the
        // hardware for one fix. Nothing is streamed and no listener is left registered.
        val provider = PROVIDERS.firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
        if (provider == null) {
            unavailable()
            return
        }

        _state.update {
            it.copy(
                locating = it.settings.coordinates == null,
                findingWatchFix = true,
                watchLocationUnavailable = false,
            )
        }
        currentFix?.cancel()
        val signal = CancellationSignal()
        currentFix = signal

        runCatching {
            manager.getCurrentLocation(
                provider,
                signal,
                context.mainExecutor,
            ) { location ->
                currentFix = null
                if (location != null) {
                    adopt(location)
                } else {
                    unavailable()
                }
            }
        }.onFailure {
            currentFix = null
            unavailable()
        }
    }

    private fun unavailable() {
        _state.update {
            it.copy(
                locating = false,
                findingWatchFix = false,
                watchLocationUnavailable = true,
                needsLocation = it.settings.coordinates == null,
            )
        }
    }

    private fun adopt(fix: Location) {
        _state.update {
            // needsLocation false at once, as before 1.5.0: otherwise "No location yet", with its
            // tappable Use Makkah, flashes up between this and the store's write being read back.
            it.copy(
                locating = false,
                findingWatchFix = false,
                watchLocationUnavailable = false,
                needsLocation = false,
            )
        }
        viewModelScope.launch {
            // The store decides, against what is stored now. If the phone's place or a choice
            // arrived while the fix was on its way, the fix is dropped and the times stay put.
            store.adoptWatchFix(Coordinates(fix.latitude, fix.longitude))
        }
    }

    /**
     * Last resort, mirroring the phone: rather than leaving someone staring at a setup
     * prompt they cannot satisfy, fall back to Makkah so the watch still shows something.
     */
    fun useDefaultLocation() {
        currentFix?.cancel()
        currentFix = null
        _state.update { it.copy(locating = false, findingWatchFix = false, needsLocation = false) }
        viewModelScope.launch { store.useMakkahUntilLocated() }
    }

    /**
     * The wearer's choice on the Location screen. Choosing this watch asks for a fresh fix now, so
     * the choice shows its result while the screen is still open; the other two need no hardware.
     */
    fun chooseLocation(source: LocationSource) {
        viewModelScope.launch {
            store.chooseLocation(source)
            refreshLocation()
        }
    }

    /**
     * School of thought, set on the wrist.
     *
     * The watch is declared standalone, so it runs on whatever it has locally. Until the
     * phone's settings reach it over the Data Layer — and on a watch whose owner never
     * installs the phone app at all, that is never — it falls back to its own defaults,
     * which are Sunni and Shafi'i. For a Hanafi user that puts Asr out by more than an
     * hour with nothing on screen to suggest anything is wrong. Being able to say so on
     * the watch itself is the only fix that does not depend on a pairing that may not
     * exist. The store already recalculates on change, so nothing else has to happen here.
     */
    fun setSect(sect: Sect) {
        viewModelScope.launch { store.setSect(sect) }
    }

    fun setMadhab(madhab: Madhab) {
        viewModelScope.launch { store.setMadhab(madhab) }
    }

    fun setQiblaVisible(visible: Boolean) {
        compassJob?.cancel()
        compassJob = null
        if (!visible) {
            _state.update { it.copy(heading = null) }
            return
        }

        val coordinates = _state.value.settings.coordinates ?: return
        val context = getApplication<Application>()
        val manager = ContextCompat.getSystemService(context, SensorManager::class.java) ?: return
        val rotationVector = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) ?: return

        val declination = runCatching {
            GeomagneticField(
                coordinates.latitude.toFloat(),
                coordinates.longitude.toFloat(),
                0f,
                System.currentTimeMillis(),
            ).declination.toDouble()
        }.getOrDefault(0.0)

        val rotation = FloatArray(9)
        val orientation = FloatArray(3)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotation, event.values)
                SensorManager.getOrientation(rotation, orientation)
                val azimuth = Math.toDegrees(orientation[0].toDouble())
                _state.update {
                    it.copy(heading = QiblaEngine.normalise(azimuth + declination))
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        manager.registerListener(listener, rotationVector, SensorManager.SENSOR_DELAY_UI)
        compassJob = viewModelScope.launch {
            try {
                while (true) delay(60_000)
            } finally {
                manager.unregisterListener(listener)
            }
        }
    }

    private suspend fun recalculate() {
        val settings = _state.value.settings
        val coordinates = settings.coordinates ?: run {
            _state.update { it.copy(today = null, next = null, needsLocation = true) }
            return
        }

        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val computed = withContext(Dispatchers.Default) {
            PrayerEngine.compute(coordinates, LocalDate.now(zone), settings.calculationPrefs) to
                PrayerEngine.nextPrayer(coordinates, settings.calculationPrefs, now, zone)
        }

        _state.update {
            it.copy(
                today = computed.first,
                next = computed.second,
                now = now,
                needsLocation = false,
                qiblaBearing = QiblaEngine.bearingToKaaba(coordinates),
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        compassJob?.cancel()
        currentFix?.cancel()
    }

    private companion object {
        /** Cheapest and most likely to have a fix first. */
        val PROVIDERS = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        )
    }
}
