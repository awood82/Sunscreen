package com.androidandrew.sunscreen.repository

import com.androidandrew.sunscreen.database.Forecast
import java.time.LocalDate

class FakeRepository {

    private val hardcodedDate = LocalDate.now().toString()
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
}