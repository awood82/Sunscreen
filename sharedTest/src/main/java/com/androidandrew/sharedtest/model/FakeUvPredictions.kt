package com.androidandrew.sharedtest.model

import com.androidandrew.sharedtest.network.FakeEpaService
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object FakeUvPredictions {

    val forecast = FakeEpaService.forecast.asExternalModel()
}

fun DailyUvIndexForecast.asExternalModel(): UvPrediction {
    return this.sortedBy { it.order }.map {
        it.asModel()
    }
}

fun HourlyUvIndexForecast.asModel(): UvPredictionPoint {
    return UvPredictionPoint(
        time = dateTimeString.asLocalTime(),
        uvIndex = this.uv.toDouble()
    )
}

fun String.asLocalTime(): LocalTime {
    return LocalTime.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}