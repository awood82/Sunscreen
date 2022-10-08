package com.androidandrew.sunscreen.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserSettingsDao {

    companion object {
        const val LOCATION = 1L
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: UserSetting)

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    fun get(id: Long): UserSetting?

    @Query("SELECT * FROM user_settings_table WHERE id=:id")
    fun getSync(id: Long): LiveData<UserSetting?>

    @Query("DELETE FROM user_settings_table")
    fun deleteAll()
}