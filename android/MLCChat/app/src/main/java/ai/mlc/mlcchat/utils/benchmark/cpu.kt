package ai.mlc.mlcchat.utils.benchmark

import ai.mlc.mlcchat.utils.benchmark.system.getProcessName
import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun cpuUsage(context: Context): Int {

    val processName = getProcessName(context)

    val totalCapacityStringBuilder = StringBuilder()
    val processStringBuilder = StringBuilder()

    try {
        val process = Runtime.getRuntime().exec("top -n 1 -b")
        val reader = BufferedReader(InputStreamReader(process.inputStream))

        var line: String?
        while (true) {
            line = reader.readLine()
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
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    val processLine = processStringBuilder.toString()
    val cpuUsageParsed = processLine.replace(Regex("\\s+"), " ").split(" ")[9].toFloat().toInt()

    val totalCapacityLine = totalCapacityStringBuilder.toString()
    val totalCapacity = Regex("^\\d+").find(totalCapacityLine)?.value?.toInt() ?: return 0

    return cpuUsageParsed * 100 / totalCapacity
}