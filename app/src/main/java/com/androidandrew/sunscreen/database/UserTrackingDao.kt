package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tracking: UserTracking)

    @Query("SELECT * FROM user_tracking_table WHERE date>=:date ORDER BY date DESC LIMIT 1")
    fun get(date: String): UserTracking?

    @Query("SELECT * FROM user_tracking_table WHERE date>=:date ORDER BY date DESC LIMIT 1")
    fun getSync(date: String): Flow<UserTracking?>

    @Query("DELETE FROM user_tracking_table")
    fun deleteAll()
}