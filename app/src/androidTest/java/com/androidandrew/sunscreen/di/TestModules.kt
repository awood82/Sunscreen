package com.androidandrew.sunscreen.di

import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {
    single { FakeEpaService }

    viewModel { MainViewModel(FakeEpaService, FakeData.clockDefaultNoon) }
}