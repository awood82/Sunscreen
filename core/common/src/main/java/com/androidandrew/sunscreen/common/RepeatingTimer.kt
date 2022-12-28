package com.androidandrew.sunscreen.common

import java.util.*
import java.util.concurrent.TimeUnit

class RepeatingTimer(private val timerTask: TimerTask,
                     private val delayMillis: Long, private val periodMillis: Long) : Timer() {

    private var isStarted = false

    companion object {
        val ONE_MINUTE = TimeUnit.MINUTES.toMillis(1)
    }

    fun start() {
        startIfNotStartedYet()
    }

    private fun startIfNotStartedYet() {
        if (!isStarted) {
            isStarted = true
            this.scheduleAtFixedRate(timerTask, delayMillis, periodMillis)
        }
    }
}