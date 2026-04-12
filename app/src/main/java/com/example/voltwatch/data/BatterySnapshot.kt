package com.example.voltwatch.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_history")
data class BatterySnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val level: Int,
    val health: String,
    val temperature: Float,
    val voltage: Int,
    val technology: String,
    val isCharging: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
