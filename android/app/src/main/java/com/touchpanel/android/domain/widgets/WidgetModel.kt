package com.touchpanel.android.domain.widgets

import kotlinx.serialization.Serializable

@Serializable
data class WidgetModel(
    val id: String,
    val type: WidgetType,
    val title: String,
    val gridCol: Int,
    val gridRow: Int,
    val colSpan: Int,
    val rowSpan: Int,
    val isEnabled: Boolean = true,
    val isRemovable: Boolean = true,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class DashboardLayout(
    val version: Int = 1,
    val columns: Int = 12,
    val rows: Int = 6,
    val widgets: List<WidgetModel>
)
