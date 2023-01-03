package com.androidandrew.sunscreen.tracksunexposure

import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserTrackingRepository
import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.model.getUvNow
import com.androidandrew.sunscreen.domain.UvFactor
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.domain.uvcalculators.vitamind.VitaminDCalculator
import com.androidandrew.sunscreen.model.UserTracking
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.Clock
import java.util.*
import java.util.concurrent.TimeUnit

class SunTracker(
    getLocalForecastForToday: GetLocalForecastForTodayUseCase,
    userSettingsRepository: UserSettingsRepository,
    private val userTrackingRepository: UserTrackingRepository,
    private val clock: Clock,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val spfUseCase = ConvertSpfUseCase()
    private val sunburnCalculator = SunburnCalculator(spfUseCase)
    private val vitaminDCalculator = VitaminDCalculator(spfUseCase)
    private lateinit var userTracking: UserTracking
    private var trackingTimer: RepeatingTimer? = null
    private val trackerScope = CoroutineScope(ioDispatcher + Job())

    private lateinit var settings: SunTrackerSettings
//    private val uvPredictionStream = getLocalForecastForToday()
    private val skinTypeStream = userSettingsRepository.getSkinTypeFlow()
    private val spfStream = userSettingsRepository.getSpfFlow()
    private val isOnReflectiveSurfaceStream = userSettingsRepository.getIsOnSnowOrWaterFlow()
    private val settingsStream = combine(
        getLocalForecastForToday(), skinTypeStream, spfStream, isOnReflectiveSurfaceStream
    ) { uvPrediction, skinType, spf, isOnReflectiveSurface ->
        if (uvPrediction.isNotEmpty()) {
            SunTrackerSettings(
                uvPrediction = uvPrediction,
                hardcodedSkinType = skinType,
                spf = spf,
                isOnReflectiveSurface = isOnReflectiveSurface
            )
        } else {
            Timber.e("collected null: uvPrediction: ${uvPrediction.isEmpty()}")
            null
        }
    }

    fun startTracking() {
        Timber.d("SunTrackerService - startTracking")
        initializeUserSettingsStream()
        initializeUserTracking()
        trackingTimer?.cancel()
        trackingTimer = createTrackingTimer().also {
            it.start()
        }
    }

    fun stopTracking() {
        trackingTimer?.cancel()
    }

    private fun initializeUserSettingsStream() {
        settingsStream
            .flowOn(ioDispatcher)
            .onEach {
                Timber.d("settings detected a change, possibly null")
                it?.let {
                    Timber.d("and they weren't null")
                    settings = it
                }
            }
            .launchIn(trackerScope)

//        trackerScope.launch {
//            settingsStream.collect {
//                Timber.d("settings detected a change")
//                it?.let {
//                    Timber.d("and they weren't null")
//                    settings = it
//                }
//            }
//        }
    }

    private fun initializeUserTracking() {
        trackerScope.launch {
            userTracking = userTrackingRepository.getUserTracking(clock.toDateString())
                ?: UserTracking(sunburnProgress = 0.0, vitaminDProgress = 0.0)
            Timber.d("Initialized tracking to $userTracking")
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
        Timber.d("Updating tracking, adding burn=$burnDelta, vitD=$vitaminDDelta")
        userTracking = UserTracking(
            sunburnProgress = userTracking.sunburnProgress.plus(burnDelta),
            vitaminDProgress = userTracking.vitaminDProgress.plus(vitaminDDelta)
        )
        Timber.d("Updated tracking to $userTracking")
        userTrackingRepository.setUserTracking(clock.toDateString(), userTracking)
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