package com.androidandrew.sunscreen.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocationViewModel(
    private val userSettingsRepo: UserSettingsRepository,
    private val locationUtil: LocationUtil,
    private val analytics: EventLogger
) : ViewModel() {

    private val _locationBarState = MutableStateFlow(LocationBarState(""))
    val locationBarState = _locationBarState.asStateFlow()

    private val _isLocationValid = MutableSharedFlow<Boolean>()
    val isLocationValid = _isLocationValid.asSharedFlow()

    init {
        analytics.startTutorial()
        analytics.viewScreen(AppDestination.Location.name)
    }

    fun onEvent(event: LocationBarEvent) {
        when (event) {
            is LocationBarEvent.TextChanged -> {
                _locationBarState.update { it.copy(typedSoFar = event.text) }
            }
            is LocationBarEvent.LocationSearched -> {
                onSearchLocation(event.location)
            }
        }
    }

    private fun onSearchLocation(zipLocation: String) {
        analytics.searchLocation(zipLocation)
        if (locationUtil.isValidZipCode(zipLocation)) {
            viewModelScope.launch {
                userSettingsRepo.setLocation(zipLocation)
                _isLocationValid.emit(true)
            }
        }
    }
}