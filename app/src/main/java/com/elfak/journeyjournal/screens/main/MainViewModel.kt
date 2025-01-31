package com.elfak.journeyjournal.screens.main

import androidx.lifecycle.MutableLiveData
import com.elfak.journeyjournal.BaseViewModel
import com.elfak.journeyjournal.data.models.UserModel
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    val user: MutableLiveData<UserModel?> = MutableLiveData()

    fun getUser() {
        coroutineScope.launch {
            user.postValue(FirebaseWrapper.getUser())
        }
    }
}