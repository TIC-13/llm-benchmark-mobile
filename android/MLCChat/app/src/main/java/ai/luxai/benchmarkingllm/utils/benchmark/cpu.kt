package ai.luxai.benchmarkingllm.utils.benchmark

import ai.luxai.benchmarkingllm.utils.benchmark.system.getProcessName
import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

fun cpuUsage(context: Context): Int? {
    try {
        val processName = getProcessName(context)

        val totalCapacityStringBuilder = StringBuilder()
        val processStringBuilder = StringBuilder()

        val allTopStringBuilder = StringBuilder()

        val process = Runtime.getRuntime().exec("top -n 1 -b")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var line: String?
        while (true) {
            line = reader.readLine()
            allTopStringBuilder.append(line + "\n")
            if (line == null) {
                break
            }
            if (line.contains("%cpu")) {
                totalCapacityStringBuilder.append(line)
            }
            if (processName?.let { line.contains(it) } == true) {
                processStringBuilder.append(line)
                break
            }
        }
        process.waitFor()
        process.destroy()

        Log.e("CPU", allTopStringBuilder.toString())

        val processLine = processStringBuilder.toString()

        val cpuUsageParsed = processLine
            .replace(Regex("\\s+"), " ")
            .split(" ")[9].toFloat().toInt()

        val totalCapacityLine = totalCapacityStringBuilder.toString()
        val totalCapacity = Regex("^\\d+").find(totalCapacityLine)?.value?.toInt() ?: return 0

        return cpuUsageParsed * 100 / totalCapacity
    }catch(e: Exception) {
        return null
    }
}