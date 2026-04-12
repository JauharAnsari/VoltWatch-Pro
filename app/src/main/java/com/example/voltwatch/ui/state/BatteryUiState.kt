package com.example.voltwatch.ui.state

data class BatteryUiState(
    val level: Int = 0,
    val health: String = "Unknown",
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val technology: String = "Unknown",
    val status: String = "Unknown",
    val isCharging: Boolean = false,
    val chargingSpeed: String = "0 %/hr",
    val timeToFull: String = "Calculating...",
    val lastChargeAmount: String = "84%",
    val lastChargeTime: String = "6HR 29M AGO",
    val targetAlertLevel: Int = 80,
    val isAlertEnabled: Boolean = false
)
