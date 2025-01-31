package com.elfak.journeyjournal.screens.register

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.elfak.journeyjournal.BaseViewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.data.models.RegisterObject
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import com.elfak.journeyjournal.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class RegisterViewModel : BaseViewModel() {

    val username = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val email = mutableStateOf("")
    val dob = mutableStateOf("")

    val registerSuccessfulEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val registerUnsuccessfulEvent: SingleLiveEvent<String> = SingleLiveEvent()

    val userSuccessfullySaveEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()

    fun register(context: Context) {
        if (username.value.trim().isEmpty() || password.value.trim()
                .isEmpty() || confirmPassword.value.trim().isEmpty() || email.value.trim()
                .isEmpty() || dob.value.trim().isEmpty()
        ) {
            registerUnsuccessfulEvent.postValue(context.getString(R.string.all_field_required_label))
            return
        }

        if (password.value.trim() != confirmPassword.value.trim()) {
            registerUnsuccessfulEvent.postValue(context.getString(R.string.passwords_dont_match_label))
            return
        }

        FirebaseWrapper.signup(
            email = email.value.trim(),
            password = password.value.trim(),
            onSuccess = {
                registerSuccessfulEvent.postValue(true)
            },
            onError = {
                registerUnsuccessfulEvent.postValue(it)
            }
        )
    }

    fun saveUser() {
        val user = RegisterObject(
            username = username.value.trim(),
            email = email.value.trim(),
            dateOfBirth = dob.value.trim()
        )

        coroutineScope.launch {
            val isSuccessful = FirebaseWrapper.saveUser(user)

            if (isSuccessful) {
                userSuccessfullySaveEvent.postValue(true)
            }
        }
    }
}