package com.androidandrew.sunscreen.data.di

import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserSettingsRepositoryImpl
import com.androidandrew.sunscreen.data.repository.UserTrackingRepository
import com.androidandrew.sunscreen.data.repository.UserTrackingRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<UserSettingsRepository> { UserSettingsRepositoryImpl(get()) }
    single<UserTrackingRepository> { UserTrackingRepositoryImpl(get()) }
}