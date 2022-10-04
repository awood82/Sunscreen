package com.androidandrew.sharedtest.network

import com.androidandrew.sunscreen.network.DailyUvIndexForecast
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.HourlyUvIndexForecast
import java.io.IOException

class FakeEpaService : EpaService {

    private val sampleResponse = """
        [HourlyUvIndexForecast(1, "92101", "Sep/25/2022 04 AM", 0}, HourlyUvIndexForecast(2, "92101", "Sep/25/2022 05 AM", 0}, HourlyUvIndexForecast(3, "92101", "Sep/25/2022 06 AM", 0}, HourlyUvIndexForecast(4, "92101", "Sep/25/2022 07 AM", 0}, HourlyUvIndexForecast(5, "92101", "Sep/25/2022 08 AM", 0}, HourlyUvIndexForecast(6, "92101", "Sep/25/2022 09 AM", 2}, HourlyUvIndexForecast(7, "92101", "Sep/25/2022 10 AM", 4}, HourlyUvIndexForecast(8, "92101", "Sep/25/2022 11 AM", 6}, HourlyUvIndexForecast(9, "92101", "Sep/25/2022 12 PM", 8}, HourlyUvIndexForecast(10, "92101", "Sep/25/2022 01 PM", 8}, HourlyUvIndexForecast(11, "92101", "Sep/25/2022 02 PM", 7}, HourlyUvIndexForecast(12, "92101", "Sep/25/2022 03 PM", 5}, HourlyUvIndexForecast(13, "92101", "Sep/25/2022 04 PM", 3}, HourlyUvIndexForecast(14, "92101", "Sep/25/2022 05 PM", 1}, HourlyUvIndexForecast(15, "92101", "Sep/25/2022 06 PM", 0}, HourlyUvIndexForecast(16, "92101", "Sep/25/2022 07 PM", 0}, HourlyUvIndexForecast(17, "92101", "Sep/25/2022 08 PM", 0}, HourlyUvIndexForecast(18, "92101", "Sep/25/2022 09 PM", 0}, HourlyUvIndexForecast(19, "92101", "Sep/25/2022 10 PM", 0}, HourlyUvIndexForecast(20, "92101", "Sep/25/2022 11 PM", 0}, HourlyUvIndexForecast(21, "92101", "Sep/26/2022 12 AM", 0}]
    """
    val sampleDailyUvForecast = listOf(
        HourlyUvIndexForecast(1, "92101", "Sep/25/2022 04 AM", 0),
        HourlyUvIndexForecast(2, "92101", "Sep/25/2022 05 AM", 0),
        HourlyUvIndexForecast(3, "92101", "Sep/25/2022 06 AM", 0),
        HourlyUvIndexForecast(4, "92101", "Sep/25/2022 07 AM", 0),
        HourlyUvIndexForecast(5, "92101", "Sep/25/2022 08 AM", 0),
        HourlyUvIndexForecast(6, "92101", "Sep/25/2022 09 AM", 2),
        HourlyUvIndexForecast(7, "92101", "Sep/25/2022 10 AM", 4),
        HourlyUvIndexForecast(8, "92101", "Sep/25/2022 11 AM", 6),
        HourlyUvIndexForecast(9, "92101", "Sep/25/2022 12 PM", 8),
        HourlyUvIndexForecast(10, "92101", "Sep/25/2022 01 PM", 8),
        HourlyUvIndexForecast(11, "92101", "Sep/25/2022 02 PM", 7),
        HourlyUvIndexForecast(12, "92101", "Sep/25/2022 03 PM", 5),
        HourlyUvIndexForecast(13, "92101", "Sep/25/2022 04 PM", 3),
        HourlyUvIndexForecast(14, "92101", "Sep/25/2022 05 PM", 1),
        HourlyUvIndexForecast(15, "92101", "Sep/25/2022 06 PM", 0),
        HourlyUvIndexForecast(16, "92101", "Sep/25/2022 07 PM", 0),
        HourlyUvIndexForecast(17, "92101", "Sep/25/2022 08 PM", 0),
        HourlyUvIndexForecast(18, "92101", "Sep/25/2022 09 PM", 0),
        HourlyUvIndexForecast(19, "92101", "Sep/25/2022 10 PM", 0),
        HourlyUvIndexForecast(20, "92101", "Sep/25/2022 11 PM", 0),
        HourlyUvIndexForecast(21, "92101", "Sep/26/2022 12 AM", 0)
    )
    private var simulateError = false

    override suspend fun getUvForecast(zipCode: String): DailyUvIndexForecast {
        when (simulateError) {
            true -> throw(IOException("Simulated error"))
            false -> return sampleDailyUvForecast
        }
    }

    fun simulateError() {
        simulateError = true
    }
}