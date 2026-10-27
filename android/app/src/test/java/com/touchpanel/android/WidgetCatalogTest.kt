package com.touchpanel.android

import com.touchpanel.android.data.layout.DefaultLayout
import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.WidgetCatalog
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class WidgetCatalogTest {

    @Test
    fun testAllTenWidgetsRegistered() {
        val types = WidgetType.values()
        assertEquals(10, types.size)

        types.forEach { type ->
            val desc = WidgetCatalog.getDescriptor(type)
            assertNotNull("Descriptor for $type must exist", desc)
            assertTrue("Default col span must be >= min span", desc.defaultColSpan >= desc.minColSpan)
            assertTrue("Default col span must be <= max span", desc.defaultColSpan <= desc.maxColSpan)
            assertTrue("Default row span must be >= min span", desc.defaultRowSpan >= desc.minRowSpan)
            assertTrue("Default row span must be <= max span", desc.defaultRowSpan <= desc.maxRowSpan)
        }
    }

    @Test
    fun testDefaultLayoutContainsTenWidgets() {
        val layout = DefaultLayout.create()
        assertEquals(12, layout.widgets.size)
        assertEquals(8, layout.columns)
        assertEquals(4, layout.rows)
        assertEquals(2, layout.pageCount)

        // Verify key widgets are present
        val hasClock = layout.widgets.any { it.type == WidgetType.CLOCK }
        val hasMango = layout.widgets.any { it.type == WidgetType.MANGO_TALKER }
        val hasVolume = layout.widgets.any { it.type == WidgetType.VOLUME }
        val hasBrightness = layout.widgets.any { it.type == WidgetType.BRIGHTNESS }
        val hasMedia = layout.widgets.any { it.type == WidgetType.MEDIA_CONTROLS }

        assertTrue(hasClock)
        assertTrue(hasMango)
        assertTrue(hasVolume)
        assertTrue(hasBrightness)
        assertTrue(hasMedia)
    }

    @Test
    fun testLayoutJsonSerialization() {
        val layout = DefaultLayout.create()
        val json = Json { ignoreUnknownKeys = true }
        val serialized = json.encodeToString(layout)

        assertTrue(serialized.contains("w_clock"))
        assertTrue(serialized.contains("w_mango_talker"))

        val restored = json.decodeFromString<DashboardLayout>(serialized)
        assertEquals(layout.widgets.size, restored.widgets.size)
        assertEquals(layout.columns, restored.columns)
    }
}
