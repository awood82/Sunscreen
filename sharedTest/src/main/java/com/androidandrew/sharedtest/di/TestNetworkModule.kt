package com.androidandrew.sharedtest.di

import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.network.EpaService
import org.koin.dsl.module

val testNetworkModule = module {
    single<EpaService> { FakeEpaService }
}