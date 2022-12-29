package com.androidandrew.sunscreen.di

import com.androidandrew.sharedtest.di.testDatabaseModule
import com.androidandrew.sharedtest.di.testNetworkModule
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.data.di.repositoryModule
import com.androidandrew.sunscreen.domain.di.domainModule
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testViewModelModule = module {
    viewModel { MainViewModel(get<FakeEpaService>(), get(), get(), get(), get(), get(), get(), get()) }
}

val testModule = module {
    single { FakeData.clockDefaultNoon }
}

val allModules = listOf(domainModule, testDatabaseModule, testNetworkModule, repositoryModule, serviceModule, viewModelModule, appModule, testViewModelModule, testModule)