package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.asUvPrediction
import com.androidandrew.sunscreen.repository.SunscreenRepository
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
import java.util.concurrent.TimeUnit

class MainViewModel(private val uvService: EpaService, private val repository: SunscreenRepository,
                    private val clock: Clock) : ViewModel() {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
        private val ZIP_CODE_LENGTH = 5
    }
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

    private var networkJob: Job? = null
    private var uvPrediction: UvPrediction? = null
    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _closeKeyboard = MutableLiveData<Boolean>(false)
    val closeKeyboard: LiveData<Boolean> = _closeKeyboard

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
    }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1))

    private var trackingTimer: RepeatingTimer? = null
    private val _isTrackingEnabled = MutableLiveData(false)
    val isTrackingEnabled: LiveData<Boolean> = _isTrackingEnabled
    private val _isCurrentlyTracking = MutableLiveData(false)
    val isCurrentlyTracking: LiveData<Boolean> = _isCurrentlyTracking

    val locationEditText = MutableLiveData("")
    var isOnSnowOrWater = false
    var spf = "1"

    init {
        viewModelScope.launch {
            repository.getLocation()?.let {
                locationEditText.postValue(it)
                refreshNetwork(it)
            }
        }
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
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1))
    }

    private fun refreshNetwork(zipCode: String) {
//        uvPrediction = hardcodedUvPrediction.trim()
        networkJob?.cancel()
        networkJob = viewModelScope.launch {
            try {
                val response = uvService.getUvForecast(zipCode)
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
            ) / TimeUnit.MINUTES.toSeconds(1)
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
            ) / TimeUnit.MINUTES.toSeconds(1)
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
                _chartHighlightValue.postValue((hour + minute / TimeUnit.HOURS.toMinutes(1).toDouble()).toFloat())
            }
        }
    }

    fun onSnowOrWaterChanged() {
        isOnSnowOrWater = !isOnSnowOrWater
        updateTimeToBurn()
    }

    fun onSearchLocation() {
        _closeKeyboard.postValue(true)
        _closeKeyboard.postValue(false)
        locationEditText.value?.let { location ->
            if (isValidZipCode(location)) {
                refreshNetwork(location)
                viewModelScope.launch {
                    repository.setLocation(location)
                }
            }
        }
    }

    private fun isValidZipCode(location: String): Boolean {
        return location.length == ZIP_CODE_LENGTH
            && location.toIntOrNull() != null
    }

    fun onSpfChanged() {
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