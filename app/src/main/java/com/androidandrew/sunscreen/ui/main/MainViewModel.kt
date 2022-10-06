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
import com.androidandrew.sunscreen.tracker.uv.trim
import com.androidandrew.sunscreen.tracker.vitamind.VitaminDCalculator
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import java.util.*

class MainViewModel(private val uvService: EpaService, private val clock: Clock) : ViewModel() {

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

    private var networkJob: Job? = null
    private var uvPrediction: UvPrediction? = null
    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _chartData = MutableLiveData<LineDataSet>()
    val chartData: LiveData<LineDataSet> = _chartData
    private val _chartHighlightValue = MutableLiveData<Float>()
    val chartHighlightValue: LiveData<Float> = _chartHighlightValue

    private val _sunUnitsToday = MutableLiveData(0.0) // ~100.0 means almost-certain sunburn
    val sunUnitsToday: LiveData<Double> = _sunUnitsToday
    val sunUnitsTrackingLabel: LiveData<String> = Transformations.map(_sunUnitsToday) { units ->
        "${units.toInt()} %"
    }

    private val _vitaminDUnitsToday = MutableLiveData(0.0) // in IU. Studies recommend 400-1000-4000 IU.
    val vitaminDUnitsToday: LiveData<Double> = _vitaminDUnitsToday
    val vitaminDTrackingLabel: LiveData<String> = Transformations.map(_vitaminDUnitsToday) { units ->
        "${units.toInt()} IU"
    }

    private val _minutesToBurn = MutableLiveData<Long>()
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
            updateChartTimeSelection()
        }
    }, RepeatingTimer.ONE_MINUTE, RepeatingTimer.ONE_MINUTE)

    private var trackingTimer: RepeatingTimer? = null
    private val _isTrackingEnabled = MutableLiveData(false)
    val isTrackingEnabled: LiveData<Boolean> = _isTrackingEnabled
    private val _isCurrentlyTracking = MutableLiveData(false)
    val isCurrentlyTracking: LiveData<Boolean> = _isCurrentlyTracking

    var isOnSnowOrWater = false
    var spf = "1"

    init {
        refreshNetwork()
        updateTimer.start()
    }

    fun onTrackingClicked() {
        trackingTimer?.cancel()
        when (_isCurrentlyTracking.value) {
            true -> {
                _isCurrentlyTracking.value = false
            }
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

    private fun refreshNetwork() {
        android.util.Log.e("EPA", "refreshNetwork")
//        uvPrediction = hardcodedUvPrediction.trim()
        networkJob?.cancel()
        networkJob = viewModelScope.launch {
            try {
                val response = uvService.getUvForecast("92123") // TODO: Remove hardcoded location
                uvPrediction = response.asUvPrediction().trim()
            } catch (e: Exception) {
//                uvPrediction = null // TODO: Verify this: No need to set uvPrediction to null. Keep the existing data at least.
                _snackbarMessage.postValue(e.message)
            }
            updateChart()
            updateChartTimeSelection()
            updateTimeToBurn()
            _isTrackingEnabled.postValue(uvPrediction != null)
        }
    }

    private fun updateTimeToBurn() {
        val minutesToBurn = uvPrediction?.let {
            SunburnCalculator.computeMaxTime(
                uvPrediction = it,
                currentTime = LocalTime.now(clock),
                sunUnitsSoFar = _sunUnitsToday.value!!,
                skinType = hardcodedSkinType,
                spf = getSpfClamped(),
                altitudeInKm = 0,
                isOnSnowOrWater = isOnSnowOrWater
            )
        } ?: UNKNOWN_BURN_TIME
        _minutesToBurn.postValue(minutesToBurn.toLong())
    }

    private fun updateBurnProgress() {
        uvPrediction?.let {
            val additionalSunUnits = SunburnCalculator.computeSunUnitsInOneMinute(
                uvIndex = it.getUvNow(LocalTime.now(clock)),
                skinType = hardcodedSkinType,
                spf = getSpfClamped(),
                altitudeInKm = 0,
                isOnSnowOrWater = isOnSnowOrWater
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
                spf = getSpfClamped(),
                altitudeInKm = 0
            ) / 60.0 // TODO: Magic number, seconds in a minute
            _vitaminDUnitsToday.postValue((_vitaminDUnitsToday.value)?.plus(additionalVitaminDIU))
        }
    }

    private fun updateChart() {
        uvPrediction?.let {
            val entries = mutableListOf<Entry>()

            for (point in uvPrediction!!) {
                entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
            }
            _chartData.postValue(LineDataSet(entries, ""))
        }
    }

    private fun updateChartTimeSelection() {
        uvPrediction?.let {
            with(LocalTime.now(clock)) {
                _chartHighlightValue.postValue((hour + minute / 60.0).toFloat())
            }
        }
    }

    fun onSnowOrWaterChanged() {
        isOnSnowOrWater = !isOnSnowOrWater
        updateTimeToBurn()
    }

    fun onSpfChanged() {
        android.util.Log.e("Burn", "spf = $spf, clamped = ${getSpfClamped()}")
        updateTimeToBurn()
    }

    fun getSpfClamped(): Int {
        val spfInt = spf.toIntOrNull()
        return when {
            spfInt == null -> 1
            spfInt > 50 -> 50
            spfInt < 1 -> 1
            else -> spfInt
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer?.cancel()
    }
}