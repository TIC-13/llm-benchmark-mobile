package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.interfaces.Measurement
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.gpuUsage
import ai.mlc.mlcchat.utils.benchmark.ramUsage
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun BenchmarkingView(
    navController: NavController,
    viewModel: AppViewModel,
    resultViewModel: ResultViewModel
){

    val localFocusManager = LocalFocusManager.current
    val context = LocalContext.current
    val questionsFileName = "qa_dataset.txt"

    val chatState = viewModel.chatState

    val modelChatState by remember {
        viewModel.chatState.modelChatState
    }

    val questions = remember {
        readQuestionsFile(context, questionsFileName).subList(0,3)
    }

    var pendingModels by remember {
        mutableStateOf(viewModel.benchmarkingModels)
    }

    var pendingQuestions by remember {
        mutableStateOf(questions)
    }

    fun saveLastResult() {
        resultViewModel.wrapResultUp(chatState.modelName.value)
    }

    LaunchedEffect(pendingModels) {
        if(pendingModels.isNotEmpty()){

            if(pendingModels.size != viewModel.benchmarkingModels.size){
                saveLastResult()
            }

            val modelState = pendingModels[0]
            modelState.startChat()
        }else{
            saveLastResult()
            navController.navigate("result")
        }
    }

    LaunchedEffect(modelChatState) {
        if(chatState.chatable()){

            if(pendingQuestions.isEmpty()){

                if(pendingModels.isNotEmpty()){
                    pendingModels = pendingModels.subList(1, pendingModels.size)
                    pendingQuestions = questions
                }

            }else{
                chatState.requestGenerate(pendingQuestions[0])
                pendingQuestions = pendingQuestions.subList(1, pendingQuestions.size)
            }
        }
    }

    Scaffold(topBar = {
        AppTopBar(
            title = chatState.modelName.value.split("-")[0]
        )
    }, modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }) {
        paddingValues ->
        HomeScreenBackground {
            ConversationView(
                paddingValues = paddingValues,
                chatState = chatState,
                resultViewModel = resultViewModel,
                modelChatState = modelChatState
            )
        }
    }
}

fun readQuestionsFile(context: Context, fileName: String): List<String> {
    val inputStream = context.assets.open(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    var line: String? = reader.readLine()
    while (line != null) {
        stringBuilder.append(line).append('\n')
        line = reader.readLine()
    }
    reader.close()

    val content = stringBuilder.toString()
    val parts = content.split("\n\n")
    val result = mutableListOf<String>()

    for (part in parts) {
        val lines = part.split("\n")
        if (lines.size >= 2) {
            result.add("${lines[0]} ${lines[1]}")
        }
    }

    return result.toList()
}