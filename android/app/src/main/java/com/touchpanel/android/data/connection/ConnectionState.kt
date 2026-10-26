package com.touchpanel.android.data.connection

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data class Connecting(val endpoint: String, val transport: TransportType) : ConnectionState()
    data class Connected(
        val hostName: String,
        val endpoint: String,
        val transport: TransportType,
        val pingLatencyMs: Long = 0L
    ) : ConnectionState()
    data class Error(val message: String, val canRetry: Boolean = true) : ConnectionState()
}

enum class TransportType {
    WIFI_WEBSOCKET,
    BLUETOOTH_SPP
}
