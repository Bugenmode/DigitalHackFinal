package com.alexey.digitalhackfinal.di

import android.app.Application
import com.alexey.digitalhackfinal.App
import com.alexey.digitalhackfinal.di.module.CommonModule
import com.alexey.digitalhackfinal.di.module.NetworkModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CommonModule::class,
        NetworkModule::class,
        AndroidSupportInjectionModule::class
    ]
)
interface ApplicationComponent {

    @Component.Builder
    interface Builder {

        fun build(): ApplicationComponent

        @BindsInstance
        fun application(application: Application): Builder
    }

    fun inject(app: App)
}