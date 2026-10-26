package com.touchpanel.android.integration.mango

/**
 * Honest status of the Mango Talker integration.
 * In accordance with Android OS security model, third-party apps can only
 * communicate with Mango Talker via verified public Intents (ACTION_DIAL, ACTION_MAIN, tel: URI).
 */
sealed class MangoIntegrationStatus {
    data object Installed : MangoIntegrationStatus()
    data object NotInstalled : MangoIntegrationStatus()
    data class Error(val message: String) : MangoIntegrationStatus()
}
