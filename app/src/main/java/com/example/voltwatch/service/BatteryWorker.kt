package com.example.voltwatch.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.voltwatch.data.AppDatabase
import com.example.voltwatch.data.BatterySnapshot

class BatteryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val applicationContext = applicationContext
        val db = AppDatabase.getDatabase(applicationContext)
        val batteryDao = db.batteryDao()

        // Get current battery intent
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }

        batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryLevel = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

            val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified Failure"
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                else -> "Unknown"
            }

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
            val isCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING

            batteryDao.insertSnapshot(
                BatterySnapshot(
                    level = batteryLevel,
                    health = health,
                    temperature = temperature,
                    voltage = voltage,
                    technology = technology,
                    isCharging = isCharging
                )
            )

            // Check for Alert
            val prefs = applicationContext.getSharedPreferences("battery_prefs", Context.MODE_PRIVATE)
            val isAlertEnabled = prefs.getBoolean("alert_enabled", false)
            val targetLevel = prefs.getInt("target_level", 80)
            val lastLevel = prefs.getInt("last_level", -1)
            val lastNotifiedTarget = prefs.getInt("last_notified_target", -1)

            if (isAlertEnabled && lastLevel != -1) {
                val isExactHit = batteryLevel == targetLevel && lastNotifiedTarget != targetLevel
                val hasCrossedUp = lastLevel < targetLevel && batteryLevel > targetLevel
                val hasCrossedDown = lastLevel > targetLevel && batteryLevel < targetLevel

                if (isExactHit || hasCrossedUp || hasCrossedDown) {
                    sendNotification(batteryLevel)
                    prefs.edit().putInt("last_notified_target", targetLevel).apply()
                }
            }
            
            // Reset notified target if we move away
            if (kotlin.math.abs(batteryLevel - targetLevel) >= 2) {
                prefs.edit().putInt("last_notified_target", -1).apply()
            }
            
            // Save current level for next run
            prefs.edit().putInt("last_level", batteryLevel).apply()
        }

        return Result.success()
    }

    private fun sendNotification(level: Int) {
        val channelId = "voltwatch_final_fix_v4"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(channelId, "VoltWatch Urgent Notification", android.app.NotificationManager.IMPORTANCE_HIGH).apply {
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("VoltWatch Background Alert")
            .setContentText("Battery has reached $level%")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2002, notification)
    }
}
