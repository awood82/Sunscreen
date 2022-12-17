package com.androidandrew.sunscreen.tracksunexposure

import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.common.toDateString
import com.androidandrew.sunscreen.common.toTime
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.model.getUvNow
import com.androidandrew.sunscreen.uvcalculators.UvFactor
import com.androidandrew.sunscreen.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.uvcalculators.vitamind.VitaminDCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.util.*
import java.util.concurrent.TimeUnit

class SunTracker(private val sunscreenRepository: UserRepositoryImpl, private val clock: Clock) {

    private lateinit var settings: SunTrackerSettings
    private var userTracking: UserTracking? = null
    private var trackingTimer: RepeatingTimer? = null
    private val trackerJob = Job()
    private val trackerScope = CoroutineScope(Dispatchers.Main + trackerJob)

    fun setSettings(sunTrackerSettings: SunTrackerSettings) {
        settings = sunTrackerSettings
    }

    fun startTracking() {
        Timber.d("SunTrackerService - startTracking")
        initializeUserTrackingInfo()
        trackingTimer?.cancel()
        trackingTimer = createTrackingTimer().also {
            it.start()
        }
    }

    fun stopTracking() {
        trackingTimer?.cancel()
    }

    private fun initializeUserTrackingInfo() {
        trackerScope.launch {
            userTracking = sunscreenRepository.getUserTrackingInfo(clock.toDateString())
            if (userTracking == null) {
                userTracking = UserTracking(
                    date = clock.toDateString(),
                    burnProgress = 0.0,
                    vitaminDProgress = 0.0
                )
            }
        }
    }

    private fun createTrackingTimer(): RepeatingTimer {
        return RepeatingTimer(object : TimerTask() {
            override fun run() {
                if (::settings.isInitialized) {
                    trackerScope.launch {
                        Timber.d("Updating burn and vitamin D progress")
                        updateTracking(getBurnProgress(), getVitaminDProgress())
                    }
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1))
    }

    suspend fun updateTracking(burnDelta: Double = 0.0, vitaminDDelta: Double = 0.0) {
        userTracking = UserTracking(
            date = clock.toDateString(),
            burnProgress = (userTracking?.burnProgress ?: 0.0).plus(burnDelta),
            vitaminDProgress = (userTracking?.vitaminDProgress ?: 0.0).plus(vitaminDDelta)
        )
        sunscreenRepository.setUserTrackingInfo(userTracking!!)
    }

    private fun getBurnProgress(): Double {
        return SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = settings.uvPrediction.getUvNow(clock.toTime()),
            skinType = settings.hardcodedSkinType,
            spf = settings.spf,
            altitudeInKm = 0,
            isOnSnowOrWater = settings.isOnReflectiveSurface
        ) / TimeUnit.MINUTES.toSeconds(1)
    }

    private fun getVitaminDProgress(): Double {
        return VitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = settings.uvPrediction.getUvNow(clock.toTime()),
            skinType = settings.hardcodedSkinType,
            clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
            spf = settings.spf,
            altitudeInKm = 0
        ) / TimeUnit.MINUTES.toSeconds(1)
    }
}