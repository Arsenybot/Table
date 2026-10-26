package com.touchpanel.android.ui.dashboard

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
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            DashboardHeader(
                isEditMode = state.isEditMode,
                connectionState = state.connectionState,
                statusMessage = state.statusMessage,
                onToggleEdit = { viewModel.toggleEditMode() },
                onAddWidget = { viewModel.openCatalog() },
                onResetLayout = { viewModel.resetLayout() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Adaptive Landscape Dashboard Grid
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Clock (top), Calendar (middle), Connection (bottom)
                Column(
                    modifier = Modifier.width(220.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WidgetCard(
                        title = "TIME & DATE",
                        modifier = Modifier.weight(1.3f).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        ClockWidget()
                    }

                    WidgetCard(
                        title = "CALENDAR",
                        modifier = Modifier.weight(1.8f).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        CalendarWidget()
                    }

                    WidgetCard(
                        modifier = Modifier.height(52.dp).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        ConnectionWidget(
                            state = state.connectionState,
                            onConnect = { _, _ -> },
                            onDisconnect = { viewModel.disconnectWindows() }
                        )
                    }
                }

                // Center Column: Mango Talker Call Card (top), Media Controls (middle), Battery (bottom)
                Column(
                    modifier = Modifier.width(320.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WidgetCard(
                        title = "MANGO TALKER",
                        modifier = Modifier.weight(1.7f).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        MangoTalkerWidget(
                            state = state.mangoState,
                            adapter = viewModel.mangoAdapter
                        )
                    }

                    WidgetCard(
                        title = "NOW PLAYING",
                        modifier = Modifier.weight(1.4f).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        MediaWidget(
                            state = state.mediaState,
                            onPlayPause = { viewModel.mediaPlayPause() },
                            onNext = { viewModel.mediaNext() },
                            onPrevious = { viewModel.mediaPrevious() }
                        )
                    }

                    WidgetCard(
                        modifier = Modifier.height(52.dp).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        BatteryWidget(
                            batteryPercent = state.batteryPercent,
                            isPowerConnected = state.isPowerConnected
                        )
                    }
                }

                // Volume Slider Column
                WidgetCard(
                    title = "VOLUME",
                    modifier = Modifier.width(96.dp).fillMaxHeight(),
                    isEditMode = state.isEditMode
                ) {
                    VolumeWidget(
                        volume = state.volume,
                        isMuted = state.isMuted,
                        onVolumeChange = { viewModel.setVolume(it) },
                        onToggleMute = { viewModel.toggleMute() }
                    )
                }

                // Brightness Slider Column
                WidgetCard(
                    title = "BRIGHTNESS",
                    modifier = Modifier.width(96.dp).fillMaxHeight(),
                    isEditMode = state.isEditMode
                ) {
                    BrightnessWidget(
                        brightness = state.brightness,
                        onBrightnessChange = { viewModel.setBrightness(it) }
                    )
                }

                // Right Column: System Shortcuts & User Custom Actions
                Column(
                    modifier = Modifier.width(260.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WidgetCard(
                        title = "SYSTEM ACTIONS",
                        modifier = Modifier.weight(2f).fillMaxWidth(),
                        isEditMode = state.isEditMode
                    ) {
                        QuickActionsWidget(
                            onExecuteShortcut = { id, key -> viewModel.executeShortcut(id, key) },
                            onLaunchApp = { id -> viewModel.launchApp(id) }
                        )
                    }

                    // Dynamically added custom widgets from user layout
                    val customWidgets = state.layout.widgets.filter { it.type == WidgetType.CUSTOM_BUTTON }
                    if (customWidgets.isNotEmpty()) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            customWidgets.take(2).forEach { w ->
                                WidgetCard(
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                    isEditMode = state.isEditMode,
                                    onDelete = { viewModel.removeWidget(w.id) }
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
    onToggleEdit: () -> Unit,
    onAddWidget: () -> Unit,
    onResetLayout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TOUCH PANEL",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Status message or connection state
            if (statusMessage != null) {
                Text(
                    text = "• $statusMessage",
                    color = StatusDisconnected,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
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
                        text = if (isConnected) "Windows Connected" else "Windows Offline",
                        color = TextSecondary,
                        fontSize = 11.sp
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
                    Text("Add", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onResetLayout,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", fontSize = 11.sp)
                }
            }

            Button(
                onClick = onToggleEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) AccentPurpleDark else SurfaceCardElevated
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isEditMode) "Done" else "Edit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
