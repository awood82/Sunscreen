package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.time.RepeatingTimer
import com.androidandrew.sunscreen.tracker.UvFactor
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.UvPredictionPoint
import com.androidandrew.sunscreen.tracker.uv.getUvNow
import com.androidandrew.sunscreen.tracker.uv.trim
import com.androidandrew.sunscreen.tracker.vitamind.VitaminDCalculator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class MainViewModel(private val sunscreenRepository: SunscreenRepository, private val clock: Clock) : ViewModel() {

    // TODO: Remove hardcoded value
    private val hardcodedUvPrediction = listOf(
        UvPredictionPoint(LocalTime.NOON.minusHours(7), 0.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(6), 0.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(5), 0.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(4), 1.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(3), 3.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(2), 6.0),
        UvPredictionPoint(LocalTime.NOON.minusHours(1), 10.0),
        UvPredictionPoint(LocalTime.NOON, 12.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(1), 11.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(2), 8.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(3), 5.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(4), 3.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(5), 1.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(6), 0.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(7), 0.0),
        UvPredictionPoint(LocalTime.NOON.plusHours(8), 0.0),
    )

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value
    private val UNKNOWN_BURN_TIME = -1L
    private val NIGHT_TIME = -2L

    private var networkJob: Job? = null

    // TODO: Remove hardcoded value
    private var location = "92123"

    private val _uvPrediction = Transformations.map(sunscreenRepository.forecast) {
        it.trim()
    }

    val chartData = Transformations.map(_uvPrediction) {
        when (it.isEmpty()) {
            true -> null
            else -> {
                val entries = mutableListOf<Entry>()

                for (point in it) {
                    entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
                }

                LineDataSet(entries, "")
            }
        }
    }

    val isChartVisible: LiveData<Boolean> = Transformations.map(chartData) {
        it != null
    }

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

    private val _minutesToBurn = MutableLiveData<Long>()
    private val minutesToBurn: LiveData<Long> = Transformations.map(_uvPrediction) {
        calculateTimeToBurn()
    }
    val burnTimeString: LiveData<String> = Transformations.map(minutesToBurn) { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> "Unknown"
            NIGHT_TIME -> "Nighttime"
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
    val isTrackingEnabled: LiveData<Boolean> = Transformations.map(_uvPrediction) {
        it.isNotEmpty()
    }
    private val _isCurrentlyTracking = MutableLiveData(false)
    val isCurrentlyTracking: LiveData<Boolean> = _isCurrentlyTracking

    init {
        updateNetwork()
        updateTimer.start()
    }

    fun onTrackingClicked() {
        trackingTimer?.cancel()
        when (_isCurrentlyTracking.value) {
            true -> _isCurrentlyTracking.value = false
            else -> {
                trackingTimer = createTrackingTimer().also {
                    it.start()
                }
                _isCurrentlyTracking.value = true
            }
        }
    }

    private fun createTrackingTimer(): RepeatingTimer {
        return RepeatingTimer(object : TimerTask() {
            override fun run() {
                updateBurnProgress()
                updateVitaminDProgress()
            }
        }, RepeatingTimer.ONE_SECOND, RepeatingTimer.ONE_SECOND)
    }

    private fun updateNetwork() {
//        uvPrediction = hardcodedUvPrediction.trim()
        networkJob?.cancel()
        networkJob = viewModelScope.launch {
//            uvPrediction = try {
                sunscreenRepository.refreshForecast(localDate = LocalDate.now(clock), location = location)
//                val response = uvService.getUvForecast("92123") // TODO: Remove hardcoded location
//                response.asUvPrediction().trim()
//            } catch (e: Exception) {
//                null
//            }
//            _uvPrediction = sunscreenRepository.getForecast()
//            updateChart()
//            updateTimeToBurn()
//            _isTrackingEnabled.postValue(_uvPrediction.value != null)
        }
//        viewModelScope.launch {
//            sunscreenRepository.refreshForecast(localDate = LocalDate.now(clock), location = location)
//        }
    }

    private fun updateTimeToBurn() {
        _minutesToBurn.postValue(calculateTimeToBurn())
    }

    private fun calculateTimeToBurn(): Long {
        return _uvPrediction.value?.let {
            when (it.isEmpty()) {
                true -> NIGHT_TIME
                false -> SunburnCalculator.computeMaxTime(
                    uvPrediction = _uvPrediction.value!!,
                    currentTime = LocalTime.now(clock),
                    sunUnitsSoFar = _sunUnitsToday.value!!,
                    skinType = hardcodedSkinType,
                    spf = SunburnCalculator.spfNoSunscreen,
                    altitudeInKm = 0,
                    isOnSnowOrWater = false).toLong()
            }
        } ?: UNKNOWN_BURN_TIME

//        return if (_uvPrediction.value.isNullOrEmpty()) {
//            UNKNOWN_BURN_TIME
//        } else {
//            SunburnCalculator.computeMaxTime(
//                uvPrediction = _uvPrediction.value!!,
//                currentTime = LocalTime.now(clock),
//                sunUnitsSoFar = _sunUnitsToday.value!!,
//                skinType = hardcodedSkinType,
//                spf = SunburnCalculator.spfNoSunscreen,
//                altitudeInKm = 0,
//                isOnSnowOrWater = false
//            ).toLong()
//        }

//                    return _uvPrediction.value?.let {
//        } ?: UNKNOWN_BURN_TIME
    }

    private fun updateBurnProgress() {
        _uvPrediction.value?.let {
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
        _uvPrediction.value?.let {
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

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer?.cancel()
    }
}