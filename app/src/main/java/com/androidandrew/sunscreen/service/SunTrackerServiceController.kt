package com.androidandrew.sunscreen.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.tracksunexposure.SunTracker
import com.androidandrew.sunscreen.tracksunexposure.SunTrackerSettings
import timber.log.Timber

class SunTrackerServiceController(private val appContext: Context, private val serviceIntent: Intent) {

    private var sunTracker: SunTracker? = null
    private lateinit var sunTrackerSettings: SunTrackerSettings

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            val binder = service as SunTrackerService.LocalBinder
            sunTracker = binder.getService()
            sunTracker?.let {
                sendSettingsToSunTracker()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected")
        }
    }

    fun setSettings(sunTrackerSettings: SunTrackerSettings) {
        Timber.d("Received new settings, but haven't sent them yet")
        this.sunTrackerSettings = sunTrackerSettings
        sendSettingsToSunTracker()
    }

    fun setSettings(uvPrediction: UvPrediction, skinType: Int, spf: Int, isOnSnowOrWater: Boolean) {
        this.setSettings(
            SunTrackerSettings(
                uvPrediction = uvPrediction,
                hardcodedSkinType = skinType,
                spf = spf,
                isOnReflectiveSurface = isOnSnowOrWater
            )
        )
    }

    fun setSpf(spf: Int) {
        if (::sunTrackerSettings.isInitialized) {
            sunTrackerSettings = sunTrackerSettings.copy(spf = spf)
            sendSettingsToSunTracker()
        }
    }

    fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean) {
        if (::sunTrackerSettings.isInitialized) {
            sunTrackerSettings = sunTrackerSettings.copy(isOnReflectiveSurface = isOnSnowOrWater)
            sendSettingsToSunTracker()
        }
    }

    private fun sendSettingsToSunTracker() {
        (sunTracker as SunTracker?)?.let {
            it.setSettings(sunTrackerSettings)
            Timber.d("Settings were sent to the service")
        } ?: Timber.w("Service has not started yet. Settings were not sent.")
    }

    fun bind() {
        Timber.d("Trying to bind to SunTrackerService")
        appContext.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind() {
        Timber.d("Trying to unbind from SunTrackerService")
        appContext.unbindService(connection)
        sunTracker = null
    }

    fun start() {
        Timber.d("Trying to start SunTrackerService")
        appContext.startForegroundService(serviceIntent)
    }

    fun stop() {
        Timber.d("Trying to stop SunTrackerService")
        appContext.stopService(serviceIntent)
    }
}