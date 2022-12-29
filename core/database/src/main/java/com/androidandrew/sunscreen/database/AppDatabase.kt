package com.androidandrew.sunscreen.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import com.androidandrew.sunscreen.database.entity.UserSettingEntity
import com.androidandrew.sunscreen.database.entity.UserTrackingEntity

@Database(entities = [UserSettingEntity::class, UserTrackingEntity::class, HourlyForecastEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Connects the database to the DAO.
    abstract val userSettingsDao: UserSettingsDao

    abstract val userTrackingDao: UserTrackingDao

    abstract val hourlyForecastDao: HourlyForecastDao
}