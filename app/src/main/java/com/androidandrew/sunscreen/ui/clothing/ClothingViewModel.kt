package com.androidandrew.sunscreen.ui.clothing

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidandrew.sunscreen.analytics.EventLogger
import com.androidandrew.sunscreen.data.repository.UserSettingsRepository
import com.androidandrew.sunscreen.model.*
import com.androidandrew.sunscreen.ui.navigation.AppDestination
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClothingViewModel(
    private val userSettingsRepo: UserSettingsRepository,
    private val analytics: EventLogger
) : ViewModel() {

    private val _isClothingDone = MutableSharedFlow<Boolean>()
    val isClothingDone = _isClothingDone.asSharedFlow()

    private val _clothing = MutableStateFlow(defaultUserClothing)
    val clothingState = _clothing.map {
        ClothingState(
            selectedTop = it.top,
            selectedBottom = it.bottom
        )
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(), initialValue = defaultUserClothing.asClothingState())

    init {
        analytics.viewScreen(AppDestination.Clothing.name)
        viewModelScope.launch {
            _clothing.update { userSettingsRepo.getClothing() }
        }
    }

    fun onEvent(event: ClothingEvent) {
        when (event) {
            is ClothingEvent.TopSelected -> {
                _clothing.update { it.copy(top = event.clothing as ClothingTop) }
            }
            is ClothingEvent.BottomSelected -> {
                _clothing.update { it.copy(bottom = event.clothing as ClothingBottom) }
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
        analytics.selectClothing(_clothing.value)
        userSettingsRepo.setClothing(_clothing.value)
    }

    private suspend fun exitOnboarding() {
        if (!userSettingsRepo.getIsOnboarded()) {
            analytics.finishTutorial()
        }
        userSettingsRepo.setIsOnboarded(true)
    }

    private suspend fun notifyClothingSelectionIsDone() {
        _isClothingDone.emit(true)
    }
}