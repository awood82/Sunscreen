package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.common.DataResult
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import java.time.LocalDate

interface HourlyForecastRepository {
    suspend fun setForecast(forecast: DailyUvIndexForecast)
    suspend fun getForecast(zipCode: String, date: LocalDate): DataResult<List<UvPredictionPoint>>
}