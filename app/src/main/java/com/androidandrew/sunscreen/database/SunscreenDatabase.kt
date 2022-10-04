package com.androidandrew.sunscreen.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Forecast::class, Tracking::class], version = 2, exportSchema = false)
abstract class SunscreenDatabase : RoomDatabase() {

    // Connects the database to the DAO.
    abstract val forecastDao: ForecastDao
}
