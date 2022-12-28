package com.androidandrew.sunscreen

import android.app.Application
import com.androidandrew.sharedtest.di.*
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.di.*
import com.androidandrew.sunscreen.domain.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            // Koin Android logger
            androidLogger()
            androidContext(this@TestApplication)
            // Order matters! Overriding modules go last.
            modules(domainModule, testDatabaseModule, testNetworkModule, repositoryModule, serviceModule, viewModelModule, appModule, testViewModelModule, testModule)
        }
    }
}