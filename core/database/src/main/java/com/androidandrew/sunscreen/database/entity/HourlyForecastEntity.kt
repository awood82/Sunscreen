package com.androidandrew.sunscreen.database.entity

import androidx.room.Entity

@Entity(tableName = "hourly_forecast_table", primaryKeys = ["zip", "date", "order"])
data class HourlyForecastEntity constructor(
    val zip: String,
    val date: String,
    val order: Int,
    val time: String,
    val uvi: Float
)