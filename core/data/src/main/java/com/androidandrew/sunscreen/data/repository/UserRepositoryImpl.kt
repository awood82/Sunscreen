package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.*
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userTrackingDao: UserTrackingDao,
    private val userSettingsDao: UserSettingsDao
    ) : UserRepository {

    override fun getUserTrackingInfoSync(date: String): Flow<UserTracking?> {
        return userTrackingDao.get(date)
    }

    override suspend fun getUserTrackingInfo(date: String): UserTracking? {
        return userTrackingDao.getOnce(date)
    }

    override suspend fun setUserTrackingInfo(tracking: UserTracking) {
        userTrackingDao.insert(tracking)
    }

    override suspend fun getLocation(): String? {
        return readStringSetting(UserSettingsDao.LOCATION)
    }

    override suspend fun setLocation(location: String) {
        saveSetting(UserSettingsDao.LOCATION, location)
    }

//    suspend fun getSpf(): Int? {
//        return readIntSetting(UserSettingsDao.SPF)
//    }
//
//    suspend fun setSpf(spf: Int) {
//        saveSetting(UserSettingsDao.SPF, spf.toString())
//    }
//
//    suspend fun getOnSnowOrWater(): Boolean? {
//        return readBooleanSetting(UserSettingsDao.ON_SNOW_OR_WATER)
//    }
//
//    suspend fun setOnSnowOrWater(isOnSnowOrWater: Boolean) {
//        saveSetting(UserSettingsDao.ON_SNOW_OR_WATER, isOnSnowOrWater.toString())
//    }

    private suspend fun saveSetting(id: Long, value: String) {
        saveSetting(UserSetting(id, value))
    }

    private suspend fun saveSetting(setting: UserSetting) {
        userSettingsDao.insert(setting)
//            Timber.d("Saved ${setting.id} = ${setting.value}")
    }

    private fun readStringSettingSync(id: Long): Flow<UserSetting?> {
        return userSettingsDao.get(id)
    }

    private suspend fun readStringSetting(id: Long): String? {
        return readSetting(id)?.value
    }

//    private suspend fun readIntSetting(id: Long): Int? {
//        return readSetting(id)?.value?.toInt()
//    }
//
//    private suspend fun readBooleanSetting(id: Long): Boolean? {
//        return readSetting(id)?.value?.toBoolean()
//    }

    private suspend fun readSetting(id: Long): UserSetting? {
        return userSettingsDao.getOnce(id)
    }
}