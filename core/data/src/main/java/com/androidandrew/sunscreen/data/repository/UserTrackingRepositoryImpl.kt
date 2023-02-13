package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.UserTrackingDao
import com.androidandrew.sunscreen.model.UserTracking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserTrackingRepositoryImpl(
    private val userTrackingDao: UserTrackingDao
) : UserTrackingRepository {
    private val NO_RECORD_YET = UserTracking(sunburnProgress = 0.0, vitaminDProgress = 0.0)

    override fun getUserTrackingFlow(date: String): Flow<UserTracking?> {
        return userTrackingDao.getDistinctFlow(date)
            .map {
                it?.toModel() ?: NO_RECORD_YET
            }
    }
    override suspend fun getUserTracking(date: String): UserTracking {
        return userTrackingDao.getOnce(date)?.toModel() ?: NO_RECORD_YET
    }
    override suspend fun setUserTracking(date: String, tracking: UserTracking) {
        userTrackingDao.insert(tracking.toEntity(date))
    }
}