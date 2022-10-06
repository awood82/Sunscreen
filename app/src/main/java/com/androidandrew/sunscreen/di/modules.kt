package com.androidandrew.sunscreen.di

import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    single { EpaApi.service }

    factory { UvChartFormatter(androidContext()) }

    viewModel { MainViewModel(get(), Clock.systemDefaultZone()) }
}
