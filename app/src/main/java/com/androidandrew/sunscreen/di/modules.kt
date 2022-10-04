package com.androidandrew.sunscreen.di

import android.content.Context
import androidx.room.Room
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.network.EpaApi
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.ui.main.MainViewModel
import com.androidandrew.sunscreen.ui.util.UvChartFormatter
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock

val appModule = module {
    fun provideDatabase(context: Context): SunscreenDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SunscreenDatabase::class.java,
            "uv_database")
        .fallbackToDestructiveMigration()
        .build()
    }

    single { provideDatabase(androidContext()) }
    single { EpaApi.service }
    single { SunscreenRepository(get(), get(), Clock.systemDefaultZone(), Dispatchers.IO) }

    factory { UvChartFormatter(androidContext()) }

    viewModel { MainViewModel(get(), Clock.systemDefaultZone()) }
}
