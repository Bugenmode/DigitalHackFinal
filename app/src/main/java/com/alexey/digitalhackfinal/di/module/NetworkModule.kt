package com.alexey.digitalhackfinal.di.module

import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

class NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(okHttpClient: OkHttpClient) : OkHttpClient {
        return okHttpClient
    }


}