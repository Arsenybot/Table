package com.touchpanel.android.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import com.touchpanel.android.data.connection.ConnectionState
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType
import com.touchpanel.android.ui.editor.WidgetCatalogDialog
import com.touchpanel.android.ui.widgets.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkGraphiteBackground)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            DashboardHeader(
                isEditMode = state.isEditMode,
                connectionState = state.connectionState,
                statusMessage = state.statusMessage,
                isErrorMessage = state.isErrorMessage,
                onToggleEdit = { viewModel.toggleEditMode() },
                onAddWidget = { viewModel.openCatalog() },
                onResetLayout = { viewModel.resetLayout() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Adaptive Landscape Dashboard Grid
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Column 1: Time, Calendar & Hardware Status (Width 220dp)
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetCard(
                        title = "ВРЕМЯ И ДАТА",
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        ClockWidget()
                    }

                    WidgetCard(
                        title = "КАЛЕНДАРЬ",
                        modifier = Modifier
                            .weight(1.8f)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        CalendarWidget()
                    }

                    WidgetCard(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        ConnectionWidget(
                            state = state.connectionState,
                            onConnect = { _, _ -> },
                            onDisconnect = { viewModel.disconnectWindows() }
                        )
                    }
                }

                // Column 2: Mango Talker Integration & Media Controls (Width 320dp)
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetCard(
                        title = "MANGO TALKER (VOIP)",
                        modifier = Modifier
                            .weight(1.7f)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        MangoTalkerWidget(
                            status = state.mangoStatus,
                            quickDialNumber = state.quickDialNumber,
                            onNumberChange = { viewModel.setQuickDialNumber(it) },
                            onDialNumber = { viewModel.dialMangoNumber(it) },
                            onLaunchApp = { viewModel.launchMangoTalkerApp() },
                            onInstallApp = { viewModel.installMangoTalker() }
                        )
                    }

                    WidgetCard(
                        title = "МЕДИАПЛЕЕР",
                        modifier = Modifier
                            .weight(1.4f)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        MediaWidget(
                            state = MediaTrackState(title = "Windows Audio Session", artist = "Готов к синхронизации"),
                            onPlayPause = { viewModel.mediaPlayPause() },
                            onNext = { viewModel.mediaNext() },
                            onPrevious = { viewModel.mediaPrevious() }
                        )
                    }

                    WidgetCard(
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        BatteryWidget(
                            batteryInfo = state.batteryInfo
                        )
                    }
                }

                // Column 3: Volume Slider (96dp)
                WidgetCard(
                    title = "ГРОМКОСТЬ",
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight(),
                    isEditMode = state.isEditMode
                ) {
                    VolumeWidget(
                        currentMode = state.volumeMode,
                        androidVolumePercent = state.androidAudio.volumePercent,
                        isAndroidMuted = state.androidAudio.isMuted,
                        windowsVolumePercent = state.windowsVolume,
                        isWindowsMuted = state.isWindowsMuted,
                        onModeChange = { viewModel.setVolumeMode(it) },
                        onAndroidVolumeChange = { viewModel.setAndroidVolume(it) },
                        onToggleAndroidMute = { viewModel.toggleAndroidMute() },
                        onWindowsVolumeChange = { viewModel.setWindowsVolume(it) },
                        onToggleWindowsMute = { viewModel.toggleWindowsMute() }
                    )
                }

                // Column 4: Brightness Slider (96dp)
                WidgetCard(
                    title = "ЯРКОСТЬ",
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight(),
                    isEditMode = state.isEditMode
                ) {
                    BrightnessWidget(
                        brightnessInfo = state.androidBrightness,
                        onBrightnessChange = { viewModel.setAndroidBrightness(it) }
                    )
                }

                // Column 5: System Shortcuts & User Custom Actions (Width 260dp)
                Column(
                    modifier = Modifier
                        .width(260.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WidgetCard(
                        title = "СИСТЕМНЫЕ ДЕЙСТВИЯ",
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        QuickActionsWidget(
                            onExecuteShortcut = { id, key -> viewModel.executeShortcut(id, key) },
                            onLaunchApp = { id -> viewModel.launchApp(id) }
                        )
                    }

                    // Dynamically configurable custom widgets
                    val customWidgets = state.layout.widgets.filter { it.type == WidgetType.CUSTOM_BUTTON }
                    if (customWidgets.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customWidgets.take(2).forEachIndexed { idx, w ->
                                WidgetCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    isEditMode = state.isEditMode,
                                    onDelete = { viewModel.removeWidget(w.id) },
                                    onMoveLeft = if (idx > 0) { { viewModel.moveWidget(idx, -1) } } else null,
                                    onMoveRight = if (idx < customWidgets.size - 1) { { viewModel.moveWidget(idx, 1) } } else null
                                ) {
                                    CustomButtonWidget(
                                        model = w,
                                        onClick = { viewModel.launchApp(w.config["actionId"] ?: "terminal") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Widget Catalog Modal
        WidgetCatalogDialog(
            isOpen = state.isCatalogOpen,
            onDismiss = { viewModel.closeCatalog() },
            onSelectWidget = { type -> viewModel.addWidget(type) }
        )
    }
}

@Composable
private fun DashboardHeader(
    isEditMode: Boolean,
    connectionState: ConnectionState,
    statusMessage: String?,
    isErrorMessage: Boolean,
    onToggleEdit: () -> Unit,
    onAddWidget: () -> Unit,
    onResetLayout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TOUCH PANEL",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Toast / Connection Indicator
            if (statusMessage != null) {
                Text(
                    text = "• $statusMessage",
                    color = if (isErrorMessage) StatusDisconnected else StatusConnected,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                val isConnected = connectionState is ConnectionState.Connected
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) StatusConnected else StatusDisconnected)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isConnected) "Windows подключен" else "Windows офлайн",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isEditMode) {
                Button(
                    onClick = onAddWidget,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Добавить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onResetLayout,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сброс", fontSize = 11.sp)
                }
            }

            Button(
                onClick = onToggleEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) AccentPurpleDark else SurfaceCardElevated
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isEditMode) "Готово" else "Раскладка",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
