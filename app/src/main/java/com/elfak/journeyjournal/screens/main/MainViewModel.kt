package com.elfak.journeyjournal.screens.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.elfak.journeyjournal.BaseViewModel
import com.elfak.journeyjournal.data.models.UserModel
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    val user: MutableLiveData<UserModel?> = MutableLiveData()
    val username = mutableStateOf("")
    val email = mutableStateOf("")
    val imageUrl = mutableStateOf("")

    fun getUser() {
        coroutineScope.launch {
            user.postValue(FirebaseWrapper.getUser())
        }
    }

    fun extractData() {
        username.value = user.value!!.username
        email.value = user.value!!.email
        imageUrl.value = user.value!!.imageURL
    }
}