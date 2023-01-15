package com.androidandrew.sunscreen.ui.skintype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SkinTypeViewModel(private val userSettingsRepo: UserSettingsRepository) : ViewModel() {

    private val _isSkinTypeSelected = MutableStateFlow(false)
    val isSkinTypeSelected = _isSkinTypeSelected.asStateFlow()

    fun onEvent(event: SkinTypeEvent) {
        when (event) {
            is SkinTypeEvent.Selected -> {
                viewModelScope.launch {
                    userSettingsRepo.setSkinType(event.skinType)
                    userSettingsRepo.setIsOnboarded(true)
                    _isSkinTypeSelected.update { true }
                }
            }
        }
    }
}