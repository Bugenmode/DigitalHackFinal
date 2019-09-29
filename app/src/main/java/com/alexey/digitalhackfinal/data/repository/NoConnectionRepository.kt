package com.alexey.digitalhackfinal.data.repository

import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class NoConnectionRepository @Inject constructor() {
    var noConnection = MutableLiveData<Boolean>().apply { postValue(false) }
}