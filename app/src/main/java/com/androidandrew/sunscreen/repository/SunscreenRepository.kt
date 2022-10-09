package com.androidandrew.sunscreen.repository

import androidx.lifecycle.LiveData
import com.androidandrew.sunscreen.database.SunscreenDatabase
import com.androidandrew.sunscreen.database.UserSetting
import com.androidandrew.sunscreen.database.UserSettingsDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock

class SunscreenRepository(private val database: SunscreenDatabase, private val clock: Clock,
                          private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

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
        withContext(dispatcher) {
            database.userSettingsDao.insert(setting)
//            Timber.d("Saved ${setting.id} = ${setting.value}")
        }
    }

    private suspend fun readStringSettingSync(id: Long): LiveData<UserSetting?> {
        return withContext(dispatcher) {
            database.userSettingsDao.getSync(id)
        }
    }

    private suspend fun readStringSetting(id: Long): UserSetting? {
        return withContext(dispatcher) {
            database.userSettingsDao.get(id)
        }
    }
}