package com.elfak.journeyjournal.data.repo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.elfak.journeyjournal.util.Permissions
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for the device position (requirement 2). The foreground service pushes
 * updates here; every screen observes the same flow.
 */
class LocationRepository(private val context: Context) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    private val _location = MutableStateFlow<LatLng?>(null)
    val location: StateFlow<LatLng?> = _location.asStateFlow()

    private val _lastUpdate = MutableStateFlow(0L)
    val lastUpdate: StateFlow<Long> = _lastUpdate.asStateFlow()

    private val _tracking = MutableStateFlow(false)
    val tracking: StateFlow<Boolean> = _tracking.asStateFlow()

    fun publish(location: Location) {
        _location.value = LatLng(location.latitude, location.longitude)
        _lastUpdate.value = System.currentTimeMillis()
    }

    fun setTracking(tracking: Boolean) {
        _tracking.value = tracking
    }

    /** One-shot fix, used when the service is not running (e.g. right after login). */
    @SuppressLint("MissingPermission")
    suspend fun refreshOnce(): LatLng? {
        if (!Permissions.hasLocation(context)) return null
        val current = runCatching {
            client.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token,
            ).await()
        }.getOrNull() ?: runCatching { client.lastLocation.await() }.getOrNull()

        return current?.let {
            publish(it)
            _location.value
        }
    }
}
