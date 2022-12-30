package com.androidandrew.sunscreen.data.di

import com.androidandrew.sunscreen.data.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<UserSettingsRepository> { UserSettingsRepositoryImpl(get()) }
    single<UserTrackingRepository> { UserTrackingRepositoryImpl(get()) }
    single<HourlyForecastRepository> { HourlyForecastRepositoryImpl(get(), get()) }
}