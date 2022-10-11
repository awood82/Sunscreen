package com.androidandrew.sharedtest.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.androidandrew.sunscreen.database.SunscreenDatabase

class FakeDatabase {

    var db: SunscreenDatabase

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SunscreenDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    fun clearDatabase() {
        db.userTrackingDao.deleteAll()
        db.userSettingsDao.deleteAll()
    }

    fun tearDown() {
        clearDatabase()
        db.close()
    }
}