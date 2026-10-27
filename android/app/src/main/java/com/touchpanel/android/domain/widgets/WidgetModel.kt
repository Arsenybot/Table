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
    val pageIndex: Int = 0,
    val isEnabled: Boolean = true,
    val isRemovable: Boolean = true,
    val config: Map<String, String> = emptyMap()
)

@Serializable
data class DashboardLayout(
    val version: Int = 2,
    val columns: Int = 8,
    val rows: Int = 4,
    val pageCount: Int = 2,
    val widgets: List<WidgetModel>
)
