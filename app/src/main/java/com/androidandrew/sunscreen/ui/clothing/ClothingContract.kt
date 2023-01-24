package com.androidandrew.sunscreen.ui.clothing

import com.androidandrew.sunscreen.model.ClothingRegion

sealed interface ClothingEvent {
    data class TopSelected(val clothing: ClothingRegion) : ClothingEvent
    data class BottomSelected(val clothing: ClothingRegion) : ClothingEvent
    object ContinuePressed : ClothingEvent
}