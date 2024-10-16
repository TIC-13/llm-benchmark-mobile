package ai.luxai.benchmarkingllm.utils.benchmark.system

import ai.luxai.benchmarkingllm.api.Phone
import android.app.ActivityManager
import android.content.Context
import android.os.Build

fun getPhoneData(context: Context): Phone {
    return Phone(
        brand_name = Build.BRAND,
        manufacturer = Build.MANUFACTURER,
        phone_model = Build.MODEL,
        total_ram = getTotalRAM(context).toInt()
    )
}

fun getTotalRAM(context: Context): Long {
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager.getMemoryInfo(memInfo)
    return memInfo.totalMem
}