package com.touchpanel.android.data.connection

import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import com.touchpanel.android.protocol.JsonProtocolSerializer
import com.touchpanel.android.protocol.ProtocolDefinition
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class WebSocketTransport(
    private val serializer: JsonProtocolSerializer = JsonProtocolSerializer()
) : CommandTransport {

    override val transportType: TransportType = TransportType.WIFI_WEBSOCKET

    private val client = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var _isConnected = false
    override val isConnected: Boolean get() = _isConnected

    private val _incomingPackets = MutableSharedFlow<String>(extraBufferCapacity = 64)
    override val incomingPackets: SharedFlow<String> = _incomingPackets.asSharedFlow()

    // Response correlation map
    private val pendingResponses = ConcurrentHashMap<String, CompletableDeferred<CommandResult>>()

    override suspend fun connect(target: String, authToken: String?, pairingPin: String?): Boolean =
        withContext(Dispatchers.IO) {
            val url = if (target.startsWith("ws://") || target.startsWith("wss://")) {
                target
            } else {
                "ws://$target:${ProtocolDefinition.DEFAULT_WEBSOCKET_PORT}"
            }

            val request = Request.Builder().url(url).build()
            val connectDeferred = CompletableDeferred<Boolean>()

            webSocket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    _isConnected = true
                    // Send initial handshake with pairingPin or authToken
                    val handshakePacket = """
                        {
                            "version": "${ProtocolDefinition.PROTOCOL_VERSION}",
                            "id": "init-${System.currentTimeMillis()}",
                            "type": "${ProtocolDefinition.PacketType.HANDSHAKE}",
                            "timestamp": ${System.currentTimeMillis()},
                            "payload": {
                                "clientName": "Android Touch Panel",
                                "authToken": "${authToken ?: ""}",
                                "pairingPin": "${pairingPin ?: ""}"
                            }
                        }
                    """.trimIndent()
                    webSocket.send(handshakePacket)
                    connectDeferred.complete(true)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    _incomingPackets.tryEmit(text)
                    val result = serializer.parseResponse(text)
                    val pending = pendingResponses.remove(result.commandId)
                    pending?.complete(result)
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    _isConnected = false
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _isConnected = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _isConnected = false
                    if (!connectDeferred.isCompleted) {
                        connectDeferred.complete(false)
                    }
                }
            })

            try {
                withTimeout(6000) {
                    connectDeferred.await()
                }
            } catch (e: Exception) {
                _isConnected = false
                false
            }
        }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _isConnected = false
        pendingResponses.values.forEach {
            it.complete(CommandResult.Error("disconnected", "Connection closed", "DISCONNECTED"))
        }
        pendingResponses.clear()
    }

    override suspend fun sendCommand(command: WindowsCommand): CommandResult =
        withContext(Dispatchers.IO) {
            if (!_isConnected || webSocket == null) {
                return@withContext CommandResult.Error(
                    commandId = command.id,
                    message = "WebSocket transport is not connected.",
                    errorCode = "NOT_CONNECTED"
                )
            }

            val jsonText = serializer.serializeCommand(command)
            val deferred = CompletableDeferred<CommandResult>()
            pendingResponses[command.id] = deferred

            val sent = webSocket?.send(jsonText) ?: false
            if (!sent) {
                pendingResponses.remove(command.id)
                return@withContext CommandResult.Error(
                    commandId = command.id,
                    message = "Failed to write packet to WebSocket socket.",
                    errorCode = "WRITE_FAILED"
                )
            }

            try {
                withTimeout(3000) {
                    deferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                pendingResponses.remove(command.id)
                CommandResult.Error(
                    commandId = command.id,
                    message = "Windows host timed out responding to command.",
                    errorCode = "TIMEOUT"
                )
            }
        }
}
