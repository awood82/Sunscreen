package com.androidandrew.sunscreen.ui.skintype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SkinTypeViewModel(
    private val userSettingsRepo: UserSettingsRepository,
    private val analytics: EventLogger
) : ViewModel() {

    private val _isSkinTypeSelected = MutableSharedFlow<Boolean>()
    val isSkinTypeSelected = _isSkinTypeSelected.asSharedFlow()

    init {
        analytics.viewScreen(AppDestination.SkinType.name)
    }

    fun onEvent(event: SkinTypeEvent) {
        when (event) {
            is SkinTypeEvent.Selected -> {
                analytics.selectSkinType(event.skinType)
                viewModelScope.launch {
                    userSettingsRepo.setSkinType(event.skinType)
                    _isSkinTypeSelected.emit(true)
                }
            }
        }
    }
}