package com.androidandrew.sunscreen.ui.main

sealed interface ForecastState {
    object Loading : ForecastState
    data class Error(val message: String) : ForecastState
    object Done : ForecastState
}