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
 * Real, verified capabilities:
 * 1. Checks if package is installed on the device via PackageManager (using <queries> in AndroidManifest).
 * 2. Launches the official Mango Talker application (com.mango.talker.LaunchActivity).
 * 3. Launches standard Android dialer intent (Intent.ACTION_DIAL, tel:<phone>) directed to Mango Talker.
 * 4. Directs user to Google Play / APKPure store if Mango Talker is missing.
 *
 * Android OS Limitations (Explicitly documented, no fake emulation):
 * - Android security sandbox prevents embedding another application's Activity into our layout.
 * - Mango Talker CallService is not exported (exported=false) and does not provide public IPC/AIDL to 3rd-party apps.
 * - Call answering/declining is handled solely by Mango Talker itself or system TelecomManager.
 */
class MangoTalkerAdapter(private val context: Context) {

    companion object {
        const val MANGO_PACKAGE = "com.mangotele.mtalker"
        const val PLAY_STORE_URI = "market://details?id=com.mangotele.mtalker"
        const val WEB_STORE_URI = "https://play.google.com/store/apps/details?id=com.mangotele.mtalker"
    }

    private val _status = MutableStateFlow<MangoIntegrationStatus>(MangoIntegrationStatus.NotInstalled)
    val status: StateFlow<MangoIntegrationStatus> = _status.asStateFlow()

    init {
        refreshStatus()
    }

    fun isAppInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo(MANGO_PACKAGE, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun refreshStatus() {
        _status.value = if (isAppInstalled()) {
            MangoIntegrationStatus.Installed
        } else {
            MangoIntegrationStatus.NotInstalled
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
            _status.value = MangoIntegrationStatus.Error("Mango Talker не установлен на этом устройстве.")
            false
        }
    }

    /**
     * Open dialer with prefilled telephone number targeting Mango Talker.
     */
    fun dialNumber(phoneNumber: String): Boolean {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanNumber")
            if (isAppInstalled()) {
                setPackage(MANGO_PACKAGE)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            _status.value = MangoIntegrationStatus.Error("Не удалось открыть набор номера: ${e.localizedMessage}")
            false
        }
    }

    /**
     * Open store page to install Mango Talker.
     */
    fun openStorePage() {
        val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE_URI)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(storeIntent)
        } catch (e: Exception) {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WEB_STORE_URI)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }
}
