package com.elfak.journeyjournal.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

/** Distance in meters between two points, using the platform (WGS84) implementation. */
fun distanceMeters(from: LatLng, to: LatLng): Double {
    val result = FloatArray(1)
    Location.distanceBetween(from.latitude, from.longitude, to.latitude, to.longitude, result)
    return result[0].toDouble()
}

fun distanceMeters(fromLat: Double, fromLng: Double, toLat: Double, toLng: Double): Double =
    distanceMeters(LatLng(fromLat, fromLng), LatLng(toLat, toLng))

/** "350 m" / "1.4 km" */
fun formatDistance(meters: Double): String = when {
    meters < 1000 -> "${meters.roundToInt()} m"
    else -> String.format("%.1f km", meters / 1000.0)
}
