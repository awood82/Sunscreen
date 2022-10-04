package com.androidandrew.sunscreen.di

import com.androidandrew.sunscreen.network.FakeEpaService
import com.androidandrew.sunscreen.repository.SunscreenRepository
import org.koin.dsl.module

val testModule = module {
    single { FakeEpaService() }
    single { SunscreenRepository(get(), FakeEpaService()) }
}