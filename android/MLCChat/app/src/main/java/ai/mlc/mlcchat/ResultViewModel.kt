package ai.mlc.mlcchat

import ai.mlc.mlcchat.api.LLMModel
import ai.mlc.mlcchat.api.PostResult
import ai.mlc.mlcchat.api.postResult
import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.getBatteryCurrentAmperes
import ai.mlc.mlcchat.utils.benchmark.getBatteryVoltageVolts
import ai.mlc.mlcchat.utils.benchmark.gpuUsage
import ai.mlc.mlcchat.utils.benchmark.isBatteryCharging
import ai.mlc.mlcchat.utils.benchmark.ramUsage
import ai.mlc.mlcchat.utils.benchmark.system.getPhoneData
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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

enum class ResultType {
    BENCHMARKING, CONVERSATION
}

val IDLE_MEASUREMENT_TIME = 3000L

class ResultViewModel(
    application: Application
) : AndroidViewModel(application) {

    private var results = arrayListOf<BenchmarkingResult>()

    private var samples = BenchmarkingSamples()
    private var idleSamples = IdleSamples()

    private var loadTime: Long? = null

    private var type = ResultType.BENCHMARKING

    fun getType(): ResultType {
        return type
    }

    fun setType(newType: ResultType) {
        type = newType
    }

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
        if(isBatteryCharging(context)) return
        samples.voltages.addSample(getBatteryVoltageVolts(context).toDouble())
        samples.currents.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun addEnergySampleIdle(context: Context) {
        if(isBatteryCharging(context)) return
        idleSamples.voltages.addSample(getBatteryVoltageVolts(context).toDouble())
        idleSamples.currents.addSample(getBatteryCurrentAmperes(context).toDouble())
    }

    fun addPrefillTimeSample(prefill: Double) {
        samples.prefillTime.addSample(prefill)
    }

    fun addDecodeTimeSample(decode: Double) {
        samples.decodeTime.addSample(decode)
    }

    fun setLoadTime(newLoadTime: Long?) {
        loadTime = newLoadTime
    }

    fun wrapResultUp(context: Context, modelName: String, sendResult: Boolean = false) {
        val result = BenchmarkingResult(
            loadTime = loadTime,
            name = modelName,
            samples = samples,
            idleSamples = idleSamples
        )
        results.add(result)

        if(sendResult)
            sendResult(context, result)

        resetSampler()
        loadTime = null
    }

    fun sendResult(context: Context, result: BenchmarkingResult) {

        val samples = result.samples

        val prefill = samples.prefill.getMeasurements()
        val decode = samples.decode.getMeasurements()

        val power = Double.NaN
        val energy = Double.NaN
        //val power = getPowerConsumption(result, getIdleSamples())
        //val energy = getEnergyConsumption(result, getIdleSamples())

        postResult(
            PostResult(
                phone = getPhoneData(context),
                llm_model = LLMModel(name = result.name),
                load_time = result.loadTime?.toInt(),
                ram = samples.ram.getMeasurements(),
                cpu = samples.cpu.getMeasurements(),
                gpu = samples.gpu.getMeasurements(),
                decode = decode,
                prefill = prefill,
                energyAverage = if(!energy.isNaN()) energy else null,
                powerAverage = if(!power.isNaN()) power else null
            )
        )
    }


    private fun resetSampler() {
        samples = BenchmarkingSamples()
        //idleSamples = IdleSamples()
    }

    fun getResults(): ArrayList<BenchmarkingResult> {
        return results
    }

    fun getIdleSamples(): IdleSamples {
        return idleSamples
    }

    fun anyIdleSampleCollected(): Boolean {
        return idleSamples.voltages.getSamples().isNotEmpty()
    }

    fun resetResults() {
        results = arrayListOf()
        resetSampler()
    }
}