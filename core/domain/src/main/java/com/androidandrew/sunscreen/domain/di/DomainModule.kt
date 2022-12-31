package com.androidandrew.sunscreen.domain.di

import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.domain.uvcalculators.vitamind.VitaminDCalculator
import org.koin.dsl.module

val domainModule = module {
    single { ConvertSpfUseCase() }
    single { SunburnCalculator(get()) }
    single { VitaminDCalculator(get()) }

    single { GetLocalForecastForTodayUseCase(get(), get(), get()) }
}