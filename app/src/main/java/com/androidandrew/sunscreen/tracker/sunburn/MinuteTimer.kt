package com.androidandrew.sunscreen.tracker.sunburn

import java.util.*

class MinuteTimer(private val timerTask: TimerTask) : Timer() {

    companion object {
        private val ONE_SECOND = 1000L // milliseconds in one second
        private val ONE_MINUTE = 60 * ONE_SECOND // millis in one minute
    }

    private var startTime: Long = 0
    private var _timer = Timer()

    fun start() {
        _timer = Timer()
        _timer.scheduleAtFixedRate(timerTask, ONE_SECOND*5, ONE_SECOND*5)
    }

    fun stop() {
        _timer.cancel()
    }
}