package ai.mlc.mlcchat

import ai.mlc.mlcchat.utils.benchmark.ResultViewModel
import android.content.Context
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
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

    LaunchedEffect(pendingModels) {
        if(pendingModels.isNotEmpty()){
            val modelState = pendingModels[0]
            modelState.startChat()
        }
    }

    LaunchedEffect(modelChatState) {
        if(chatState.chatable()){

            if(pendingQuestions.isEmpty()){

                if(pendingModels.isEmpty()){

                }else{
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
        TopAppBar(
            title = {
                Text(
                    text = chatState.modelName.value.split("-")[0],
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        )
    }, modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val lazyColumnListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            if(chatState.messages.isEmpty()){
                CircularProgressIndicator()
            }else{
                MessagesView(
                    modifier = Modifier.fillMaxSize(),
                    lazyColumnListState = lazyColumnListState,
                    coroutineScope = coroutineScope,
                    chatState = chatState
                )
            }
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