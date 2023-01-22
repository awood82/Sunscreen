package com.androidandrew.sunscreen.ui.clothing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.model.*
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
        val toSave = convertClothingForRepo()
        userSettingsRepo.setClothing(toSave)
    }

    private fun convertClothingForRepo(): UserClothing {
        return UserClothing(
            top = _topClothingItem ?: defaultTop,
            bottom = _bottomClothingItem ?: defaultBottom
        )
    }

    private suspend fun exitOnboarding() {
        userSettingsRepo.setIsOnboarded(true)
    }

    private suspend fun notifyClothingSelectionIsDone() {
        _isClothingSelected.emit(true)
    }
}