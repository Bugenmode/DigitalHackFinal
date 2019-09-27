package com.alexey.digitalhackfinal.data.repository

import androidx.lifecycle.MutableLiveData

class NoConnectionRepository {
    var noConnection = MutableLiveData<Boolean>().apply { postValue(false) }
}