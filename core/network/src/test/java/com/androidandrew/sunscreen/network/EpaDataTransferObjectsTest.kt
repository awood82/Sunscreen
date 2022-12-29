package com.androidandrew.sunscreen.network

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class EpaDataTransferObjectsTest {

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
