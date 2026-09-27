package com.elfak.journeyjournal.ui.places

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.data.model.PlaceType
import com.elfak.journeyjournal.di.ServiceLocator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddPlaceUiState(
    val saving: Boolean = false,
    val error: String? = null,
    val savedPlaceId: String? = null,
)

class AddPlaceViewModel : ViewModel() {

    private val places = ServiceLocator.placeRepository
    private val locations = ServiceLocator.locationRepository

    val location: StateFlow<LatLng?> = locations.location

    private val _state = MutableStateFlow(AddPlaceUiState())
    val state: StateFlow<AddPlaceUiState> = _state.asStateFlow()

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch { locations.refreshOnce() }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun save(title: String, description: String, type: PlaceType, photoUri: Uri?) {
        val position = location.value
        val problem = when {
            title.isBlank() -> "Unesite naziv objekta."
            position == null -> "Trenutna lokacija još nije poznata."
            else -> null
        }
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                places.addPlace(
                    title = title,
                    description = description,
                    type = type,
                    lat = requireNotNull(position).latitude,
                    lng = position.longitude,
                    photoUri = photoUri,
                )
            }
                .onSuccess { id -> _state.update { it.copy(saving = false, savedPlaceId = id) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(saving = false, error = error.message ?: "Čuvanje nije uspelo.")
                    }
                }
        }
    }
}
