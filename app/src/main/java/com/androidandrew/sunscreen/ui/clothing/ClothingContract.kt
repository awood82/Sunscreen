package com.androidandrew.sunscreen.ui.clothing

import com.androidandrew.sunscreen.model.ClothingBottom
import com.androidandrew.sunscreen.model.ClothingRegion
import com.androidandrew.sunscreen.model.ClothingTop
import com.androidandrew.sunscreen.model.UserClothing

data class ClothingState(
    val selectedTop: ClothingTop,
    val selectedBottom: ClothingBottom
)

fun UserClothing.asClothingState(): ClothingState {
    return ClothingState(
        selectedTop = this.top,
        selectedBottom = this.bottom
    )
}

sealed interface ClothingEvent {
    data class TopSelected(val clothing: ClothingRegion) : ClothingEvent
    data class BottomSelected(val clothing: ClothingRegion) : ClothingEvent
    object ContinuePressed : ClothingEvent
}