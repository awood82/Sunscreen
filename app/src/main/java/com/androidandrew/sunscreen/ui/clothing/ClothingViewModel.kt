package com.androidandrew.sunscreen.ui.clothing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.domain.UvFactor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ClothingViewModel(private val userSettingsRepo: UserSettingsRepository) : ViewModel() {

    private val _isClothingSelected = MutableSharedFlow<Boolean>()
    val isContinuePressed = _isClothingSelected.asSharedFlow()

    private var _topClothingItem: ClothingTop? = null
    private var _bottomClothingItem: ClothingBottom? = null

    fun onEvent(event: ClothingEvent) {
        when (event) {
            is ClothingEvent.TopSelected -> {
                _topClothingItem = event.clothing as ClothingTop
            }
            is ClothingEvent.BottomSelected -> {
                _bottomClothingItem = event.clothing as ClothingBottom
            }
            is ClothingEvent.ContinuePressed -> {
                viewModelScope.launch {
                    saveClothesToRepository()
                    exitOnboarding()
                    notifyClothingSelectionIsDone()
                }
            }
        }
    }

    @VisibleForTesting
    private suspend fun saveClothesToRepository() {
        // TODO: Maybe change how values are stored in repo
        val toSave = if (_topClothingItem == null || _bottomClothingItem == null) {
            UvFactor.Clothing.SHORTS_T_SHIRT // default
        } else if (_topClothingItem == ClothingTop.NOTHING && _bottomClothingItem == ClothingBottom.NOTHING) {
            UvFactor.Clothing.NAKED
        } else if (_topClothingItem == ClothingTop.NOTHING && _bottomClothingItem == ClothingBottom.SHORTS) {
            UvFactor.Clothing.SHORTS_NO_SHIRT
        } else if (_topClothingItem == ClothingTop.NOTHING && _bottomClothingItem == ClothingBottom.PANTS) {
            UvFactor.Clothing.PANTS_NO_SHIRT
        } else if (_topClothingItem == ClothingTop.T_SHIRT && _bottomClothingItem == ClothingBottom.PANTS) {
            UvFactor.Clothing.PANTS_T_SHIRT
        } else if (_topClothingItem == ClothingTop.LONG_SLEEVE_SHIRT && _bottomClothingItem == ClothingBottom.PANTS) {
            UvFactor.Clothing.PANTS_LONG_SLEEVE_SHIRT
        } else {
            UvFactor.Clothing.SHORTS_T_SHIRT
        }
        userSettingsRepo.setClothing(toSave.ordinal)
    }

    private suspend fun exitOnboarding() {
        userSettingsRepo.setIsOnboarded(true)
    }

    private suspend fun notifyClothingSelectionIsDone() {
        _isClothingSelected.emit(true)
    }
}