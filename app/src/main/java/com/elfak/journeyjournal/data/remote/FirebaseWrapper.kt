package com.elfak.journeyjournal.data.remote

import android.net.Uri
import com.elfak.journeyjournal.data.models.RegisterObject
import com.elfak.journeyjournal.data.models.UserModel
import com.elfak.journeyjournal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object FirebaseWrapper {

    private val fAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val database = Firebase.database.reference
    private val userCollection = database.child("users")
    private val fStorage = Firebase.storage.reference

    fun getCurrentUser(): FirebaseUser? {
        return fAuth.currentUser
    }

    fun isLoggedIn(): Boolean {
        return fAuth.currentUser != null
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess.invoke()
            } else {
                onError.invoke(task.exception?.localizedMessage ?: Constants.UNKNOWN_ERROR)
            }
        }
    }

    fun logout(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            fAuth.signOut()
            onSuccess.invoke()
        } catch (e: Exception) {
            onError.invoke(e.localizedMessage ?: Constants.UNKNOWN_ERROR)
        }
    }

    fun signup(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess.invoke(fAuth.currentUser?.uid ?: "")
            } else {
                onError.invoke(task.exception?.localizedMessage ?: Constants.UNKNOWN_ERROR)
            }
        }
    }

    fun resetPassword(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        fAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess.invoke()
            } else {
                onError.invoke(task.exception?.localizedMessage ?: Constants.UNKNOWN_ERROR)
            }
        }
    }

    suspend fun getUser(): UserModel? {
        val userId = fAuth.currentUser?.uid

        if (userId != null) {

            return suspendCancellableCoroutine { continuation ->
                userCollection.child(userId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue(UserModel::class.java)
                            continuation.resume(user)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resumeWithException(error.toException())
                        }

                    })
            }
        } else {
            throw Exception("User not logged in")
        }
    }

    suspend fun saveUser(user: RegisterObject): Boolean {

        val userId = fAuth.currentUser?.uid

        if (userId != null) {

            return suspendCancellableCoroutine { continuation ->
                database.child("users").child(userId).setValue(user)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(true)
                        } else {
                            task.exception?.let {
                                continuation.resumeWithException(it)
                            } ?: continuation.resume(false)
                        }
                    }
            }
        } else {
            throw Exception("User not logged in")
        }
    }

    suspend fun updateUserPicture(
        imageURI: Uri
    ): Unit = suspendCancellableCoroutine { continuation ->

        val userId = fAuth.currentUser?.uid

        if (userId != null) {
            val storageRef = fStorage.child("profilePictures/$userId")

            storageRef.delete()

            val uploadTask = storageRef.putFile(imageURI)

            uploadTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    storageRef.downloadUrl.addOnCompleteListener { urlTask ->
                        if (urlTask.isSuccessful) {
                            val downloadUrl = urlTask.result.toString()

                            val userRef = userCollection.child(userId)

                            userRef.child("imageURL").setValue(downloadUrl).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    continuation.resume(Unit)
                                } else {
                                    continuation.resumeWithException(
                                        it.exception ?: Exception("Failed to update user data")
                                    )
                                }
                            }
                        } else {
                            continuation.resumeWithException(
                                urlTask.exception ?: Exception("Failed to get download URL")
                            )
                        }
                    }
                } else {
                    continuation.resumeWithException(
                        task.exception ?: Exception("Failed to upload image")
                    )
                }
            }

            continuation.invokeOnCancellation {
                uploadTask.cancel()
            }

        } else {
            throw Exception("User not logged in")
        }
    }

}