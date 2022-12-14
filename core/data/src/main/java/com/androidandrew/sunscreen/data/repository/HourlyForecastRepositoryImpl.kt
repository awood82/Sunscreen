package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.HourlyForecastDao
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class HourlyForecastRepositoryImpl(
    private val hourlyForecastDao: HourlyForecastDao,
    private val uvService: EpaService
) : HourlyForecastRepository {

    override fun getForecastFlow(zipCode: String, date: LocalDate): Flow<List<UvPredictionPoint>> {
        return hourlyForecastDao.getFlow(zipCode, date.toString())
            .distinctUntilChanged()
            .map {
                it.asModel().trim()
            }
            .onEach {
                if (it.isEmpty()) {
                    refreshNetwork(zipCode)
                }
            }
    }

    override suspend fun getForecast(zipCode: String, date: LocalDate): List<UvPredictionPoint> {
        var forecast = hourlyForecastDao.getOnce(zipCode, date.toString())
        if (forecast.isEmpty()) {
            refreshNetwork(zipCode)
            forecast = hourlyForecastDao.getOnce(zipCode, date.toString())
        }
        return forecast.asModel().trim()
    }

    override suspend fun setForecast(forecast: DailyUvIndexForecast) {
        hourlyForecastDao.insert(forecast.asEntity())
    }

    private suspend fun refreshNetwork(zipCode: String) {
        android.util.Log.i("HourlyForecastRepositoryImpl", "Refreshing from network for zip $zipCode")
        try {
            val response = uvService.getUvForecast(zipCode)
            hourlyForecastDao.insert(response.asEntity())
        } catch (e: Exception) {
            android.util.Log.e("HourlyForecastRepositoryImpl", "Exception: $e")
        }
    }
}