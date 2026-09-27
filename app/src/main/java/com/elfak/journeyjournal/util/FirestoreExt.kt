package com.elfak.journeyjournal.util

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Live query results as a cold Flow; the listener is removed when collection stops. */
fun Query.snapshots(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener(MetadataChanges.EXCLUDE) { value, error ->
        when {
            error != null -> close(error)
            value != null -> trySend(value)
        }
    }
    awaitClose { registration.remove() }
}

fun DocumentReference.snapshots(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { value, error ->
        when {
            error != null -> close(error)
            value != null -> trySend(value)
        }
    }
    awaitClose { registration.remove() }
}
