package com.androidandrew.sharedtest.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.database.UserSettingsDao

class FakeDatabase {

    var db: SunscreenDatabase
    var dao: UserSettingsDao

    init {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SunscreenDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
        dao = db.userSettingsDao
    }
}