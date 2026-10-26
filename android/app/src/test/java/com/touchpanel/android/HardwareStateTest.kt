package com.touchpanel.android

import com.touchpanel.android.core.hardware.BatteryInfo
import com.touchpanel.android.core.hardware.PowerSource
import org.junit.Assert.*
import org.junit.Test

class HardwareStateTest {

    @Test
    fun testBatteryPercentageClamping() {
        // Verify formula: ((level / scale) * 100).coerceIn(0, 100)
        fun calculate(level: Int, scale: Int): Int {
            if (level >= 0 && scale > 0) {
                return ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
            }
            return 50
        }

        assertEquals(85, calculate(85, 100))
        assertEquals(0, calculate(0, 100))
        assertEquals(100, calculate(100, 100))
        assertEquals(100, calculate(120, 100)) // Clamped
        assertEquals(50, calculate(-1, 100)) // Fallback
    }

    @Test
    fun testPowerSourceClassification() {
        val infoAc = BatteryInfo(percentage = 100, isCharging = true, powerSource = PowerSource.AC)
        assertTrue(infoAc.isCharging)
        assertEquals(PowerSource.AC, infoAc.powerSource)

        val infoUsb = BatteryInfo(percentage = 70, isCharging = true, powerSource = PowerSource.USB)
        assertEquals(PowerSource.USB, infoUsb.powerSource)
    }

    @Test
    fun testVolumeStreamConversion() {
        // Convert 0..100% to Android stream index 0..max (e.g. max = 15)
        fun percentToIndex(percent: Int, max: Int): Int {
            val clamped = percent.coerceIn(0, 100)
            return ((clamped.toFloat() / 100f) * max).toInt().coerceIn(0, max)
        }

        assertEquals(0, percentToIndex(0, 15))
        assertEquals(15, percentToIndex(100, 15))
        assertEquals(7, percentToIndex(50, 15))
        assertEquals(15, percentToIndex(110, 15)) // Clamped
    }

    @Test
    fun testBrightnessPercentClamping() {
        fun clampBrightness(input: Int): Int = input.coerceIn(5, 100)

        assertEquals(5, clampBrightness(1))
        assertEquals(50, clampBrightness(50))
        assertEquals(100, clampBrightness(100))
        assertEquals(100, clampBrightness(120))
    }
}
