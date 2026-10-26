package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
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

@Composable
fun VolumeWidget(
    volume: Int,
    isMuted: Boolean,
    onVolumeChange: (Int) -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderHeightPx by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Percentage indicator
        Text(
            text = if (isMuted) "MUTE" else "$volume%",
            color = if (isMuted) StatusDisconnected else TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hardware Vertical Slider bar
        Box(
            modifier = Modifier
                .weight(1f)
                .width(42.dp)
                .clip(RoundedCornerShape(21.dp))
                .background(SurfaceCardElevated)
                .onSizeChanged { sliderHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        val touchY = change.position.y.coerceIn(0f, sliderHeightPx.toFloat())
                        // Bottom is 0%, top is 100%
                        val pct = ((sliderHeightPx - touchY) / sliderHeightPx * 100).toInt()
                        onVolumeChange(pct.coerceIn(0, 100))
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Filled portion
            val fillRatio = if (isMuted) 0f else (volume / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillRatio)
                    .clip(RoundedCornerShape(21.dp))
                    .background(
                        if (volume > 85) StatusWarning else AccentPurple
                    )
            )

            // Inner thumb indicator line
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mute / Speaker Toggle button
        IconButton(
            onClick = onToggleMute,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = when {
                    isMuted -> Icons.Default.VolumeMute
                    volume < 40 -> Icons.Default.VolumeDown
                    else -> Icons.Default.VolumeUp
                },
                contentDescription = "Mute toggle",
                tint = if (isMuted) StatusDisconnected else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
