package com.touchpanel.android.core.hardware

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AndroidAudioState(
    val currentVolume: Int = 50,
    val maxVolume: Int = 15,
    val volumePercent: Int = 50,
    val isMuted: Boolean = false,
    val isRingerSilent: Boolean = false
)

/**
 * Real Android Audio Manager.
 * Reads and sets hardware media audio volume (STREAM_MUSIC) on the Android smartphone.
 * Subscribes to system volume change events via ContentObserver so changes from
 * physical volume keys are instantly reflected in the UI.
 */
class AndroidVolumeManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _audioState = MutableStateFlow(readCurrentAudioState())
    val audioState: StateFlow<AndroidAudioState> = _audioState.asStateFlow()

    private var contentObserver: ContentObserver? = null

    private fun readCurrentAudioState(): AndroidAudioState {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val percent = ((current.toFloat() / max.toFloat()) * 100f).toInt().coerceIn(0, 100)
        val isMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC) || current == 0
        val isRingerSilent = audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL

        return AndroidAudioState(
            currentVolume = current,
            maxVolume = max,
            volumePercent = percent,
            isMuted = isMuted,
            isRingerSilent = isRingerSilent
        )
    }

    fun setVolumePercent(percent: Int) {
        val clampedPercent = percent.coerceIn(0, 100)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val targetVolume = ((clampedPercent.toFloat() / 100f) * max).toInt().coerceIn(0, max)

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
        _audioState.value = readCurrentAudioState()
    }

    fun toggleMute() {
        val currentState = _audioState.value
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)

        if (currentState.isMuted) {
            // Unmute to half volume or 1 step
            val restoreVolume = (max / 2).coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, restoreVolume, 0)
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
        }
        _audioState.value = readCurrentAudioState()
    }

    fun startListening() {
        if (contentObserver == null) {
            contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    _audioState.value = readCurrentAudioState()
                }
            }
            context.contentResolver.registerContentObserver(
                Settings.System.CONTENT_URI,
                true,
                contentObserver!!
            )
        }
        _audioState.value = readCurrentAudioState()
    }

    fun stopListening() {
        contentObserver?.let {
            context.contentResolver.unregisterContentObserver(it)
            contentObserver = null
        }
    }
}
