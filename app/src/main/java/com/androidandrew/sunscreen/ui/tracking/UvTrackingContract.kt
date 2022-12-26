package com.androidandrew.sunscreen.ui.tracking

// TODO: Consider breaking this down into sub-components. It has a lot of arguments.
// TODO: Also consider making this a sealed interface w/ one state, so initialState might not be needed.
// TODO: Also, it's UI-centric (e.g. button) instead of telling the state of the UI.
data class UvTrackingState(
    val isTrackingPossible: Boolean,
    val isTracking: Boolean,
    val spfOfSunscreenAppliedToSkin: String,
    val isOnSnowOrWater: Boolean,
    val sunburnProgressAmount: Int,
    val sunburnProgressPercent0to1: Float,
    val vitaminDProgressAmount: Int,
    val vitaminDProgressPercent0to1: Float
) {
    companion object {
        val initialState = UvTrackingState(
            isTrackingPossible = false,
            isTracking = false,
            spfOfSunscreenAppliedToSkin = "0",
            isOnSnowOrWater = false,
            sunburnProgressAmount = 0,
            sunburnProgressPercent0to1 = 0.0f,
            vitaminDProgressAmount = 0,
            vitaminDProgressPercent0to1 = 0.0f
        )
    }
}

sealed interface UvTrackingEvent {
    object TrackingButtonClicked: UvTrackingEvent
    data class SpfChanged(val spf: String): UvTrackingEvent
    data class IsOnSnowOrWaterChanged(val isOnSnowOrWater: Boolean): UvTrackingEvent
}