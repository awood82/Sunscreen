package com.androidandrew.sunscreen.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.androidandrew.sunscreen.database.entity.UserTrackingEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserTrackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(tracking: UserTrackingEntity)

    @Query("SELECT * FROM user_tracking_table WHERE date >= :date ORDER BY date DESC LIMIT 1")
    abstract suspend fun getOnce(date: String): UserTrackingEntity?

    fun getDistinctFlow(date:String): Flow<UserTrackingEntity?> {
        return getFlow(date)
    }

    @Query("SELECT * FROM user_tracking_table WHERE date >= :date ORDER BY date DESC LIMIT 1")
    protected abstract fun getFlow(date: String): Flow<UserTrackingEntity?>

    @Query("DELETE FROM user_tracking_table")
    abstract suspend fun deleteAll()
}