package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.UserTracking
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserTrackingInfoSync(date: String): Flow<UserTracking?>

    suspend fun getUserTrackingInfo(date: String): UserTracking?

    suspend fun setUserTrackingInfo(tracking: UserTracking)

    suspend fun getLocation(): String?

    suspend fun setLocation(location: String)

//    suspend fun getSpf(): Int?
//
//    suspend fun setSpf(spf: Int)
//
//    suspend fun getOnSnowOrWater(): Boolean?
//
//    suspend fun setOnSnowOrWater(isOnSnowOrWater: Boolean)
}