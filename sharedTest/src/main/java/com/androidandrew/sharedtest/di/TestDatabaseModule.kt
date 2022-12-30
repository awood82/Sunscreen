package com.androidandrew.sharedtest.di

import android.content.Context
import androidx.room.Room
import com.androidandrew.sunscreen.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val testDatabaseModule = module {
    fun provideDatabase(context: Context): AppDatabase {
//        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    factory { provideDatabase(androidContext()) }

    single {
        val database = get<AppDatabase>()
        database.userTrackingDao
    }

    single {
        val database = get<AppDatabase>()
        database.userSettingsDao
    }

    single {
        val database = get<AppDatabase>()
        database.hourlyForecastDao
    }
}