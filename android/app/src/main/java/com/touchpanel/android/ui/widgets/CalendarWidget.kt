package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarWidget(
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val today = remember { Calendar.getInstance() }

    val monthFormat = remember { SimpleDateFormat("LLLL yyyy", Locale("ru")) }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
        // Month header with prev/next navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthFormat.format(calendarMonth.time).replaceFirstChar { it.uppercase() },
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = {
                        val newCal = calendarMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, -1)
                        calendarMonth = newCal
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Предыдущий месяц",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val newCal = calendarMonth.clone() as Calendar
                        newCal.add(Calendar.MONTH, 1)
                        calendarMonth = newCal
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Следующий месяц",
                        tint = TextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Days of week header (Russian)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val weekDays = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            weekDays.forEachIndexed { idx, day ->
                Text(
                    text = day,
                    color = if (idx >= 5) AccentPurple.copy(alpha = 0.8f) else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Grid of dates
        val cal = calendarMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            var dayCounter = 1 - firstDayOfWeek

            for (week in 0 until 5) {
                if (dayCounter > maxDays) break
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (dow in 0 until 7) {
                        if (dayCounter in 1..maxDays) {
                            val isToday = dayCounter == today.get(Calendar.DAY_OF_MONTH) &&
                                    calendarMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                                    calendarMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR)

                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isToday) AccentPurple else androidx.compose.ui.graphics.Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (isToday) TextPrimary else TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.size(20.dp))
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}
