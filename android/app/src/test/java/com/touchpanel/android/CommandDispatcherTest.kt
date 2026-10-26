package com.touchpanel.android

import com.touchpanel.android.data.connection.ConnectionManager
import com.touchpanel.android.data.connection.ConnectionState
import com.touchpanel.android.domain.commands.CommandDispatcher
import com.touchpanel.android.domain.commands.CommandResult
import com.touchpanel.android.domain.commands.WindowsCommand
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CommandDispatcherTest {

    @Test
    fun testDispatchWhenDisconnectedReturnsError() = runBlocking {
        val connectionManager = ConnectionManager()
        val dispatcher = CommandDispatcher(connectionManager)

        val cmd = WindowsCommand.SetVolume("cmd-vol-99", 50)
        val result = dispatcher.dispatch(cmd)

        assertTrue(result is CommandResult.Error)
        val error = result as CommandResult.Error
        assertEquals("cmd-vol-99", error.commandId)
        assertEquals("NO_CONNECTION", error.errorCode)
        assertTrue(error.message.contains("Windows client is not connected"))
    }
}
