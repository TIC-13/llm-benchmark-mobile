package ai.luxai.benchmarkingllm.utils.benchmark

import android.os.Debug

fun ramUsage(): Int {
    val memoryInfo = Debug.MemoryInfo()
    Debug.getMemoryInfo(memoryInfo)
    return memoryInfo.totalPss/1024
}