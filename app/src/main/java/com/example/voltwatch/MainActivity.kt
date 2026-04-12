package com.example.voltwatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.voltwatch.service.BatteryWorker
import com.example.voltwatch.ui.BatteryMonitorScreen
import com.example.voltwatch.ui.HistoryScreen
import com.example.voltwatch.ui.theme.VoltWatchTheme
import com.example.voltwatch.viewmodel.BatteryViewModel
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        scheduleBatteryLogging()

        setContent {
            VoltWatchTheme {
                val navController = rememberNavController()
                val viewModel: BatteryViewModel = viewModel()
                
                NavHost(navController = navController, startDestination = "monitor") {
                    composable("monitor") {
                        BatteryMonitorScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun scheduleBatteryLogging() {
        val loggingRequest = PeriodicWorkRequestBuilder<BatteryWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "BatteryLogging",
            ExistingPeriodicWorkPolicy.KEEP,
            loggingRequest
        )
    }
}