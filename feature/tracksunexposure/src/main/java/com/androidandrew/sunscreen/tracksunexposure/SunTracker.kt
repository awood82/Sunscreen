package com.androidandrew.sunscreen.tracksunexposure

import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.common.toDateString
import com.androidandrew.sunscreen.common.toTime
import com.androidandrew.sunscreen.data.repository.UserTrackingRepository
import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.model.getUvNow
import com.androidandrew.sunscreen.domain.UvFactor
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.domain.uvcalculators.vitamind.VitaminDCalculator
import com.androidandrew.sunscreen.model.UserTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.util.*
import java.util.concurrent.TimeUnit

class SunTracker(private val userTrackingRepository: UserTrackingRepository, private val clock: Clock) {

    private val spfUseCase = ConvertSpfUseCase()
    private val sunburnCalculator = SunburnCalculator(spfUseCase)
    private val vitaminDCalculator = VitaminDCalculator(spfUseCase)
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
            userTracking = userTrackingRepository.getUserTracking(clock.toDateString())
            if (userTracking == null) {
                userTracking = UserTracking(
                    sunburnProgress = 0.0,
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
            sunburnProgress = (userTracking?.sunburnProgress ?: 0.0).plus(burnDelta),
            vitaminDProgress = (userTracking?.vitaminDProgress ?: 0.0).plus(vitaminDDelta)
        )
        userTrackingRepository.setUserTracking(clock.toDateString(), userTracking!!)
    }

    private fun getBurnProgress(): Double {
        return sunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = settings.uvPrediction.getUvNow(clock.toTime()),
            skinType = settings.hardcodedSkinType,
            spf = settings.spf,
            altitudeInKm = 0,
            isOnSnowOrWater = settings.isOnReflectiveSurface
        ) / TimeUnit.MINUTES.toSeconds(1)
    }

    private fun getVitaminDProgress(): Double {
        return vitaminDCalculator.computeIUVitaminDInOneMinute(
            uvIndex = settings.uvPrediction.getUvNow(clock.toTime()),
            skinType = settings.hardcodedSkinType,
            clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
            spf = settings.spf,
            altitudeInKm = 0
        ) / TimeUnit.MINUTES.toSeconds(1)
    }
}