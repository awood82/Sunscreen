package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userTrackingDao: UserTrackingDao,
    private val userSettingsDao: UserSettingsDao
) : UserRepository {

    companion object {
        //        const val HAS_SETUP_COMPLETED = 1L  ( // TODO : With renumbering)
        const val LOCATION = 1L
        const val SPF = 10L
        const val IS_ON_SNOW_OR_WATER = 11L
    }

    override fun getUserTrackingFlow(date: String): Flow<UserTracking?> {
        return userTrackingDao.getFlow(date)
    }
    override suspend fun getUserTracking(date: String): UserTracking? {
        return userTrackingDao.getOnce(date)
    }
    override suspend fun setUserTracking(tracking: UserTracking) {
        userTrackingDao.insert(tracking)
    }

    override fun getLocationFlow(): Flow<String?> {
        return readStringSettingFlow(LOCATION)
    }
    override suspend fun getLocation(): String? {
        return readStringSetting(LOCATION)
    }
    override suspend fun setLocation(location: String) {
        writeSetting(LOCATION, location)
    }

    override fun getSpfFlow(): Flow<Int?> {
        return readIntSettingFlow(SPF)
    }
    override suspend fun getSpf(): Int? {
        return readIntSetting(SPF)
    }
    override suspend fun setSpf(spf: Int) {
        writeSetting(SPF, spf.toString())
    }

    override fun getIsOnSnowOrWaterFlow(): Flow<Boolean?> {
        return readBooleanSettingFlow(IS_ON_SNOW_OR_WATER)
    }
    override suspend fun getIsOnSnowOrWater(): Boolean? {
        return readBooleanSetting(IS_ON_SNOW_OR_WATER)
    }
    override suspend fun setIsOnSnowOrWater(isOnSnowOrWater: Boolean) {
        writeSetting(IS_ON_SNOW_OR_WATER, isOnSnowOrWater.toString())
    }

    private suspend fun writeSetting(id: Long, value: String) {
        writeSetting(UserSetting(id, value))
    }
    private suspend fun writeSetting(setting: UserSetting) {
        userSettingsDao.insert(setting)
    }

    private fun readStringSettingFlow(id: Long): Flow<String?> {
        return readSettingFlow(id).map {
            it?.value
        }
    }
    private suspend fun readStringSetting(id: Long): String? {
        return readSetting(id)?.value
    }

    private fun readIntSettingFlow(id: Long): Flow<Int?> {
        return readSettingFlow(id).map {
            it?.value?.toIntOrNull()
        }
    }
    private suspend fun readIntSetting(id: Long): Int? {
        return readSetting(id)?.value?.toIntOrNull()
    }

    private fun readBooleanSettingFlow(id: Long): Flow<Boolean?> {
        return readSettingFlow(id).map {
            it?.value?.toBooleanStrictOrNull()
        }
    }
    private suspend fun readBooleanSetting(id: Long): Boolean? {
        return readSetting(id)?.value?.toBooleanStrictOrNull()
    }

    private fun readSettingFlow(id: Long): Flow<UserSetting?> {
        return userSettingsDao.getFlow(id)
    }
    private suspend fun readSetting(id: Long): UserSetting? {
        return userSettingsDao.getOnce(id)
    }
}