package com.androidandrew.sunscreen.di

import com.androidandrew.sharedtest.di.testDatabaseModule
import com.androidandrew.sharedtest.di.testNetworkModule
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.domain.di.domainModule
import org.koin.dsl.module
import java.time.Clock

val testModule = module {
    single<Clock> { FakeData.clockDefaultNoon }
}

val allModules = listOf(domainModule, testDatabaseModule, testNetworkModule, repositoryModule, serviceModule, analyticsModule, viewModelModule, appModule, testModule)