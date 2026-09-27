package com.elfak.journeyjournal.ui.places

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.data.model.Comment
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.di.ServiceLocator
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModel : ViewModel() {

    private val places = ServiceLocator.placeRepository
    private val locations = ServiceLocator.locationRepository

    private val placeId = MutableStateFlow<String?>(null)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    val currentUid: String? get() = ServiceLocator.auth.currentUser?.uid

    val location: StateFlow<LatLng?> = locations.location

    val place: StateFlow<Place?> = placeId.filterNotNull()
        .flatMapLatest { places.observePlace(it) }
        .catch { error ->
            _message.value = error.message
            emit(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val comments: StateFlow<List<Comment>> = placeId.filterNotNull()
        .flatMapLatest { places.observeComments(it) }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val myRating: StateFlow<Int?> = placeId.filterNotNull()
        .flatMapLatest { places.observeMyRating(it) }
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val visited: StateFlow<Boolean> = placeId.filterNotNull()
        .flatMapLatest { places.observeVisited(it) }
        .catch { emit(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun bind(id: String) {
        placeId.value = id
        viewModelScope.launch { locations.refreshOnce() }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun rate(value: Int) = run("Hvala na oceni! (+5 poena)") { id ->
        places.ratePlace(id, value)
    }

    fun comment(text: String) = run("Komentar je dodat. (+5 poena)") { id ->
        places.addComment(id, text)
    }

    fun visit() {
        val position = location.value
        if (position == null) {
            _message.value = "Trenutna lokacija još nije poznata."
            return
        }
        run("Lokacija je obeležena kao posećena! (+10 poena)") { id ->
            places.visitPlace(id, position.latitude, position.longitude)
        }
    }

    fun deleteComment(commentId: String) = run("Komentar je obrisan.") { id ->
        places.deleteComment(id, commentId)
    }

    fun deletePlace(onDeleted: () -> Unit) {
        val id = placeId.value ?: return
        viewModelScope.launch {
            _busy.value = true
            runCatching { places.deletePlace(id) }
                .onSuccess { onDeleted() }
                .onFailure { _message.value = it.message }
            _busy.value = false
        }
    }

    private fun run(success: String, action: suspend (String) -> Unit) {
        val id = placeId.value ?: return
        viewModelScope.launch {
            _busy.value = true
            runCatching { action(id) }
                .onSuccess { _message.value = success }
                .onFailure { _message.value = it.message ?: "Radnja nije uspela." }
            _busy.value = false
        }
    }
}
