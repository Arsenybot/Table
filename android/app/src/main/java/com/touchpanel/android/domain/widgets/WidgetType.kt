package com.touchpanel.android.domain.widgets

import kotlinx.serialization.Serializable

@Serializable
enum class WidgetType {
    CLOCK,
    CALENDAR,
    VOLUME,
    BRIGHTNESS,
    MEDIA_CONTROLS,
    QUICK_ACTIONS,
    CONNECTION_STATUS,
    MANGO_TALKER,
    BATTERY_STATUS,
    CUSTOM_BUTTON
}
