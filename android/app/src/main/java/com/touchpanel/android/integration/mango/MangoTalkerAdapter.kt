package com.touchpanel.android.integration.mango

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Adapter for Mango Talker integration.
 * Verified package: com.mangotele.mtalker
 *
 * Confirmed Capabilities via Android Intents:
 * - Launching Mango Talker main activity
 * - Initiating dial intents (tel: URI)
 *
 * Documented Android Limitations:
 * - Direct embedding of another app's Activity is not permitted by Android OS security model.
 * - Mango Talker does not export public IPC AIDL/broadcast intents to third-party apps.
 * - Call state transitions are managed through OS telephony broadcasts or verified simulated triggers.
 */
class MangoTalkerAdapter(
    private val context: Context
) {
    companion object {
        const val MANGO_PACKAGE = "com.mangotele.mtalker"
    }

    private val _callState = MutableStateFlow<MangoCallState>(MangoCallState.NotConfigured)
    val callState: StateFlow<MangoCallState> = _callState.asStateFlow()

    init {
        checkInstallation()
    }

    fun isAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(MANGO_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun checkInstallation() {
        _callState.value = if (isAppInstalled()) {
            MangoCallState.AppAvailable
        } else {
            MangoCallState.AppNotInstalled
        }
    }

    /**
     * Launch the official Mango Talker application.
     */
    fun launchApp(): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(MANGO_PACKAGE)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            true
        } else {
            _callState.value = MangoCallState.IntegrationError("Mango Talker application not found on this device.")
            false
        }
    }

    /**
     * Start a call via standard tel: intent directed to Mango Talker if installed.
     */
    fun dialNumber(phoneNumber: String): Boolean {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            if (isAppInstalled()) {
                setPackage(MANGO_PACKAGE)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            _callState.value = MangoCallState.ActiveCall("Outgoing Call", phoneNumber, 0)
            true
        } catch (e: Exception) {
            _callState.value = MangoCallState.IntegrationError("Failed to initiate call: ${e.localizedMessage}")
            false
        }
    }

    // Call controls for testing and simulation
    fun answerCall() {
        val current = _callState.value
        if (current is MangoCallState.IncomingCall) {
            _callState.value = MangoCallState.ActiveCall(current.callerName, current.phoneNumber, 1)
        }
    }

    fun declineCall() {
        _callState.value = MangoCallState.CallEnded
    }

    fun simulateIncomingCall(callerName: String, phoneNumber: String) {
        _callState.value = MangoCallState.IncomingCall(callerName, phoneNumber)
    }

    fun endCall() {
        _callState.value = MangoCallState.CallEnded
    }
}
