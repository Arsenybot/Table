package com.touchpanel.android.data.layout

import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType

object DefaultLayout {
    fun create(): DashboardLayout {
        val widgets = listOf(
            // === PAGE 0: HARDWARE & COMMUNICATIONS ===
            // 1. Clock & Date
            WidgetModel(
                id = "w_clock",
                type = WidgetType.CLOCK,
                title = "Clock & Date",
                gridCol = 0,
                gridRow = 0,
                colSpan = 2,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = false
            ),
            // 2. Calendar
            WidgetModel(
                id = "w_calendar",
                type = WidgetType.CALENDAR,
                title = "Calendar",
                gridCol = 0,
                gridRow = 2,
                colSpan = 2,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = true
            ),
            // 3. Mango Talker VoIP Card
            WidgetModel(
                id = "w_mango_talker",
                type = WidgetType.MANGO_TALKER,
                title = "Mango Talker",
                gridCol = 2,
                gridRow = 0,
                colSpan = 3,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = true
            ),
            // 4. Media Player Controls
            WidgetModel(
                id = "w_media",
                type = WidgetType.MEDIA_CONTROLS,
                title = "Now Playing",
                gridCol = 2,
                gridRow = 2,
                colSpan = 3,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = true
            ),
            // 5. Vertical Volume Slider
            WidgetModel(
                id = "w_volume",
                type = WidgetType.VOLUME,
                title = "Volume",
                gridCol = 5,
                gridRow = 0,
                colSpan = 1,
                rowSpan = 4,
                pageIndex = 0,
                isRemovable = false
            ),
            // 6. Vertical Brightness Slider
            WidgetModel(
                id = "w_brightness",
                type = WidgetType.BRIGHTNESS,
                title = "Brightness",
                gridCol = 6,
                gridRow = 0,
                colSpan = 1,
                rowSpan = 4,
                pageIndex = 0,
                isRemovable = true
            ),
            // 7. Battery Status
            WidgetModel(
                id = "w_battery",
                type = WidgetType.BATTERY_STATUS,
                title = "Battery",
                gridCol = 7,
                gridRow = 0,
                colSpan = 1,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = true
            ),
            // 8. Connection Status & Toggles
            WidgetModel(
                id = "w_connection",
                type = WidgetType.CONNECTION_STATUS,
                title = "Connection",
                gridCol = 7,
                gridRow = 2,
                colSpan = 1,
                rowSpan = 2,
                pageIndex = 0,
                isRemovable = false
            ),

            // === PAGE 1: SHORTCUTS & USER MACROS ===
            // 9. System Quick Actions
            WidgetModel(
                id = "w_quick_actions",
                type = WidgetType.QUICK_ACTIONS,
                title = "System Shortcuts",
                gridCol = 0,
                gridRow = 0,
                colSpan = 4,
                rowSpan = 4,
                pageIndex = 1,
                isRemovable = true
            ),
            // 10. Custom Macro: Terminal
            WidgetModel(
                id = "w_custom_btn_1",
                type = WidgetType.CUSTOM_BUTTON,
                title = "Terminal",
                gridCol = 4,
                gridRow = 0,
                colSpan = 2,
                rowSpan = 2,
                pageIndex = 1,
                isRemovable = true,
                config = mapOf("actionId" to "wt", "label" to "Terminal")
            ),
            // 11. Custom Macro: Calculator
            WidgetModel(
                id = "w_custom_btn_2",
                type = WidgetType.CUSTOM_BUTTON,
                title = "Calculator",
                gridCol = 4,
                gridRow = 2,
                colSpan = 2,
                rowSpan = 2,
                pageIndex = 1,
                isRemovable = true,
                config = mapOf("actionId" to "calc", "label" to "Calculator")
            ),
            // 12. Custom Macro: Browser
            WidgetModel(
                id = "w_custom_btn_3",
                type = WidgetType.CUSTOM_BUTTON,
                title = "Browser",
                gridCol = 6,
                gridRow = 0,
                colSpan = 2,
                rowSpan = 2,
                pageIndex = 1,
                isRemovable = true,
                config = mapOf("actionId" to "browser", "label" to "Browser")
            )
        )

        return DashboardLayout(
            version = 2,
            columns = 8,
            rows = 4,
            pageCount = 2,
            widgets = widgets
        )
    }
}
