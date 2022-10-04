package com.androidandrew.sunscreen

import android.app.Application
import com.androidandrew.sunscreen.di.appModule
import com.androidandrew.sunscreen.di.testModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApplication)
            // Order matters! Overriding modules go last.
            modules(appModule, testModule)
        }
    }
}