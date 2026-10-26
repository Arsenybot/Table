package com.touchpanel.android.data.connection

import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import kotlinx.coroutines.flow.SharedFlow

interface CommandTransport {
    val transportType: TransportType
    val isConnected: Boolean
    val incomingPackets: SharedFlow<String>

    suspend fun connect(target: String, authToken: String?, pairingPin: String?): Boolean
    suspend fun disconnect()
    suspend fun sendCommand(command: WindowsCommand): CommandResult
}
