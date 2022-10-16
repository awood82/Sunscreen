package com.androidandrew.sunscreen.ui.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.repository.SunscreenRepository
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InitViewModel(private val repository: SunscreenRepository,
                    private val locationUtil: LocationUtil) : ViewModel() {

    private val _navigate = MutableStateFlow(0)
    val navigate = _navigate.asStateFlow()

    init {
        viewModelScope.launch {
            val location = repository.getLocation()
            _navigate.value = when (locationUtil.isValidZipCode(location ?: "")) {
                true -> R.id.action_initFragment_to_mainFragment
                false -> R.id.action_initFragment_to_locationFragment
            }
        }
    }

    fun onNavigationComplete() {
        _navigate.value = 0
    }
}