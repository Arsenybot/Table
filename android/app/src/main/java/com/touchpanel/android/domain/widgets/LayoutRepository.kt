package com.touchpanel.android.domain.widgets

import kotlinx.coroutines.flow.Flow

interface LayoutRepository {
    fun getLayout(): Flow<DashboardLayout>
    suspend fun saveLayout(layout: DashboardLayout)
    suspend fun addWidget(widget: WidgetModel)
    suspend fun removeWidget(widgetId: String)
    suspend fun updateWidget(widget: WidgetModel)
    suspend fun resetToDefault(): DashboardLayout
}
