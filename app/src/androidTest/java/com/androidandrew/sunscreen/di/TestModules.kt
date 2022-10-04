package com.androidandrew.sunscreen.di

import com.androidandrew.sunscreen.network.FakeEpaService
import com.androidandrew.sunscreen.ui.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

private val noon = Instant.parse("2022-09-25T12:00:00.00Z")
private val clockDefaultNoon = Clock.fixed(noon, ZoneId.of("UTC"))

val testModule = module {
    single { FakeEpaService() }

    viewModel { MainViewModel(FakeEpaService(), clockDefaultNoon) }
}