package com.touchpanel.android.protocol

import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import kotlinx.serialization.json.*

class JsonProtocolSerializer {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun serializeCommand(command: WindowsCommand): String {
        val (action, params) = when (command) {
            is WindowsCommand.SetVolume -> {
                ProtocolDefinition.Action.SET_VOLUME to buildJsonObject {
                    put("level", command.level)
                    put("isRelative", command.isRelative)
                }
            }
            is WindowsCommand.ToggleMute -> {
                ProtocolDefinition.Action.TOGGLE_MUTE to buildJsonObject {}
            }
            is WindowsCommand.SetBrightness -> {
                ProtocolDefinition.Action.SET_BRIGHTNESS to buildJsonObject {
                    put("level", command.level)
                }
            }
            is WindowsCommand.MediaPlayPause -> {
                ProtocolDefinition.Action.MEDIA_PLAY_PAUSE to buildJsonObject {}
            }
            is WindowsCommand.MediaNext -> {
                ProtocolDefinition.Action.MEDIA_NEXT to buildJsonObject {}
            }
            is WindowsCommand.MediaPrevious -> {
                ProtocolDefinition.Action.MEDIA_PREVIOUS to buildJsonObject {}
            }
            is WindowsCommand.LaunchApplication -> {
                ProtocolDefinition.Action.LAUNCH_APPLICATION to buildJsonObject {
                    put("appId", command.appId)
                    command.customPath?.let { put("customPath", it) }
                }
            }
            is WindowsCommand.ExecuteShortcut -> {
                ProtocolDefinition.Action.EXECUTE_SHORTCUT to buildJsonObject {
                    put("shortcutId", command.shortcutId)
                    put("keyCombo", command.keyCombo)
                }
            }
            is WindowsCommand.Ping -> {
                ProtocolDefinition.Action.PING to buildJsonObject {}
            }
        }

        val payload = buildJsonObject {
            put("action", action)
            put("params", params)
        }

        val packet = ProtocolPacket(
            version = ProtocolDefinition.PROTOCOL_VERSION,
            id = command.id,
            type = ProtocolDefinition.PacketType.COMMAND,
            timestamp = System.currentTimeMillis(),
            payload = payload
        )

        return json.encodeToString(ProtocolPacket.serializer(), packet)
    }

    fun parseResponse(rawJson: String): CommandResult {
        return try {
            val root = json.parseToJsonElement(rawJson).jsonObject
            val id = root["id"]?.jsonPrimitive?.content ?: "unknown"
            val payload = root["payload"]?.jsonObject ?: JsonObject(emptyMap())

            val status = payload["status"]?.jsonPrimitive?.content ?: "unknown"
            if (status == "success" || status == "authenticated") {
                val action = payload["action"]?.jsonPrimitive?.content ?: "command"
                val details = mutableMapOf<String, String>()
                payload["result"]?.jsonObject?.forEach { (k, v) ->
                    details[k] = v.jsonPrimitive.contentOrNull ?: v.toString()
                }
                CommandResult.Success(commandId = id, action = action, details = details)
            } else {
                val message = payload["message"]?.jsonPrimitive?.content ?: "Command failed on Windows"
                val errorCode = payload["errorCode"]?.jsonPrimitive?.content ?: "ERROR"
                CommandResult.Error(commandId = id, message = message, errorCode = errorCode)
            }
        } catch (e: Exception) {
            CommandResult.Error(
                commandId = "deserialization_error",
                message = "Failed to parse host response: ${e.localizedMessage}",
                errorCode = "PARSE_ERROR"
            )
        }
    }
}
