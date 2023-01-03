package com.androidandrew.sunscreen.ui.main

import androidx.lifecycle.*
import com.androidandrew.sunscreen.common.RepeatingTimer
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.data.repository.UserTrackingRepository
import com.androidandrew.sunscreen.domain.ConvertSpfUseCase
import com.androidandrew.sunscreen.domain.usecases.GetLocalForecastForTodayUseCase
import com.androidandrew.sunscreen.service.SunTrackerServiceController
import com.androidandrew.sunscreen.domain.uvcalculators.sunburn.SunburnCalculator
import com.androidandrew.sunscreen.model.uv.toChartData
import com.androidandrew.sunscreen.ui.burntime.BurnTimeUiState
import com.androidandrew.sunscreen.ui.chart.UvChartUiState
import com.androidandrew.sunscreen.ui.location.LocationBarEvent
import com.androidandrew.sunscreen.ui.location.LocationBarState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.domain.uvcalculators.vitamind.VitaminDCalculator
import com.androidandrew.sunscreen.model.UserSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit

class MainViewModel(
    getLocalForecastForToday: GetLocalForecastForTodayUseCase,
    private val userSettingsRepo: UserSettingsRepository,
    userTrackingRepo: UserTrackingRepository,
    private val convertSpfUseCase: ConvertSpfUseCase,
    private val sunburnCalculator: SunburnCalculator,
    private val locationUtil: LocationUtil,
    private val clock: Clock,
    private val sunTrackerServiceController: SunTrackerServiceController
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private const val UNKNOWN_BURN_TIME = -1L
    }

    // User Settings
    private val _location = userSettingsRepo.getLocationFlow()
    private val _skinType = userSettingsRepo.getSkinTypeFlow()
    private val _isOnSnowOrWater = userSettingsRepo.getIsOnSnowOrWaterFlow()
    private val _spf = userSettingsRepo.getSpfFlow()
    private val _spfToDisplay = MutableStateFlow("")
    private val _userSettings = combine(
        _location, _skinType, _isOnSnowOrWater, _spf
    ) { location, skinType, isOnSnowOrWater, spf ->
        UserSettings(
            location = location,
            skinType = skinType,
            isOnSnowOrWater = isOnSnowOrWater,
            spf = spf
        )
    }

    // User Tracking
    private val _isCurrentlyTracking = MutableStateFlow(false)

    // Time Tracking
    private val _lastDateUsed = MutableStateFlow(getDateToday())
    private val _lastLocalTimeUsed = MutableStateFlow(LocalTime.now(clock))

    // Forecast
//    private val _uvPrediction = MutableStateFlow<UvPrediction>(emptyList())
    private val _uvPrediction = getLocalForecastForToday().shareIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(), replay = 1
    )

    val appState = userSettingsRepo.getIsOnboardedFlow()
        .distinctUntilChanged()
        .map { isOnboarded ->
            when (isOnboarded) {
                true -> {
                    Timber.d("Setup completed")
                    startTimers()
                    _locationBarState.update { it.copy(typedSoFar = _location.firstOrNull() ?: "") }
                    _spfToDisplay.update { convertSpfUseCase.forDisplay(userSettingsRepo.getSpf()) }
//                    viewModelScope.launch {
//                        getLocalForecastForToday().collect {
//                            Timber.d("GetLocalForecastForTodayUseCase detected a change")
//                            _uvPrediction.value = it
//                        }
//                    }
                    AppState.Onboarded
                }
                false -> AppState.NotOnboarded
            }
        }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = AppState.Loading)

    private val _locationBarState = MutableStateFlow(LocationBarState(typedSoFar = ""))
    val locationBarState = _locationBarState
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = _locationBarState.value)

    private val _userTrackingInfo = userTrackingRepo.getUserTrackingFlow(_lastDateUsed.value)

    val uvTrackingState: StateFlow<UvTrackingState> = combine(
        _isCurrentlyTracking, _uvPrediction, _userTrackingInfo, _spfToDisplay, _isOnSnowOrWater
    ) { isTracking, prediction, trackingInfo, spf, isOnSnowOrWater ->
        Timber.d("Updating UvTrackingState")
        Timber.d("spf = $spf")
        UvTrackingState(
            isTrackingPossible = prediction.isNotEmpty(),
            isTracking = isTracking,
            spfOfSunscreenAppliedToSkin = spf,
            isOnSnowOrWater = isOnSnowOrWater,
            sunburnProgressAmount = trackingInfo?.sunburnProgress?.toInt() ?: 0, // ~100.0 means almost-certain sunburn
            sunburnProgressPercent0to1 = (trackingInfo?.sunburnProgress ?: 0.0).div(SunburnCalculator.MAX_SUN_UNITS).toFloat(),
            vitaminDProgressAmount = trackingInfo?.vitaminDProgress?.toInt() ?: 0, // in IU. Studies recommend 400-1000-4000 IU.
            vitaminDProgressPercent0to1 = (trackingInfo?.vitaminDProgress ?: 0.0).div(
                VitaminDCalculator.RECOMMENDED_IU).toFloat()
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvTrackingState.initialState)

    val uvChartUiState: StateFlow<UvChartUiState> = combine(
        _uvPrediction, _lastLocalTimeUsed
    ) { prediction, time ->
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

    private val _minutesToBurn = combine(
        _userTrackingInfo, _userSettings, _lastLocalTimeUsed, _uvPrediction, _spfToDisplay
    ) { trackingSoFar, userSettings, time, forecast, spfToDisplay ->
        when (forecast.isNotEmpty()) {
            true -> sunburnCalculator.computeMaxTime(
                uvPrediction = forecast,
                currentTime = time,
                sunUnitsSoFar = trackingSoFar?.sunburnProgress ?: 0.0,
                skinType = userSettings.skinType!!,
                spf = convertSpfUseCase.forCalculations(spfToDisplay.toIntOrNull()),
                altitudeInKm = 0,
                isOnSnowOrWater = userSettings.isOnSnowOrWater!!
            ).toLong()
            false -> UNKNOWN_BURN_TIME
        }
    }

    val burnTimeUiState: StateFlow<BurnTimeUiState> = _minutesToBurn.map { minutes ->
        when (minutes) {
            UNKNOWN_BURN_TIME -> BurnTimeUiState.Unknown
            SunburnCalculator.NO_BURN_EXPECTED.toLong() -> BurnTimeUiState.Unlikely
            else -> BurnTimeUiState.Known(minutes)
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = BurnTimeUiState.Unknown)

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage
    // TODO: Snackbar.make(binding.main, message, Snackbar.LENGTH_LONG).show()

    // TODO: Could move these Timers elsewhere like in TickHandler: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
    private val updateTimer =
        RepeatingTimer(object : TimerTask() {
            override fun run() {
                Timber.d("Update timer is running")
                _lastLocalTimeUsed.update { LocalTime.now(clock) }
            }
        }, TimeUnit.MINUTES.toMillis(1), TimeUnit.MINUTES.toMillis(1))

    private val dailyTrackingRefreshTimer =
        RepeatingTimer(object : TimerTask() {
            override fun run() {
                _lastDateUsed.update { getDateToday() }
            }
        }, TimeUnit.HOURS.toMillis(1), TimeUnit.HOURS.toMillis(1))

    // TODO: Same thing, refactor w/ the timers
//    val dayRefresher = viewModelScope.launch {
//        _lastDateUsed
//            .onEach {
//                userRepository.getLocation()?.let {
//                    refreshNetwork(it)
//                }
//            }
//            .collect()
//    }

//    val locationRefresher = viewModelScope.launch {
//        _location
//            .distinctUntilChanged()
//            .onEach { location ->
//                location?.let {
//                    Timber.d("forecastRefresher is refreshing for $location")
//                    _locationBarState.update { it.copy(/*LocationBarState*/(location)) }
////                    refreshForecast(location)
//                }
//            }
//            .collect()
//    }

    init {
        Timber.d("Initializing MainViewModel")
        viewModelScope.launch {
            _spfToDisplay.update { convertSpfUseCase.forDisplay(userSettingsRepo.getSpf()) }
        }
    }

    private fun startTimers() {
        Timber.d("Starting timers")
        updateTimer.start()
        dailyTrackingRefreshTimer.start()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimers()
    }

    private fun stopTimers() {
        Timber.d("Stopping timers")
        updateTimer.cancel()
        dailyTrackingRefreshTimer.cancel()
    }

    fun onLocationBarEvent(event: LocationBarEvent) {
        Timber.d("onLocationBarEvent: $event")
        when (event) {
            is LocationBarEvent.TextChanged -> {
                _locationBarState.update { it.copy(typedSoFar = event.text) }
            }
            is LocationBarEvent.LocationSearched -> {
                onSearchLocation(event.location)
            }
        }
    }

    fun onUvTrackingEvent(event: UvTrackingEvent) {
        Timber.d("onUvTrackingEvent: $event")
        when (event) {
            is UvTrackingEvent.TrackingButtonClicked -> onTrackingClicked()
            is UvTrackingEvent.SpfChanged -> {
                _spfToDisplay.update { event.spf }
                event.spf.toIntOrNull()?.let {
                    viewModelScope.launch {
                        userSettingsRepo.setSpf(it)
                    }
                }
            }
            is UvTrackingEvent.IsOnSnowOrWaterChanged -> {
                viewModelScope.launch {
                    userSettingsRepo.setIsOnSnowOrWater(event.isOnSnowOrWater)
                }
            }
        }
    }

    fun onTrackingClicked() {
        when (_isCurrentlyTracking.value) {
            true -> {
                sunTrackerServiceController.stop()
                _isCurrentlyTracking.update { false }
            }
            else -> {
                viewModelScope.launch {
                    sunTrackerServiceController.start()
                    _isCurrentlyTracking.update { true }
                }
            }
        }
    }

/*
    private fun refreshForecast(zipCode: String) {
        Timber.i("Refreshing forecast $zipCode")

        viewModelScope.launch {
            hourlyForecastRepository.getForecastFlow(
                zipCode = zipCode,
                date = LocalDate.now(clock)
            ).collect { forecast ->
                _uvPrediction.value = forecast
                if (forecast.isEmpty()) {
                    _snackbarMessage.postValue("Network error") // TODO: Handle network errors
                }
            }
        }
    }
*/
    private fun onSearchLocation(location: String) {
        if (locationUtil.isValidZipCode(location)) {
            viewModelScope.launch {
                Timber.d("Updating location ($location) in repo")
                userSettingsRepo.setLocation(location)
            }
        }
    }

    private fun getDateToday(): String {
        return LocalDate.now(clock).toString()
    }
}