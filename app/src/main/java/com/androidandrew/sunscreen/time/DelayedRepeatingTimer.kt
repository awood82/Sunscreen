package com.androidandrew.sunscreen.time

import java.util.*

class DelayedRepeatingTimer(private val timerTask: TimerTask,
        private val delayMillis: Long, private val periodMillis: Long) : Timer() {

    companion object {
        const val ONE_SECOND = 1000L // milliseconds in one second
        const val ONE_MINUTE = 60 * ONE_SECOND // millis in one minute
    }

    fun start() {
        this.scheduleAtFixedRate(timerTask, delayMillis, periodMillis)
    }
}