package ai.mlc.mlcchat.utils.benchmark

class Sampler {

    private val samples = arrayListOf<Int>()
    private var total = 0
    private var numSamples = 0

    fun addSample(sample: Int): Unit {
        total += sample
        numSamples ++
        samples.add(sample)
    }

    fun average(): Int? {
        if(numSamples == 0) return null
        return total/numSamples
    }

}