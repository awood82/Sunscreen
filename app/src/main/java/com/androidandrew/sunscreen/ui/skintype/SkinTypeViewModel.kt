package com.androidandrew.sunscreen.ui.skintype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SkinTypeViewModel(private val userSettingsRepo: UserSettingsRepository) : ViewModel() {

    private val _isSkinTypeSelected = MutableSharedFlow<Boolean>()
    val isSkinTypeSelected = _isSkinTypeSelected.asSharedFlow()

    fun onEvent(event: SkinTypeEvent) {
        when (event) {
            is SkinTypeEvent.Selected -> {
                viewModelScope.launch {
                    userSettingsRepo.setSkinType(event.skinType)
                    _isSkinTypeSelected.emit(true)
                }
            }
        }
    }
}