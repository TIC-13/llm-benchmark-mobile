package ai.mlc.mlcchat.interfaces

import ai.mlc.mlcchat.BenchmarkingSamples

data class BenchmarkingResult(
    val name: String,
    val samples: BenchmarkingSamples
)

data class Measurement(
    val average: Number,
    val std: Number,
    val peak: Number,
    val median: Number,
)