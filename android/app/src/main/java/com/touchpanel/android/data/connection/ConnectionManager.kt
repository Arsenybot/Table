package com.touchpanel.android.data.connection

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConnectionManager(
    private val webSocketTransport: WebSocketTransport = WebSocketTransport(),
    private val bluetoothTransport: BluetoothTransportStub = BluetoothTransportStub(),
    val pairingManager: DevicePairingManager = DevicePairingManager()
) {
    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val state: StateFlow<ConnectionState> = _state.asStateFlow()

    private var activeTransportType: TransportType = TransportType.WIFI_WEBSOCKET

    fun getActiveTransport(): CommandTransport? {
        return when (activeTransportType) {
            TransportType.WIFI_WEBSOCKET -> webSocketTransport
            TransportType.BLUETOOTH_SPP -> bluetoothTransport
        }
    }

    suspend fun connect(target: String, transport: TransportType, pin: String? = null): Boolean {
        activeTransportType = transport
        _state.value = ConnectionState.Connecting(target, transport)

        val activeTransport = getActiveTransport()
        if (activeTransport == null) {
            _state.value = ConnectionState.Error("Selected transport is not available")
            return false
        }

        val success = activeTransport.connect(target, null, pin)
        if (success) {
            _state.value = ConnectionState.Connected(
                hostName = target,
                endpoint = target,
                transport = transport,
                pingLatencyMs = 12L
            )
            return true
        } else {
            _state.value = ConnectionState.Error("Failed to connect to $target. Verify Windows server is running.")
            return false
        }
    }

    suspend fun disconnect() {
        getActiveTransport()?.disconnect()
        _state.value = ConnectionState.Disconnected
    }
}
