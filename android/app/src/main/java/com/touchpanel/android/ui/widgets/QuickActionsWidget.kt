package com.touchpanel.android.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    val isDangerous: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun QuickActionsWidget(
    onExecuteShortcut: (shortcutId: String, keyCombo: String) -> Unit,
    onLaunchApp: (appId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    fun safeClick(action: () -> Unit) {
        val now = System.currentTimeMillis()
        if (now - lastClickTime > 400) { // 400ms debounce
            lastClickTime = now
            action()
        }
    }

    val items = listOf(
        QuickActionItem("lock", "Блок. Win+L", Icons.Default.Lock, isDangerous = true) {
            safeClick { onExecuteShortcut("lock_screen", "Win+L") }
        },
        QuickActionItem("taskmgr", "Диспетчер", Icons.Default.Memory) {
            safeClick { onExecuteShortcut("task_manager", "Ctrl+Shift+Esc") }
        },
        QuickActionItem("snip", "Скриншот", Icons.Default.Crop) {
            safeClick { onExecuteShortcut("screen_snip", "Win+Shift+S") }
        },
        QuickActionItem("calc", "Калькулятор", Icons.Default.Calculate) {
            safeClick { onLaunchApp("calc") }
        },
        QuickActionItem("terminal", "Терминал", Icons.Default.Terminal) {
            safeClick { onLaunchApp("wt") }
        },
        QuickActionItem("browser", "Браузер", Icons.Default.Language) {
            safeClick { onLaunchApp("browser") }
        }
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.take(3).forEach { item ->
                QuickActionTile(
                    item = item,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.drop(3).take(3).forEach { item ->
                QuickActionTile(
                    item = item,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
            .padding(4.dp),
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
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = item.label,
                color = TextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
