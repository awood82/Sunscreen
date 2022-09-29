package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.asUvPrediction
import com.androidandrew.sunscreen.time.RepeatingTimer
import com.androidandrew.sunscreen.tracker.UvFactor
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.UvPrediction
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import com.androidandrew.sunscreen.tracker.vitamind.VitaminDCalculator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import java.util.*

class MainViewModel(private val uvService: EpaService, private val clock: Clock) : ViewModel() {

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value
    private val UNKNOWN_BURN_TIME = -1L

    private var networkJob: Job? = null

    private var uvPrediction: UvPrediction? = null
    private val _chartData = MutableLiveData<LineDataSet>()
    val chartData: LiveData<LineDataSet> = _chartData

    private val _sunUnitsToday = MutableLiveData(0.0) // ~100.0 means almost-certain sunburn
    val sunUnitsToday: LiveData<Double> = _sunUnitsToday
    val sunUnitsTrackingLabel: LiveData<String> = Transformations.map(_sunUnitsToday) { units ->
        "${units.toInt()}%"
    }

    private val _vitaminDUnitsToday = MutableLiveData(0.0) // in IU. Studies recommend 400-1000-4000 IU.
    val vitaminDUnitsToday: LiveData<Double> = _vitaminDUnitsToday
    val vitaminDTrackingLabel: LiveData<String> = Transformations.map(_vitaminDUnitsToday) { units ->
        "${units.toInt()} IU"
    }

    private val _minutesToBurn = MutableLiveData(0L)
    val burnTimeString: LiveData<String> = Transformations.map(_minutesToBurn) { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> "Unknown"
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> "No burn expected"
            else -> "$minutes min"
        }
    }

    private val updateTimer = RepeatingTimer(object : TimerTask() {
        override fun run() {
            updateTimeToBurn()
        }
    }, RepeatingTimer.ONE_MINUTE, RepeatingTimer.ONE_MINUTE)

    private var trackingTimer: RepeatingTimer? = null
    private val _isStartTrackingEnabled = MutableLiveData(false)
    val isStartTrackingEnabled: LiveData<Boolean> = _isStartTrackingEnabled

    init {
        refreshNetwork()
        updateTimer.start()
    }

    fun onStartTracking() {
        trackingTimer?.cancel()
        trackingTimer = createTrackingTimer().also {
            it.start()
            _isStartTrackingEnabled.value = false
        }
    }

    fun onStopTracking() {
        trackingTimer?.cancel()
        _isStartTrackingEnabled.value = true
    }

    private fun createTrackingTimer(): RepeatingTimer {
        return RepeatingTimer(object : TimerTask() {
            override fun run() {
                updateBurnProgress()
                updateVitaminDProgress()
            }
        }, RepeatingTimer.ONE_SECOND, RepeatingTimer.ONE_SECOND)
    }

    private fun refreshNetwork() {
        networkJob?.cancel()
        networkJob = viewModelScope.launch {
//            uvPrediction = hardcodedUvPrediction
            uvPrediction = try {
                val response = uvService.getUvForecast("92123") // TODO: Remove hardcoded location
                response.asUvPrediction()
            } catch (e: Exception) {
                null
            }
            updateChart()
            updateTimeToBurn()
        }
    }

    private fun updateTimeToBurn() {
        val minutesToBurn = uvPrediction?.let {
            SunburnCalculator.computeMaxTime(
                uvPrediction = it,
                currentTime = LocalTime.now(clock),
                sunUnitsSoFar = _sunUnitsToday.value!!,
                skinType = hardcodedSkinType,
                spf = SunburnCalculator.spfNoSunscreen,
                altitudeInKm = 0,
                isOnSnowOrWater = false
            )
        } ?: UNKNOWN_BURN_TIME
        _minutesToBurn.postValue(minutesToBurn.toLong())
        println("Minutes = $minutesToBurn")
    }

    private fun updateBurnProgress() {
        uvPrediction?.let {
            val additionalSunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
                uvIndex = it.getUvNow(LocalTime.now(clock)),
                skinType = hardcodedSkinType,
                spf = SunburnCalculator.spfNoSunscreen,
                altitudeInKm = 0,
                isOnSnowOrWater = false
            ) / 60.0 // TODO: Magic number, seconds in a minute
            _sunUnitsToday.postValue( (_sunUnitsToday.value)?.plus(additionalSunUnits) )
        }
    }

    private fun updateVitaminDProgress() {
        uvPrediction?.let {
            val additionalVitaminDIU = VitaminDCalculator.computeIUVitaminDInOneMinute(
                uvIndex = it.getUvNow(LocalTime.now(clock)),
                skinType = hardcodedSkinType,
                clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
                spf = VitaminDCalculator.spfNoSunscreen,
                altitudeInKm = 0
            ) / 60.0 // TODO: Magic number, seconds in a minute
            _vitaminDUnitsToday.postValue((_vitaminDUnitsToday.value)?.plus(additionalVitaminDIU))
        }
    }

    private fun updateChart() {
        uvPrediction?.let {
            val entries = mutableListOf<Entry>()

            // TODO: Remove leading and trailing 0's

            for (point in uvPrediction!!) {
                entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
            }
            _chartData.value = LineDataSet(entries, "UV Index")
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer?.cancel()
    }
}