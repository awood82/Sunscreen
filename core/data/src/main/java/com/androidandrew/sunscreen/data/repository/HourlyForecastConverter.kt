package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Network to Model isn't necessary since Network data flows to DB and then to the UI as Model

// Network to DB
fun DailyUvIndexForecast.asEntity(): List<HourlyForecastEntity> {
    var firstDate = ""
    return this
        .sortedBy { it.order }
        .map { it.asEntity() }
        .also { firstDate = it.getOrNull(0)?.date ?: "" }
        .filter { it.date == firstDate } // Remove the '12AM tomorrow' portion of the response
}

fun HourlyUvIndexForecast.asEntity(): HourlyForecastEntity {
    return HourlyForecastEntity(
        zip = zip,
        date = dateTimeString.asLocalDate().toString(),
        order = order,
        time = dateTimeString.asLocalTime().toString(),
        uvi = uv.toFloat()
    )
}

// Convert the network response's DATE_TIME String, in the format of "Sep/26/2022 08 AM", to a LocalTime
fun String.asLocalTime(): LocalTime {
    return LocalTime.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}

fun String.asLocalDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}

// DB to Model
fun HourlyForecastEntity.asModel(): UvPredictionPoint {
    return UvPredictionPoint(
        time = LocalTime.parse(time),
        uvIndex = uvi.toDouble()
    )
}

fun List<HourlyForecastEntity>.asModel(): UvPrediction {
    return this.sortedBy { it.order }.map { hourly ->
        hourly.asModel()
    }
}

