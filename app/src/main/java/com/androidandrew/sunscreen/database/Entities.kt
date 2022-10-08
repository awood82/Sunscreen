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