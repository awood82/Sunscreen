package com.androidandrew.sunscreen.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import com.androidandrew.sunscreen.R
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class SunTrackerService : Service(), DefaultLifecycleObserver {

    companion object {
        const val CHANNEL_ID: String = "SunTrackerService" //this::class.java.name
        const val NOTIFICATION_NAME: String = "SunTrackerService" //this::class.java.name
        const val NOTIFICATION_ID = 4242
    }

    // Expose public functions to clients
    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
//        fun getService(): SunTrackerService = this@SunTrackerService
        fun getService(): ISunTracker = sunTracker
    }

    private val notificationHandler: INotificationHandler by inject { parametersOf(CHANNEL_ID) }
    private val sunTracker: ISunTracker by inject()

    override fun onCreate() {
        Timber.d("SunTrackerService - onCreate")

        notificationHandler.createChannel(name = NOTIFICATION_NAME)
        val notification = notificationHandler.buildNotification(
            title = getString(R.string.notification_title),
            text = getString(R.string.notification_text)
        )
        startForeground(NOTIFICATION_ID, notification)

        sunTracker.startTracking()
    }

    override fun onBind(intent: Intent?): IBinder {
        Timber.d("SunTrackerService - onBind")
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("SunTrackerService - onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("SunTrackerService - onDestroy")
        sunTracker.stopTracking()
        notificationHandler.deleteChannel()
    }
}