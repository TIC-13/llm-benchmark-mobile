package ai.mlc.mlcchat.interfaces

data class BenchmarkingResult(
    val name: String,
    val cpu: Measurement,
    val gpu: Measurement,
    val ram: Measurement,
    val prefill: Measurement,
    val decode: Measurement,
)

data class Measurement(
    val average: Number,
    val std: Number,
    val peak: Number,
    val median: Number,
)