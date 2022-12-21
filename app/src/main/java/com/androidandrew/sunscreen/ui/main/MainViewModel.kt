package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.uv.asUvPrediction
import com.androidandrew.sunscreen.model.uv.toChartData
import com.androidandrew.sunscreen.ui.burntime.BurnTimeState
import com.androidandrew.sunscreen.ui.chart.UvChartState
import com.androidandrew.sunscreen.util.LocationUtil
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
class MainViewModel(
    private val uvService: EpaService, private val userRepository: UserRepositoryImpl,
    private val locationUtil: LocationUtil, private val clock: Clock,
    private val sunTrackerServiceController: SunTrackerServiceController
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
    }

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value

    val locationEditText = MutableStateFlow("")
    val isOnSnowOrWater = MutableStateFlow(false)
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

    val isTrackingEnabled = _uvPrediction.mapLatest { prediction ->
        prediction.isNotEmpty()
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = false)

    private val _userTrackingInfo = _lastDateUsed.flatMapLatest { date ->
        onSearchLocation() // Will only refresh if the ZIP code is valid
        userRepository.getUserTrackingInfoSync(date)
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

    val burnTimeState: StateFlow<BurnTimeState> = _minutesToBurn.map { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> BurnTimeState.Unknown
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> BurnTimeState.Unlikely
            else -> BurnTimeState.Known(minutes)
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), BurnTimeState.Unknown)

    val uvChartState: StateFlow<UvChartState> = combine(_uvPrediction, _lastLocalTimeUsed) { predictions, time ->
        when (predictions.isEmpty()) {
            false -> {
                val highlight = with (time) {
                    (hour + minute / TimeUnit.HOURS.toMinutes(1).toDouble()).toFloat()
                }
                UvChartState.HasData(predictions.toChartData(), highlight)
            }
            true -> UvChartState.NoData
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvChartState.NoData)

    private val updateTimer =
        RepeatingTimer(object : TimerTask() {
            override fun run() {
                _lastLocalTimeUsed.value = LocalTime.now(clock)
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1))

    private val dailyTrackingRefreshTimer =
        RepeatingTimer(object : TimerTask() {
            override fun run() {
                _lastDateUsed.value = getDateToday()
            }
        }, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1))

    init {
        viewModelScope.launch {
            userRepository.getLocation()?.let {
                locationEditText.value = it
                refreshNetwork(it)
            }
        }
        updateTimer.start()
        dailyTrackingRefreshTimer.start()
    }

    fun onTrackingClicked() {
        when (_isCurrentlyTracking.value) {
            true -> {
                sunTrackerServiceController.unbind()
                _isCurrentlyTracking.value = false
            }
            else -> {
                /* TODO: Could have service read these settings as a flow from the repository,
                so they'd be able to update in real-time. Need to change the variable definitions here.
                But since changes are infrequent, keep it simple and just relaunch the service if a setting changes. */
                sunTrackerServiceController.setSettings(
                    uvPrediction = _uvPrediction.value,
                    skinType = hardcodedSkinType,
                    spf = spf.value.toIntOrNull() ?: SunburnCalculator.spfNoSunscreen,
                    isOnSnowOrWater = isOnSnowOrWater.value
                )
                sunTrackerServiceController.bind()
                _isCurrentlyTracking.value = true
            }
        }
    }

    fun onSpfChanged(s: CharSequence) {
        if (_isCurrentlyTracking.value == true) {
            sunTrackerServiceController.setSpf(s.toString().toIntOrNull() ?: SunburnCalculator.spfNoSunscreen)
        }
    }

    fun onIsSnowOrWaterChanged(isOn: Boolean) {
        if (_isCurrentlyTracking.value == true) {
            sunTrackerServiceController.setIsOnSnowOrWater(isOn)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (_isCurrentlyTracking.value == true) {
            // Start the service so it continues to run while the app is in the background
            sunTrackerServiceController.start()
        }
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

    fun onSearchLocation() {
        _closeKeyboard.postValue(true)
        _closeKeyboard.postValue(false)
        locationEditText.value.let { location ->
            if (locationUtil.isValidZipCode(location)) {
                refreshNetwork(location)
                viewModelScope.launch {
                    userRepository.setLocation(location)
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
    }
}