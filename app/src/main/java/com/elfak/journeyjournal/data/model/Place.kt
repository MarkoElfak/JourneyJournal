package com.elfak.journeyjournal.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

/**
 * A place registered by a user ("objekat" from the specification): a location on the map
 * plus attributes - title, description, type, photo, rating, comments.
 */
data class Place(
    @get:Exclude val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = PlaceType.OSTALO.name,
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val photoUrl: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val createdAt: Timestamp? = null,
    /** Updated on every interaction (rating, comment, visit) - used by the date filter. */
    val lastInteractionAt: Timestamp? = null,
    val ratingSum: Long = 0,
    val ratingCount: Long = 0,
    val commentCount: Long = 0,
    val visitCount: Long = 0,
) {
    @get:Exclude
    val placeType: PlaceType get() = PlaceType.fromName(type)

    @get:Exclude
    val position: LatLng get() = LatLng(lat, lng)

    @get:Exclude
    val averageRating: Double
        get() = if (ratingCount == 0L) 0.0 else ratingSum.toDouble() / ratingCount

    companion object {
        fun from(doc: DocumentSnapshot): Place? =
            doc.toObject(Place::class.java)?.copy(id = doc.id)
    }
}
