package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.common.DataResult
import com.androidandrew.sunscreen.database.HourlyForecastDao
import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import timber.log.Timber
import java.time.LocalDate

class HourlyForecastRepositoryImpl(
    private val hourlyForecastDao: HourlyForecastDao,
    private val uvService: EpaService
) : HourlyForecastRepository {

    override suspend fun setForecast(forecast: DailyUvIndexForecast) {
        hourlyForecastDao.insert(forecast.asEntity())
    }

    override suspend fun getForecast(zipCode: String, date: LocalDate): DataResult<List<UvPredictionPoint>> {
        var forecastFromDb = readForecastFromDatabaseFor(zipCode, date)
        if (forecastFromDb.isEmpty()) {
            val forecastFromNetwork = getForecastFromNetwork(zipCode)
            if (forecastFromNetwork.isSuccess) {
                forecastFromDb = forecastFromNetwork.getOrNull()!!.asEntity()
                cacheForecast(forecastFromDb)
            } else {
                return DataResult.Error(forecastFromNetwork.exceptionOrNull()!!)
            }
        }
        return DataResult.Success(forecastFromDb.asModel().trim())
    }

    private suspend fun getForecastFromNetwork(zipCode: String): Result<List<HourlyUvIndexForecast>> {
        Timber.i("HourlyForecastRepositoryImpl", "Refreshing from network for zip $zipCode")
        return uvService.getUvForecast(zipCode)
    }

    private suspend fun readForecastFromDatabaseFor(zipCode: String, date: LocalDate): List<HourlyForecastEntity> {
        return hourlyForecastDao.getOnce(zipCode, date.toString())
    }

    private suspend fun cacheForecast(forecast: List<HourlyForecastEntity>) {
        hourlyForecastDao.insert(forecast)
    }
}