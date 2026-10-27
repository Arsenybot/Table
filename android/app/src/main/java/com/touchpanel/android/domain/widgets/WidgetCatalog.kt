package com.touchpanel.android.domain.widgets

data class WidgetDescriptor(
    val type: WidgetType,
    val displayName: String,
    val description: String,
    val defaultColSpan: Int,
    val defaultRowSpan: Int,
    val minColSpan: Int = 1,
    val maxColSpan: Int = 6,
    val minRowSpan: Int = 1,
    val maxRowSpan: Int = 4,
    val defaultTitle: String
)

object WidgetCatalog {
    val descriptors: Map<WidgetType, WidgetDescriptor> = listOf(
        WidgetDescriptor(
            type = WidgetType.CLOCK,
            displayName = "Digital Clock",
            description = "OLED digital clock with live seconds, day, and current timezone",
            defaultColSpan = 2,
            defaultRowSpan = 2,
            minColSpan = 2,
            maxColSpan = 4,
            minRowSpan = 1,
            maxRowSpan = 2,
            defaultTitle = "Clock"
        ),
        WidgetDescriptor(
            type = WidgetType.CALENDAR,
            displayName = "Calendar",
            description = "Interactive monthly calendar with today's date highlighted",
            defaultColSpan = 2,
            defaultRowSpan = 2,
            minColSpan = 2,
            maxColSpan = 4,
            minRowSpan = 2,
            maxRowSpan = 4,
            defaultTitle = "Calendar"
        ),
        WidgetDescriptor(
            type = WidgetType.VOLUME,
            displayName = "Windows Volume",
            description = "Vertical slider with mute toggle and direct Windows level synchronization",
            defaultColSpan = 1,
            defaultRowSpan = 4,
            minColSpan = 1,
            maxColSpan = 2,
            minRowSpan = 2,
            maxRowSpan = 4,
            defaultTitle = "Volume"
        ),
        WidgetDescriptor(
            type = WidgetType.BRIGHTNESS,
            displayName = "Windows Brightness",
            description = "Vertical slider for adjusting Windows display brightness",
            defaultColSpan = 1,
            defaultRowSpan = 4,
            minColSpan = 1,
            maxColSpan = 2,
            minRowSpan = 2,
            maxRowSpan = 4,
            defaultTitle = "Brightness"
        ),
        WidgetDescriptor(
            type = WidgetType.MEDIA_CONTROLS,
            displayName = "Media Player",
            description = "Track information, playback progress, and media keys (Play/Pause, Next, Prev)",
            defaultColSpan = 3,
            defaultRowSpan = 2,
            minColSpan = 2,
            maxColSpan = 5,
            minRowSpan = 2,
            maxRowSpan = 3,
            defaultTitle = "Now Playing"
        ),
        WidgetDescriptor(
            type = WidgetType.QUICK_ACTIONS,
            displayName = "Quick System Actions",
            description = "1-tap Windows shortcuts: Lock, Task Manager, Screen Snip, Calculator",
            defaultColSpan = 4,
            defaultRowSpan = 4,
            minColSpan = 2,
            maxColSpan = 6,
            minRowSpan = 2,
            maxRowSpan = 4,
            defaultTitle = "System Actions"
        ),
        WidgetDescriptor(
            type = WidgetType.CONNECTION_STATUS,
            displayName = "Connection & Toggles",
            description = "Wi-Fi, Bluetooth status, Windows host IP address and ping latency",
            defaultColSpan = 1,
            defaultRowSpan = 2,
            minColSpan = 1,
            maxColSpan = 2,
            minRowSpan = 1,
            maxRowSpan = 2,
            defaultTitle = "Connection"
        ),
        WidgetDescriptor(
            type = WidgetType.MANGO_TALKER,
            displayName = "Mango Talker",
            description = "Incoming and active call card with call actions and app launch intent",
            defaultColSpan = 3,
            defaultRowSpan = 2,
            minColSpan = 2,
            maxColSpan = 5,
            minRowSpan = 2,
            maxRowSpan = 3,
            defaultTitle = "Mango Talker"
        ),
        WidgetDescriptor(
            type = WidgetType.BATTERY_STATUS,
            displayName = "Battery & Power",
            description = "Smartphone battery level, charging status, and thermal metrics",
            defaultColSpan = 1,
            defaultRowSpan = 2,
            minColSpan = 1,
            maxColSpan = 2,
            minRowSpan = 1,
            maxRowSpan = 2,
            defaultTitle = "Battery"
        ),
        WidgetDescriptor(
            type = WidgetType.CUSTOM_BUTTON,
            displayName = "Custom Macro Button",
            description = "Customizable button for launching user apps or custom key combinations",
            defaultColSpan = 2,
            defaultRowSpan = 2,
            minColSpan = 1,
            maxColSpan = 3,
            minRowSpan = 1,
            maxRowSpan = 2,
            defaultTitle = "Custom Action"
        )
    ).associateBy { it.type }

    fun getDescriptor(type: WidgetType): WidgetDescriptor {
        return descriptors[type] ?: throw IllegalArgumentException("Unknown widget type: $type")
    }
}
