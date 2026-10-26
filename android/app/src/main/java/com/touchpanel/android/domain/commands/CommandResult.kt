package com.touchpanel.android.domain.commands

import kotlinx.serialization.Serializable

@Serializable
sealed class CommandResult {
    abstract val commandId: String

    @Serializable
    data class Success(
        override val commandId: String,
        val action: String,
        val details: Map<String, String> = emptyMap()
    ) : CommandResult()

    @Serializable
    data class Error(
        override val commandId: String,
        val message: String,
        val errorCode: String = "COMMAND_FAILED"
    ) : CommandResult()
}
