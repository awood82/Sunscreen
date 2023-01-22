package com.androidandrew.sunscreen.ui.clothing

interface ClothingRegion
enum class ClothingTop : ClothingRegion {
    NOTHING,
    T_SHIRT,
    LONG_SLEEVE_SHIRT
}

enum class ClothingBottom : ClothingRegion {
    NOTHING,
    SHORTS,
    PANTS
}

sealed interface ClothingEvent {
    data class TopSelected(val clothing: ClothingRegion) : ClothingEvent
    data class BottomSelected(val clothing: ClothingRegion) : ClothingEvent
    object ContinuePressed : ClothingEvent
}