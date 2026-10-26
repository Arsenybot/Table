package com.touchpanel.android.domain.commands

import kotlinx.serialization.Serializable

@Serializable
sealed class WindowsCommand {
    abstract val id: String

    @Serializable
    data class SetVolume(
        override val id: String,
        val level: Int,
        val isRelative: Boolean = false
    ) : WindowsCommand()

    @Serializable
    data class ToggleMute(
        override val id: String
    ) : WindowsCommand()

    @Serializable
    data class SetBrightness(
        override val id: String,
        val level: Int
    ) : WindowsCommand()

    @Serializable
    data class MediaPlayPause(
        override val id: String
    ) : WindowsCommand()

    @Serializable
    data class MediaNext(
        override val id: String
    ) : WindowsCommand()

    @Serializable
    data class MediaPrevious(
        override val id: String
    ) : WindowsCommand()

    @Serializable
    data class LaunchApplication(
        override val id: String,
        val appId: String,
        val customPath: String? = null
    ) : WindowsCommand()

    @Serializable
    data class ExecuteShortcut(
        override val id: String,
        val shortcutId: String,
        val keyCombo: String
    ) : WindowsCommand()

    @Serializable
    data class Ping(
        override val id: String
    ) : WindowsCommand()
}
