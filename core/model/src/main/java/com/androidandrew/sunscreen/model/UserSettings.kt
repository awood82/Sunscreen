package com.androidandrew.sunscreen.model

data class UserSettings(
    val location: String?,
    val skinType: Int?,
    val spf: Int?,
    val isOnSnowOrWater: Boolean?
)