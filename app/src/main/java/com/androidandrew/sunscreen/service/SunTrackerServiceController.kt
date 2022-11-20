package com.androidandrew.sunscreen.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import timber.log.Timber
import java.time.Clock

class SunTrackerServiceController(private val appContext: Context, private val clock: Clock) {

    private var sunTrackerService: SunTrackerService? = null
    private lateinit var uvPrediction: UvPrediction
    private var skinType = 0
    private var spf = 0
    private var isOnSnowOrWater = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            val binder = service as SunTrackerService.LocalBinder
            sunTrackerService = binder.getService()

            sunTrackerService?.setUvPrediction(uvPrediction)
            sunTrackerService?.setClock(clock)
            sunTrackerService?.setSkinType(skinType)
            sunTrackerService?.setSpf(spf)
            sunTrackerService?.setIsOnSnowOrWater(isOnSnowOrWater)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected")
        }
    }

    fun bind(uvPrediction: UvPrediction, skinType: Int, spf: Int, isOnSnowOrWater: Boolean) {
        Timber.d("Trying to bind to SunTrackerService")
        this.uvPrediction = uvPrediction
        this.skinType = skinType
        this.spf = spf
        this.isOnSnowOrWater = isOnSnowOrWater
        getServiceIntent().also { intent ->
            appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        Timber.d("Trying to unbind from SunTrackerService")
        appContext.unbindService(connection)
        sunTrackerService = null
    }

    fun start() {
        Timber.d("Trying to start SunTrackerService")
        appContext.startForegroundService(getServiceIntent())
    }

    fun stop() {
        Timber.d("Trying to stop SunTrackerService")
        appContext.stopService(getServiceIntent())
    }

    private fun getServiceIntent(): Intent {
        return Intent(appContext, SunTrackerService::class.java)
    }
}