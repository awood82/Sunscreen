package com.androidandrew.sunscreen.data.di

import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single { UserRepositoryImpl(get(), get()) }
}