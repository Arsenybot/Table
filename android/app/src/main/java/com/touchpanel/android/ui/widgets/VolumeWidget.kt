package com.touchpanel.android.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import com.touchpanel.android.ui.dashboard.VolumeControlMode

@Composable
fun VolumeWidget(
    currentMode: VolumeControlMode,
    androidVolumePercent: Int,
    isAndroidMuted: Boolean,
    windowsVolumePercent: Int,
    isWindowsMuted: Boolean,
    onModeChange: (VolumeControlMode) -> Unit,
    onAndroidVolumeChange: (Int) -> Unit,
    onToggleAndroidMute: () -> Unit,
    onWindowsVolumeChange: (Int) -> Unit,
    onToggleWindowsMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderHeightPx by remember { mutableIntStateOf(1) }

    val isAndroid = currentMode == VolumeControlMode.ANDROID_DEVICE
    val activePercent = if (isAndroid) androidVolumePercent else windowsVolumePercent
    val isMuted = if (isAndroid) isAndroidMuted else isWindowsMuted

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mode Selector: Android Phone vs Windows PC
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceCardElevated)
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isAndroid) AccentPurple else Color.Transparent)
                    .clickable { onModeChange(VolumeControlMode.ANDROID_DEVICE) }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Android",
                    color = if (isAndroid) TextPrimary else TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (!isAndroid) AccentPurple else Color.Transparent)
                    .clickable { onModeChange(VolumeControlMode.WINDOWS_HOST) }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PC",
                    color = if (!isAndroid) TextPrimary else TextMuted,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Numerical Percentage readout
        Text(
            text = if (isMuted) "MUTE" else "$activePercent%",
            color = if (isMuted) StatusDisconnected else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Hardware Vertical Slider bar
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCardElevated)
                .onSizeChanged { sliderHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(currentMode) {
                    detectVerticalDragGestures { change, _ ->
                        val touchY = change.position.y.coerceIn(0f, sliderHeightPx.toFloat())
                        val pct = ((sliderHeightPx - touchY) / sliderHeightPx * 100).toInt()
                        if (isAndroid) {
                            onAndroidVolumeChange(pct.coerceIn(0, 100))
                        } else {
                            onWindowsVolumeChange(pct.coerceIn(0, 100))
                        }
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            val fillRatio = if (isMuted) 0f else (activePercent / 100f).coerceIn(0f, 1f)
            val fillColor by animateColorAsState(
                targetValue = when {
                    isMuted -> TextDisabled
                    activePercent > 85 -> StatusWarning
                    isAndroid -> AccentPurple
                    else -> Color(0xFF6366F1)
                },
                label = "volumeFillColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillRatio)
                    .clip(RoundedCornerShape(18.dp))
                    .background(fillColor)
            )

            // Inner thumb indicator line
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .width(14.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mute / Speaker Toggle button
        IconButton(
            onClick = {
                if (isAndroid) onToggleAndroidMute() else onToggleWindowsMute()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = when {
                    isMuted -> Icons.Default.VolumeMute
                    activePercent < 40 -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = "Mute toggle",
                tint = if (isMuted) StatusDisconnected else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
