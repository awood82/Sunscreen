package com.androidandrew.sunscreen.network.di

import com.androidandrew.sunscreen.network.EpaApi
import org.koin.dsl.module

val networkModule = module {
    single { EpaApi.service }
}