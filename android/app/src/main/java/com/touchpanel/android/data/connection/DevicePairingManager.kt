package com.touchpanel.android.data.connection

import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(
    val id: String,
    val name: String,
    val transportType: TransportType,
    val address: String,
    val authToken: String,
    val lastConnected: Long = System.currentTimeMillis()
)

class DevicePairingManager {
    private val pairedDevices = mutableMapOf<String, PairedDevice>()

    fun getDevices(): List<PairedDevice> = pairedDevices.values.toList()

    fun addDevice(device: PairedDevice) {
        pairedDevices[device.id] = device
    }

    fun removeDevice(deviceId: String) {
        pairedDevices.remove(deviceId)
    }

    fun findDeviceByAddress(address: String): PairedDevice? {
        return pairedDevices.values.find { it.address.equals(address, ignoreCase = true) }
    }
}
