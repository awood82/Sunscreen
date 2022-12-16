package com.androidandrew.sunscreen.model.uv

import com.androidandrew.sunscreen.network.DailyUvIndexForecast
import com.androidandrew.sunscreen.network.HourlyUvIndexForecast
import com.androidandrew.sunscreen.network.asLocalTime

fun HourlyUvIndexForecast.asUvPredictionPoint(): UvPredictionPoint {
    return UvPredictionPoint(
        time = dateTimeString.asLocalTime(),
        uvIndex = uv.toDouble()
    )
}

fun DailyUvIndexForecast.asUvPrediction(): UvPrediction {
    return this.sortedBy {it.order}.map { hourly ->
        hourly.asUvPredictionPoint()
    }
}