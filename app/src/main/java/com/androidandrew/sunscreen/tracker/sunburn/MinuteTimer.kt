package com.androidandrew.sunscreen.tracker.sunburn

import java.util.*

class MinuteTimer(private val timerTask: TimerTask, private val delay: Long = ONE_MINUTE) : Timer() {

    companion object {
        private const val ONE_SECOND = 1000L // milliseconds in one second
        private const val ONE_MINUTE = 60 * ONE_SECOND // millis in one minute
    }

    private var _timer = Timer()

    fun start() {
        _timer = Timer()
        _timer.scheduleAtFixedRate(timerTask, delay, ONE_MINUTE)
    }

    fun stop() {
        _timer.cancel()
    }
}