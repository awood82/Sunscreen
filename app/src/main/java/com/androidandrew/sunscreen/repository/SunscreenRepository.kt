package com.androidandrew.sunscreen.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.androidandrew.sunscreen.database.Forecast
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.database.asDomainModel
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.asDatabaseForecasts
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate

class SunscreenRepository(
    private val database: SunscreenDatabase,
    private val epaService: EpaService,
    private val clock: Clock,
    //private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()) {
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

    private val hardcodedDate = LocalDate.now(clock).toString()
    private val hardcodedLocation = "92123"
    private val hardcodedForecast = listOf(
        Forecast(hardcodedDate, hardcodedLocation, 5, 0.0),
        Forecast(hardcodedDate, hardcodedLocation, 6, 0.0),
        Forecast(hardcodedDate, hardcodedLocation, 7, 0.0),
        Forecast(hardcodedDate, hardcodedLocation, 8, 1.0),
        Forecast(hardcodedDate, hardcodedLocation, 9, 3.0),
        Forecast(hardcodedDate, hardcodedLocation, 10, 6.0),
        Forecast(hardcodedDate, hardcodedLocation, 11, 10.0),
        Forecast(hardcodedDate, hardcodedLocation, 12, 12.0),
        Forecast(hardcodedDate, hardcodedLocation, 13, 11.0),
        Forecast(hardcodedDate, hardcodedLocation, 14, 8.0),
        Forecast(hardcodedDate, hardcodedLocation, 15, 5.0),
        Forecast(hardcodedDate, hardcodedLocation, 16, 3.0),
        Forecast(hardcodedDate, hardcodedLocation, 17, 1.0),
        Forecast(hardcodedDate, hardcodedLocation, 17, 0.0),
        Forecast(hardcodedDate, hardcodedLocation, 19, 0.0),
        Forecast(hardcodedDate, hardcodedLocation, 20, 0.0),
    )

    // TODO: Move this into another database? Use MutableLiveData?
//    private var localDateFilter: String = "" //LocalDate.now().toString()
//    private var locationFilter: String = ""

    val forecast: LiveData<List<UvPredictionPoint>> =
        Transformations.map(
            database.forecastDao.getForecasts(hardcodedDate, hardcodedLocation)) {
                it.asDomainModel()
            }

    suspend fun refreshForecast(localDate: LocalDate, location: String) {
        withContext(dispatcher) {
//            database.forecastDao.deleteForecasts() // TODO: Remove after testing

            // Don't reload from the network if a forecast was already retrieved today
//            if (database.forecastDao.getForecastsCount(hardcodedDate, hardcodedLocation) <= 1) {
            if (database.forecastDao.getForecastsCount(localDate.toString(), location) <= 1) {
                val uvForecast = epaService.getUvForecast(location)
                val dbForecast = uvForecast.asDatabaseForecasts()
                database.forecastDao.insertForecasts(dbForecast)
            }
        }
    }
}