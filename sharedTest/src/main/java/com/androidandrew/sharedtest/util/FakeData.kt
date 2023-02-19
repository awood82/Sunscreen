package com.androidandrew.sharedtest.util

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FakeData {
    companion object {
        const val zip = "12345"
        const val dateNetworkFormatted = "Sep/25/2022"
        const val nextDateNetworkFormatted = "Sep/26/2022"
        val localDate: LocalDate = LocalDate.of(2022, 9, 25)
        val nextLocalDate: LocalDate = LocalDate.of(2022, 9, 26)

        val noon: Instant = Instant.parse("2022-09-25T12:00:00.00Z")
        val clockDefaultNoon: Clock = Clock.fixed(noon, ZoneId.of("UTC"))
    }
}