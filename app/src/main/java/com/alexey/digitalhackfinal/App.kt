package com.alexey.digitalhackfinal

import android.app.Application
import com.alexey.digitalhackfinal.di.ApplicationComponent
import com.alexey.digitalhackfinal.di.DaggerApplicationComponent
import com.alexey.digitalhackfinal.di.DaggerComponentProvider
import timber.log.Timber

class App : Application(), DaggerComponentProvider {

    override val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .application(this)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        setupTimber()

        component.inject(this)
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll()
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    var newTag = tag
                    if (tag != null) newTag = "AEBIT: $tag"
                    super.log(priority, newTag, message, t)
                }
            })
            Timber.i("App created")
        }
    }
}