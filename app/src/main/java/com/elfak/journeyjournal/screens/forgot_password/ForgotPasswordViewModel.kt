package com.elfak.journeyjournal.screens.forgot_password

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.elfak.journeyjournal.BaseViewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import com.elfak.journeyjournal.utils.SingleLiveEvent

class ForgotPasswordViewModel : BaseViewModel() {

    val email = mutableStateOf("")

    val resetPasswordSuccessfulEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    val resetPasswordUnsuccessfulEvent: SingleLiveEvent<String> = SingleLiveEvent()

    fun forgotPassword(context: Context) {
        if (email.value.trim().isEmpty()) {
            resetPasswordUnsuccessfulEvent.postValue(context.getString(R.string.all_field_required_label))
            return
        }

        FirebaseWrapper.resetPassword(
            email = email.value.trim(),
            onSuccess = {
                resetPasswordSuccessfulEvent.postValue(true)
            },
            onError = {
                resetPasswordUnsuccessfulEvent.postValue(it)
            }
        )
    }
}