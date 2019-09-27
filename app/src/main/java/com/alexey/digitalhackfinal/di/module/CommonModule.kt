package com.alexey.digitalhackfinal.di.module

import android.app.Application
import com.alexey.digitalhackfinal.App
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CommonModule {


    @Provides
    @Singleton
    fun provideApp(application : Application) : App {
        return application as App
    }


}