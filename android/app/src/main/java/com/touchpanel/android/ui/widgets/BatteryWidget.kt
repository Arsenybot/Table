package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.hardware.BatteryInfo
import com.touchpanel.android.core.hardware.PowerSource
import com.touchpanel.android.core.theme.*

@Composable
fun BatteryWidget(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Hardware Battery Icon
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(SurfaceCardElevated),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(batteryInfo.percentage / 100f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                batteryInfo.isCharging -> StatusConnected
                                batteryInfo.percentage > 20 -> StatusConnected
                                else -> StatusDisconnected
                            }
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${batteryInfo.percentage}%",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Power status label
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (batteryInfo.isCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging",
                    tint = StatusWarning,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = when (batteryInfo.powerSource) {
                    PowerSource.AC -> "Dock AC"
                    PowerSource.USB -> "USB Dock"
                    PowerSource.WIRELESS -> "Qi Wireless"
                    PowerSource.BATTERY -> "${batteryInfo.temperatureCelsius}°C"
                },
                color = if (batteryInfo.isCharging) StatusWarning else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
