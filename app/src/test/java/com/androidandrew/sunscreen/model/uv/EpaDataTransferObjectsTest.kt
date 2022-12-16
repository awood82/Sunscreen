package com.androidandrew.sunscreen.model.uv

import com.androidandrew.sunscreen.network.HourlyUvIndexForecast
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class EpaDataTransferObjectsTest {

    private val forecast6am = HourlyUvIndexForecast(6, "92101", "Sep/25/2022 06 AM", 2)
    private val forecast12pm = HourlyUvIndexForecast(12, "92101", "Sep/25/2022 12 PM", 14)
    private val forecast6pm = HourlyUvIndexForecast(18, "92101", "Sep/25/2022 06 PM", 2)

    private val sampleDailyUvIndexForecast = listOf(forecast6am, forecast12pm, forecast6pm)

    @Test
    fun hourlyUvIndexForecast_asUvPredictionPoint_convertsUvIndex() {
        val uvPredictionPoint = forecast12pm.asUvPredictionPoint()

        assertEquals(14, uvPredictionPoint.uvIndex.toInt())
    }

    @Test
    fun hourlyUvIndexForecast_asUvPredictionPoint_convertsNoonTime() {
        val expectedTime = LocalTime.NOON

        val uvPredictionPoint = forecast12pm.asUvPredictionPoint()

        assertEquals(expectedTime, uvPredictionPoint.time)
    }

    @Test
    fun hourlyUvIndexForecast_asUvPredictionPoint_convertsEveningTime() {
        val expectedTime = LocalTime.NOON.plusHours(6)

        val uvPredictionPoint = forecast6pm.asUvPredictionPoint()

        assertEquals(expectedTime, uvPredictionPoint.time)
    }

    @Test
    fun dailyUvIndexForecast_asUvPrediction_convertsUvIndex() {
        val uvPrediction = sampleDailyUvIndexForecast.asUvPrediction()

        assertEquals(2, uvPrediction[0].uvIndex.toInt())
        assertEquals(14, uvPrediction[1].uvIndex.toInt())
        assertEquals(2, uvPrediction[2].uvIndex.toInt())
    }

    @Test
    fun dailyUvIndexForecast_asUvPrediction_outOfOrder_isSorted() {
        val unsortedForecast = listOf(forecast12pm, forecast6am, forecast6pm)

        val uvPrediction = unsortedForecast.asUvPrediction()

        assertEquals(2, uvPrediction[0].uvIndex.toInt())
        assertEquals(14, uvPrediction[1].uvIndex.toInt())
        assertEquals(2, uvPrediction[2].uvIndex.toInt())
    }
}