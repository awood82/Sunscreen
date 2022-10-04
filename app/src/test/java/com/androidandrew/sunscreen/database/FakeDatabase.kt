package com.androidandrew.sunscreen.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.androidandrew.sunscreen.util.FakeData

class FakeDatabase {

    private lateinit var db: SunscreenDatabase

    fun createDatabase(): SunscreenDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SunscreenDatabase::class.java)
            // Allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
//        dao = db.totClockDao
//        repo = SettingsRepository(dao, coroutinesTestRule.testDispatcherProvider)

        return db
    }

    fun insertForecasts(howMany: Int) {
        insertForecasts(howMany, FakeData.localDate.toString())
    }

    fun insertForecastsTomorrow(howMany: Int) {
        insertForecasts(howMany, FakeData.localDate.plusDays(1).toString())
    }

    private fun insertForecasts(howMany: Int, date: String) {
        val startingHour = 6
        val list = mutableListOf<Forecast>()
        for (i in 0 until howMany) {
            val forecast = Forecast(
                date = date,
                location = FakeData.zip,
                hour = startingHour + i,
                uvIndex = i.toDouble()
            )
            list.add(forecast)
        }
        db.forecastDao.insertForecasts(list)
    }
}