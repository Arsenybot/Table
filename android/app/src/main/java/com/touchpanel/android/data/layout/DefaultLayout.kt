package com.touchpanel.android.data.layout

import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType

object DefaultLayout {
    fun create(): DashboardLayout {
        val widgets = listOf(
            // 1. Clock & Date
            WidgetModel(
                id = "w_clock",
                type = WidgetType.CLOCK,
                title = "Clock & Date",
                gridCol = 0,
                gridRow = 0,
                colSpan = 3,
                rowSpan = 2,
                isRemovable = false
            ),
            // 2. Calendar
            WidgetModel(
                id = "w_calendar",
                type = WidgetType.CALENDAR,
                title = "Calendar",
                gridCol = 0,
                gridRow = 2,
                colSpan = 3,
                rowSpan = 3,
                isRemovable = true
            ),
            // 3. Mango Talker Call Card
            WidgetModel(
                id = "w_mango_talker",
                type = WidgetType.MANGO_TALKER,
                title = "Mango Talker",
                gridCol = 3,
                gridRow = 0,
                colSpan = 4,
                rowSpan = 3,
                isRemovable = true
            ),
            // 4. Media Player Controls
            WidgetModel(
                id = "w_media",
                type = WidgetType.MEDIA_CONTROLS,
                title = "Now Playing",
                gridCol = 3,
                gridRow = 3,
                colSpan = 4,
                rowSpan = 2,
                isRemovable = true
            ),
            // 5. Vertical Volume Slider
            WidgetModel(
                id = "w_volume",
                type = WidgetType.VOLUME,
                title = "Volume",
                gridCol = 7,
                gridRow = 0,
                colSpan = 2,
                rowSpan = 4,
                isRemovable = false
            ),
            // 6. Vertical Brightness Slider
            WidgetModel(
                id = "w_brightness",
                type = WidgetType.BRIGHTNESS,
                title = "Brightness",
                gridCol = 9,
                gridRow = 0,
                colSpan = 2,
                rowSpan = 4,
                isRemovable = true
            ),
            // 7. System Quick Actions
            WidgetModel(
                id = "w_quick_actions",
                type = WidgetType.QUICK_ACTIONS,
                title = "System Shortcuts",
                gridCol = 7,
                gridRow = 4,
                colSpan = 4,
                rowSpan = 2,
                isRemovable = true
            ),
            // 8. Connection Status & Toggles
            WidgetModel(
                id = "w_connection",
                type = WidgetType.CONNECTION_STATUS,
                title = "Connection",
                gridCol = 0,
                gridRow = 5,
                colSpan = 3,
                rowSpan = 1,
                isRemovable = false
            ),
            // 9. Battery Status
            WidgetModel(
                id = "w_battery",
                type = WidgetType.BATTERY_STATUS,
                title = "Battery & Power",
                gridCol = 3,
                gridRow = 5,
                colSpan = 4,
                rowSpan = 1,
                isRemovable = true
            ),
            // 10. Custom Macro Button
            WidgetModel(
                id = "w_custom_btn",
                type = WidgetType.CUSTOM_BUTTON,
                title = "Terminal",
                gridCol = 11,
                gridRow = 0,
                colSpan = 1,
                rowSpan = 2,
                isRemovable = true,
                config = mapOf("actionId" to "launch_terminal", "label" to "Terminal")
            )
        )

        return DashboardLayout(
            version = 1,
            columns = 12,
            rows = 6,
            widgets = widgets
        )
    }
}
