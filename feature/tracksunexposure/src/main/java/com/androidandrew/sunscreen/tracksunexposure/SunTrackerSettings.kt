package com.androidandrew.sunscreen.tracksunexposure

import com.androidandrew.sunscreen.model.UvPrediction

data class SunTrackerSettings(
    val uvPrediction: UvPrediction,
    val skinType: Int,
    // TODO: Add clothing
    val spf: Int = 0,
    val isOnReflectiveSurface: Boolean = false
)