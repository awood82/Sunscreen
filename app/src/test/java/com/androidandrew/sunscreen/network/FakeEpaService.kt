package com.androidandrew.sunscreen.network

object FakeEpaService : EpaService {

    const val sampleResponse = """
        [{"ORDER": 1, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 04 AM", "UV_VALUE": 0}, {"ORDER": 2, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 05 AM", "UV_VALUE": 0}, {"ORDER": 3, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 06 AM", "UV_VALUE": 0}, {"ORDER": 4, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 07 AM", "UV_VALUE": 0}, {"ORDER": 5, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 08 AM", "UV_VALUE": 0}, {"ORDER": 6, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 09 AM", "UV_VALUE": 2}, {"ORDER": 7, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 10 AM", "UV_VALUE": 4}, {"ORDER": 8, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 11 AM", "UV_VALUE": 6}, {"ORDER": 9, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 12 PM", "UV_VALUE": 8}, {"ORDER": 10, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 01 PM", "UV_VALUE": 8}, {"ORDER": 11, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 02 PM", "UV_VALUE": 7}, {"ORDER": 12, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 03 PM", "UV_VALUE": 5}, {"ORDER": 13, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 04 PM", "UV_VALUE": 3}, {"ORDER": 14, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 05 PM", "UV_VALUE": 1}, {"ORDER": 15, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 06 PM", "UV_VALUE": 0}, {"ORDER": 16, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 07 PM", "UV_VALUE": 0}, {"ORDER": 17, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 08 PM", "UV_VALUE": 0}, {"ORDER": 18, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 09 PM", "UV_VALUE": 0}, {"ORDER": 19, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 10 PM", "UV_VALUE": 0}, {"ORDER": 20, "ZIP": "92101", "DATE_TIME": "Sep/25/2022 11 PM", "UV_VALUE": 0}, {"ORDER": 21, "ZIP": "92101", "DATE_TIME": "Sep/26/2022 12 AM", "UV_VALUE": 0}]
    """

    override suspend fun getUvForecast(): String {
        return sampleResponse
    }
}