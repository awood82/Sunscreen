package com.androidandrew.sunscreen.ui.location

data class LocationBarState(
    val typedSoFar: String
)

sealed interface LocationBarEvent {
    data class TextChanged(val text: String) : LocationBarEvent
    data class LocationSearched(val location: String) : LocationBarEvent
}