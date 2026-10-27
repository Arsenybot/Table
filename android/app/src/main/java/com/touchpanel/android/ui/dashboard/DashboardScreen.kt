package com.touchpanel.android.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*
import com.touchpanel.android.data.connection.ConnectionState
import com.touchpanel.android.domain.widgets.GridCollisionResolver
import com.touchpanel.android.domain.widgets.WidgetCatalog
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType
import com.touchpanel.android.ui.editor.WidgetCatalogDialog
import com.touchpanel.android.ui.widgets.*
import kotlin.math.roundToInt

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
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            DashboardHeader(
                isEditMode = state.isEditMode,
                activePage = state.activePage,
                pageCount = state.layout.pageCount,
                connectionState = state.connectionState,
                statusMessage = state.statusMessage,
                isErrorMessage = state.isErrorMessage,
                onToggleEdit = { viewModel.toggleEditMode() },
                onAddWidget = { viewModel.openCatalog() },
                onResetLayout = { viewModel.resetLayout() },
                onPageChange = { viewModel.setActivePage(it) }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Main Canvas with 7-Layer Architecture
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                MobileDashboardGrid(
                    state = state,
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Bottom Page Navigation Bar (when 2+ pages)
            if (state.layout.pageCount > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until state.layout.pageCount) {
                        val isCurrent = i == state.activePage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(if (isCurrent) 18.dp else 10.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) AccentPurple else BorderSubtle)
                                .clickable { viewModel.setActivePage(i) }
                        )
                    }
                }
            }
        }

        // Widget Catalog Modal (Layer 6)
        WidgetCatalogDialog(
            isOpen = state.isCatalogOpen,
            onDismiss = { viewModel.closeCatalog() },
            onSelectWidget = { type -> viewModel.addWidget(type) }
        )
    }
}

@Composable
private fun MobileDashboardGrid(
    state: DashboardUiState,
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val layout = state.layout
    val columns = layout.columns
    val rows = layout.rows
    val currentPage = state.activePage
    val pageWidgets = layout.widgets.filter { it.pageIndex == currentPage }

    val gapDp = 8.dp
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val totalColGap = gapDp * (columns - 1)
        val totalRowGap = gapDp * (rows - 1)

        val cellWidthDp = ((availableWidth - totalColGap) / columns).coerceAtLeast(20.dp)
        val cellHeightDp = ((availableHeight - totalRowGap) / rows).coerceAtLeast(20.dp)

        val cellWidthPx = with(density) { cellWidthDp.toPx() }
        val cellHeightPx = with(density) { cellHeightDp.toPx() }
        val gapPx = with(density) { gapDp.toPx() }

        // Local drag state
        var draggingWidgetId by remember { mutableStateOf<String?>(null) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }
        var ghostCol by remember { mutableStateOf<Int?>(null) }
        var ghostRow by remember { mutableStateOf<Int?>(null) }

        // LAYER 1: Normal Grid Widgets
        Box(modifier = Modifier.fillMaxSize()) {
            pageWidgets.forEach { widget ->
                val isSelected = state.isEditMode && state.selectedWidgetId == widget.id
                val isBeingDragged = draggingWidgetId == widget.id

                val xPosDp = (cellWidthDp + gapDp) * widget.gridCol
                val yPosDp = (cellHeightDp + gapDp) * widget.gridRow
                val widgetWidthDp = cellWidthDp * widget.colSpan + gapDp * (widget.colSpan - 1)
                val widgetHeightDp = cellHeightDp * widget.rowSpan + gapDp * (widget.rowSpan - 1)

                if (!isBeingDragged) {
                    Box(
                        modifier = Modifier
                            .offset(x = xPosDp, y = yPosDp)
                            .size(width = widgetWidthDp, height = widgetHeightDp)
                            .pointerInput(widget.id, state.isEditMode) {
                                if (state.isEditMode) {
                                    detectDragGestures(
                                        onDragStart = {
                                            viewModel.selectWidget(widget.id)
                                            draggingWidgetId = widget.id
                                            ghostCol = widget.gridCol
                                            ghostRow = widget.gridRow
                                            dragOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount

                                            // Calculate ghost grid position
                                            val currentXPx = widget.gridCol * (cellWidthPx + gapPx) + dragOffset.x
                                            val currentYPx = widget.gridRow * (cellHeightPx + gapPx) + dragOffset.y

                                            val calculatedCol = (currentXPx / (cellWidthPx + gapPx)).roundToInt()
                                                .coerceIn(0, columns - widget.colSpan)
                                            val calculatedRow = (currentYPx / (cellHeightPx + gapPx)).roundToInt()
                                                .coerceIn(0, rows - widget.rowSpan)

                                            ghostCol = calculatedCol
                                            ghostRow = calculatedRow
                                        },
                                        onDragEnd = {
                                            val targetCol = ghostCol ?: widget.gridCol
                                            val targetRow = ghostRow ?: widget.gridRow
                                            viewModel.moveWidgetToSlot(widget.id, targetCol, targetRow, currentPage)
                                            draggingWidgetId = null
                                            ghostCol = null
                                            ghostRow = null
                                            dragOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggingWidgetId = null
                                            ghostCol = null
                                            ghostRow = null
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                }
                            }
                    ) {
                        WidgetCard(
                            title = widget.title,
                            isEditMode = state.isEditMode,
                            isSelected = isSelected,
                            isDragging = false,
                            onClick = {
                                if (state.isEditMode) {
                                    if (state.selectedWidgetId == widget.id) {
                                        viewModel.selectWidget(null)
                                    } else {
                                        viewModel.selectWidget(widget.id)
                                    }
                                }
                            }
                        ) {
                            RenderWidgetBody(widget, state, viewModel)
                        }
                    }
                }
            }
        }

        // LAYER 2: Ghost Drop Target Slot (Dashed outline where widget will drop)
        if (state.isEditMode && draggingWidgetId != null && ghostCol != null && ghostRow != null) {
            val draggingWidget = pageWidgets.find { it.id == draggingWidgetId }
            if (draggingWidget != null) {
                val gCol = ghostCol!!
                val gRow = ghostRow!!
                val ghostXDp = (cellWidthDp + gapDp) * gCol
                val ghostYDp = (cellHeightDp + gapDp) * gRow
                val ghostWidthDp = cellWidthDp * draggingWidget.colSpan + gapDp * (draggingWidget.colSpan - 1)
                val ghostHeightDp = cellHeightDp * draggingWidget.rowSpan + gapDp * (draggingWidget.rowSpan - 1)

                Box(
                    modifier = Modifier
                        .offset(x = ghostXDp, y = ghostYDp)
                        .size(width = ghostWidthDp, height = ghostHeightDp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentPurple.copy(alpha = 0.15f))
                        .border(2.dp, AccentPurple.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                )
            }
        }

        // LAYER 3: Lifted Dragging Widget Card
        if (draggingWidgetId != null) {
            val draggingWidget = pageWidgets.find { it.id == draggingWidgetId }
            if (draggingWidget != null) {
                val origXDp = (cellWidthDp + gapDp) * draggingWidget.gridCol
                val origYDp = (cellHeightDp + gapDp) * draggingWidget.gridRow
                val widgetWidthDp = cellWidthDp * draggingWidget.colSpan + gapDp * (draggingWidget.colSpan - 1)
                val widgetHeightDp = cellHeightDp * draggingWidget.rowSpan + gapDp * (draggingWidget.rowSpan - 1)

                val dragXDp = origXDp + with(density) { dragOffset.x.toDp() }
                val dragYDp = origYDp + with(density) { dragOffset.y.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = dragXDp, y = dragYDp)
                        .size(width = widgetWidthDp, height = widgetHeightDp)
                ) {
                    WidgetCard(
                        title = draggingWidget.title,
                        isEditMode = true,
                        isSelected = true,
                        isDragging = true
                    ) {
                        RenderWidgetBody(draggingWidget, state, viewModel)
                    }
                }
            }
        }

        // LAYER 4 & 5: Selection Overlay & Floating External Context Toolbar
        if (state.isEditMode && state.selectedWidgetId != null && draggingWidgetId == null) {
            val selectedWidget = pageWidgets.find { it.id == state.selectedWidgetId }
            if (selectedWidget != null) {
                val selXDp = (cellWidthDp + gapDp) * selectedWidget.gridCol
                val selYDp = (cellHeightDp + gapDp) * selectedWidget.gridRow
                val selWidthDp = cellWidthDp * selectedWidget.colSpan + gapDp * (selectedWidget.colSpan - 1)
                val selHeightDp = cellHeightDp * selectedWidget.rowSpan + gapDp * (selectedWidget.rowSpan - 1)

                // LAYER 4: 44dp Touch Resize Handle at bottom right of card
                val handleSize = 44.dp
                Box(
                    modifier = Modifier
                        .offset(
                            x = selXDp + selWidthDp - handleSize / 2,
                            y = selYDp + selHeightDp - handleSize / 2
                        )
                        .size(handleSize)
                        .pointerInput(selectedWidget.id) {
                            detectDragGestures(
                                onDragEnd = {},
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    if (dragAmount.x > 30f) {
                                        viewModel.resizeWidget(selectedWidget.id, deltaCol = 1, deltaRow = 0)
                                    } else if (dragAmount.x < -30f) {
                                        viewModel.resizeWidget(selectedWidget.id, deltaCol = -1, deltaRow = 0)
                                    }
                                    if (dragAmount.y > 30f) {
                                        viewModel.resizeWidget(selectedWidget.id, deltaCol = 0, deltaRow = 1)
                                    } else if (dragAmount.y < -30f) {
                                        viewModel.resizeWidget(selectedWidget.id, deltaCol = 0, deltaRow = -1)
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(AccentPurple)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenWith,
                            contentDescription = "Resize Handle",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // LAYER 5: Floating Context Toolbar (Anchored ABOVE or BELOW the card outside its box)
                val toolbarAbove = selYDp > 56.dp
                val toolbarYDp = if (toolbarAbove) (selYDp - 48.dp) else (selYDp + selHeightDp + 6.dp)
                val toolbarXDp = (selXDp + (selWidthDp / 2) - 130.dp).coerceIn(4.dp, availableWidth - 264.dp)

                Surface(
                    modifier = Modifier
                        .offset(x = toolbarXDp, y = toolbarYDp)
                        .height(42.dp)
                        .shadow(12.dp, RoundedCornerShape(21.dp)),
                    shape = RoundedCornerShape(21.dp),
                    color = SurfaceCardFloating,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSelected)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Width -
                        IconButton(
                            onClick = { viewModel.resizeWidget(selectedWidget.id, deltaCol = -1, deltaRow = 0) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Narrower", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }

                        // Width +
                        IconButton(
                            onClick = { viewModel.resizeWidget(selectedWidget.id, deltaCol = 1, deltaRow = 0) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Wider", tint = TextPrimary, modifier = Modifier.size(16.dp))
                        }

                        // Move Page
                        if (layout.pageCount > 1) {
                            val targetPage = if (currentPage == 0) 1 else 0
                            IconButton(
                                onClick = { viewModel.moveWidgetToPage(selectedWidget.id, targetPage) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentPage == 0) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                                    contentDescription = "Move to Page ${targetPage + 1}",
                                    tint = AccentPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Delete
                        if (selectedWidget.isRemovable) {
                            IconButton(
                                onClick = { viewModel.removeWidget(selectedWidget.id) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusDisconnected, modifier = Modifier.size(16.dp))
                            }
                        }

                        // Done / Close selection
                        IconButton(
                            onClick = { viewModel.selectWidget(null) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = StatusConnected, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderWidgetBody(
    widget: WidgetModel,
    state: DashboardUiState,
    viewModel: DashboardViewModel
) {
    when (widget.type) {
        WidgetType.CLOCK -> ClockWidget()
        WidgetType.CALENDAR -> CalendarWidget()
        WidgetType.MANGO_TALKER -> MangoTalkerWidget(
            status = state.mangoStatus,
            quickDialNumber = state.quickDialNumber,
            onNumberChange = { viewModel.setQuickDialNumber(it) },
            onDialNumber = { viewModel.dialMangoNumber(it) },
            onLaunchApp = { viewModel.launchMangoTalkerApp() },
            onInstallApp = { viewModel.installMangoTalker() }
        )
        WidgetType.MEDIA_CONTROLS -> MediaWidget(
            state = MediaTrackState(title = "Windows Audio Session", artist = "Готов к синхронизации"),
            onPlayPause = { viewModel.mediaPlayPause() },
            onNext = { viewModel.mediaNext() },
            onPrevious = { viewModel.mediaPrevious() }
        )
        WidgetType.VOLUME -> VolumeWidget(
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
        WidgetType.BRIGHTNESS -> BrightnessWidget(
            brightnessInfo = state.androidBrightness,
            onBrightnessChange = { viewModel.setAndroidBrightness(it) }
        )
        WidgetType.BATTERY_STATUS -> BatteryWidget(batteryInfo = state.batteryInfo)
        WidgetType.CONNECTION_STATUS -> ConnectionWidget(
            state = state.connectionState,
            onConnect = { _, _ -> },
            onDisconnect = { viewModel.disconnectWindows() }
        )
        WidgetType.QUICK_ACTIONS -> QuickActionsWidget(
            onExecuteShortcut = { id, key -> viewModel.executeShortcut(id, key) },
            onLaunchApp = { id -> viewModel.launchApp(id) }
        )
        WidgetType.CUSTOM_BUTTON -> CustomButtonWidget(
            model = widget,
            onClick = { viewModel.launchApp(widget.config["actionId"] ?: "terminal") }
        )
    }
}

@Composable
private fun DashboardHeader(
    isEditMode: Boolean,
    activePage: Int,
    pageCount: Int,
    connectionState: ConnectionState,
    statusMessage: String?,
    isErrorMessage: Boolean,
    onToggleEdit: () -> Unit,
    onAddWidget: () -> Unit,
    onResetLayout: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "TOUCH PANEL",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Page Selector Pills
            if (pageCount > 1) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (i in 0 until pageCount) {
                        val active = i == activePage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) AccentPurple else Color.Transparent)
                                .clickable { onPageChange(i) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Стр ${i + 1}",
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))
            }

            // Toast / Connection Indicator
            if (statusMessage != null) {
                Text(
                    text = "• $statusMessage",
                    color = if (isErrorMessage) StatusDisconnected else StatusConnected,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            } else {
                val isConnected = connectionState is ConnectionState.Connected
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) StatusConnected else StatusDisconnected)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isConnected) "Windows онлайн" else "Windows офлайн",
                        color = TextSecondary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (isEditMode) {
                Button(
                    onClick = onAddWidget,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Виджет", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onResetLayout,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Сброс", fontSize = 10.sp)
                }
            }

            Button(
                onClick = onToggleEdit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEditMode) AccentPurpleDark else SurfaceCardElevated
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isEditMode) "Готово" else "Настроить",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
