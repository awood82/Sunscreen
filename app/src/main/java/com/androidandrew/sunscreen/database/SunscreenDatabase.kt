package com.androidandrew.sunscreen.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserSetting::class], version = 1, exportSchema = false)
abstract class SunscreenDatabase : RoomDatabase() {

    // Connects the database to the DAO.
    abstract val userSettingsDao: UserSettingsDao
}