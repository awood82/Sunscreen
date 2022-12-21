package com.androidandrew.sunscreen.ui.burntime

sealed interface BurnTimeState {
    data class Known(val minutes: Long): BurnTimeState
    object Unknown: BurnTimeState
    object Unlikely: BurnTimeState
}