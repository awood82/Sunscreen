package com.androidandrew.sunscreen.ui.init

import androidx.annotation.NavigationRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.R
import com.androidandrew.sunscreen.data.repository.UserRepositoryImpl
import com.androidandrew.sunscreen.util.LocationUtil
import kotlinx.coroutines.flow.*

class InitViewModel(repository: UserRepositoryImpl, locationUtil: LocationUtil) : ViewModel() {

    private val _location = repository.getLocationSync()
    @NavigationRes val navigationId = _location.map {
        when (locationUtil.isValidZipCode(it ?: "")) {
            true -> R.id.action_initFragment_to_mainFragment
            false -> R.id.action_initFragment_to_locationFragment
        }
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = 0)
}