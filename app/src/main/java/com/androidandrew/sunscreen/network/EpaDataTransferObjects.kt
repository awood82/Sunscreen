package com.androidandrew.sunscreen.network

import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.time.LocalTime

@JsonClass(generateAdapter = true)
data class HourlyUvIndexForecast(
    @Json(name="ORDER") val order: Int,
    @Json(name="ZIP") val zip: String,
    @Json(name="DATE_TIME") val dateTimeString: String,
    @Json(name="UV_VALUE") val uv: Int
)

/*data class DateTimeDto()

fun HourlyUvIndexForecast.asUvPrediction(): UvPredictionPoint {
    val localDateTime: LocalDateTime = LocalDateTime(dateTimeString)
    return UvPredictionPoint(
        time = LocalTime.of(localDateTime.)
    )
}*/