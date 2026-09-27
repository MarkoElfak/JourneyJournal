package com.elfak.journeyjournal.di

import android.content.Context
import com.elfak.journeyjournal.data.model.PlaceFilter
import com.elfak.journeyjournal.data.repo.AuthRepository
import com.elfak.journeyjournal.data.repo.LocationRepository
import com.elfak.journeyjournal.data.repo.PhotoRepository
import com.elfak.journeyjournal.data.repo.PlaceRepository
import com.elfak.journeyjournal.data.repo.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Minimal manual dependency container - keeps the project free of annotation processors while
 * still giving every ViewModel a single shared instance of each repository.
 */
object ServiceLocator {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * Lives as long as the process. Used for work that must not be cancelled when the screen that
     * started it goes away - registration is the important case: signing in immediately swaps the
     * auth screen for the main screen, which would cancel a ViewModel-scoped upload.
     */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val photoRepository: PhotoRepository by lazy { PhotoRepository(appContext) }
    val authRepository: AuthRepository by lazy { AuthRepository(auth, firestore, photoRepository) }
    val userRepository: UserRepository by lazy { UserRepository(firestore) }
    val placeRepository: PlaceRepository by lazy {
        PlaceRepository(firestore, auth, userRepository, photoRepository)
    }
    val locationRepository: LocationRepository by lazy { LocationRepository(appContext) }

    /** Filter state is shared so that the map and the table always show the same selection. */
    val filter = MutableStateFlow(PlaceFilter())
}
