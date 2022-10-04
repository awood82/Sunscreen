package com.androidandrew.sunscreen.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import java.time.LocalTime

//@Entity(tableName="date_location_table")
//data class DateAndLocation constructor(
//    @PrimaryKey val date: String,
//    val location: String
//)

@Entity(tableName = "forecast_table", primaryKeys = ["date", "location", "hour"])
data class Forecast constructor (
    val date: String,
    val location: String,
    val hour: Int,
    val uvIndex: Double
)

fun List<Forecast>.asDomainModel(): List<UvPredictionPoint> {
    return map {
        UvPredictionPoint(
            time = LocalTime.of(it.hour, 0, 0, 0),
            uvIndex = it.uvIndex
        )
    }
}

@Entity(tableName = "tracking_table")
data class Tracking constructor (
    @PrimaryKey val date: Long,
    val sunUnitsSoFar: Double,
    val vitaminDSoFar: Double
    // spf currently?
    // cover?
)