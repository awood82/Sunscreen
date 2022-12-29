package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.UserTrackingDao
import com.androidandrew.sunscreen.model.UserTracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserTrackingRepositoryImpl(
    private val userTrackingDao: UserTrackingDao
) : UserTrackingRepository {
    override fun getUserTrackingFlow(date: String): Flow<UserTracking?> {
        return userTrackingDao.getFlow(date).map {
            it?.toModel()
        }
    }
    override suspend fun getUserTracking(date: String): UserTracking? {
        return userTrackingDao.getOnce(date)?.toModel()
    }
    override suspend fun setUserTracking(date: String, tracking: UserTracking) {
        userTrackingDao.insert(tracking.toEntity(date))
    }
}