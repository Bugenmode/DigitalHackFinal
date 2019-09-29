package com.alexey.digitalhackfinal.data.remote.services

import com.alexey.digitalhackfinal.data.remote.model.PointModel
import com.alexey.digitalhackfinal.data.remote.model.PointResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/points")
    fun postPoints(
        @Body pointModel: PointModel
    ) : Single<Response<PointResponse>>

    @GET("/points")
    fun getPoints() : Single<Response<List<PointResponse>>>
}