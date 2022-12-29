package com.androidandrew.sunscreen.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tracking_table")
data class UserTrackingEntity constructor(
    @PrimaryKey(autoGenerate = false)
    var date: String,

    @ColumnInfo(name="sunburn_progress")
    var sunburnProgress: Double,

    @ColumnInfo(name="vitamin_d_progress")
    var vitaminDProgress: Double
)