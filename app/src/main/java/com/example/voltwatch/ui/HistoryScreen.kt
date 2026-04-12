package com.example.voltwatch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltwatch.data.BatterySnapshot
import com.example.voltwatch.ui.theme.TextGray
import com.example.voltwatch.viewmodel.BatteryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: BatteryViewModel, onBack: () -> Unit) {
    val history by viewModel.batteryHistory.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("BATTERY HISTORY", style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 2.sp)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(history) { snapshot ->
                HistoryItem(snapshot)
                HorizontalDivider(color = TextGray.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun HistoryItem(snapshot: BatterySnapshot) {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val date = sdf.format(Date(snapshot.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${snapshot.level}%",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (snapshot.level > 20) Color.White else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(text = date, style = MaterialTheme.typography.labelSmall.copy(color = TextGray))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = snapshot.health, style = MaterialTheme.typography.bodyLarge.copy(color = Color.White))
            Text(text = "${snapshot.temperature}°C", style = MaterialTheme.typography.labelSmall.copy(color = TextGray))
        }
    }
}
