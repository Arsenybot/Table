package com.touchpanel.android.domain.commands

import com.touchpanel.android.data.connection.ConnectionManager
import com.touchpanel.android.data.connection.ConnectionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class CommandDispatcher(
    private val connectionManager: ConnectionManager
) {
    private val _lastResult = MutableSharedFlow<CommandResult>(extraBufferCapacity = 64)
    val lastResult: SharedFlow<CommandResult> = _lastResult.asSharedFlow()

    suspend fun dispatch(command: WindowsCommand): CommandResult {
        val currentState = connectionManager.state.value

        // Never fake success when disconnected from the Windows host
        if (currentState !is ConnectionState.Connected) {
            val error = CommandResult.Error(
                commandId = command.id,
                message = "Windows client is not connected. Connect via Wi-Fi or Bluetooth.",
                errorCode = "NO_CONNECTION"
            )
            _lastResult.tryEmit(error)
            return error
        }

        return try {
            val transport = connectionManager.getActiveTransport()
            if (transport == null) {
                val error = CommandResult.Error(
                    commandId = command.id,
                    message = "No active transport available.",
                    errorCode = "NO_TRANSPORT"
                )
                _lastResult.tryEmit(error)
                error
            } else {
                val result = transport.sendCommand(command)
                _lastResult.tryEmit(result)
                result
            }
        } catch (e: Exception) {
            val error = CommandResult.Error(
                commandId = command.id,
                message = e.localizedMessage ?: "Failed to transmit command",
                errorCode = "TRANSMIT_EXCEPTION"
            )
            _lastResult.tryEmit(error)
            error
        }
    }
}
