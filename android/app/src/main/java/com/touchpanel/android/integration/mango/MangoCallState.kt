package com.touchpanel.android.integration.mango

sealed class MangoCallState {
    data object NotConfigured : MangoCallState()
    data object AppNotInstalled : MangoCallState()
    data object AppAvailable : MangoCallState()
    data class IncomingCall(
        val callerName: String,
        val phoneNumber: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : MangoCallState()
    data class ActiveCall(
        val callerName: String,
        val phoneNumber: String,
        val durationSeconds: Long
    ) : MangoCallState()
    data object CallEnded : MangoCallState()
    data class IntegrationError(val message: String) : MangoCallState()
}
