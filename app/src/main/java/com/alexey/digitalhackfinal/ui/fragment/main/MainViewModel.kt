package com.alexey.digitalhackfinal.ui.fragment.main

import androidx.lifecycle.MutableLiveData
import com.alexey.digitalhackfinal.ui.base.BaseViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor() : BaseViewModel() {

    var wayA = MutableLiveData<String>()
    var wayB = MutableLiveData<String>()

}