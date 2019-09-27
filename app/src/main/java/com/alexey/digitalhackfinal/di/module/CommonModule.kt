package com.alexey.digitalhackfinal.di.module

import android.app.Application
import com.alexey.digitalhackfinal.App
import com.alexey.digitalhackfinal.data.local.prefs.PreferencesHelper
import com.alexey.digitalhackfinal.data.local.prefs.PreferencesHelperImpl
import com.alexey.digitalhackfinal.utils.AppConstants
import com.alexey.digitalhackfinal.utils.DateDeserializer
import com.alexey.digitalhackfinal.utils.DateSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import org.threeten.bp.LocalDateTime
import javax.inject.Singleton

@Module
class CommonModule {

    @Provides
    @Singleton
    fun provideApp(application : Application) : App {
        return application as App
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDateTime::class.java, DateSerializer())
            .registerTypeAdapter(LocalDateTime::class.java, DateDeserializer())
            .create()
    }

    @Provides
    @Singleton
    fun providePreferencesHelper(app: App): PreferencesHelper {
        return PreferencesHelperImpl(app, AppConstants.PREF_NAME)
    }
}