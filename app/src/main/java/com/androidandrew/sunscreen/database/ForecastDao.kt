package com.androidandrew.sunscreen.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ForecastDao {

    /**
     * UV Forecasts (one forecast = one UV index data point for one location at one hour)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertForecasts(forecast: List<Forecast>)

    @Query("SELECT * FROM forecast_table WHERE date=:date AND location=:location")
    fun getForecasts(date: String, location: String): LiveData<List<Forecast>>

    @Query("SELECT COUNT(*) FROM forecast_table WHERE date=:date AND location=:location")
    fun getForecastsCount(date: String, location: String): Int

    @Query("SELECT * FROM forecast_table WHERE date>:date AND location=:location ORDER BY date ASC")
    fun getForecastsInFuture(date: String, location: String): LiveData<List<Forecast>>

    @Query("DELETE FROM forecast_table")
    fun deleteForecasts()
}