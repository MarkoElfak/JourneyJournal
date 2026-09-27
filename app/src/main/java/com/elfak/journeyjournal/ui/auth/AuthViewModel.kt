package com.elfak.journeyjournal.ui.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.data.repo.AuthRepository
import com.elfak.journeyjournal.data.repo.PhotoUploadWarning
import com.elfak.journeyjournal.di.ServiceLocator
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class AuthViewModel : ViewModel() {

    private val repository = ServiceLocator.authRepository

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun clearMessages() = _state.update { it.copy(error = null, info = null) }

    fun login(username: String, password: String) {
        val problem = when {
            username.isBlank() -> "Unesite korisničko ime."
            password.isBlank() -> "Unesite lozinku."
            else -> null
        }
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.login(username, password) }
                .onFailure { error -> _state.update { it.copy(loading = false, error = message(error)) } }
                .onSuccess { _state.update { it.copy(loading = false) } }
        }
    }

    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        fullName: String,
        phone: String,
        photoUri: Uri?,
    ) {
        val problem = when {
            !AuthRepository.USERNAME_REGEX.matches(username) ->
                "Korisničko ime: 3-20 karaktera (slova, brojevi, . _ -)."
            password.length < 6 -> "Lozinka mora imati najmanje 6 karaktera."
            password != confirmPassword -> "Lozinke se ne poklapaju."
            fullName.isBlank() -> "Unesite ime i prezime."
            phone.isBlank() -> "Unesite broj telefona."
            photoUri == null -> "Snimite profilnu fotografiju."
            else -> null
        }
        if (problem != null) {
            _state.update { it.copy(error = problem) }
            return
        }

        // Creating the account signs the user in, which immediately replaces this screen. The
        // upload therefore runs on the application scope and only the UI update is scoped here.
        val registration = ServiceLocator.appScope.async {
            repository.register(username, password, fullName, phone, requireNotNull(photoUri))
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { registration.await() }
                .onSuccess { _state.update { it.copy(loading = false) } }
                .onFailure { error ->
                    // The account exists in this case, only the photo failed - not a blocker.
                    val info = (error as? PhotoUploadWarning)?.message
                    _state.update {
                        it.copy(
                            loading = false,
                            info = info,
                            error = if (info == null) message(error) else null,
                        )
                    }
                }
        }
    }

    private fun message(error: Throwable): String = when (error) {
        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> "Pogrešno korisničko ime ili lozinka."
        is FirebaseAuthUserCollisionException -> "Korisničko ime je već zauzeto."
        is FirebaseAuthWeakPasswordException -> "Lozinka je previše slaba."
        is FirebaseNetworkException -> "Nema internet konekcije."
        else -> error.message ?: "Došlo je do greške."
    }
}
