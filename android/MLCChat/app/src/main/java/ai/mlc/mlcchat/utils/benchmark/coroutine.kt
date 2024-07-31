package ai.mlc.mlcchat.utils.benchmark

import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
fun launchEffectWithCoroutinesAndDelay(delayMillis: Long, action: () -> Unit): Job {
    val job = GlobalScope.launch(Dispatchers.Main) {
        delay(delayMillis)
        action()
    }
    return job
}

