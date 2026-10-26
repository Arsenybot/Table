package com.touchpanel.android.core.hardware

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryInfo(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val powerSource: PowerSource = PowerSource.BATTERY,
    val health: String = "Good",
    val temperatureCelsius: Float = 25f
)

enum class PowerSource {
    BATTERY,
    AC,
    USB,
    WIRELESS
}

/**
 * Real Android Battery Monitor.
 * Uses BroadcastReceiver with Intent.ACTION_BATTERY_CHANGED to receive real-time
 * hardware battery telemetry without wasteful background polling.
 */
class BatteryMonitor(private val context: Context) {

    private val _batteryState = MutableStateFlow(BatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    private var isRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != Intent.ACTION_BATTERY_CHANGED) return

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val percentage = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
            } else {
                50
            }

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val powerSource = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> PowerSource.AC
                BatteryManager.BATTERY_PLUGGED_USB -> PowerSource.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> PowerSource.WIRELESS
                else -> PowerSource.BATTERY
            }

            val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            val health = when (healthInt) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                else -> "Normal"
            }

            val tempTenths = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250)
            val temperature = tempTenths / 10f

            _batteryState.value = BatteryInfo(
                percentage = percentage,
                isCharging = isCharging,
                powerSource = powerSource,
                health = health,
                temperatureCelsius = temperature
            )
        }
    }

    fun startListening() {
        if (!isRegistered) {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            // ACTION_BATTERY_CHANGED is a sticky broadcast, returns the initial intent immediately
            val initialIntent = context.registerReceiver(receiver, filter)
            if (initialIntent != null) {
                receiver.onReceive(context, initialIntent)
            }
            isRegistered = true
        }
    }

    fun stopListening() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(receiver)
            } catch (ignored: IllegalArgumentException) {
                // Receiver was not registered
            }
            isRegistered = false
        }
    }
}
