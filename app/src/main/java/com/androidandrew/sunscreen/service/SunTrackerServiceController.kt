package com.androidandrew.sunscreen.service

import android.content.Context
import android.content.Intent
import timber.log.Timber

class SunTrackerServiceController(private val appContext: Context, private val serviceIntent: Intent) {

    fun start() {
        Timber.d("Trying to start SunTrackerService")
        appContext.startForegroundService(serviceIntent)
    }

    fun stop() {
        Timber.d("Trying to stop SunTrackerService")
        appContext.stopService(serviceIntent)
    }
}