package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.BorderSubtle
import com.touchpanel.android.core.theme.SurfaceCardElevated
import com.touchpanel.android.core.theme.TextPrimary
import com.touchpanel.android.core.theme.TextSecondary

data class QuickActionItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsWidget(
    onExecuteShortcut: (shortcutId: String, keyCombo: String) -> Unit,
    onLaunchApp: (appId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        QuickActionItem("lock", "Lock", Icons.Default.Lock) {
            onExecuteShortcut("lock_screen", "Win+L")
        },
        QuickActionItem("taskmgr", "TaskMgr", Icons.Default.Memory) {
            onExecuteShortcut("task_manager", "Ctrl+Shift+Esc")
        },
        QuickActionItem("snip", "Snip", Icons.Default.Crop) {
            onExecuteShortcut("screen_snip", "Win+Shift+S")
        },
        QuickActionItem("calc", "Calc", Icons.Default.Calculate) {
            onLaunchApp("calc")
        },
        QuickActionItem("terminal", "Terminal", Icons.Default.Terminal) {
            onLaunchApp("wt")
        },
        QuickActionItem("browser", "Browser", Icons.Default.Language) {
            onLaunchApp("browser")
        }
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.take(3).forEach { item ->
                QuickActionTile(
                    item = item,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.drop(3).take(3).forEach { item ->
                QuickActionTile(
                    item = item,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    item: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCardElevated)
            .clickable { item.onClick() }
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.label,
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
