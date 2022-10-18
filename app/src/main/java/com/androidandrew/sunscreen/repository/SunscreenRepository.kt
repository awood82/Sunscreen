package com.androidandrew.sunscreen.repository

import androidx.lifecycle.LiveData
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.database.UserSetting
import com.androidandrew.sunscreen.database.UserSettingsDao
import com.androidandrew.sunscreen.database.UserTracking
import kotlinx.coroutines.flow.Flow
import java.time.Clock

class SunscreenRepository(private val database: SunscreenDatabase, private val clock: Clock) {

    fun getUserTrackingInfoSync(date: String): Flow<UserTracking?> {
        return database.userTrackingDao.get(date)
    }

    suspend fun getUserTrackingInfo(date: String): UserTracking? {
        return database.userTrackingDao.getOnce(date)
    }

    suspend fun setUserTrackingInfo(tracking: UserTracking) {
        database.userTrackingDao.insert(tracking)
    }

    suspend fun getLocation(): String? {
        return readStringSetting(UserSettingsDao.LOCATION)?.value
    }

    suspend fun setLocation(location: String) {
        saveSetting(UserSettingsDao.LOCATION, location)
    }

    private suspend fun saveSetting(id: Long, value: String) {
        saveSetting(UserSetting(id, value))
    }

    private suspend fun saveSetting(setting: UserSetting) {
        database.userSettingsDao.insert(setting)
//            Timber.d("Saved ${setting.id} = ${setting.value}")
    }

    private fun readStringSettingSync(id: Long): LiveData<UserSetting?> {
        return database.userSettingsDao.get(id)
    }

    private suspend fun readStringSetting(id: Long): UserSetting? {
        return database.userSettingsDao.getOnce(id)
    }
}