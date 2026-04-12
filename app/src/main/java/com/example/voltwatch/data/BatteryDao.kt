package com.example.voltwatch.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: BatterySnapshot)

    @Query("SELECT * FROM battery_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<BatterySnapshot>>

    @Query("DELETE FROM battery_history")
    suspend fun clearHistory()
}
