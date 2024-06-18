package ai.mlc.mlcchat.interfaces

data class BenchmarkingResult(
    val name: String,
    val cpu: Measurement,
    val gpu: Measurement,
    val ram: Measurement,
    val toks: Measurement
)

data class Measurement(
    val average: Int,
    val std: Int,
    val peak: Int
)