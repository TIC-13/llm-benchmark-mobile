package ai.mlc.mlcchat.utils.benchmark

import ai.mlc.mlcchat.interfaces.Measurement
import kotlin.math.sqrt

class Sampler {

    private val samples = arrayListOf<Int>()
    private var peak = 0

    fun addSample(sample: Int): Unit {
        if(sample > peak) peak = sample
        samples.add(sample)
    }

    fun average(): Double {
        return samples.average()
    }

    fun peak(): Int {
        return peak
    }

    fun std(): Double {
        val mean = samples.average()
        val sumOfSquaredDiffs = samples.sumOf { (it - mean) * (it - mean) }
        val variance = sumOfSquaredDiffs / samples.size
        return sqrt(variance)
    }

    fun measurements(): Measurement {
        return Measurement(
            average = this.average().toInt(),
            peak = this.peak(),
            std = this.std().toInt()
        )
    }

}