package com.touchpanel.android.ui.widgets

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material3.Icon
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
import com.touchpanel.android.core.hardware.BrightnessInfo
import com.touchpanel.android.core.theme.*

@Composable
fun BrightnessWidget(
    brightnessInfo: BrightnessInfo,
    onBrightnessChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderHeightPx by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mode badge (Window vs System)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardElevated)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (brightnessInfo.hasSystemWritePermission) "SYSTEM" else "DISPLAY",
                color = AccentPurple,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Numerical percentage
        Text(
            text = "${brightnessInfo.percent}%",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Hardware Slider Track
        Box(
            modifier = Modifier
                .weight(1f)
                .width(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCardElevated)
                .onSizeChanged { sliderHeightPx = it.height.coerceAtLeast(1) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        val touchY = change.position.y.coerceIn(0f, sliderHeightPx.toFloat())
                        val pct = ((sliderHeightPx - touchY) / sliderHeightPx * 100).toInt()
                        onBrightnessChange(pct.coerceIn(5, 100))
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            val fillRatio = (brightnessInfo.percent / 100f).coerceIn(0.05f, 1f)
            val fillColor by animateColorAsState(
                targetValue = Color(0xFFF1F5F9).copy(alpha = 0.85f),
                label = "brightnessFill"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillRatio)
                    .clip(RoundedCornerShape(18.dp))
                    .background(fillColor)
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .width(14.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (brightnessInfo.percent > 50) Icons.Default.Brightness7 else Icons.Default.Brightness5,
                contentDescription = "Brightness level",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
