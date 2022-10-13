package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.network.asUvPrediction
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.time.RepeatingTimer
import com.androidandrew.sunscreen.tracker.UvFactor
import com.androidandrew.sunscreen.tracker.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.tracker.uv.UvPrediction
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
import java.util.concurrent.TimeUnit

class MainViewModel(private val uvService: EpaService, private val repository: SunscreenRepository,
                    private val clock: Clock) : ViewModel() {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
        private val ZIP_CODE_LENGTH = 5
    }

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value
    private var lastDateUsed = getDateToday()

    private var networkJob: Job? = null
    private var uvPrediction: UvPrediction? = null
    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _closeKeyboard = MutableLiveData(false)
    val closeKeyboard: LiveData<Boolean> = _closeKeyboard

    private val _chartData = MutableLiveData<LineDataSet>()
    val chartData: LiveData<LineDataSet> = _chartData
    private val _chartHighlightValue = MutableLiveData<Float>()
    val chartHighlightValue: LiveData<Float> = _chartHighlightValue

    private var trackingTimer: RepeatingTimer? = null
    private val _isTrackingEnabled = MutableLiveData(false)
    val isTrackingEnabled: LiveData<Boolean> = _isTrackingEnabled
    private val _isCurrentlyTracking = MutableLiveData(false)
    val isCurrentlyTracking: LiveData<Boolean> = _isCurrentlyTracking

    val locationEditText = MutableLiveData("")
    var isOnSnowOrWater = false
    var spf = "1"


    private var _userTrackingInfo = repository.getUserTrackingInfoSync(lastDateUsed)
    val sunUnitsToday = Transformations.map(_userTrackingInfo) { tracking ->
        tracking?.burnProgress ?: 0.0 // ~100.0 means almost-certain sunburn
    }
    val vitaminDUnitsToday = Transformations.map(_userTrackingInfo) { tracking ->
        tracking?.vitaminDProgress ?: 0.0 // in IU. Studies recommend 400-1000-4000 IU.
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

    private val dailyTrackingRefreshTimer = RepeatingTimer(object: TimerTask() {
        override fun run() {
            if (lastDateUsed != getDateToday()) {
                lastDateUsed = getDateToday()
                viewModelScope.launch {
                    forceTrackingRefresh()
                }
                _userTrackingInfo = repository.getUserTrackingInfoSync(lastDateUsed)
                onSearchLocation() // Will only refresh if the ZIP code is valid
            }
        }
    }, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1))

    init {
        viewModelScope.launch {
            repository.getLocation()?.let {
                locationEditText.postValue(it)
                refreshNetwork(it)
            }
        }
        updateTimer.start()
        dailyTrackingRefreshTimer.start()
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
                val addToBurn = getBurnProgress()
                val addToVitaminD = getVitaminDProgress()
                viewModelScope.launch {
                    forceTrackingRefresh(addToBurn, addToVitaminD)
                }
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

    suspend fun forceTrackingRefresh(burnDelta: Double = 0.0, vitaminDDelta: Double = 0.0) {
        val userTrackingInfo = repository.getUserTrackingInfo(lastDateUsed)
            ?: UserTracking(lastDateUsed, 0.0, 0.0)
        userTrackingInfo.burnProgress += burnDelta
        userTrackingInfo.vitaminDProgress += vitaminDDelta
        repository.setUserTrackingInfo(userTrackingInfo)
    }

    private fun updateTimeToBurn() {
        val minutesToBurn = uvPrediction?.let {
            SunburnCalculator.computeMaxTime(
                uvPrediction = it,
                currentTime = LocalTime.now(clock),
                sunUnitsSoFar = sunUnitsToday.value ?: 0.0,
                skinType = hardcodedSkinType,
                spf = getSpfClamped(),
                altitudeInKm = 0,
                isOnSnowOrWater = isOnSnowOrWater
            )
        } ?: UNKNOWN_BURN_TIME
        _minutesToBurn.postValue(minutesToBurn.toLong())
    }

    private fun getBurnProgress(): Double {
        return uvPrediction?.let {
            SunburnCalculator.computeSunUnitsInOneMinute(
                uvIndex = it.getUvNow(LocalTime.now(clock)),
                skinType = hardcodedSkinType,
                spf = getSpfClamped(),
                altitudeInKm = 0,
                isOnSnowOrWater = isOnSnowOrWater
            ) / TimeUnit.MINUTES.toSeconds(1)
        } ?: 0.0
    }

    private fun getVitaminDProgress(): Double {
        return uvPrediction?.let {
            VitaminDCalculator.computeIUVitaminDInOneMinute(
                uvIndex = it.getUvNow(LocalTime.now(clock)),
                skinType = hardcodedSkinType,
                clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
                spf = getSpfClamped(),
                altitudeInKm = 0
            ) / TimeUnit.MINUTES.toSeconds(1)
        } ?: 0.0
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

    private fun getDateToday(): String {
        return LocalDate.now(clock).toString()
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer?.cancel()
    }
}