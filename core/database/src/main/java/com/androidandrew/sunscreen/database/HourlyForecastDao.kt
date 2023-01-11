package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.HourlyForecastEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class HourlyForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(forecast: List<HourlyForecastEntity>)

    @Query("SELECT * FROM hourly_forecast_table WHERE zip = :zip AND date =" +
            " (SELECT MIN(date) FROM hourly_forecast_table WHERE zip = :zip AND date >= :date)" +
            "ORDER BY date DESC, `order` ASC")
    abstract suspend fun getOnce(zip: String, date: String): List<HourlyForecastEntity>

    fun getDistinctFlow(zip: String, date: String): Flow<List<HourlyForecastEntity>> {
        return getFlow(zip, date).distinctUntilChanged()
    }

    @Query("SELECT * FROM hourly_forecast_table WHERE zip = :zip AND date =" +
            " (SELECT MIN(date) FROM hourly_forecast_table WHERE zip = :zip AND date >= :date)" +
            "ORDER BY date DESC, `order` ASC")
    protected abstract fun getFlow(zip: String, date: String): Flow<List<HourlyForecastEntity>>

    @Query("DELETE FROM hourly_forecast_table")
    abstract suspend fun deleteAll()
}