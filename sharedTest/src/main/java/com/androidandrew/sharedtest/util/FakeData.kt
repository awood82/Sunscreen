package com.androidandrew.sharedtest.util

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class FakeData {
    companion object {
        val noon = Instant.parse("2022-09-25T12:00:00.00Z")
        val clockDefaultNoon = Clock.fixed(noon, ZoneId.of("UTC"))
    }
}