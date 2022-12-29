package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.UserSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: UserSettingEntity)

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    suspend fun getOnce(id: Long): UserSettingEntity?

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    fun getFlow(id: Long): Flow<UserSettingEntity?>

    @Query("DELETE FROM user_settings_table")
    suspend fun deleteAll()
}