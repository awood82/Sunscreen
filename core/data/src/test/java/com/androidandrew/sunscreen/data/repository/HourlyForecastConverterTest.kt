package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.network.model.HourlyUvIndexForecast
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class HourlyForecastConverterTest {

    private val forecast6am = HourlyUvIndexForecast(6, "92101", "Sep/25/2022 06 AM", 2)
    private val forecast12pm = HourlyUvIndexForecast(12, "92101", "Sep/25/2022 12 PM", 14)
    private val forecast6pm = HourlyUvIndexForecast(18, "92101", "Sep/25/2022 06 PM", 2)
    private val forecast12amTomorrow = HourlyUvIndexForecast(21, "92101", "Sep/26/2022 12 AM", 0)

    private val sampleDailyUvIndexForecast = listOf(forecast6am, forecast12pm, forecast6pm)

    @Test
    fun hourlyUvIndexForecast_asUvPredictionPoint_convertsUvIndex() {
        val hourlyForecastEntity = forecast12pm.asEntity()

        assertEquals(14, hourlyForecastEntity.uvi.toInt())
    }

    @Test
    fun hourlyUvIndexForecast_asEntity_convertsNoonTime() {
        val expectedTime = LocalTime.NOON

        val hourlyForecastEntity = forecast12pm.asEntity()

        assertEquals(expectedTime.toString(), hourlyForecastEntity.time)
    }

    @Test
    fun hourlyUvIndexForecast_asUvPredictionPoint_convertsEveningTime() {
        val expectedTime = LocalTime.NOON.plusHours(6)

        val hourlyForecastEntity = forecast6pm.asEntity()

        assertEquals(expectedTime.toString(), hourlyForecastEntity.time)
    }

    @Test
    fun dailyUvIndexForecast_asUvPrediction_convertsUvIndex() {
        val hourlyForecastEntity = sampleDailyUvIndexForecast.asEntity()

        assertEquals(2, hourlyForecastEntity[0].uvi.toInt())
        assertEquals(14, hourlyForecastEntity[1].uvi.toInt())
        assertEquals(2, hourlyForecastEntity[2].uvi.toInt())
    }

    @Test
    fun dailyUvIndexForecast_asUvPrediction_outOfOrder_isSorted() {
        val unsortedForecast = listOf(forecast12pm, forecast6am, forecast6pm)

        val hourlyForecastEntity = unsortedForecast.asEntity()

        assertEquals(2, hourlyForecastEntity[0].uvi.toInt())
        assertEquals(14, hourlyForecastEntity[1].uvi.toInt())
        assertEquals(2, hourlyForecastEntity[2].uvi.toInt())
    }

    // The data.epa.gov endpoint often returns a midnight value, which messes up the repo calls
    // for tomorrow when it looks to see if any data already.
    @Test
    fun dailyUvIndexForecast_asUvPrediction_withExtraMidnightValue_removesMidnight() {
        val forecast = listOf(forecast6am, forecast12pm, forecast6pm, forecast12amTomorrow)

        val hourlyForecastEntity = forecast.asEntity()

        assertEquals(3, hourlyForecastEntity.size)
        assertEquals(2, hourlyForecastEntity[0].uvi.toInt())
        assertEquals(14, hourlyForecastEntity[1].uvi.toInt())
        assertEquals(2, hourlyForecastEntity[2].uvi.toInt())
    }

    @Test
    fun asLocalTime_convertsToLocalTime() {
        val networkResponse = "Sep/25/2022 06 AM"

        val actual = networkResponse.asLocalTime()

        assertEquals(LocalTime.of(6, 0), actual)
    }

    @Test
    fun asLocalDate_convertsToLocalDate() {
        val networkResponse = "Sep/25/2022 06 AM"

        val actual = networkResponse.asLocalDate()

        assertEquals(LocalDate.of(2022, 9, 25), actual)
    }
}