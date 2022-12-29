package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.model.UserTracking
import kotlinx.coroutines.flow.Flow

interface UserTrackingRepository {
    fun getUserTrackingFlow(date: String): Flow<UserTracking?>
    suspend fun getUserTracking(date: String): UserTracking?
    suspend fun setUserTracking(date: String, tracking: UserTracking)
}