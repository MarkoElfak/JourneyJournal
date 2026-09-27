package com.elfak.journeyjournal.ui.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.data.model.AppUser
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.data.model.PlaceFilter
import com.elfak.journeyjournal.data.model.applyFilter
import com.elfak.journeyjournal.di.ServiceLocator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared source of places for the map and the table. The filter lives in [ServiceLocator] so both
 * views always show the same selection (requirements 3 and 4).
 */
class PlacesViewModel : ViewModel() {

    private val places = ServiceLocator.placeRepository
    private val users = ServiceLocator.userRepository
    private val locations = ServiceLocator.locationRepository

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val currentUid: String? get() = ServiceLocator.auth.currentUser?.uid

    val filter: StateFlow<PlaceFilter> = ServiceLocator.filter.asStateFlow()

    val location: StateFlow<LatLng?> = locations.location

    val tracking: StateFlow<Boolean> = locations.tracking

    val allPlaces: StateFlow<List<Place>> = places.observePlaces()
        .catch { error ->
            _error.value = error.message
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allUsers: StateFlow<List<AppUser>> = users.observeUsers()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Places after search, attribute, date and radius filtering. */
    val visiblePlaces: StateFlow<List<Place>> =
        combine(allPlaces, ServiceLocator.filter, location) { places, filter, location ->
            places.applyFilter(filter, location)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Distinct authors, used by the "filtriraj po autoru" dropdown. */
    val authors: StateFlow<List<String>> = allPlaces
        .map { places ->
            places.map { it.authorUsername }.filter { it.isNotBlank() }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(query: String) = ServiceLocator.filter.update { it.copy(query = query) }

    fun applyFilter(filter: PlaceFilter) {
        ServiceLocator.filter.value = filter
    }

    fun resetFilter() {
        ServiceLocator.filter.value = PlaceFilter()
    }

    fun refreshLocation() {
        viewModelScope.launch { locations.refreshOnce() }
    }

    fun dismissError() {
        _error.value = null
    }
}
