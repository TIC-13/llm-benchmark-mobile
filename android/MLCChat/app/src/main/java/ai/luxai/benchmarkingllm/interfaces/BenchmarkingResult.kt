package ai.luxai.benchmarkingllm.interfaces

import ai.luxai.benchmarkingllm.BenchmarkingSamples
import ai.luxai.benchmarkingllm.IdleSamples

data class BenchmarkingResult(
    val loadTime: Long?,
    val name: String,
    val samples: BenchmarkingSamples,
    val idleSamples: IdleSamples
)

