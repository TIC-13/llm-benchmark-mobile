package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import android.content.Context
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
import androidx.compose.ui.text.capitalize
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

    val numModelsTotal = benchmarkingModelsLabels.size
    val numModelsDone = numModelsTotal - pendingModels.size + 1

    LaunchedEffect(pendingModels) {
        if(pendingModels.isNotEmpty()){

            if(pendingModels.size != viewModel.benchmarkingModels.size){
                saveLastResult()
            }

            val modelState = pendingModels[0]
            modelState.startChat()
        }else{
            saveLastResult()
            resultViewModel.setType(ResultType.BENCHMARKING)
            navController.navigate("result")
        }
    }

    LaunchedEffect(modelChatState) {
        if(chatState.chatable()){

            delay(1000)

            if(pendingQuestions.isEmpty()){

                if(pendingModels.isNotEmpty()){
                    pendingModels = pendingModels.subList(1, pendingModels.size)
                    pendingQuestions = questions
                }

            }else{

                chatState.requestGenerate("${pendingQuestions[0]}.")
                pendingQuestions = pendingQuestions.subList(1, pendingQuestions.size)
            }
        }
    }

    Scaffold(topBar = {
        AppTopBar(
            title = "${chatState.modelName.value.split("-")[0].replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }} - Model $numModelsDone of $numModelsTotal"
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