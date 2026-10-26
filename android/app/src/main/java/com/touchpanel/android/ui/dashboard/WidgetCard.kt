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
    elevationLevel: CardElevationLevel = CardElevationLevel.DEFAULT,
    onDelete: (() -> Unit)? = null,
    onMoveLeft: (() -> Unit)? = null,
    onMoveRight: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    val surfaceColor = when (elevationLevel) {
        CardElevationLevel.DEFAULT -> SurfaceCard
        CardElevationLevel.ELEVATED -> SurfaceCardElevated
        CardElevationLevel.FLOATING -> SurfaceCardFloating
    }

    val shadowElevation: Dp = when {
        isEditMode -> 8.dp
        elevationLevel == CardElevationLevel.FLOATING -> 6.dp
        elevationLevel == CardElevationLevel.ELEVATED -> 4.dp
        else -> 2.dp
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isSelected -> BorderSelected
            isEditMode -> BorderFocused
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
                spotColor = if (isEditMode) AccentPurple.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.8f)
            )
            .clip(shape)
            .background(surfaceColor)
            .border(
                BorderStroke(
                    width = if (isEditMode || isSelected) 1.5.dp else 1.dp,
                    color = animatedBorderColor
                ),
                shape
            )
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (title != null || isEditMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (title != null) {
                        Text(
                            text = title.uppercase(),
                            color = if (isEditMode) AccentPurple else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            maxLines = 1
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (isEditMode) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (onMoveLeft != null) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCardActive)
                                        .clickable { onMoveLeft() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Move Left",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (onMoveRight != null) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCardActive)
                                        .clickable { onMoveRight() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Move Right",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            if (onDelete != null) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(StatusDisconnected.copy(alpha = 0.2f))
                                        .clickable { onDelete() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete widget",
                                        tint = StatusDisconnected,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                content()
            }
        }
    }
}
