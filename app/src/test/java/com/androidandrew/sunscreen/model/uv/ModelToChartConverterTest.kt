package com.androidandrew.sunscreen.model.uv

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.androidandrew.sharedtest.model.FakeUvPredictions
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.trim
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelToChartConverterTest {

    @Test
    fun noPrediction_toChartData_hasNoData() {
        val noPredictions: UvPrediction = emptyList()

        val data = noPredictions.toChartData()

        assertTrue(data.values.isEmpty())
    }

    @Test
    fun prediction_toChartData_containsData() {
        // First entries not trimmed:
        //   HourlyUvIndexForecast(5, FakeData.zip, "${FakeData.dateNetworkFormatted} 08 AM", 0), i.e UVI 0 at 8A<
        //   HourlyUvIndexForecast(6, FakeData.zip, "${FakeData.dateNetworkFormatted} 09 AM", 2), i.e. UVI 2 at 9AM
        val predictions: UvPrediction = FakeUvPredictions.forecast.trim()

        val data = predictions.toChartData("new label")

        assertTrue(data.values.isNotEmpty())
        assertEquals(predictions.size, data.values.size)
        assertEquals(8f, data.values[0].x)
        assertEquals(0f, data.values[0].y)
        assertEquals(9f, data.values[1].x)
        assertEquals(2f, data.values[1].y)
        assertEquals("new label", data.label)
    }
}