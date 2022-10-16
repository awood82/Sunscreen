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
import com.androidandrew.sunscreen.util.LocationUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val uvService: EpaService, private val repository: SunscreenRepository,
                    private val locationUtil: LocationUtil, private val clock: Clock) : ViewModel() {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
    }

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value

    val locationEditText = MutableStateFlow("")
    var isOnSnowOrWater = MutableStateFlow(false)
    val spf = MutableStateFlow("1")

    private val _lastDateUsed = MutableStateFlow(getDateToday())
    private val _lastLocalTimeUsed = MutableStateFlow(LocalTime.now(clock))

    private var networkJob: Job? = null
    private val _uvPrediction = MutableStateFlow<UvPrediction>(emptyList())

    private val _isCurrentlyTracking = MutableLiveData(false)
    val isCurrentlyTracking: LiveData<Boolean> = _isCurrentlyTracking

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _closeKeyboard = MutableLiveData(false)
    val closeKeyboard: LiveData<Boolean> = _closeKeyboard

    val chartData = _uvPrediction.mapNotNull { predictionList ->
        val entries = mutableListOf<Entry>()
        for (point in predictionList) {
            entries.add(Entry(point.time.hour.toFloat(), point.uvIndex.toFloat()))
        }
        LineDataSet(entries, "")
    }

    val chartHighlightValue = combine(_lastLocalTimeUsed, _uvPrediction) { time, prediction ->
        when (prediction.isNotEmpty()) {
            true -> with (time) {
                    (hour + minute / TimeUnit.HOURS.toMinutes(1).toDouble()).toFloat()
                }
            false -> -1.0f
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = -1.0f)

    private var trackingTimer: RepeatingTimer? = null
    val isTrackingEnabled = _uvPrediction.mapLatest { prediction ->
        prediction.isNotEmpty()
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = false)

    private val _userTrackingInfo = _lastDateUsed.flatMapLatest { date ->
        onSearchLocation() // Will only refresh if the ZIP code is valid
        repository.getUserTrackingInfoSync(date)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), null)

    val sunUnitsToday = _userTrackingInfo.mapLatest { tracking ->
        tracking?.burnProgress ?: 0.0 // ~100.0 means almost-certain sunburn
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), 0.0)

    val vitaminDUnitsToday = _userTrackingInfo.mapLatest { tracking ->
        tracking?.vitaminDProgress ?: 0.0 // in IU. Studies recommend 400-1000-4000 IU.
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), 0.0)

    private val _minutesToBurn = combine(_lastLocalTimeUsed, _uvPrediction, isOnSnowOrWater, spf) { time, forecast, snowOrWater, _ ->
        when (forecast.isNotEmpty()) {
            true -> SunburnCalculator.computeMaxTime(
                uvPrediction = forecast,
                currentTime = time,
                sunUnitsSoFar = sunUnitsToday.value, //_userTrackingInfo.value[0].burnProgress,
                skinType = hardcodedSkinType,
                spf = getSpf(),
                altitudeInKm = 0,
                isOnSnowOrWater = snowOrWater
            ).toLong()
            false -> UNKNOWN_BURN_TIME
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), UNKNOWN_BURN_TIME)

    val burnTimeString = _minutesToBurn.map { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> "Unknown"
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> "No burn expected"
            else -> "$minutes min"
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), "Unknown")

    private val updateTimer = RepeatingTimer(object : TimerTask() {
        override fun run() {
            _lastLocalTimeUsed.value = LocalTime.now(clock)
        }
    }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1))

    private val dailyTrackingRefreshTimer = RepeatingTimer(object: TimerTask() {
        override fun run() {
            _lastDateUsed.value = getDateToday()
        }
    }, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1))

    init {
        viewModelScope.launch {
            repository.getLocation()?.let {
                locationEditText.value = it
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
                viewModelScope.launch {
                    updateTracking(getBurnProgress(), getVitaminDProgress())
                }
            }
        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(1))
    }

    private fun refreshNetwork(zipCode: String) {
        networkJob?.cancel()
        networkJob = viewModelScope.launch {
            try {
                val response = uvService.getUvForecast(zipCode)
                _uvPrediction.value = response.asUvPrediction().trim()
            } catch (e: Exception) {
//                uvPrediction = null // TODO: Verify this: No need to set uvPrediction to null. Keep the existing data at least.
                _snackbarMessage.postValue(e.message)
            }
        }
    }

    suspend fun updateTracking(burnDelta: Double = 0.0, vitaminDDelta: Double = 0.0) {
        val userTracking = UserTracking(
            date = _lastDateUsed.value,
            burnProgress = sunUnitsToday.value + burnDelta,
            vitaminDProgress = vitaminDUnitsToday.value + vitaminDDelta
        )
        repository.setUserTrackingInfo(userTracking)
    }

    private fun getBurnProgress(): Double {
        return when (_uvPrediction.value.isNotEmpty()) {
            true -> SunburnCalculator.computeSunUnitsInOneMinute(
                    uvIndex = _uvPrediction.value.getUvNow(_lastLocalTimeUsed.value),
                    skinType = hardcodedSkinType,
                    spf = getSpf(),
                    altitudeInKm = 0,
                    isOnSnowOrWater = isOnSnowOrWater.value
                ) / TimeUnit.MINUTES.toSeconds(1)
            false -> 0.0
        }
    }

    private fun getVitaminDProgress(): Double {
        return when (_uvPrediction.value.isNotEmpty()) {
            true -> VitaminDCalculator.computeIUVitaminDInOneMinute(
                    uvIndex = _uvPrediction.value.getUvNow(_lastLocalTimeUsed.value),
                    skinType = hardcodedSkinType,
                    clothing = UvFactor.Clothing.SHORTS_NO_SHIRT,
                    spf = getSpf(),
                    altitudeInKm = 0
                ) / TimeUnit.MINUTES.toSeconds(1)
            false -> 0.0
        }
    }

    fun onSearchLocation() {
        _closeKeyboard.postValue(true)
        _closeKeyboard.postValue(false)
        locationEditText.value.let { location ->
            if (locationUtil.isValidZipCode(location)) {
                refreshNetwork(location)
                viewModelScope.launch {
                    repository.setLocation(location)
                }
            }
        }
    }

    private fun getDateToday(): String {
        return LocalDate.now(clock).toString()
    }

    private fun getSpf(): Int {
        return spf.value.toIntOrNull() ?: SunburnCalculator.spfNoSunscreen
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        trackingTimer?.cancel()
    }
}