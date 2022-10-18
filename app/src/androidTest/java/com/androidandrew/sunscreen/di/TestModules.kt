package com.androidandrew.sunscreen.di

import android.content.Context
import androidx.room.Room
import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sharedtest.util.FakeData
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val testModule = module {
    fun provideDatabase(context: Context): SunscreenDatabase {
//        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, SunscreenDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }
    single { FakeData.clockDefaultNoon }
    single { FakeEpaService }
    single { provideDatabase(androidContext()) }
    single { SunscreenRepository(get(), get()) }

    viewModel { MainViewModel(get<FakeEpaService>(), get(), get(), get()) }
}