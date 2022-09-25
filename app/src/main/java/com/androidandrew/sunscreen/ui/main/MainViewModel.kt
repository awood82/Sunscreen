package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.tracker.sunburn.MinuteTimer
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import java.time.LocalTime
import java.util.*

class MainViewModel(private val currentTime: LocalTime = LocalTime.now()) : ViewModel() {

    // TODO: Remove hardcoded value
    private val hardcodedUvPrediction = listOf(
        UvPredictionPoint(LocalTime.NOON.minusHours(5), 0.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(4), 1.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(3), 2.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(2), 4.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(1), 6.0),
        UvPredictionPoint(LocalTime.NOON, 8.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(1), 8.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(2), 7.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(3), 5.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(4), 3.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(5), 1.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(6), 0.0),
    )

    // TODO: Remove hardcoded value
    private val hardcodedSkinType = 2

    private val _sunUnitsToday = MutableLiveData(0.0) // ~100.0 means almost-certain sunburn
    val sunUnitsToday: LiveData<Double> = _sunUnitsToday

    private val _minutesToBurn = MutableLiveData(0L)
    val burnTimeString: LiveData<String> = Transformations.map(_minutesToBurn) { minutes ->
        when (minutes) {
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> "No burn expected"
            else -> "$minutes min"
        }
    }

    private val updateTimer = MinuteTimer(object : TimerTask() {
        override fun run() {
            updateTimeToBurn()
        }
    })

    private val trackingTimer = MinuteTimer(object : TimerTask() {
        override fun run() {
            updateBurnProgress()
            updateVitaminDProgress()
        }
    })

    init {
        updateTimeToBurn()
        updateTimer.start()
    }

    fun onStartTracking() {
        trackingTimer.start()
    }

    fun onStopTracking() {
        trackingTimer.stop()
    }

    private fun updateTimeToBurn() {
        val minutesToBurn = SunburnCalculator.computeMaxTime(
            uvPrediction = hardcodedUvPrediction,
            currentTime = currentTime,
            sunUnitsSoFar = _sunUnitsToday.value!!,
            skinType = hardcodedSkinType,
            spf = SunburnCalculator.spfNoSunscreen,
            altitudeInKm = 0,
            isOnSnowOrWater = false)
        _minutesToBurn.postValue(minutesToBurn.toLong())
        println("Minutes = $minutesToBurn")
    }

    private fun updateBurnProgress() {
        val additionalSunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
            uvIndex = hardcodedUvPrediction.getUvNow(currentTime),
            skinType = hardcodedSkinType,
            spf = SunburnCalculator.spfNoSunscreen,
            altitudeInKm = 0,
            isOnSnowOrWater = false
        )
        _sunUnitsToday.postValue( (_sunUnitsToday.value)?.plus(additionalSunUnits))
    }

    private fun updateVitaminDProgress() {
        // TODO
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer.cancel()
    }
}