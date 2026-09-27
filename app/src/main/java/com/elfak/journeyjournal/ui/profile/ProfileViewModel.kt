package com.elfak.journeyjournal.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.data.model.AppUser
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.di.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel : ViewModel() {

    private val auth = ServiceLocator.authRepository
    private val users = ServiceLocator.userRepository
    private val places = ServiceLocator.placeRepository
    private val locations = ServiceLocator.locationRepository

    private val uid = MutableStateFlow(ServiceLocator.auth.currentUser?.uid)

    val user: StateFlow<AppUser?> = uid.filterNotNull()
        .flatMapLatest { users.observeUser(it) }
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val myPlaces: StateFlow<List<Place>> = places.observePlaces()
        .map { list -> list.filter { it.authorId == uid.value } }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tracking: StateFlow<Boolean> = locations.tracking
    val location = locations.location

    fun logout() {
        val current = uid.value
        viewModelScope.launch {
            if (current != null) runCatching { users.setSharingLocation(current, false) }
            auth.logout()
        }
    }
}
