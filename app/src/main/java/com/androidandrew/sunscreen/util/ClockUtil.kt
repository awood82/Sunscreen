package com.androidandrew.sunscreen.util

import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime

fun Clock.toDateString(): String = LocalDate.now(this).toString()

fun Clock.toTime(): LocalTime = LocalTime.now(this)