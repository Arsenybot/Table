package com.touchpanel.android

import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import com.touchpanel.android.protocol.JsonProtocolSerializer
import com.touchpanel.android.protocol.ProtocolDefinition
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsonProtocolTest {

    private lateinit var serializer: JsonProtocolSerializer

    @Before
    fun setUp() {
        serializer = JsonProtocolSerializer()
    }

    @Test
    fun testSerializeVolumeCommand() {
        val cmd = WindowsCommand.SetVolume(id = "test-vol-1", level = 65)
        val json = serializer.serializeCommand(cmd)

        assertTrue(json.contains("\"version\":\"1.0.0\""))
        assertTrue(json.contains("\"action\":\"SetVolume\""))
        assertTrue(json.contains("\"level\":65"))
        assertTrue(json.contains("\"id\":\"test-vol-1\""))
    }

    @Test
    fun testSerializeBrightnessCommand() {
        val cmd = WindowsCommand.SetBrightness(id = "test-bri-2", level = 80)
        val json = serializer.serializeCommand(cmd)

        assertTrue(json.contains("\"action\":\"SetBrightness\""))
        assertTrue(json.contains("\"level\":80"))
    }

    @Test
    fun testSerializeShortcutCommand() {
        val cmd = WindowsCommand.ExecuteShortcut(id = "test-shc-3", shortcutId = "lock_screen", keyCombo = "Win+L")
        val json = serializer.serializeCommand(cmd)

        assertTrue(json.contains("\"action\":\"ExecuteShortcut\""))
        assertTrue(json.contains("\"shortcutId\":\"lock_screen\""))
        assertTrue(json.contains("\"keyCombo\":\"Win+L\""))
    }

    @Test
    fun testParseSuccessResponse() {
        val raw = """
            {
                "version": "1.0.0",
                "id": "test-vol-1",
                "type": "response",
                "timestamp": 1729944000000,
                "payload": {
                    "status": "success",
                    "action": "SetVolume",
                    "result": {
                        "currentVolume": "65"
                    }
                }
            }
        """.trimIndent()

        val result = serializer.parseResponse(raw)
        assertTrue(result is CommandResult.Success)
        val success = result as CommandResult.Success
        assertEquals("test-vol-1", success.commandId)
        assertEquals("SetVolume", success.action)
        assertEquals("65", success.details["currentVolume"])
    }

    @Test
    fun testParseErrorResponse() {
        val raw = """
            {
                "version": "1.0.0",
                "id": "test-app-4",
                "type": "response",
                "timestamp": 1729944000000,
                "payload": {
                    "status": "error",
                    "errorCode": "APP_NOT_FOUND",
                    "message": "Application calc.exe is not installed"
                }
            }
        """.trimIndent()

        val result = serializer.parseResponse(raw)
        assertTrue(result is CommandResult.Error)
        val error = result as CommandResult.Error
        assertEquals("test-app-4", error.commandId)
        assertEquals("APP_NOT_FOUND", error.errorCode)
        assertEquals("Application calc.exe is not installed", error.message)
    }
}
