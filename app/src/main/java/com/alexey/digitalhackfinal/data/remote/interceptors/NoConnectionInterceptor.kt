package com.alexey.digitalhackfinal.data.remote.interceptors

import com.alexey.digitalhackfinal.data.repository.NoConnectionRepository
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class NoConnectionInterceptor @Inject constructor(
    private val noConnectionRepository: NoConnectionRepository
) : Interceptor{

    val emptyResponse =
        Response.Builder()
            .request(Request.Builder().url("http://localhost/").build())
            .protocol(Protocol.HTTP_2)
            .message("")
            .code(522)
            .body("{\"error\":\"No connection!\"}".toResponseBody("application/json; charset=utf-8".toMediaTypeOrNull()))
            .build()

    override fun intercept(chain: Interceptor.Chain): Response {
        var response: Response = emptyResponse
        try {
            response = chain.proceed(chain.request())
            noConnectionRepository.noConnection.postValue(false)
        } catch (ex: Exception) {
            when (ex) {
                is SocketTimeoutException, is UnknownHostException -> noConnectionRepository.noConnection.postValue(true)
            }
        }
        return response
    }
}