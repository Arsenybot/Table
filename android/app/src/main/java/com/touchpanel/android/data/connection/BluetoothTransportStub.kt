package com.touchpanel.android.data.connection

import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Bluetooth SPP (Serial Port Profile) RFCOMM Transport Stub.
 *
 * Provides architecture and contracts for Bluetooth RFCOMM transport to a paired
 * Windows Bluetooth receiver. Full RFCOMM requires runtime BLUETOOTH_CONNECT permissions
 * and active paired Windows host.
 */
class BluetoothTransportStub : CommandTransport {

    override val transportType: TransportType = TransportType.BLUETOOTH_SPP
    override val isConnected: Boolean = false

    private val _incomingPackets = MutableSharedFlow<String>(extraBufferCapacity = 16)
    override val incomingPackets: SharedFlow<String> = _incomingPackets.asSharedFlow()

    override suspend fun connect(target: String, authToken: String?, pairingPin: String?): Boolean {
        // Bluetooth transport architecture stub
        return false
    }

    override suspend fun disconnect() {
        // No-op for stub
    }

    override suspend fun sendCommand(command: WindowsCommand): CommandResult {
        return CommandResult.Error(
            commandId = command.id,
            message = "Bluetooth transport is not yet connected. Please use Wi-Fi WebSocket or pair Windows PC.",
            errorCode = "BT_NOT_CONNECTED"
        )
    }
}
