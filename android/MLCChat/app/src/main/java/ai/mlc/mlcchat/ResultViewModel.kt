package ai.mlc.mlcchat

import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.getBatteryCurrentAmperes
import ai.mlc.mlcchat.utils.benchmark.getBatteryVoltageVolts
import ai.mlc.mlcchat.utils.benchmark.gpuUsage
import ai.mlc.mlcchat.utils.benchmark.ramUsage
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

data class BenchmarkingSamples(
    val cpu: Sampler = Sampler(),
    val gpu: Sampler = Sampler(),
    val ram: Sampler = Sampler(),
    val voltages: Sampler = Sampler(),
    val currents: Sampler = Sampler(),
    val prefill: Sampler = Sampler(),
    val decode: Sampler = Sampler(),
    val prefillTime: Sampler = Sampler(),
    val decodeTime: Sampler = Sampler()
)

data class IdleSamples(
    val voltages: Sampler = Sampler(),
    val currents: Sampler = Sampler(),
)

class ResultViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var results = arrayListOf<BenchmarkingResult>()

    private var samples = BenchmarkingSamples()
    private var idleSamples = IdleSamples()

    fun addBenchmarkingSample(context: Context) {
        samples.cpu.addSample(cpuUsage(context).toDouble())
        samples.gpu.addSample(gpuUsage().toDouble())
        samples.ram.addSample(ramUsage().toDouble())
    }

    fun addTokenSample(prefill: Double, decode: Double) {
        samples.prefill.addSample(prefill)
        samples.decode.addSample(decode)
    }

    fun addEnergySample(context: Context) {
        samples.voltages.addSample(getBatteryVoltageVolts(context).toDouble())
        samples.currents.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun addEnergySampleIdle(context: Context) {
        idleSamples.voltages.addSample(getBatteryVoltageVolts(context).toDouble())
        idleSamples.currents.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun addPrefillTimeSample(prefill: Double) {
        samples.prefillTime.addSample(prefill)
    }

    fun addDecodeTimeSample(decode: Double) {
        samples.decodeTime.addSample(decode)
    }

    fun wrapResultUp(modelName: String) {
        results.add(
            BenchmarkingResult(
                name = modelName,
                samples = samples,
                idleSamples = idleSamples
            )
        )
        resetSampler()
    }

    private fun resetSampler() {
        samples = BenchmarkingSamples()
        idleSamples = IdleSamples()
    }

    fun getResults(): ArrayList<BenchmarkingResult> {
        return results
    }

    fun getIdleSamples(): IdleSamples {
        return idleSamples
    }

    fun resetResults() {
        results = arrayListOf()
        resetSampler()
    }
}