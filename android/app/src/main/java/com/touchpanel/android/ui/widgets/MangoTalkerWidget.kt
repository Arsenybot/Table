package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import com.touchpanel.android.integration.mango.MangoIntegrationStatus

@Composable
fun MangoTalkerWidget(
    status: MangoIntegrationStatus,
    quickDialNumber: String,
    onNumberChange: (String) -> Unit,
    onDialNumber: (String) -> Unit,
    onLaunchApp: () -> Unit,
    onInstallApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AccentPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Mango Talker",
                        tint = AccentPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Mango Talker",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (status) {
                            is MangoIntegrationStatus.Installed -> "com.mangotele.mtalker готов"
                            is MangoIntegrationStatus.NotInstalled -> "Не установлен"
                            is MangoIntegrationStatus.Error -> "Ошибка запуска"
                        },
                        color = when (status) {
                            is MangoIntegrationStatus.Installed -> StatusConnected
                            else -> StatusWarning
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Status chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (status is MangoIntegrationStatus.Installed)
                            StatusConnected.copy(alpha = 0.15f)
                        else
                            StatusWarning.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (status is MangoIntegrationStatus.Installed) "Доступен" else "Отсутствует",
                    color = if (status is MangoIntegrationStatus.Installed) StatusConnected else StatusWarning,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Body: Quick dial input if installed, or install prompt if missing
        when (status) {
            is MangoIntegrationStatus.Installed -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick dial input field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardElevated)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = quickDialNumber,
                            onValueChange = onNumberChange,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(AccentPurple),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Call
                            ),
                            keyboardActions = KeyboardActions(
                                onCall = { onDialNumber(quickDialNumber) }
                            ),
                            decorationBox = { innerTextField ->
                                if (quickDialNumber.isEmpty()) {
                                    Text(
                                        text = "+7 (___) ___-__-__",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        )

                        IconButton(
                            onClick = { onDialNumber(quickDialNumber) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Вызов",
                                tint = StatusConnected,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Launch app button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onLaunchApp,
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SurfaceCardActive),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Открыть Mango Talker",
                                fontSize = 10.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            is MangoIntegrationStatus.NotInstalled, is MangoIntegrationStatus.Error -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Для приёма и совершения звонков установите приложение Mango Talker на это Android-устройство.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )

                    Button(
                        onClick = onInstallApp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Установить Mango Talker",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Honest footer documentation
        Text(
            text = "Интеграция через системные Intents. Прямой перехват приватных VoIP-вызовов ограничен архитектурой безопасности Android OS.",
            color = TextMuted,
            fontSize = 8.sp,
            lineHeight = 10.sp
        )
    }
}
