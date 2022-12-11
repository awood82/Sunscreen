package com.androidandrew.sunscreen.service

import com.androidandrew.sunscreen.tracker.uv.UvPrediction

data class SunTrackerSettings(
    val uvPrediction: UvPrediction,
    val hardcodedSkinType: Int = 2, // TODO: Remove hardcoded value
    val spf: Int = 0,
    val isOnReflectiveSurface: Boolean = false
)