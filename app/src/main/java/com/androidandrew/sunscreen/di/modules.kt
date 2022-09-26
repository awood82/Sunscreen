package com.androidandrew.sunscreen.di

import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    viewModel { MainViewModel(EpaApi.service, Clock.systemDefaultZone()) }
}
