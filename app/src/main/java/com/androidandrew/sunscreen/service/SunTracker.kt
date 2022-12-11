package com.androidandrew.sunscreen.service

import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.time.RepeatingTimer
import com.androidandrew.sunscreen.tracker.UvFactor
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import com.androidandrew.sunscreen.tracker.vitamind.VitaminDCalculator
import com.androidandrew.sunscreen.util.toDateString
import com.androidandrew.sunscreen.util.toTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.util.*
import java.util.concurrent.TimeUnit

class SunTracker(private val sunscreenRepository: SunscreenRepository, private val clock: Clock) : ISunTracker {

    private lateinit var settings: SunTrackerSettings
    private var userTracking: UserTracking? = null
    private var trackingTimer: RepeatingTimer? = null
    private val trackerJob = Job()
    private val trackerScope = CoroutineScope(Dispatchers.Main + trackerJob)

    init {
        initializeUserTrackingInfo()
    }

    fun setSettings(sunTrackerSettings: SunTrackerSettings) {
        settings = sunTrackerSettings
    }

    override fun startTracking() {
        Timber.d("SunTrackerService - startTracking")
        if (!::settings.isInitialized) {
            Timber.e("startTracking called before setSettings")
            throw IllegalStateException()
        }
        trackingTimer?.cancel()
        trackingTimer = createTrackingTimer().also {
            it.start()
        }
    }

    override fun stopTracking() {
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