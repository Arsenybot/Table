package com.touchpanel.android.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

object ProtocolDefinition {
    const val PROTOCOL_VERSION = "1.0.0"
    const val DEFAULT_WEBSOCKET_PORT = 8088
    const val BLUETOOTH_SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"

    object PacketType {
        const val HANDSHAKE = "handshake"
        const val COMMAND = "command"
        const val RESPONSE = "response"
        const val EVENT = "event"
    }

    object Action {
        const val SET_VOLUME = "SetVolume"
        const val TOGGLE_MUTE = "ToggleMute"
        const val SET_BRIGHTNESS = "SetBrightness"
        const val MEDIA_PLAY_PAUSE = "MediaPlayPause"
        const val MEDIA_NEXT = "MediaNext"
        const val MEDIA_PREVIOUS = "MediaPrevious"
        const val LAUNCH_APPLICATION = "LaunchApplication"
        const val EXECUTE_SHORTCUT = "ExecuteShortcut"
        const val PING = "Ping"
    }
}

@Serializable
data class ProtocolPacket(
    val version: String = ProtocolDefinition.PROTOCOL_VERSION,
    val id: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: JsonObject
)
