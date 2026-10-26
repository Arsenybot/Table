package com.touchpanel.android.core.hardware

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.WindowManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BrightnessInfo(
    val percent: Int = 75,
    val isWindowLevelOnly: Boolean = true,
    val hasSystemWritePermission: Boolean = false
)

/**
 * Real Android Brightness Manager.
 * Adjusts Android display brightness reliably:
 * 1. Immediate Window-level brightness (0.01f to 1.0f) via WindowManager.LayoutParams.
 *    Does not require WRITE_SETTINGS permission and immediately affects the active Touch Panel display.
 * 2. Checks Settings.System.canWrite(context). If granted, synchronizes with system-wide SCREEN_BRIGHTNESS.
 */
class AndroidBrightnessManager(
    private val context: Context,
    private val activityProvider: () -> Activity?
) {

    private val _brightnessState = MutableStateFlow(BrightnessInfo())
    val brightnessState: StateFlow<BrightnessInfo> = _brightnessState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        val hasPermission = Settings.System.canWrite(context)
        val currentSysBrightness = try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            180
        }
        val sysPercent = ((currentSysBrightness / 255f) * 100f).toInt().coerceIn(1, 100)

        val windowBrightness = activityProvider()?.window?.attributes?.screenBrightness ?: -1f
        val effectivePercent = if (windowBrightness in 0f..1f) {
            (windowBrightness * 100f).toInt().coerceIn(1, 100)
        } else {
            sysPercent
        }

        _brightnessState.value = BrightnessInfo(
            percent = effectivePercent,
            isWindowLevelOnly = !hasPermission,
            hasSystemWritePermission = hasPermission
        )
    }

    fun setBrightnessPercent(percent: Int) {
        val clamped = percent.coerceIn(5, 100)
        val floatVal = clamped / 100f

        // Apply immediately to current Window (always succeeds within the application)
        activityProvider()?.let { activity ->
            activity.runOnUiThread {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = floatVal
                activity.window.attributes = layoutParams
            }
        }

        // If system permission is granted, write to system settings as well
        val hasPermission = Settings.System.canWrite(context)
        if (hasPermission) {
            try {
                val systemVal = ((clamped / 100f) * 255).toInt().coerceIn(10, 255)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    systemVal
                )
            } catch (e: Exception) {
                // fallback to window brightness
            }
        }

        _brightnessState.value = BrightnessInfo(
            percent = clamped,
            isWindowLevelOnly = !hasPermission,
            hasSystemWritePermission = hasPermission
        )
    }

    /**
     * Request system-level WRITE_SETTINGS permission if user wishes to change global phone brightness
     */
    fun createRequestPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
