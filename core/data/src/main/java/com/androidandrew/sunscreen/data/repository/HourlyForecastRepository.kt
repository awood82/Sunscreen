package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HourlyForecastRepository {
    fun getForecastFlow(zip: String, date: LocalDate): Flow<List<UvPredictionPoint>>
    suspend fun getForecast(zip: String, date: LocalDate): List<UvPredictionPoint>
    suspend fun setForecast(forecast: DailyUvIndexForecast)
}