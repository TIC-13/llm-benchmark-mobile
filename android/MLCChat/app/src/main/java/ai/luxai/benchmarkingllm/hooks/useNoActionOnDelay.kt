package ai.luxai.benchmarkingllm.hooks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember

@Composable
fun useNoActionOnDelay(delay: Long = 500L): (onPress: () -> Unit) -> Unit {

    val timeStamp = remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun run(onPress: () -> Unit){
        val currTime = System.currentTimeMillis()
        if(currTime > timeStamp.longValue + delay){
            onPress()
            timeStamp.longValue = currTime
        }
    }

    return ::run
}