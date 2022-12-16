package com.androidandrew.sharedtest.di

import com.androidandrew.sharedtest.network.FakeEpaService
import org.koin.dsl.module

val testNetworkModule = module {
    single { FakeEpaService }
}