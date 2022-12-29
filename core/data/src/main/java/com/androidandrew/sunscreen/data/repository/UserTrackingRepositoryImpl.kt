package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.UserTracking
import com.androidandrew.sunscreen.database.UserTrackingDao
import kotlinx.coroutines.flow.Flow

class UserTrackingRepositoryImpl(
    private val userTrackingDao: UserTrackingDao
) : UserTrackingRepository {
    override fun getUserTrackingFlow(date: String): Flow<UserTracking?> {
        return userTrackingDao.getFlow(date)
    }
    override suspend fun getUserTracking(date: String): UserTracking? {
        return userTrackingDao.getOnce(date)
    }
    override suspend fun setUserTracking(tracking: UserTracking) {
        userTrackingDao.insert(tracking)
    }
}