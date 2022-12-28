package com.androidandrew.sunscreen.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocationViewModel(
    private val repository: UserRepositoryImpl,
    private val locationUtil: LocationUtil
) : ViewModel() {

    private val _locationBarState = MutableStateFlow(LocationBarState(""))
    val locationBarState = _locationBarState.asStateFlow()

    private val _isLocationValid = MutableStateFlow(false)
    val isLocationValid = _isLocationValid.asStateFlow()

    fun onEvent(event: LocationBarEvent) {
        when (event) {
            is LocationBarEvent.TextChanged -> {
                _locationBarState.value = LocationBarState(typedSoFar = event.text)
            }
            is LocationBarEvent.LocationSearched -> {
                onSearchLocation(event.location)
            }
        }
    }

    fun onSearchLocation(zipLocation: String) {
        if (locationUtil.isValidZipCode(zipLocation)) {
            viewModelScope.launch {
                repository.setLocation(zipLocation)
                _isLocationValid.value = true
            }
        }
    }
}