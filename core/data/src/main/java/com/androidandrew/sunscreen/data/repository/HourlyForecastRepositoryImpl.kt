package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.HourlyForecastDao
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class HourlyForecastRepositoryImpl(
    private val hourlyForecastDao: HourlyForecastDao
) : HourlyForecastRepository {

    override fun getForecastFlow(zip: String, date: LocalDate): Flow<List<UvPredictionPoint>> {
        return hourlyForecastDao.getFlow(zip, date.toString()).map {
            it.asModel()
        }
    }

    override suspend fun getForecast(zip: String, date: LocalDate): List<UvPredictionPoint> {
        return hourlyForecastDao.getOnce(zip, date.toString()).map {
            it.asModel()
        }
    }

    override suspend fun setForecast(forecast: DailyUvIndexForecast) {
        hourlyForecastDao.insert(forecast.asEntity())
    }
}