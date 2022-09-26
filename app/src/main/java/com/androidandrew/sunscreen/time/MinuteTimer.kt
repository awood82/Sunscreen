package com.androidandrew.sunscreen.time

import java.util.*

class MinuteTimer(private val timerTask: TimerTask) : Timer() {

    companion object {
        private const val ONE_SECOND = 1000L // milliseconds in one second
        private const val ONE_MINUTE = 60 * ONE_SECOND // millis in one minute
    }

    fun start() {
        this.scheduleAtFixedRate(timerTask, ONE_MINUTE, ONE_MINUTE)
    }
}