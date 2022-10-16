package com.androidandrew.sunscreen.di

import android.content.Context
import androidx.room.Room
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.chart.UvChartFormatter
import com.androidandrew.sunscreen.ui.location.LocationViewModel
import com.androidandrew.sunscreen.util.LocationUtil
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    fun provideDatabase(context: Context): SunscreenDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SunscreenDatabase::class.java,
            "sunscreen_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single<Clock> { Clock.systemDefaultZone() }
    single { EpaApi.service }
    single { provideDatabase(androidContext()) }
    single { SunscreenRepository(get(), get()) }

    factory { LocationUtil() }
    factory { UvChartFormatter(androidContext()) }

    viewModel { LocationViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
}
