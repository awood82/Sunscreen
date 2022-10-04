package com.androidandrew.sunscreen.network

import com.androidandrew.sunscreen.database.Forecast
import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@JsonClass(generateAdapter = true)
data class HourlyUvIndexForecast(
    @Json(name="ORDER") val order: Int,
    @Json(name="ZIP") val zip: String,
    @Json(name="DATE_TIME") val dateTimeString: String,
    @Json(name="UV_VALUE") val uv: Int
)

typealias DailyUvIndexForecast = List<HourlyUvIndexForecast>

val timeResponseFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM/dd/yyyy h a")

// Convert the DATE_TIME String, in the format of "Sep/26/2022 08 AM", to a LocalTime
fun String.asLocalTime(): LocalTime {
    return LocalTime.parse(this, timeResponseFormatter)
}

// Convert the DATE_TIME String, in the format of "Sep/26/2022 08 AM", to a LocalDateTime
fun String.asLocalDate(): LocalDate {
    return LocalDate.parse(this, timeResponseFormatter)
}

// Convert the DATE_TIME String, in the format of "Sep/26/2022 08 AM", to a LocalDateTime
fun String.asLocalDateTime(): LocalDateTime {
    return LocalDateTime.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}

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

fun HourlyUvIndexForecast.asDatabaseForecast(): Forecast {
    return Forecast(
        date = dateTimeString.asLocalDate().toString(),
        location = zip,
        hour = dateTimeString.asLocalTime().hour,
        uvIndex = uv.toDouble()
    )
}

fun DailyUvIndexForecast.asDatabaseForecasts(): List<Forecast> {
    return this.sortedBy { it.order }.map { hourly ->
        hourly.asDatabaseForecast()
    }
}
