package com.androidandrew.sharedtest.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.androidandrew.sunscreen.database.AppDatabase
import com.androidandrew.sunscreen.database.HourlyForecastDao
import com.androidandrew.sunscreen.database.UserSettingsDao
import com.androidandrew.sunscreen.database.UserTrackingDao

class FakeDatabaseWrapper {

    var db: AppDatabase
    var userTrackingDao: UserTrackingDao
    var userSettingsDao: UserSettingsDao
    var hourlyForecastDao: HourlyForecastDao

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        userTrackingDao = db.userTrackingDao
        userSettingsDao = db.userSettingsDao
        hourlyForecastDao = db.hourlyForecastDao
    }

    suspend fun clearDatabase() {
        userTrackingDao.deleteAll()
        userSettingsDao.deleteAll()
        hourlyForecastDao.deleteAll()
    }

    suspend fun tearDown() {
        clearDatabase()
        db.close()
    }
}