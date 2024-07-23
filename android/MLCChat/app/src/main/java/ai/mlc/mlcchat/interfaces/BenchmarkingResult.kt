package ai.mlc.mlcchat.interfaces

import ai.mlc.mlcchat.BenchmarkingSamples
import ai.mlc.mlcchat.IdleSamples

data class BenchmarkingResult(
    val loadTime: Long?,
    val name: String,
    val samples: BenchmarkingSamples,
    val idleSamples: IdleSamples
)

