package com.androidandrew.sunscreen.ui.location

import androidx.annotation.NavigationRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LocationViewModel(
    private val repository: UserRepositoryImpl,
    private val locationUtil: LocationUtil
) : ViewModel() {

    private val _navigationId = MutableStateFlow(0)
    @NavigationRes val navigationId = _navigationId.asStateFlow()

    fun onSearchLocation(zipLocation: String) {
        if (locationUtil.isValidZipCode(zipLocation)) {
            viewModelScope.launch {
                repository.setLocation(zipLocation)
                _navigationId.value = R.id.action_locationFragment_to_mainFragment
            }
        }
    }
}