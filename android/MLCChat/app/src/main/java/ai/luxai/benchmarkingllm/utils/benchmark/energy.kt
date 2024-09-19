package ai.luxai.benchmarkingllm.utils.benchmark

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlin.math.abs

fun getBatteryVoltageVolts(context: Context): Float {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus = context.registerReceiver(null, intentFilter)
    val result  = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
    if(result == -1) throw Exception("Erro ao calcular a tensão")
    val voltage = abs(result.toFloat())
    return if(voltage > 1000)
        voltage / 1000F
    else
        voltage
}

fun getBatteryCurrentAmperes(context: Context): Float {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val result =
        batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toFloat()
    val current = abs(result)
    return if (current > 1000)
        current / 1_000_000
    else
        current / 1_000
}

fun isBatteryCharging(context: Context): Boolean {
    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

    val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
}

