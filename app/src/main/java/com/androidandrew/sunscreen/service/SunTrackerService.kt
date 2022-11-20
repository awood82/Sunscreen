package com.androidandrew.sunscreen.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.DefaultLifecycleObserver
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.time.RepeatingTimer
import com.androidandrew.sunscreen.tracker.UvFactor
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import com.androidandrew.sunscreen.tracker.vitamind.VitaminDCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit

class SunTrackerService : Service(), DefaultLifecycleObserver {

    companion object {
        const val CHANNEL_ID: String = "SunTrackerService" //this::class.java.name
        const val NOTIFICATION_NAME: String = "SunTrackerService" //this::class.java.name
        const val NOTIFICATION_ID = 4242
    }

    // Expose public functions to clients
    private val binder = LocalBinder()

    inner class LocalBinder: Binder() {
        fun getService(): SunTrackerService = this@SunTrackerService
    }

    fun setUvPrediction(prediction: UvPrediction) {
        uvPrediction = prediction
    }

    fun setClock(newClock: Clock) {
        clock = newClock
    }

    fun setSkinType(skinType: Int) {
        hardcodedSkinType = skinType
    }

    fun setSpf(newSpf: Int) {
        spf = newSpf
    }

    fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean) {
        isOnReflectiveSurface = isOnSnowOrWater
    }

    // TODO: These need to be passed in as arguments or read from repository
    private lateinit var uvPrediction: UvPrediction
    private var clock: Clock = Clock.systemDefaultZone()
    private var hardcodedSkinType = 2 // TODO: Remove hardcoded value
    private var userTracking: UserTracking? = null
    private var spf = 0
    private var isOnReflectiveSurface = false

    private val notificationHandler: INotificationHandler by inject { parametersOf(CHANNEL_ID) }
    private val sunscreenRepository: SunscreenRepository by inject()
    private var trackingTimer: RepeatingTimer? = null
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        Timber.d("SunTrackerService - onCreate")
        initializeUserTrackingInfo()

        notificationHandler.createChannel(name = NOTIFICATION_NAME)
        val notification = notificationHandler.buildNotification(
            title = getString(R.string.notification_title),
            text = getString(R.string.notification_text)
        )
        startForeground(NOTIFICATION_ID, notification)

        startTracking()
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
        trackingTimer?.cancel()
        notificationHandler.deleteChannel()
    }

    private fun getDate(): String {
        return LocalDate.now(clock).toString()
    }

    private fun getTime(): LocalTime {
        return LocalTime.now(clock)
    }

    private fun initializeUserTrackingInfo() {
        serviceScope.launch {
            userTracking = sunscreenRepository.getUserTrackingInfo(getDate())
            if (userTracking == null) {
                userTracking = UserTracking(
                    date = getDate(),
                    burnProgress = 0.0,
                    vitaminDProgress = 0.0
                )
            }
        }
    }

    private fun startTracking() {
        Timber.d("SunTrackerService - startTracking")
        trackingTimer?.cancel()
        trackingTimer = createTrackingTimer().also {
            it.start()
        }
    }

    private fun createTrackingTimer(): RepeatingTimer {
        return RepeatingTimer(object : TimerTask() {
            override fun run() {
                if (::uvPrediction.isInitialized) {
                    serviceScope.launch {
                        Timber.d("Updating burn and vitamin D progress")
                        updateTracking(getBurnProgress(), getVitaminDProgress())
                    }
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1))
    }

    suspend fun updateTracking(burnDelta: Double = 0.0, vitaminDDelta: Double = 0.0) {
        userTracking = UserTracking(
            date = getDate(),
            burnProgress = (userTracking?.burnProgress ?: 0.0).plus(burnDelta),
            vitaminDProgress = (userTracking?.vitaminDProgress ?: 0.0).plus(vitaminDDelta)
        )
        sunscreenRepository.setUserTrackingInfo(userTracking!!)
    }

    private fun getBurnProgress(): Double {
        return SunburnCalculator.computeSunUnitsInOneMinute(
                uvIndex = uvPrediction.getUvNow(getTime()),
                skinType = hardcodedSkinType,
                spf = spf,
                altitudeInKm = 0,
                isOnSnowOrWater = isOnReflectiveSurface
        ) / TimeUnit.MINUTES.toSeconds(1)
    }

    private fun getVitaminDProgress(): Double {
        return VitaminDCalculator.computeIUVitaminDInOneMinute(
                uvIndex = uvPrediction.getUvNow(getTime()),
                skinType = hardcodedSkinType,
                clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
                spf = spf,
                altitudeInKm = 0
        ) / TimeUnit.MINUTES.toSeconds(1)
    }
}