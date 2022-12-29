package com.androidandrew.sunscreen.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings_table")
data class UserSettingEntity constructor(
    @PrimaryKey(autoGenerate = false)
    val id: Long,

    @ColumnInfo(name="value")
    val value: String
)