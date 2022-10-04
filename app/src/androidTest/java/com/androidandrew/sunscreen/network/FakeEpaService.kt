package com.androidandrew.sunscreen.network

import java.io.IOException
import java.time.format.DateTimeFormatter

class FakeEpaService : EpaService {

    val dateOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM/dd/yyyy")

    private val location = "92123"
    private val date = "Sep/25/2022" //LocalDate.now().format(dateOnlyFormatter)
//    private val sampleResponse = """
//        [HourlyUvIndexForecast(1, "92101", "Sep/25/2022 04 AM", 0}, HourlyUvIndexForecast(2, "92101", "Sep/25/2022 05 AM", 0}, HourlyUvIndexForecast(3, "92101", "Sep/25/2022 06 AM", 0}, HourlyUvIndexForecast(4, "92101", "Sep/25/2022 07 AM", 0}, HourlyUvIndexForecast(5, "92101", "Sep/25/2022 08 AM", 0}, HourlyUvIndexForecast(6, "92101", "Sep/25/2022 09 AM", 2}, HourlyUvIndexForecast(7, "92101", "Sep/25/2022 10 AM", 4}, HourlyUvIndexForecast(8, "92101", "Sep/25/2022 11 AM", 6}, HourlyUvIndexForecast(9, "92101", "Sep/25/2022 12 PM", 8}, HourlyUvIndexForecast(10, "92101", "Sep/25/2022 01 PM", 8}, HourlyUvIndexForecast(11, "92101", "Sep/25/2022 02 PM", 7}, HourlyUvIndexForecast(12, "92101", "Sep/25/2022 03 PM", 5}, HourlyUvIndexForecast(13, "92101", "Sep/25/2022 04 PM", 3}, HourlyUvIndexForecast(14, "92101", "Sep/25/2022 05 PM", 1}, HourlyUvIndexForecast(15, "92101", "Sep/25/2022 06 PM", 0}, HourlyUvIndexForecast(16, "92101", "Sep/25/2022 07 PM", 0}, HourlyUvIndexForecast(17, "92101", "Sep/25/2022 08 PM", 0}, HourlyUvIndexForecast(18, "92101", "Sep/25/2022 09 PM", 0}, HourlyUvIndexForecast(19, "92101", "Sep/25/2022 10 PM", 0}, HourlyUvIndexForecast(20, "92101", "Sep/25/2022 11 PM", 0}, HourlyUvIndexForecast(21, "92101", "Sep/26/2022 12 AM", 0}]
//    """
    val sampleDailyUvForecast = listOf(
        HourlyUvIndexForecast(1, location, "$date 04 AM", 0),
        HourlyUvIndexForecast(2, location, "$date 05 AM", 0),
        HourlyUvIndexForecast(3, location, "$date 06 AM", 0),
        HourlyUvIndexForecast(4, location, "$date 07 AM", 0),
        HourlyUvIndexForecast(5, location, "$date 08 AM", 0),
        HourlyUvIndexForecast(6, location, "$date 09 AM", 2),
        HourlyUvIndexForecast(7, location, "$date 10 AM", 4),
        HourlyUvIndexForecast(8, location, "$date 11 AM", 6),
        HourlyUvIndexForecast(9, location, "$date 12 PM", 8),
        HourlyUvIndexForecast(10, location, "$date 01 PM", 13),
        HourlyUvIndexForecast(11, location, "$date 02 PM", 7),
        HourlyUvIndexForecast(12, location, "$date 03 PM", 5),
        HourlyUvIndexForecast(13, location, "$date 04 PM", 3),
        HourlyUvIndexForecast(14, location, "$date 05 PM", 1),
        HourlyUvIndexForecast(15, location, "$date 06 PM", 0),
        HourlyUvIndexForecast(16, location, "$date 07 PM", 0),
        HourlyUvIndexForecast(17, location, "$date 08 PM", 0),
        HourlyUvIndexForecast(18, location, "$date 09 PM", 0),
        HourlyUvIndexForecast(19, location, "$date 10 PM", 0),
        HourlyUvIndexForecast(20, location, "$date 11 PM", 0),
        HourlyUvIndexForecast(21, location, "Sep/26/2022 12 AM", 0)
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