package com.elfak.journeyjournal.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Exclude

data class Comment(
    @get:Exclude val id: String = "",
    val placeId: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null,
) {
    companion object {
        fun from(doc: DocumentSnapshot): Comment? =
            doc.toObject(Comment::class.java)?.copy(id = doc.id)
    }
}

/** One user's rating of one place; document id == user id, so a user rates a place once. */
data class Rating(
    @get:Exclude val userId: String = "",
    val username: String = "",
    val value: Int = 0,
    val createdAt: Timestamp? = null,
) {
    companion object {
        fun from(doc: DocumentSnapshot): Rating? =
            doc.toObject(Rating::class.java)?.copy(userId = doc.id)
    }
}
