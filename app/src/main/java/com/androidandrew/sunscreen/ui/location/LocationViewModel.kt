package com.androidandrew.sunscreen.ui.location

import androidx.annotation.NavigationRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(private val locationUtil: LocationUtil,
                        private val repository: UserRepositoryImpl
) : ViewModel() {

    private val _navigate = MutableStateFlow(0)
    @NavigationRes val navigate = _navigate.asStateFlow()

    fun onNavigationComplete() {
        _navigate.value = 0
    }

    fun onSearchLocation(zipLocation: String) {
        if (locationUtil.isValidZipCode(zipLocation)) {
            viewModelScope.launch {
                repository.setLocation(zipLocation)
                _navigate.value = R.id.action_locationFragment_to_mainFragment
            }
        }
    }
}