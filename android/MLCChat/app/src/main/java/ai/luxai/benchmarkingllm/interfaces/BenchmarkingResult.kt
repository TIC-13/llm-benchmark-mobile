package ai.luxai.benchmarkingllm.interfaces

import ai.luxai.benchmarkingllm.BenchmarkingSamples
import ai.luxai.benchmarkingllm.IdleSamples
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BenchmarkingResult(
    val loadTime: Long?,
    val name: String,
    val samples: BenchmarkingSamples,
    val idleSamples: IdleSamples
)

val gson = Gson()

fun BenchmarkingResult.toJson(): String {
    return gson.toJson(this)
}

fun String.toBenchmarkingResult(): BenchmarkingResult {
    val type = object : TypeToken<BenchmarkingResult>() {}.type
    return gson.fromJson(this, type)
}