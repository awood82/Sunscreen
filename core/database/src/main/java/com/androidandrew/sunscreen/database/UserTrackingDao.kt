package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.UserTrackingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tracking: UserTrackingEntity)

    @Query("SELECT * FROM user_tracking_table WHERE date>=:date ORDER BY date DESC LIMIT 1")
    suspend fun getOnce(date: String): UserTrackingEntity?

    @Query("SELECT * FROM user_tracking_table WHERE date>=:date ORDER BY date DESC LIMIT 1")
    fun getFlow(date: String): Flow<UserTrackingEntity?>

    @Query("DELETE FROM user_tracking_table")
    suspend fun deleteAll()
}