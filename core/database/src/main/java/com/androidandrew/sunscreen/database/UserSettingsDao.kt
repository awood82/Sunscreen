package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    companion object {
        const val LOCATION = 1L
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: UserSetting)

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    suspend fun getOnce(id: Long): UserSetting?

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    fun get(id: Long): Flow<UserSetting?>

    @Query("DELETE FROM user_settings_table")
    suspend fun deleteAll()
}