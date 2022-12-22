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
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.model.UvPredictionPoint
import com.androidandrew.sunscreen.model.uv.toChartData
import com.androidandrew.sunscreen.ui.burntime.BurnTimeState
import com.androidandrew.sunscreen.ui.chart.UvChartState
import com.androidandrew.sunscreen.ui.tracking.UvTrackingEvent
import com.androidandrew.sunscreen.ui.tracking.UvTrackingState
import com.androidandrew.sunscreen.util.LocationUtil
import com.androidandrew.sunscreen.uvcalculators.vitamind.VitaminDCalculator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
) : ViewModel(), DefaultLifecycleObserver {

    companion object {
        private val UNKNOWN_BURN_TIME = -1L
    }

//    var exposedUvChartState: UvChartState by mutableStateOf(UvChartState.NoData)

    private val hardcodedSkinType = 2 // TODO: Remove hardcoded value

    val locationEditText = MutableStateFlow("")
    val isOnSnowOrWater = MutableStateFlow(false)
    val spf = MutableStateFlow("1")

    private val _lastDateUsed = MutableStateFlow(getDateToday())
    private val _lastLocalTimeUsed = MutableStateFlow(LocalTime.now(clock))

    private var networkJob: Job? = null
//    private val _uvPrediction = MutableStateFlow<UvPrediction>(emptyList())
    private var _uvPrediction: UvPrediction = emptyList()
    private val _uvPredictionWasUpdated = MutableStateFlow(0.0)   // TODO: I've messed something up and _uvPrediction as a MutableStateFlow list is not updating anything. Temporary workaround so I can wrap up the other changes.

    private val _isCurrentlyTracking = MutableStateFlow(false)

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _closeKeyboard = MutableLiveData(false)
    val closeKeyboard: LiveData<Boolean> = _closeKeyboard

    private val _userTrackingInfo = _lastDateUsed.flatMapLatest { date ->
        onSearchLocation() // Will only refresh if the ZIP code is valid
        userRepository.getUserTrackingInfoSync(date)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), null)

    private val _minutesToBurn = combine(
        _lastLocalTimeUsed, /*_uvPrediction,*/ _userTrackingInfo, isOnSnowOrWater, spf
    ) { time, /*forecast,*/ userTrackingInfo, snowOrWater, spf ->
        Timber.e("Updating minutes. Predictions = ${_uvPrediction.size}, spf = $spf")
        when (_uvPrediction.isNotEmpty()) {
            true -> SunburnCalculator.computeMaxTime(
                uvPrediction = _uvPrediction,
                currentTime = time,
                sunUnitsSoFar = userTrackingInfo?.burnProgress ?: 0.0,
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

    val uvChartState: StateFlow<UvChartState> = combine(/*_uvPrediction,*/ _lastLocalTimeUsed, _uvPredictionWasUpdated) { /*prediction,*/ time, onePt ->
        Timber.e("Updating chart state. Predictions = ${_uvPrediction.size}")
        when (_uvPrediction.isEmpty()) {
            false -> {
                val highlight = with (time) {
                    (hour + minute / TimeUnit.HOURS.toMinutes(1).toDouble()).toFloat()
                }
                Timber.e("with data and highlight $highlight")
                val pred: UvPrediction = listOf(
                    //val time: LocalTime, val uvIndex: Double
                    UvPredictionPoint(LocalTime.NOON.minusHours(2), 2.0),
                    UvPredictionPoint(LocalTime.NOON.minusHours(1), 4.0 + onePt),
                    UvPredictionPoint(LocalTime.NOON, 6.0),
                    UvPredictionPoint(LocalTime.NOON.plusHours(1), 4.0),
                    UvPredictionPoint(LocalTime.NOON.plusHours(2), 2.0)
                )
//                UvChartState.HasData(_uvPrediction.toChartData(), highlight)
                UvChartState.HasData(pred.toChartData(), highlight)
            }
            true -> UvChartState.NoData
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvChartState.NoData)

    val uvTrackingState: StateFlow<UvTrackingState> = combine(
        /*_uvPrediction,*/ _uvPredictionWasUpdated, _isCurrentlyTracking, _userTrackingInfo, spf, isOnSnowOrWater
    ) { prediction, isTrackingNow, trackingInfo, spf, isOnSnowOrWater ->
        UvTrackingState(
            buttonLabel = when(isTrackingNow) {
                true -> R.string.stop_tracking
                false -> R.string.start_tracking
            },
            buttonEnabled = _uvPrediction.isNotEmpty(),
            spf = spf,
            isOnSnowOrWater = isOnSnowOrWater,
            sunburnProgressLabelMinusUnits = trackingInfo?.burnProgress?.toInt() ?: 0, // ~100.0 means almost-certain sunburn
            sunburnProgress0to1 = (trackingInfo?.burnProgress ?: 0.0).div(SunburnCalculator.maxSunUnits).toFloat(),
            vitaminDProgressLabelMinusUnits = trackingInfo?.vitaminDProgress?.toInt() ?: 0, // in IU. Studies recommend 400-1000-4000 IU.
            vitaminDProgress0to1 = (trackingInfo?.vitaminDProgress ?: 0.0).div(VitaminDCalculator.recommendedIU).toFloat()
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = UvTrackingState.initialState)

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

                _uvPredictionWasUpdated.value = _uvPredictionWasUpdated.value + 1.0
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
                    uvPrediction = _uvPrediction,
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
        networkJob = viewModelScope.launch {
            try {
                val response = uvService.getUvForecast(zipCode)
//                exposedUvChartState = UvChartState.HasData(response.asUvPrediction().trim().toChartData(), 0.0f)
                Timber.e("Refreshed $zipCode, got ${response.asUvPrediction().trim().size} entries")
                _uvPrediction = response.asUvPrediction().trim()
                _uvPredictionWasUpdated.value = _uvPredictionWasUpdated.value - 3.0
            } catch (e: Exception) {
//                _uvPrediction.update { emptyList() } //TODO: Verify this: No need to set uvPrediction to null. Keep the existing data at least?
                Timber.e("Oops, exception ${e.message ?: ""}")
                _uvPrediction = emptyList()
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