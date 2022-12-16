package com.androidandrew.sunscreen

import android.app.Application
import com.androidandrew.sharedtest.di.testDatabaseModule
import com.androidandrew.sunscreen.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApplication)
            // Order matters! Overriding modules go last.
            modules(testDatabaseModule, testNetworkModule, repositoryModule, serviceModule, viewModelModule, appModule, testViewModelModule, testModule)
        }
    }
}