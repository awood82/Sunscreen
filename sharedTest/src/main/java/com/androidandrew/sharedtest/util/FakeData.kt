package com.androidandrew.sharedtest.util

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FakeData {
    companion object {
        val zip = "12345"
        val dateNetworkFormatted = "Sep/25/2022"
        val nextDateNetworkFormatted = "Sep/26/2022"
        val localDate = LocalDate.of(2022, 9, 25)

        val noon = Instant.parse("2022-09-25T12:00:00.00Z")
        val clockDefaultNoon = Clock.fixed(noon, ZoneId.of("UTC"))
    }
}