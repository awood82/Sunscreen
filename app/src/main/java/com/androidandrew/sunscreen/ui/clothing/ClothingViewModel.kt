package com.androidandrew.sunscreen.ui.clothing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ClothingViewModel(private val userSettingsRepo: UserSettingsRepository) : ViewModel() {

    private val _isClothingSelected = MutableSharedFlow<Boolean>()
    val isClothingSelected = _isClothingSelected.asSharedFlow()

    fun onEvent(event: ClothingEvent) {
        when (event) {
            is ClothingEvent.Selected -> {
                viewModelScope.launch {
                    userSettingsRepo.setClothing(event.clothing.ordinal)
                    userSettingsRepo.setIsOnboarded(true)
                    _isClothingSelected.emit(true)
                }
            }
        }
    }
}