package com.touchpanel.android.data.layout

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.LayoutRepository
import com.touchpanel.android.domain.widgets.WidgetModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "touch_panel_layout")

class DataStoreLayoutRepository(
    private val context: Context
) : LayoutRepository {

    private val layoutKey = stringPreferencesKey("dashboard_layout_json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    override fun getLayout(): Flow<DashboardLayout> {
        return context.dataStore.data.map { preferences ->
            val jsonString = preferences[layoutKey]
            if (jsonString.isNullOrBlank()) {
                DefaultLayout.create()
            } else {
                try {
                    json.decodeFromString<DashboardLayout>(jsonString)
                } catch (e: Exception) {
                    DefaultLayout.create()
                }
            }
        }
    }

    override suspend fun saveLayout(layout: DashboardLayout) {
        val serialized = json.encodeToString(layout)
        context.dataStore.edit { preferences ->
            preferences[layoutKey] = serialized
        }
    }

    override suspend fun addWidget(widget: WidgetModel) {
        context.dataStore.edit { preferences ->
            val current = getOrCreateCurrentLayout(preferences[layoutKey])
            val updated = current.copy(widgets = current.widgets + widget)
            preferences[layoutKey] = json.encodeToString(updated)
        }
    }

    override suspend fun removeWidget(widgetId: String) {
        context.dataStore.edit { preferences ->
            val current = getOrCreateCurrentLayout(preferences[layoutKey])
            val updated = current.copy(widgets = current.widgets.filter { it.id != widgetId })
            preferences[layoutKey] = json.encodeToString(updated)
        }
    }

    override suspend fun updateWidget(widget: WidgetModel) {
        context.dataStore.edit { preferences ->
            val current = getOrCreateCurrentLayout(preferences[layoutKey])
            val updated = current.copy(widgets = current.widgets.map {
                if (it.id == widget.id) widget else it
            })
            preferences[layoutKey] = json.encodeToString(updated)
        }
    }

    override suspend fun resetToDefault(): DashboardLayout {
        val default = DefaultLayout.create()
        saveLayout(default)
        return default
    }

    private fun getOrCreateCurrentLayout(raw: String?): DashboardLayout {
        if (raw.isNullOrBlank()) return DefaultLayout.create()
        return try {
            json.decodeFromString(raw)
        } catch (e: Exception) {
            DefaultLayout.create()
        }
    }
}
