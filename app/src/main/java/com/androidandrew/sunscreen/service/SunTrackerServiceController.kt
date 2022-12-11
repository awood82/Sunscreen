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

    private var sunTracker: ISunTracker? = null
    private lateinit var sunTrackerSettings: SunTrackerSettings

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected")
            val binder = service as SunTrackerService.LocalBinder
            sunTracker = binder.getService()

            sendSettingsToService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected")
        }
    }

    fun setSettings(sunTrackerSettings: SunTrackerSettings) {
        Timber.d("Received new settings, but haven't sent them yet")
        this.sunTrackerSettings = sunTrackerSettings
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

    private fun sendSettingsToService() {
        (sunTracker as SunTracker?)?.let {
            it.setSettings(sunTrackerSettings)
            Timber.d("Settings were sent to the service")
        } ?: Timber.w("Service has not started yet. Settings were not sent.")
    }

    fun bind() {
        Timber.d("Trying to bind to SunTrackerService")
        getServiceIntent().also { intent ->
            appContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        Timber.d("Trying to unbind from SunTrackerService")
        appContext.unbindService(connection)
        sunTracker = null
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