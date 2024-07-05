package ai.mlc.mlcchat.utils.benchmark

import ai.mlc.mlcchat.interfaces.Measurement
import kotlin.math.sqrt

class Sampler {

    private val samples = arrayListOf<Double>()
    private var peak = Double.MIN_VALUE
    private var creationTime = 0L

    init {
        creationTime = System.currentTimeMillis()
    }

    fun addSample(sample: Double): Unit {
        if(sample > peak) peak = sample
        samples.add(sample)
    }

    fun getSamples(): ArrayList<Double> {
        return samples
    }

    fun average(): Double {
        return samples.average()
    }

    fun peak(): Double {
        return peak
    }

    fun std(): Double {
        val mean = samples.average()
        val sumOfSquaredDiffs = samples.sumOf { (it - mean) * (it - mean) }
        val variance = sumOfSquaredDiffs / samples.size
        return sqrt(variance)
    }

    fun median(): Double {
        if (samples.isEmpty()) {
            //throw IllegalArgumentException("The list cannot be empty")
            return Double.NaN
        }

        val sortedValues = samples.sorted()
        val middle = sortedValues.size / 2

        return if (sortedValues.size % 2 == 0) {
            (sortedValues[middle - 1] + sortedValues[middle]) / 2.0
        } else {
            sortedValues[middle]
        }
    }

    fun getCreationTime(): Long {
        return creationTime
    }

}