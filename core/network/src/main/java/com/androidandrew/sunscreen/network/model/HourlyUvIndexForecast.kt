package com.androidandrew.sunscreen.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HourlyUvIndexForecast(
    @Json(name="ORDER") val order: Int,
    @Json(name="ZIP") val zip: String,
    @Json(name="DATE_TIME") val dateTimeString: String,
    @Json(name="UV_VALUE") val uv: Int
)

typealias DailyUvIndexForecast = List<HourlyUvIndexForecast>