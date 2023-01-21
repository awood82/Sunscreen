package com.androidandrew.sunscreen.data.repository

import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {
    fun getIsOnboardedFlow(): Flow<Boolean>
    suspend fun getIsOnboarded(): Boolean
    suspend fun setIsOnboarded(isOnboarded: Boolean)

    fun getLocationFlow(): Flow<String>
    suspend fun getLocation(): String
    suspend fun setLocation(location: String)

    fun getSkinTypeFlow(): Flow<Int>
    suspend fun getSkinType(): Int
    suspend fun setSkinType(skinType: Int)

    fun getClothingFlow(): Flow<Int>
    suspend fun getClothing(): Int
    suspend fun setClothing(clothing: Int)

    fun getSpfFlow(): Flow<Int>
    suspend fun getSpf(): Int
    suspend fun setSpf(spf: Int)

    fun getIsOnSnowOrWaterFlow(): Flow<Boolean>
    suspend fun getIsOnSnowOrWater(): Boolean
    suspend fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean)
}