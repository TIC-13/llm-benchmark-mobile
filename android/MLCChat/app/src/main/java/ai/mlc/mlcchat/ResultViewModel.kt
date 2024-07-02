package ai.mlc.mlcchat

import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.interfaces.Measurement
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.gpuUsage
import ai.mlc.mlcchat.utils.benchmark.ramUsage
import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ResultViewModel(
        application: Application
) : AndroidViewModel(application) {

    private var results = arrayListOf<BenchmarkingResult>()

    private var cpuSamples = Sampler()
    private var gpuSamples = Sampler()
    private var ramSamples = Sampler()

    fun addBenchmarkingSample(context: Context) {
        cpuSamples.addSample(cpuUsage(context))
        gpuSamples.addSample(gpuUsage())
        ramSamples.addSample(ramUsage())
    }

    fun wrapResultUp(modelName: String) {
        results.add(
            BenchmarkingResult(
                name = modelName,
                cpu = cpuSamples.measurements(),
                gpu = gpuSamples.measurements(),
                ram = ramSamples.measurements(),
                toks = Measurement(0,0,0)
            )
        )
        resetSamplers()
    }

    private fun resetSamplers() {
        cpuSamples = Sampler()
        gpuSamples = Sampler()
        ramSamples = Sampler()
    }

    fun getResults(): ArrayList<BenchmarkingResult> {
        return results
    }

    fun resetResults() {
        results = arrayListOf()
        resetSamplers()
    }
}