package com.elfak.journeyjournal.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

/** A registered user; document id == Firebase Auth uid. */
data class AppUser(
    @get:Exclude val uid: String = "",
    val username: String = "",
    val fullName: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val points: Long = 0,
    val placesCount: Long = 0,
    val createdAt: Timestamp? = null,
    // Last known position, published while location tracking is on.
    val lastLat: Double? = null,
    val lastLng: Double? = null,
    val lastSeen: Timestamp? = null,
    val sharingLocation: Boolean = false,
) {
    @get:Exclude
    val rank: Rank get() = Rank.of(points)

    companion object {
        fun from(doc: DocumentSnapshot): AppUser? =
            doc.toObject(AppUser::class.java)?.copy(uid = doc.id)
    }
}

/** Rank titles awarded for collected points (requirement 5). */
enum class Rank(val title: String, val minPoints: Long) {
    POCETNIK("Početnik", 0),
    PUTNIK("Putnik", 50),
    ISTRAZIVAC("Istraživač", 150),
    AVANTURISTA("Avanturista", 300),
    LEGENDA("Legenda", 600);

    companion object {
        fun of(points: Long): Rank = entries.last { points >= it.minPoints }

        /** Points still needed for the next rank, or null when already at the top. */
        fun pointsToNext(points: Long): Long? =
            entries.firstOrNull { it.minPoints > points }?.let { it.minPoints - points }
    }
}
