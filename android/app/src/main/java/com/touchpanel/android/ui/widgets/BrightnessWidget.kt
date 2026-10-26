package com.touchpanel.android.ui.widgets

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
import com.touchpanel.android.core.theme.*

@Composable
fun BrightnessWidget(
    brightness: Int,
    onBrightnessChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderHeightPx by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$brightness%",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(8.dp))

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
                        val pct = ((sliderHeightPx - touchY) / sliderHeightPx * 100).toInt()
                        onBrightnessChange(pct.coerceIn(0, 100))
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            val fillRatio = (brightness / 100f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(fillRatio)
                    .clip(RoundedCornerShape(21.dp))
                    .background(Color(0xFFE2E8F0).copy(alpha = 0.85f))
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .width(16.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (brightness > 50) Icons.Default.Brightness7 else Icons.Default.Brightness5,
                contentDescription = "Brightness level",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
