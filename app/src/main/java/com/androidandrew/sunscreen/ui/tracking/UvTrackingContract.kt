package com.androidandrew.sunscreen.ui.tracking

import androidx.annotation.StringRes
import com.androidandrew.sunscreen.R

// TODO: Consider breaking this down into sub-components. It has a lot of arguments.
data class UvTrackingState(
    @StringRes val buttonLabel: Int,
    val buttonEnabled: Boolean,
    val spf: String,
    val isOnSnowOrWater: Boolean,
//    val sunburnProgressLabelMinusUnits: Int,
//    val sunburnProgress0to1: Float,
//    val vitaminDProgressLabelMinusUnits: Int,
//    val vitaminDProgress0to1: Float
) {
    companion object {
        val initialState = UvTrackingState(
            buttonLabel = R.string.start_tracking,
            buttonEnabled = false,
            spf = "0",
            isOnSnowOrWater = false,
//            sunburnProgressLabelMinusUnits = 0,
//            sunburnProgress0to1 = 0.0f,
//            vitaminDProgressLabelMinusUnits = 0,
//            vitaminDProgress0to1 = 0.0f
        )
    }
}

sealed interface UvTrackingEvent {
    object TrackingButtonClicked: UvTrackingEvent
    data class SpfChanged(val spf: String): UvTrackingEvent
    data class IsOnSnowOrWaterChanged(val isOnSnowOrWater: Boolean): UvTrackingEvent
}