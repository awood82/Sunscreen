package com.androidandrew.sunscreen.di

import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    factory { UvChartFormatter(androidContext()) }

    viewModel { MainViewModel(EpaApi.service, Clock.systemDefaultZone()) }
}
