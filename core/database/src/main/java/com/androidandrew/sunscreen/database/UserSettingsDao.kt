package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.UserSettingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings_table WHERE id = :id")
    abstract suspend fun getOnce(id: Long): UserSettingEntity?

    fun getDistinctFlow(id: Long): Flow<UserSettingEntity?> {
        return getFlow(id).distinctUntilChanged()
    }

    @Query("SELECT * FROM user_settings_table WHERE id = :id")
    protected abstract fun getFlow(id: Long): Flow<UserSettingEntity?>

    @Query("DELETE FROM user_settings_table")
    abstract suspend fun deleteAll()
}