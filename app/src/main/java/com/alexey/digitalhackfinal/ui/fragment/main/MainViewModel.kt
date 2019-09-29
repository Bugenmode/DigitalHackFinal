package com.alexey.digitalhackfinal.ui.fragment.main

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.alexey.digitalhackfinal.data.remote.model.Location
import com.alexey.digitalhackfinal.data.remote.model.PointModel
import com.alexey.digitalhackfinal.data.remote.model.PointResponse
import com.alexey.digitalhackfinal.data.repository.PointRepository
import com.alexey.digitalhackfinal.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CheckResult")
class MainViewModel @Inject constructor(
    private val pointRepository: PointRepository
) : BaseViewModel() {

    var wayA = MutableLiveData<String>()
    var wayB = MutableLiveData<String>()
    var data = MutableLiveData<List<PointResponse>>()

    fun getPoints() {
        pointRepository.getPoints()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.isSuccessful) {
                    data.value = it.body()
                }
            }, {
                Timber.d(it.toString())
            })
    }

    fun postPoints(location: Location, signal: String) {
        pointRepository.postPoints(PointModel(location, signal))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d(it.toString())
            }, {

            })
    }
}