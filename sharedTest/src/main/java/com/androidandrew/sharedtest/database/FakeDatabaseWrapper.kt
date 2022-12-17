package com.androidandrew.sharedtest.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.androidandrew.sunscreen.database.AppDatabase
import com.androidandrew.sunscreen.database.UserSettingsDao
import com.androidandrew.sunscreen.database.UserTrackingDao

class FakeDatabaseWrapper {

    var db: AppDatabase
    var userTrackingDao: UserTrackingDao
    var userSettingsDao: UserSettingsDao

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        userTrackingDao = db.userTrackingDao
        userSettingsDao = db.userSettingsDao
    }

    suspend fun clearDatabase() {
        userTrackingDao.deleteAll()
        userSettingsDao.deleteAll()
    }

    suspend fun tearDown() {
        clearDatabase()
        db.close()
    }
}