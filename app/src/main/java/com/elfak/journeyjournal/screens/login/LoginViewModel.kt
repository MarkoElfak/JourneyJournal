package com.elfak.journeyjournal.screens.login

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.elfak.journeyjournal.BaseViewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import com.elfak.journeyjournal.utils.SingleLiveEvent

class LoginViewModel : BaseViewModel() {

    val email = mutableStateOf("")
    val password = mutableStateOf("")

    val loginSuccessfulEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val loginUnsuccessfulEvent: SingleLiveEvent<String> = SingleLiveEvent()

    fun login(context: Context) {
        if (email.value.trim().isEmpty() || password.value.trim().isEmpty()) {
            loginUnsuccessfulEvent.postValue(context.getString(R.string.email_password_required_label))
            return
        }

        FirebaseWrapper.login(
            email = email.value.trim(),
            password = password.value.trim(),
            onSuccess = {
                loginSuccessfulEvent.postValue(true)
            },
            onError = {
                loginUnsuccessfulEvent.postValue(it)
            },
        )
    }
}