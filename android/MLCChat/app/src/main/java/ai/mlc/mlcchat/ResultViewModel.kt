package ai.mlc.mlcchat

import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.interfaces.Measurement
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.getBatteryCurrentAmperes
import ai.mlc.mlcchat.utils.benchmark.getBatteryVoltageVolts
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
    private var voltages = Sampler()
    private var currents = Sampler()
    private var voltagesIdle = Sampler()
    private var currentsIdle = Sampler()
    private var prefillSamples = Sampler()
    private var decodeSamples = Sampler()

    fun addBenchmarkingSample(context: Context) {
        cpuSamples.addSample(cpuUsage(context).toDouble())
        gpuSamples.addSample(gpuUsage().toDouble())
        ramSamples.addSample(ramUsage().toDouble())
    }

    fun addTokenSample(prefill: Double, decode: Double) {
        prefillSamples.addSample(prefill)
        decodeSamples.addSample(decode)
    }

    fun addEnergySample(context: Context) {
        voltages.addSample(getBatteryVoltageVolts(context).toDouble())
        currents.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun addEnergySampleIdle(context: Context) {
        voltagesIdle.addSample(getBatteryVoltageVolts(context).toDouble())
        currentsIdle.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun wrapResultUp(modelName: String) {
        results.add(
            BenchmarkingResult(
                name = modelName,
                cpu = cpuSamples.measurements(),
                gpu = gpuSamples.measurements(),
                ram = ramSamples.measurements(),
                prefill = prefillSamples.measurements(),
                decode = decodeSamples.measurements()
            )
        )
        resetSamplers()
    }

    private fun resetSamplers() {
        cpuSamples = Sampler()
        gpuSamples = Sampler()
        ramSamples = Sampler()
        prefillSamples = Sampler()
        decodeSamples = Sampler()
        voltages = Sampler()
        currents = Sampler()
    }

    fun getResults(): ArrayList<BenchmarkingResult> {
        return results
    }

    fun resetResults() {
        results = arrayListOf()
        resetSamplers()
    }
}