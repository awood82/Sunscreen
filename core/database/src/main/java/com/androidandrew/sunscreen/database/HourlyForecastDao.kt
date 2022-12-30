package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HourlyForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(forecast: List<HourlyForecastEntity>)

    @Query("SELECT * FROM hourly_forecast_table WHERE zip = :zip AND date =" +
            " (SELECT MIN(date) FROM hourly_forecast_table WHERE zip = :zip AND date >= :date)" +
            "ORDER BY date DESC, `order` ASC")
    suspend fun getOnce(zip: String, date: String): List<HourlyForecastEntity>

    @Query("SELECT * FROM hourly_forecast_table WHERE zip = :zip AND date =" +
            " (SELECT MIN(date) FROM hourly_forecast_table WHERE zip = :zip AND date >= :date)" +
            "ORDER BY date DESC, `order` ASC")
    fun getFlow(zip: String, date: String): Flow<List<HourlyForecastEntity>>

    @Query("DELETE FROM hourly_forecast_table")
    suspend fun deleteAll()
}