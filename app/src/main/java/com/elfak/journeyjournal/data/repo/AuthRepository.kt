package com.elfak.journeyjournal.data.repo

import android.net.Uri
import android.util.Log
import com.elfak.journeyjournal.data.model.AppUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Thrown when the account was created but the profile photo could not be uploaded. */
class PhotoUploadWarning(message: String) : Exception(message)

/**
 * Registration / login (requirement 1).
 *
 * The specification asks for a username instead of an e-mail, so the username is mapped to a
 * stable synthetic e-mail address for Firebase Authentication. Auth already guarantees that an
 * e-mail is unique, which makes usernames unique as well.
 */
class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val photos: PhotoRepository,
) {

    val currentUid: String? get() = auth.currentUser?.uid

    fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(username: String, password: String) {
        auth.signInWithEmailAndPassword(emailFor(username), password).await()
    }

    suspend fun register(
        username: String,
        password: String,
        fullName: String,
        phone: String,
        photoUri: Uri,
    ) {
        val result = auth.createUserWithEmailAndPassword(emailFor(username), password).await()
        val uid = requireNotNull(result.user).uid

        var warning: String? = null
        val photoUrl = try {
            photos.upload(photoUri, "profile_photos/$uid.jpg")
        } catch (e: Exception) {
            Log.w(TAG, "Profile photo upload failed", e)
            warning = "Nalog je kreiran, ali slanje fotografije nije uspelo: ${e.message}"
            ""
        }

        val user = AppUser(
            username = username.trim(),
            fullName = fullName.trim(),
            phone = phone.trim(),
            photoUrl = photoUrl,
            points = 0,
            createdAt = Timestamp.now(),
        )
        db.collection(UserRepository.COLLECTION).document(uid).set(user).await()

        warning?.let { throw PhotoUploadWarning(it) }
    }

    fun logout() = auth.signOut()

    private fun emailFor(username: String) = "${username.trim().lowercase()}@$EMAIL_DOMAIN"

    companion object {
        private const val TAG = "AuthRepository"
        const val EMAIL_DOMAIN = "journeyjournal.app"
        val USERNAME_REGEX = Regex("^[a-zA-Z0-9._-]{3,20}$")
    }
}
