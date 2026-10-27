package com.touchpanel.android.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchpanel.android.core.theme.*

enum class CardElevationLevel {
    DEFAULT,
    ELEVATED,
    FLOATING
}

@Composable
fun WidgetCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    isEditMode: Boolean = false,
    isSelected: Boolean = false,
    isDragging: Boolean = false,
    elevationLevel: CardElevationLevel = CardElevationLevel.DEFAULT,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    val surfaceColor = when {
        isDragging -> SurfaceCardFloating.copy(alpha = 0.85f)
        isSelected -> SurfaceCardElevated
        elevationLevel == CardElevationLevel.FLOATING -> SurfaceCardFloating
        elevationLevel == CardElevationLevel.ELEVATED -> SurfaceCardElevated
        else -> SurfaceCard
    }

    val shadowElevation: Dp = when {
        isDragging -> 16.dp
        isSelected -> 10.dp
        isEditMode -> 6.dp
        elevationLevel == CardElevationLevel.FLOATING -> 6.dp
        elevationLevel == CardElevationLevel.ELEVATED -> 4.dp
        else -> 2.dp
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isSelected -> BorderSelected
            isEditMode -> BorderFocused.copy(alpha = 0.5f)
            else -> BorderSubtle
        },
        animationSpec = tween(durationMillis = 200),
        label = "cardBorderColor"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = if (isSelected) AccentPurple else Color.Black.copy(alpha = 0.8f)
            )
            .clip(shape)
            .background(surfaceColor)
            .border(
                BorderStroke(
                    width = if (isSelected) 2.dp else if (isEditMode) 1.dp else 1.dp,
                    color = animatedBorderColor
                ),
                shape
            )
            .clickable(enabled = isEditMode && onClick != null) {
                onClick?.invoke()
            }
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title.uppercase(),
                        color = if (isSelected) AccentPurple else TextSecondary,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.7.sp,
                        maxLines = 1
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentPurple)
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}
