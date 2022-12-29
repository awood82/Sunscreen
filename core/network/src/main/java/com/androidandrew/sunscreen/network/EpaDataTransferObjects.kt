package com.androidandrew.sunscreen.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
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

// Convert the DATE_TIME String, in the format of "Sep/26/2022 08 AM", to a LocalTime
fun String.asLocalTime(): LocalTime {
    return LocalTime.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}

fun String.asLocalDate(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ofPattern("MMM/dd/yyyy h a"))
}
