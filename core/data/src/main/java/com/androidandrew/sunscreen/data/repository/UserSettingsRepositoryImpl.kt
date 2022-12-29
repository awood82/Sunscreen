package com.androidandrew.sunscreen.data.repository

import com.androidandrew.sunscreen.database.*
import com.androidandrew.sunscreen.database.entity.UserSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepositoryImpl(
    private val userSettingsDao: UserSettingsDao
) : UserSettingsRepository {

    companion object {
        //        const val HAS_SETUP_COMPLETED = 1L  ( // TODO : With renumbering)
        const val LOCATION = 1L
        const val SKIN_TYPE = 2L
        const val SPF = 10L
        const val IS_ON_SNOW_OR_WATER = 11L
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

    override fun getSkinTypeFlow(): Flow<Int?> {
        return readIntSettingFlow(SKIN_TYPE)
    }
    override suspend fun getSkinType(): Int? {
        return readIntSetting(SKIN_TYPE)
    }
    override suspend fun setSkinType(skinType: Int) {
        writeSetting(SKIN_TYPE, skinType.toString())
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
        writeSetting(UserSettingEntity(id, value))
    }
    private suspend fun writeSetting(setting: UserSettingEntity) {
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

    private fun readSettingFlow(id: Long): Flow<UserSettingEntity?> {
        return userSettingsDao.getFlow(id)
    }
    private suspend fun readSetting(id: Long): UserSettingEntity? {
        return userSettingsDao.getOnce(id)
    }
}