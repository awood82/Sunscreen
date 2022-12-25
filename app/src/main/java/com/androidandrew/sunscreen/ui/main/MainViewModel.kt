package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.network.EpaService
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.model.UvPrediction
import com.androidandrew.sunscreen.model.trim
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.uv.asUvPrediction
import com.androidandrew.sunscreen.model.uv.toChartData
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.ui.chart.UvChartUiState
import com.androidandrew.sunscreen.ui.location.LocationBarEvent
import com.androidandrew.sunscreen.ui.location.LocationBarState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.uvcalculators.vitamind.VitaminDCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
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
)
    : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
    }

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value

    // TODO: Move these into settings repository
    private val isOnSnowOrWater = MutableStateFlow(false)
    private val spf = MutableStateFlow("1")

    private val _lastDateUsed = MutableStateFlow(getDateToday())
    private val _lastLocalTimeUsed = MutableStateFlow(LocalTime.now(clock))

    private var networkJob: Job? = null
    private val _uvPrediction = MutableStateFlow<UvPrediction>(emptyList())

    private val _isCurrentlyTracking = MutableStateFlow(false)

    private val _locationBarState = MutableStateFlow(LocationBarState(typedSoFar = ""))
    val locationBarState = _locationBarState
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), _locationBarState.value)

//    private val _userTrackingInfo = userRepository.getUserTrackingInfoSync(_lastDateUsed.value)

    private val _userTrackingInfo = _lastDateUsed.flatMapLatest { date ->
        onSearchLocation(userRepository.getLocation() ?: "") // Will only refresh if the ZIP code is valid
        userRepository.getUserTrackingInfoSync(date)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), null)

    val uvTrackingState: StateFlow<UvTrackingState> = combine(
        _isCurrentlyTracking, _uvPrediction, _userTrackingInfo, spf, isOnSnowOrWater
    ) { isTracking, prediction, trackingInfo, spf, isOnSnowOrWater ->
        UvTrackingState(
            buttonLabel = when (isTracking) {
                true -> R.string.stop_tracking
                false -> R.string.start_tracking
            },
            buttonEnabled = prediction.isNotEmpty(),
            spf = spf,
            isOnSnowOrWater = isOnSnowOrWater,
            sunburnProgressLabelMinusUnits = trackingInfo?.burnProgress?.toInt() ?: 0, // ~100.0 means almost-certain sunburn
            sunburnProgress0to1 = (trackingInfo?.burnProgress ?: 0.0).div(SunburnCalculator.maxSunUnits).toFloat(),
            vitaminDProgressLabelMinusUnits = trackingInfo?.vitaminDProgress?.toInt() ?: 0, // in IU. Studies recommend 400-1000-4000 IU.
            vitaminDProgress0to1 = (trackingInfo?.vitaminDProgress ?: 0.0).div(VitaminDCalculator.recommendedIU).toFloat()
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvTrackingState.initialState)

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    val uvChartUiState: StateFlow<UvChartUiState> = combine(_uvPrediction, _lastLocalTimeUsed) { prediction, time ->
        when (prediction.isEmpty()) {
            true -> UvChartUiState.NoData
            false -> {
                val xHighlight = with (time) {
                    (hour + minute / TimeUnit.HOURS.toMinutes(1).toDouble()).toFloat()
                }
                UvChartUiState.HasData(prediction.toChartData(), xHighlight)
            }
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvChartUiState.NoData)

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

    val burnTimeUiState: StateFlow<BurnTimeUiState> = _minutesToBurn.map { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> BurnTimeUiState.Unknown
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> BurnTimeUiState.Unlikely
            else -> BurnTimeUiState.Known(minutes)
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), BurnTimeUiState.Unknown)

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

    val networkRefresher = viewModelScope.launch {
        userRepository.getLocationSync()
            .distinctUntilChanged()
            .onEach { location ->
                location?.let {
                    _locationBarState.value = LocationBarState(location)
                    refreshNetwork(location)
                }
            }
            .collect()
    }

    val dayRefresher = viewModelScope.launch {
        _lastDateUsed
            .onEach {
                userRepository.getLocation()?.let {
                    refreshNetwork(it)
                }
            }
            .collect()
    }
//    private val _userTrackingInfo = _lastDateUsed.flatMapLatest { date ->
//        onSearchLocation(userRepository.getLocation() ?: "") // Will only refresh if the ZIP code is valid
//        userRepository.getUserTrackingInfoSync(date)
//    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), null)

    init {
        updateTimer.start()
        dailyTrackingRefreshTimer.start()
    }

    override fun onCleared() {
        super.onCleared()
        updateTimer.cancel()
        dailyTrackingRefreshTimer.cancel()
    }

    fun onLocationBarEvent(event: LocationBarEvent) {
        when (event) {
            is LocationBarEvent.TextChanged -> {
                _locationBarState.value = LocationBarState(typedSoFar = event.text)
            }
            is LocationBarEvent.LocationSearched -> {
                onSearchLocation(event.location)
            }
        }
    }

    fun onUvTrackingEvent(event: UvTrackingEvent) {
        when (event) {
            is UvTrackingEvent.TrackingButtonClicked -> {
                onTrackingClicked()
            }
            is UvTrackingEvent.SpfChanged -> {
                spf.value = event.spf
                onSpfChanged(event.spf)
            }
            is UvTrackingEvent.IsOnSnowOrWaterChanged -> {
                isOnSnowOrWater.value = event.isOnSnowOrWater
                onIsSnowOrWaterChanged(event.isOnSnowOrWater)
            }
        }
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
        if (_isCurrentlyTracking.value) {
            sunTrackerServiceController.setSpf(s.toString().toIntOrNull() ?: SunburnCalculator.spfNoSunscreen)
        }
    }

    fun onIsSnowOrWaterChanged(isOn: Boolean) {
        if (_isCurrentlyTracking.value) {
            sunTrackerServiceController.setIsOnSnowOrWater(isOn)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (_isCurrentlyTracking.value) {
            // Start the service so it continues to run while the app is in the background
            sunTrackerServiceController.start()
        }
    }

    private fun refreshNetwork(zipCode: String) {
        networkJob?.cancel()
        Timber.i("Refreshing zip $zipCode")
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

    fun onSearchLocation(location: String) {
        if (locationUtil.isValidZipCode(location)) {
            viewModelScope.launch {
                userRepository.setLocation(location)
            }
        }
    }

    private fun getDateToday(): String {
        return LocalDate.now(clock).toString()
    }

    private fun getSpf(): Int {
        return spf.value.toIntOrNull() ?: SunburnCalculator.spfNoSunscreen
    }
}