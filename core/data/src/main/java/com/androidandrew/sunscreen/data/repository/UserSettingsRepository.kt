package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.model.UserClothing
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

    fun getClothingFlow(): Flow<UserClothing>
    suspend fun getClothing(): UserClothing
    suspend fun setClothing(clothing: UserClothing)

    fun getSpfFlow(): Flow<Int>
    suspend fun getSpf(): Int
    suspend fun setSpf(spf: Int)

    fun getIsOnSnowOrWaterFlow(): Flow<Boolean>
    suspend fun getIsOnSnowOrWater(): Boolean
    suspend fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean)
}