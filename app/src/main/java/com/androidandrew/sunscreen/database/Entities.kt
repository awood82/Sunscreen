package com.androidandrew.sunscreen.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings_table")
data class UserSetting constructor(
    @PrimaryKey(autoGenerate = false)
    var id: Long,

    @ColumnInfo(name="value")
    var value: String
)

@Entity(tableName = "user_tracking_table")
data class UserTracking constructor(
    @PrimaryKey(autoGenerate = false)
    var date: String,

    @ColumnInfo(name="burn_progress")
    var burnProgress: Double,

    @ColumnInfo(name="vitamin_d_progress")
    var vitaminDProgress: Double
)