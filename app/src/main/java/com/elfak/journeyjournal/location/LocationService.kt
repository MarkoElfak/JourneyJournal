package com.elfak.journeyjournal.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import com.elfak.journeyjournal.JourneyApp
import com.elfak.journeyjournal.MainActivity
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.data.AppConfig
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.di.ServiceLocator
import com.elfak.journeyjournal.util.Permissions
import com.elfak.journeyjournal.util.distanceMeters
import com.elfak.journeyjournal.util.formatDistance
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Foreground service that keeps track of the user's position the whole time the app is running
 * (requirement 2), publishes it to Firestore so other users see it on the map, and notifies the
 * user when they come close to a registered place.
 */
class LocationService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locations = ServiceLocator.locationRepository
    private val users = ServiceLocator.userRepository
    private val places = ServiceLocator.placeRepository
    private val auth = ServiceLocator.auth

    private var knownPlaces: List<Place> = emptyList()
    private val notifiedPlaceIds = mutableSetOf<String>()
    private var lastUpload = 0L

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            locations.publish(location)
            uploadThrottled(location.latitude, location.longitude)
            notifyNearbyPlaces(LatLng(location.latitude, location.longitude))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stop()
                return START_NOT_STICKY
            }
            else -> start()
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun start() {
        if (!Permissions.hasLocation(this)) {
            Log.w(TAG, "Location permission missing - stopping service")
            stop()
            return
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, buildNotification(), type)
        locations.setTracking(true)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            AppConfig.LOCATION_INTERVAL_MS,
        ).setMinUpdateDistanceMeters(5f).build()
        client.requestLocationUpdates(request, callback, mainLooper)

        // Keep a local copy of all places so proximity can be evaluated on every fix.
        scope.launch {
            places.observePlaces()
                .catch { Log.w(TAG, "Places listener failed", it) }
                .collect { knownPlaces = it }
        }
    }

    private fun stop() {
        client.removeLocationUpdates(callback)
        locations.setTracking(false)
        auth.currentUser?.uid?.let { uid ->
            scope.launch { runCatching { users.setSharingLocation(uid, false) } }
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun uploadThrottled(lat: Double, lng: Double) {
        val now = System.currentTimeMillis()
        if (now - lastUpload < AppConfig.LOCATION_UPLOAD_INTERVAL_MS) return
        lastUpload = now
        val uid = auth.currentUser?.uid ?: return
        scope.launch {
            runCatching { users.publishLocation(uid, lat, lng) }
                .onFailure { Log.w(TAG, "Location upload failed", it) }
        }
    }

    private fun notifyNearbyPlaces(position: LatLng) {
        val uid = auth.currentUser?.uid
        val manager = NotificationManagerCompat.from(this)
        knownPlaces.forEach { place ->
            val distance = distanceMeters(position, place.position)
            when {
                distance <= AppConfig.NEARBY_RADIUS_METERS &&
                        place.id !in notifiedPlaceIds &&
                        place.authorId != uid -> {
                    notifiedPlaceIds += place.id
                    if (Permissions.granted(this, android.Manifest.permission.POST_NOTIFICATIONS) ||
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    ) {
                        manager.notify(place.id.hashCode(), buildNearbyNotification(place, distance))
                    }
                }
                // Allow a new notification once the user leaves and comes back.
                distance > AppConfig.NEARBY_RADIUS_METERS * 2 -> notifiedPlaceIds -= place.id
            }
        }
    }

    private fun contentIntent(placeId: String? = null): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            placeId?.let { putExtra(MainActivity.EXTRA_PLACE_ID, it) }
        }
        return PendingIntent.getActivity(
            this,
            placeId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, JourneyApp.CHANNEL_TRACKING)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Praćenje lokacije je uključeno")
            .setSmallIcon(R.drawable.ic_notification_place)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent())
            .build()

    private fun buildNearbyNotification(place: Place, distance: Double): Notification =
        NotificationCompat.Builder(this, JourneyApp.CHANNEL_NEARBY)
            .setContentTitle("U blizini: ${place.title}")
            .setContentText("${place.placeType.label} • ${formatDistance(distance)} od vas")
            .setSmallIcon(R.drawable.ic_notification_place)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent(place.id))
            .build()

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        locations.setTracking(false)
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.elfak.journeyjournal.START_TRACKING"
        const val ACTION_STOP = "com.elfak.journeyjournal.STOP_TRACKING"

        fun start(context: Context) {
            val intent = Intent(context, LocationService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.startService(Intent(context, LocationService::class.java).setAction(ACTION_STOP))
        }
    }
}
