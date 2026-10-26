package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.touchpanel.android.core.theme.AccentPurple
import com.touchpanel.android.core.theme.TextPrimary
import com.touchpanel.android.core.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockWidget(
    modifier: Modifier = Modifier
) {
    var currentTime by remember { mutableStateOf(Date()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle-aware clock updates: pauses tick loop when activity is paused or screen is off
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) {
                currentTime = Date()
                delay(1000)
            }
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("ru")) }
    val secondsFormat = remember { SimpleDateFormat("ss", Locale("ru")) }
    val dayFormat = remember { SimpleDateFormat("EEEE", Locale("ru")) }
    val dateFormat = remember { SimpleDateFormat("d MMMM yyyy", Locale("ru")) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = timeFormat.format(currentTime),
                color = TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                lineHeight = 42.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = secondsFormat.format(currentTime),
                color = AccentPurple,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = dayFormat.format(currentTime).uppercase(Locale("ru")),
            color = AccentPurple,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = dateFormat.format(currentTime),
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
