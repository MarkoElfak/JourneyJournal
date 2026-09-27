package com.elfak.journeyjournal.data.repo

import android.net.Uri
import com.elfak.journeyjournal.data.AppConfig
import com.elfak.journeyjournal.data.Points
import com.elfak.journeyjournal.data.model.Comment
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.data.model.PlaceType
import com.elfak.journeyjournal.util.distanceMeters
import com.elfak.journeyjournal.util.snapshots
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Places ("objekti") and every interaction with them: creating, rating, commenting and
 * visiting. Every interaction also awards points to the acting user and refreshes the
 * `lastInteractionAt` field the filters use.
 */
class PlaceRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val users: UserRepository,
    private val photos: PhotoRepository,
) {

    private val places = db.collection(COLLECTION)

    private val uid: String get() = requireNotNull(auth.currentUser?.uid) { "Korisnik nije prijavljen." }

    fun observePlaces(): Flow<List<Place>> =
        places.orderBy("createdAt", Query.Direction.DESCENDING).snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull(Place::from) }

    fun observePlace(placeId: String): Flow<Place?> =
        places.document(placeId).snapshots().map(Place::from)

    suspend fun addPlace(
        title: String,
        description: String,
        type: PlaceType,
        lat: Double,
        lng: Double,
        photoUri: Uri?,
    ): String {
        val me = users.get(uid)
        val ref = places.document()
        val photoUrl = photoUri?.let { photos.upload(it, "place_photos/${ref.id}.jpg") }.orEmpty()
        val now = Timestamp.now()

        ref.set(
            Place(
                title = title.trim(),
                description = description.trim(),
                type = type.name,
                lat = lat,
                lng = lng,
                photoUrl = photoUrl,
                authorId = uid,
                authorUsername = me?.username.orEmpty(),
                createdAt = now,
                lastInteractionAt = now,
            )
        ).await()

        db.collection(UserRepository.COLLECTION).document(uid).update(
            mapOf(
                "points" to FieldValue.increment(Points.NEW_PLACE),
                "placesCount" to FieldValue.increment(1),
            )
        ).await()

        return ref.id
    }

    suspend fun deletePlace(placeId: String) {
        places.document(placeId).delete().await()
    }

    // ---------------------------------------------------------------- ratings

    fun observeMyRating(placeId: String): Flow<Int?> =
        places.document(placeId).collection(RATINGS).document(uid).snapshots()
            .map { it.getLong("value")?.toInt() }

    suspend fun ratePlace(placeId: String, value: Int) {
        require(value in 1..5) { "Ocena mora biti između 1 i 5." }
        val me = users.get(uid)
        val placeRef = places.document(placeId)
        val ratingRef = placeRef.collection(RATINGS).document(uid)
        val userRef = db.collection(UserRepository.COLLECTION).document(uid)

        db.runTransaction { tx ->
            val existing = tx.get(ratingRef)
            val place = tx.get(placeRef)
            val previous = existing.getLong("value")?.toInt()
            val authorId = place.getString("authorId")

            val sumDelta = value - (previous ?: 0)
            val countDelta = if (previous == null) 1L else 0L

            tx.set(
                ratingRef,
                mapOf(
                    "username" to me?.username.orEmpty(),
                    "value" to value,
                    "createdAt" to Timestamp.now(),
                )
            )
            tx.update(
                placeRef,
                mapOf(
                    "ratingSum" to FieldValue.increment(sumDelta.toLong()),
                    "ratingCount" to FieldValue.increment(countDelta),
                    "lastInteractionAt" to Timestamp.now(),
                )
            )
            // Points only for the first rating, and never for rating your own place.
            if (previous == null && authorId != uid) {
                tx.update(userRef, "points", FieldValue.increment(Points.RATING))
            }
        }.await()
    }

    // --------------------------------------------------------------- comments

    fun observeComments(placeId: String): Flow<List<Comment>> =
        places.document(placeId).collection(COMMENTS)
            .orderBy("createdAt", Query.Direction.DESCENDING).snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull(Comment::from) }

    suspend fun addComment(placeId: String, text: String) {
        require(text.isNotBlank()) { "Komentar ne može biti prazan." }
        val me = users.get(uid)
        val placeRef = places.document(placeId)
        val commentRef = placeRef.collection(COMMENTS).document()
        val userRef = db.collection(UserRepository.COLLECTION).document(uid)

        val place = placeRef.get().await()
        val batch = db.batch()
        batch.set(
            commentRef,
            Comment(
                placeId = placeId,
                userId = uid,
                username = me?.username.orEmpty(),
                userPhotoUrl = me?.photoUrl.orEmpty(),
                text = text.trim(),
                createdAt = Timestamp.now(),
            )
        )
        batch.update(
            placeRef,
            mapOf(
                "commentCount" to FieldValue.increment(1),
                "lastInteractionAt" to Timestamp.now(),
            )
        )
        if (place.getString("authorId") != uid) {
            batch.update(userRef, "points", FieldValue.increment(Points.COMMENT))
        }
        batch.commit().await()
    }

    suspend fun deleteComment(placeId: String, commentId: String) {
        val placeRef = places.document(placeId)
        val batch = db.batch()
        batch.delete(placeRef.collection(COMMENTS).document(commentId))
        batch.update(placeRef, "commentCount", FieldValue.increment(-1))
        batch.commit().await()
    }

    // ----------------------------------------------------------------- visits

    fun observeVisited(placeId: String): Flow<Boolean> =
        places.document(placeId).collection(VISITS).document(uid).snapshots()
            .map { it.exists() }

    /** Check in at a place the user is physically standing next to. */
    suspend fun visitPlace(placeId: String, userLat: Double, userLng: Double) {
        val placeRef = places.document(placeId)
        val visitRef = placeRef.collection(VISITS).document(uid)
        val userRef = db.collection(UserRepository.COLLECTION).document(uid)
        val me = users.get(uid)

        // Pre-check so the user gets a clear message without paying for a transaction round trip.
        val snapshot = placeRef.get().await()
        val preDistance = distanceMeters(
            userLat, userLng,
            snapshot.getDouble("lat") ?: 0.0,
            snapshot.getDouble("lng") ?: 0.0,
        )
        check(preDistance <= AppConfig.VISIT_RADIUS_METERS) {
            "Previše ste daleko od lokacije (${preDistance.toInt()} m). Priđite bliže."
        }

        db.runTransaction { tx ->
            val place = tx.get(placeRef)
            val visit = tx.get(visitRef)
            val lat = place.getDouble("lat") ?: 0.0
            val lng = place.getDouble("lng") ?: 0.0
            val distance = distanceMeters(userLat, userLng, lat, lng)
            if (distance > AppConfig.VISIT_RADIUS_METERS) {
                throw IllegalStateException("Previše ste daleko od lokacije (${distance.toInt()} m).")
            }
            if (visit.exists()) {
                throw IllegalStateException("Ovu lokaciju ste već obišli.")
            }

            tx.set(
                visitRef,
                mapOf(
                    "username" to me?.username.orEmpty(),
                    "createdAt" to Timestamp.now(),
                )
            )
            tx.update(
                placeRef,
                mapOf(
                    "visitCount" to FieldValue.increment(1),
                    "lastInteractionAt" to Timestamp.now(),
                )
            )
            if (place.getString("authorId") != uid) {
                tx.update(userRef, "points", FieldValue.increment(Points.VISIT))
            }
        }.await()
    }

    companion object {
        const val COLLECTION = "places"
        const val RATINGS = "ratings"
        const val COMMENTS = "comments"
        const val VISITS = "visits"
    }
}
