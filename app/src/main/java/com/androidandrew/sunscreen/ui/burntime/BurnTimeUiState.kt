package com.androidandrew.sunscreen.ui.burntime

sealed interface BurnTimeUiState {
    data class Known(val minutes: Long) : BurnTimeUiState
    object Unknown : BurnTimeUiState
    object Unlikely : BurnTimeUiState
}