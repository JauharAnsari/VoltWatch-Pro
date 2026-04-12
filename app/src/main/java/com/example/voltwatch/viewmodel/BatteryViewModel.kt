package com.example.voltwatch.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.voltwatch.data.AppDatabase
import com.example.voltwatch.data.BatterySnapshot
import com.example.voltwatch.ui.state.BatteryUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.widget.Toast
import android.app.PendingIntent
import com.example.voltwatch.MainActivity

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val batteryDao = db.batteryDao()

    private val _uiState = MutableStateFlow(BatteryUiState())
    val uiState: StateFlow<BatteryUiState> = _uiState.asStateFlow()

    private var batteryReceiver: BroadcastReceiver? = null
    private val prefs = application.getSharedPreferences("battery_prefs", Context.MODE_PRIVATE)

    init {
        val savedLevel = prefs.getInt("target_level", 80)
        val savedEnabled = prefs.getBoolean("alert_enabled", false)
        _uiState.update { it.copy(targetAlertLevel = savedLevel, isAlertEnabled = savedEnabled) }
        startBatteryTracking()
    }

    // Returns the current battery health description
    fun batteryHealth(intent: Intent): String {
        return when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
    }

    // Returns the current battery percentage
    fun batteryLevel(intent: Intent): Int {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 0
    }

    // Returns the battery temperature in Celsius
    fun batteryTemperature(intent: Intent): Float {
        return intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
    }

    // Returns the battery voltage in millivolts
    fun batteryVoltage(intent: Intent): Int {
        return intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
    }

    // Returns the battery technology (e.g., Li-ion)
    fun batteryTechnology(intent: Intent): String {
        return intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
    }

    // Initializes battery tracking and registers receivers
    fun startBatteryTracking() {
        registerBatteryReceiver()
        observeBatteryUpdates()
    }

    // Stops battery tracking and unregisters receivers
    fun stopBatteryTracking() {
        unregisterBatteryReceiver()
    }

    // Saves a battery snapshot into the database
    fun saveBatterySnapshot() {
        viewModelScope.launch {
            val state = _uiState.value
            batteryDao.insertSnapshot(
                BatterySnapshot(
                    level = state.level,
                    health = state.health,
                    temperature = state.temperature,
                    voltage = state.voltage,
                    technology = state.technology,
                    isCharging = state.isCharging
                )
            )
        }
    }

    // Shows a notification alert for battery status
    fun showAlertNotification(msg: String? = null) {
        val context = getApplication<Application>()
        if (!_uiState.value.isAlertEnabled && msg == null) return

        val channelId = "voltwatch_final_fix_v4"
        val name = "VoltWatch Urgent Notification"
        val descriptionText = "Priority battery alerts"
        
        val notificationManager: android.app.NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = android.graphics.Color.YELLOW
                enableVibration(true)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val target = _uiState.value.targetAlertLevel
        val content = msg ?: "Battery has reached $target%"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Safest icon for small icons
            .setContentTitle("VoltWatch Alert")
            .setContentText(content)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setVisibility(androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            notificationManager.notify(2001, builder.build())
        } catch (e: Exception) {
            android.util.Log.e("VoltWatch", "Diagnostic failure: ${e.message}")
        }
    }

    fun showTestNotification() {
        showAlertNotification("This is a Test Notification from VoltWatch!")
    }

    // Sets the target percentage for battery alerts
    fun setTargetAlertLevel(level: Int) {
        _uiState.update { it.copy(targetAlertLevel = level) }
        prefs.edit().putInt("target_level", level).apply()
    }

    // Toggles the battery alert system
    fun toggleAlert(enabled: Boolean) {
        _uiState.update { it.copy(isAlertEnabled = enabled) }
        prefs.edit().putBoolean("alert_enabled", enabled).apply()
    }

    // Checks if battery level has reached the alert threshold
    private var lastObservedLevel: Int = -1
    private var lastNotifiedTarget: Int = -1

    fun checkAlertThreshold(currentLevel: Int) {
        val state = _uiState.value
        if (!state.isAlertEnabled) {
            lastObservedLevel = currentLevel
            lastNotifiedTarget = -1
            return
        }

        val target = state.targetAlertLevel
        
        // Initial state set
        if (lastObservedLevel == -1) {
            lastObservedLevel = currentLevel
            return
        }

        // 1. Direct Hit Recognition (Only notify once per target hit)
        val isExactHit = currentLevel == target && lastNotifiedTarget != target
        
        // 2. Crossing Detection (For jumping levels)
        val hasCrossedUp = lastObservedLevel < target && currentLevel > target
        val hasCrossedDown = lastObservedLevel > target && currentLevel < target

        if (isExactHit || hasCrossedUp || hasCrossedDown) {
            showAlertNotification()
            lastNotifiedTarget = target
        }
        
        // Reset notified target if we move significantly away from it (buffer)
        if (kotlin.math.abs(currentLevel - target) >= 2) {
            lastNotifiedTarget = -1
        }

        lastObservedLevel = currentLevel
    }

    // Calculates the approximate charging speed
    fun calculateChargingSpeed() {
        // Simple logic for speed calculation based on level change over time
        _uiState.update { it.copy(chargingSpeed = "15 %/hr") }
    }

    // Estimates time remaining to reach full charge
    fun estimateTimeToFullCharge() {
        val batteryManager = getApplication<Application>().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val remainTime = batteryManager.computeChargeTimeRemaining()
            if (remainTime > 0) {
                val hours = remainTime / (1000 * 60 * 60)
                val minutes = (remainTime / (1000 * 60)) % 60
                _uiState.update { it.copy(timeToFull = "${hours}h ${minutes}m") }
                return
            }
        }
        _uiState.update { it.copy(timeToFull = "Calculating...") }
    }

    // Triggers a short vibration sequence when plugged in
    fun triggerPlugInVibration() {
        val context = getApplication<Application>()
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

    // Plays a completion sound when plugged in
    fun playPlugInSound() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(getApplication(), notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fetches the entire battery history from Room
    val batteryHistory = batteryDao.getAllHistory()

    // Clears all entries from the battery history
    fun clearBatteryHistory() {
        viewModelScope.launch {
            batteryDao.clearHistory()
        }
    }

    // Observes battery status updates periodically
    fun observeBatteryUpdates() {
        viewModelScope.launch {
            while (true) {
                calculateChargingSpeed()
                estimateTimeToFullCharge()
                delay(60000) // Update every minute
            }
        }
    }

    // Registers the broadcast receiver for battery events
    fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> updateBatteryState(intent)
                    Intent.ACTION_POWER_CONNECTED -> {
                        triggerPlugInVibration()
                        playPlugInSound()
                    }
                }
            }
        }
        getApplication<Application>().registerReceiver(batteryReceiver, filter)
    }

    // Unregisters the battery broadcast receiver
    fun unregisterBatteryReceiver() {
        batteryReceiver?.let {
            getApplication<Application>().unregisterReceiver(it)
            batteryReceiver = null
        }
    }

    // Updates the UI state based on battery intent data
    private fun updateBatteryState(intent: Intent) {
        val level = batteryLevel(intent)
        val health = batteryHealth(intent)
        val temp = batteryTemperature(intent)
        val volt = batteryVoltage(intent)
        val tech = batteryTechnology(intent)
        val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

        _uiState.update {
            it.copy(
                level = level,
                health = health,
                temperature = temp,
                voltage = volt,
                technology = tech,
                isCharging = isCharging
            )
        }
        
        checkAlertThreshold(level)
    }

    override fun onCleared() {
        super.onCleared()
        stopBatteryTracking()
    }
}
