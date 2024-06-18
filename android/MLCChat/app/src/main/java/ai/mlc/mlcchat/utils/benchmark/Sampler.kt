package ai.mlc.mlcchat.utils.benchmark

class Sampler {

    private val samples = arrayListOf<Int>()
    private var total = 0
    private var numSamples = 0
    private var peak = 0

    fun addSample(sample: Int): Unit {
        total += sample
        numSamples ++
        if(sample > peak) peak = sample
        samples.add(sample)
    }

    fun average(): Int? {
        if(numSamples == 0) return null
        return total/numSamples
    }

    fun peak(): Int {
        return peak
    }

}