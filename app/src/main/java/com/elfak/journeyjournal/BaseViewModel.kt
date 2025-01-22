package com.elfak.journeyjournal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elfak.journeyjournal.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus

abstract class BaseViewModel: ViewModel() {

    var errorEvent: SingleLiveEvent<String?> = SingleLiveEvent()

    private val exceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            run {
                exception.printStackTrace()
                errorEvent.postValue(exception.message)
            }
        }

    protected val coroutineScope = viewModelScope + Dispatchers.IO + exceptionHandler
}