package com.example.voltwatch.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltwatch.ui.components.CustomCircularProgressBar
import com.example.voltwatch.ui.components.StatCard
import com.example.voltwatch.ui.theme.LightPeach
import com.example.voltwatch.ui.theme.TextGray
import com.example.voltwatch.viewmodel.BatteryViewModel

@Composable
fun BatteryMonitorScreen(
    viewModel: BatteryViewModel,
    onNavigateToHistory: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted
            }
        }
    )

    LaunchedEffect(state.isAlertEnabled) {
        if (state.isAlertEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp), // Increased padding to clear status bar/notch
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "History",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                Text(
                    text = "VoltWatch Pro",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Light
                    )
                )
                Text(
                    text = if (state.isCharging) "CHARGING..." else "PLUG IN THE CABLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (state.isCharging) MaterialTheme.colorScheme.secondary else LightPeach,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Circular Progress
            CustomCircularProgressBar(
                percentage = state.level,
                isCharging = state.isCharging,
                modifier = Modifier.padding(8.dp)
            )

            // Battery Details (Temp, Volt, Tech)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BatteryDetailItem(label = "TEMP", value = "${state.temperature}°C")
                BatteryDetailItem(label = "VOLT", value = "${state.voltage}mV")
                BatteryDetailItem(label = "TECH", value = state.technology)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Last charge",
                    value = "To ${state.lastChargeAmount}",
                    detail = state.lastChargeTime,
                    icon = Icons.Default.Power,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Battery health",
                    value = state.health,
                    detail = "MAX CAPACITY",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }

            // Alert Controller Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ALERT SYSTEM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextGray,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Target: ${state.targetAlertLevel}%",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.TextButton(
                                onClick = { viewModel.showTestNotification() },
                                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                            }
                            Switch(
                                checked = state.isAlertEnabled,
                                onCheckedChange = { 
                                    viewModel.toggleAlert(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                    uncheckedThumbColor = TextGray,
                                    uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                    
                    if (state.isAlertEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Slider(
                            value = state.targetAlertLevel.toFloat(),
                            onValueChange = { viewModel.setTargetAlertLevel(it.toInt()) },
                            valueRange = 1f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tip: For Android 14, ensure 'Unrestricted' battery usage is enabled if background alerts are delayed.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextGray.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                lineHeight = 12.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Available Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AVAILABLE TIME",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextGray,
                        letterSpacing = 1.sp
                    )
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = if (state.isCharging) state.timeToFull else "12 HR 45 M",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BatteryDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = com.example.voltwatch.ui.theme.TextGray,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
    }
}
