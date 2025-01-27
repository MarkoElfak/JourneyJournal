package com.elfak.journeyjournal.data.remote

import com.elfak.journeyjournal.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseWrapper {

    private val fAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

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

}