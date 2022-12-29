package com.androidandrew.sunscreen.database.di

import android.app.Application
import androidx.room.Room
import com.androidandrew.sunscreen.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    fun provideDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "sunscreen_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { provideDatabase(androidApplication()) }

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