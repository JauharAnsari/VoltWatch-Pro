package com.example.voltwatch.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.voltwatch.R
import com.example.voltwatch.ui.theme.BatteryGreenGlow
import com.example.voltwatch.ui.theme.BatteryGreenHighlight
import com.example.voltwatch.ui.theme.BatteryGreenMid
import com.example.voltwatch.ui.theme.BatteryGreenStart
import com.example.voltwatch.ui.theme.BatteryLowRed
import com.example.voltwatch.ui.theme.TextGray
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CustomCircularProgressBar(
    percentage: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "percentageAnimation"
    )

    val isLowBattery = percentage < 20
    
    val progressBrush = if (isLowBattery) {
        Brush.linearGradient(listOf(BatteryLowRed, BatteryLowRed.copy(alpha = 0.8f)))
    } else {
        Brush.sweepGradient(
            0.0f to BatteryGreenStart, // 0%
            0.4f to BatteryGreenMid,   // 40%
            0.7f to BatteryGreenHighlight, // 70%
            1.0f to BatteryGreenGlow   // 100%
        )
    }

    // Lottie Composition
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.battery_charging_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isCharging
    )

    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(280.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.width / 2) - 10.dp.toPx()
            
            // Draw background dots
            val dotRadius = 1.5.dp.toPx()
            val dotCount = 80
            for (i in 0 until dotCount) {
                val angle = (i * 360f / dotCount) - 90f
                val angleRad = Math.toRadians(angle.toDouble())
                val x = center.x + radius * cos(angleRad).toFloat()
                val y = center.y + radius * sin(angleRad).toFloat()
                drawCircle(
                    color = TextGray.copy(alpha = 0.2f),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }

            // Draw progress arc
            drawArc(
                brush = progressBrush,
                startAngle = -90f,
                sweepAngle = (animatedPercentage / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage %",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLowBattery) BatteryLowRed else Color.White
                )
            )
            Text(
                text = "CURRENT LVL",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextGray,
                    letterSpacing = 1.sp
                )
            )
            
            if (isCharging) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = null,
                    tint = if (isLowBattery) BatteryLowRed else BatteryGreenHighlight,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
