package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.UserTracking
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserTrackingFlow(date: String): Flow<UserTracking?>
    suspend fun getUserTracking(date: String): UserTracking?
    suspend fun setUserTracking(tracking: UserTracking)

    fun getLocationFlow(): Flow<String?>
    suspend fun getLocation(): String?
    suspend fun setLocation(location: String)

    fun getSpfFlow(): Flow<Int?>
    suspend fun getSpf(): Int?
    suspend fun setSpf(spf: Int)

    fun getIsOnSnowOrWaterFlow(): Flow<Boolean?>
    suspend fun getIsOnSnowOrWater(): Boolean?
    suspend fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean)
}