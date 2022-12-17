package com.androidandrew.sunscreen.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserSetting::class, UserTracking::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Connects the database to the DAO.
    abstract val userSettingsDao: UserSettingsDao

    abstract val userTrackingDao: UserTrackingDao
}