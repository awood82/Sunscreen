package com.androidandrew.sunscreen.common

import java.util.*
import java.util.concurrent.TimeUnit

class RepeatingTimer(private val timerTask: TimerTask,
                     private val delayMillis: Long, private val periodMillis: Long) : Timer() {

    companion object {
        val ONE_MINUTE = TimeUnit.MINUTES.toMillis(1)
    }

    fun start() {
        this.scheduleAtFixedRate(timerTask, delayMillis, periodMillis)
    }
}