package com.elfak.journeyjournal.data.repo

import com.elfak.journeyjournal.data.model.AppUser
import com.elfak.journeyjournal.util.snapshots
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserRepository(private val db: FirebaseFirestore) {

    private val users = db.collection(COLLECTION)

    /** All users ordered by points - feeds the leaderboard (requirement 5). */
    fun observeUsers(): Flow<List<AppUser>> =
        users.orderBy("points", Query.Direction.DESCENDING).snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull(AppUser::from) }

    fun observeUser(uid: String): Flow<AppUser?> =
        users.document(uid).snapshots().map(AppUser::from)

    suspend fun get(uid: String): AppUser? = AppUser.from(users.document(uid).get().await())

    suspend fun publishLocation(uid: String, lat: Double, lng: Double) {
        users.document(uid).update(
            mapOf(
                "lastLat" to lat,
                "lastLng" to lng,
                "lastSeen" to Timestamp.now(),
                "sharingLocation" to true,
            )
        ).await()
    }

    suspend fun setSharingLocation(uid: String, sharing: Boolean) {
        users.document(uid).update("sharingLocation", sharing).await()
    }

    companion object {
        const val COLLECTION = "users"
    }
}
