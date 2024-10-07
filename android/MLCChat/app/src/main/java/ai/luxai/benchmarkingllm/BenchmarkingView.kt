package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.components.AppTopBar
import ai.luxai.benchmarkingllm.components.LockScreenOrientation
import ai.luxai.benchmarkingllm.utils.benchmark.ModelStatusLog
import android.content.Context
import android.content.pm.ActivityInfo
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

@Composable
fun BenchmarkingView(
    navController: NavController,
    viewModel: AppViewModel,
    resultViewModel: ResultViewModel
){

    val localFocusManager = LocalFocusManager.current
    val chatState = viewModel.chatState
    val modelChatState by remember {
        viewModel.chatState.modelChatState
    }

    val context = LocalContext.current

    fun saveResult() {
        resultViewModel.wrapResultUp(
            context = context,
            modelName = chatState.modelName.value,
            sendResult = true
        )
    }

    fun finishAll() {
        resultViewModel.setType(ResultType.BENCHMARKING)
        viewModel.resetBenchmarkingModels()
        navController.navigate("result")
    }

    val (modelName, numModelsTotal, numModelsDone) = useBenchmarking(
        numQuestions = 2,
        viewModel = viewModel,
        onSaveSingleResult = ::saveResult,
        onFinishAll = ::finishAll
    )

    Scaffold(topBar = {
        AppTopBar(
            title = "$modelName - Model $numModelsDone of $numModelsTotal"
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
                modelChatState = modelChatState,
            )
        }
    }
}

data class ExecutingModelsState(
    val modelName: String,
    val numModelsTotal: Int,
    val numModelsDone: Int
)

@Composable
fun useBenchmarking(
    viewModel: AppViewModel,
    onSaveSingleResult: () -> Unit,
    onFinishAll: () -> Unit,
    numQuestions: Int = 2,
): ExecutingModelsState {

    val context = LocalContext.current
    val modelStatusLog = ModelStatusLog(context)
    val questionsFileName = "qa_dataset.txt"

    val chatState = viewModel.chatState

    val modelChatState by remember {
        viewModel.chatState.modelChatState
    }

    val questions = remember {
        readQuestionsFile(context, questionsFileName).subList(0,numQuestions)
    }

    var pendingModels by remember {
        mutableStateOf(viewModel.benchmarkingModels)
    }

    var pendingQuestions by remember {
        mutableStateOf(questions)
    }

    val numModelsTotal = viewModel.benchmarkingModels.size
    val numModelsDone = numModelsTotal - pendingModels.size + 1
    val modelName = chatState.modelName.value.split("-")[0].replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }

    fun goToNextModel(): Unit {
        if(pendingModels.isNotEmpty()){
            val modelState = pendingModels[0]
            modelStatusLog.logEndModel(modelState.modelConfig.modelId)
            pendingModels = pendingModels.subList(1, pendingModels.size)
            pendingQuestions = questions
            return
        }
        onFinishAll()
    }

    fun goToNextQuestion(): Unit {
        if(pendingQuestions.isEmpty()){
            goToNextModel()
            return
        }
        chatState.requestGenerate("${pendingQuestions[0]}.")
        pendingQuestions = pendingQuestions.subList(1, pendingQuestions.size)
    }

    //Init model
    LaunchedEffect(pendingModels) {
        if(pendingModels.isNotEmpty()){
            if(pendingModels.size != viewModel.benchmarkingModels.size){
                onSaveSingleResult()
            }
            val modelState = pendingModels[0]
            modelStatusLog.logStartModel(modelState.modelConfig.modelId)
            modelState.startChat()
        }else{
            onSaveSingleResult()
            onFinishAll()
        }
    }

    //Init question
    LaunchedEffect(modelChatState) {
        if(chatState.chatable()){
            delay(1000)
            try {
                goToNextQuestion()
            }catch(e: Exception){
                Log.d("crash", "question")
                goToNextModel()
            }
        }
    }

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    return ExecutingModelsState(
        modelName = modelName,
        numModelsTotal = numModelsTotal,
        numModelsDone = numModelsDone
    )
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