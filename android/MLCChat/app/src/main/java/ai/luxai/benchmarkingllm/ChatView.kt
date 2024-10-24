package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.components.AppTopBar
import ai.luxai.benchmarkingllm.utils.benchmark.cpuUsage
import ai.luxai.benchmarkingllm.utils.benchmark.gpuUsage
import ai.luxai.benchmarkingllm.utils.benchmark.launchEffectWithCoroutinesAndDelay
import ai.luxai.benchmarkingllm.utils.benchmark.ramUsage
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterial3Api
@Composable
fun ChatView(
    navController: NavController, chatState: AppViewModel.ChatState, resultViewModel: ResultViewModel
) {

    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current

    fun toResults() {
        resultViewModel.wrapResultUp(context, chatState.modelName.value)
        resultViewModel.setType(ResultType.CONVERSATION)
        navController.navigate("result")
    }

    val modelChatState by remember {
        chatState.modelChatState
    }

    fun onBackButton() {
        if(chatState.interruptable()){
            chatState.requestResetChat(clearsHistory = true)
            navController.popBackStack()
        }
    }

    BackHandler {
        onBackButton()
    }

    fun onRefresh() {
        if(chatState.modelChatState.value == ModelChatState.Ready) {
            chatState.requestResetChat(clearsHistory = true)
        }
    }

    val canRefresh = chatState.modelChatState.value == ModelChatState.Ready

    Scaffold(topBar =
    {
        AppTopBar(
            title = chatState.modelName.value,
            onBack = { onBackButton() },
            backEnabled = chatState.interruptable(),
            actions = {
                IconButton(
                    onClick = ::onRefresh,
                    enabled = canRefresh
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = "reset the chat",
                        tint = if(canRefresh) MaterialTheme.colorScheme.onPrimary else Color.Gray
                    )
                }
                IconButton(
                    onClick = { toResults() },
                    enabled = true
                ) {
                    Icon(
                        imageVector = Icons.Filled.BarChart,
                        contentDescription = "continue to results",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
            }
        }
    )
    }, modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }) { paddingValues ->
        HomeScreenBackground {
            ConversationView(
                paddingValues = paddingValues,
                chatState = chatState,
                resultViewModel = resultViewModel,
                modelChatState = modelChatState
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary
                        )
                        .padding(5.dp)
                ){
                    SendMessageView(chatState = chatState)
                }
            }
        }
    }
}

@Composable
fun ConversationView(
    paddingValues: PaddingValues,
    chatState: AppViewModel.ChatState,
    resultViewModel: ResultViewModel,
    modelChatState: ModelChatState,
    children: @Composable() (ColumnScope.() -> Unit)? = null
) {

    val reportState by remember {
        chatState.report
    }

    var startReadMessageTime by remember { mutableStateOf(0L) }
    var initLoadTime by remember { mutableStateOf<Long?>(null) }

    var cancelExecutionJob by remember { mutableStateOf<Job?>(null)}

    val context = LocalContext.current

    //useMeasureEnergyConsumption(
    //    context = context,
    //    chatState = chatState,
    //    resultViewModel = resultViewModel
    //)

    //Medição de CPU, GPU e RAM
    LaunchedEffect(Unit) {
        resultViewModel.resetResults()
        withContext(Dispatchers.IO) {
            while(true) {
                delay(25)
                if(chatState.modelChatState.value === ModelChatState.Generating)
                    resultViewModel.addBenchmarkingSample(context)
            }
        }
    }

    //Medição do tempo de inicialização
    LaunchedEffect(modelChatState) {
        if(modelChatState === ModelChatState.Reloading){
            initLoadTime = System.currentTimeMillis()
            return@LaunchedEffect
        }
        if(initLoadTime !== null){
            val finishLoadTime = System.currentTimeMillis()
            resultViewModel.setLoadTime(finishLoadTime - initLoadTime!!)
            initLoadTime = null
        }
    }

    //Medição de tokens por segundo
    LaunchedEffect(reportState) {

        Log.d("repState","Report state: $reportState")

        if(reportState.trim() !== ""){
            val regex = Regex("\\d+[,.]\\d+")
            val matches = regex.findAll(reportState).map { it.value }.toList()
            val numericValues = matches.map { it.replace(",", ".").toDouble() }
            if(numericValues.size == 2)
                resultViewModel.addTokenSample(numericValues[0], numericValues[1])
        }
    }

    //Medição do tempo de decode
    LaunchedEffect(modelChatState) {
        if(modelChatState === ModelChatState.Ready && startReadMessageTime != 0L) {
            val endReadMessageTime = System.currentTimeMillis()
            val timeDecode = endReadMessageTime - startReadMessageTime
            val lastAnswerNotEmpty = chatState.messages.findLast { it.role === MessageRole.Assistant && it.text.isNotEmpty() }
            lastAnswerNotEmpty?.decodeTime = timeDecode
            if (lastAnswerNotEmpty != null) {
                chatState.updateMessage(lastAnswerNotEmpty)
                resultViewModel.addDecodeTimeSample(timeDecode.toDouble() / 1000F)
                cancelExecutionJob?.cancel()
            }
        }
        if(modelChatState === ModelChatState.Generating)
            startReadMessageTime = System.currentTimeMillis()
    }

    val modelStartedAnswering =
        chatState.messages.isNotEmpty() &&
                chatState.messages.last().role === MessageRole.Assistant &&
                chatState.messages.last().text.isNotEmpty() &&
                startReadMessageTime != 0L

    //Medição do tempo de prefill
    LaunchedEffect(modelStartedAnswering) {
        if(modelStartedAnswering){
            val endReadMessageTime = System.currentTimeMillis()
            val prefillTime = endReadMessageTime - startReadMessageTime
            val lastAnswer = chatState.messages.findLast { it.role === MessageRole.Assistant }
            lastAnswer?.prefillTime = prefillTime
            if (lastAnswer != null) {
                chatState.updateMessage(lastAnswer)
                resultViewModel.addPrefillTimeSample(prefillTime.toDouble() / 1000F)
                cancelExecutionJob = launchEffectWithCoroutinesAndDelay(delayMillis = 60000) {
                    chatState.requestResetChat(clearsHistory = false)
                }
            }
            startReadMessageTime = System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ){
        val lazyColumnListState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(0.dp, 60.dp)
                .background(color = MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if(chatState.report.value.trim() !== ""){
                Text(
                    text = chatState.report.value,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                )
            }
            BenchmarkView(
                modifier = Modifier
                    .fillMaxWidth(),
                fontWeight = FontWeight.Light,
                textColor = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column (
            modifier = Modifier
                .fillMaxSize()
        ){
            MessagesView(
                modifier = Modifier.weight(9f),
                lazyColumnListState = lazyColumnListState,
                coroutineScope = coroutineScope,
                chatState = chatState
            )
            if (children != null) {
                children()
            }
        }
    }
}

@Composable
fun useMeasureEnergyConsumption(
    context: Context,
    chatState: AppViewModel.ChatState,
    resultViewModel: ResultViewModel
): Unit {
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while(true) {
                delay(25)
                if(chatState.modelChatState.value === ModelChatState.Generating) {
                    resultViewModel.addEnergySample(context)
                }
            }
        }
    }
}


@Composable
fun BenchmarkView(
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    fontWeight: FontWeight = FontWeight.Normal,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {

    val context = LocalContext.current

    var ram by remember { mutableStateOf(0) }
    var cpu by remember { mutableStateOf<Int?>(0) }
    var gpu by remember { mutableStateOf<Int?>(0) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO){
            while(true) {
                delay(500)
                ram = ramUsage()
                cpu = cpuUsage(context)
                gpu = gpuUsage()
            }
        }
    }

    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ){
        Text(text = "CPU: ${if(cpu !== null) "${cpu}%" else "-"}", color = textColor, fontWeight = fontWeight, style = style)
        Text(text = "GPU: ${if(gpu !== null) "${gpu}%" else "-"}", color = textColor, fontWeight = fontWeight, style = style)
        Text(text = "RAM: ${ram}MB", color = textColor, fontWeight = fontWeight, style = style)
    }
}

@Composable
fun MessagesView(modifier: Modifier = Modifier, lazyColumnListState: LazyListState, coroutineScope: CoroutineScope, chatState: AppViewModel.ChatState) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp, alignment = Alignment.Bottom),
        state = lazyColumnListState
    ) {
        coroutineScope.launch {
            lazyColumnListState.animateScrollToItem(chatState.messages.size)
        }
        items(
            items = chatState.messages,
            key = { message -> message.id },
        ) { message ->
            MessageView(messageData = message)
        }
        item {
            // place holder item for scrolling to the bottom
        }
    }
}

@Composable
fun MessageView(messageData: MessageData) {

    @Composable
    fun BottomText(text: String) {
        Text(
            modifier = Modifier
                .fillMaxWidth(),
            text = text,
            textAlign = TextAlign.Right,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraLight,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
        )
    }

    SelectionContainer {
        if (messageData.role == MessageRole.Assistant) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(10.dp)
                    .widthIn(max = 250.dp)
                    .wrapContentWidth()
            ) {
                Text(
                    text = messageData.text,
                    textAlign = TextAlign.Left,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                )

                if(messageData.prefillTime !== null) {
                    Spacer(modifier = Modifier.height(20.dp))
                    BottomText(text = "Prefill time: ${messageData.prefillTime?.let { formatDouble(it.toDouble()/1000, "s")}}")
                }

                if(messageData.decodeTime !== null) {
                    BottomText(text = "Decode time: ${messageData.decodeTime?.let { formatDouble(it.toDouble()/1000, "s")}}")
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageData.text,
                    textAlign = TextAlign.Right,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(10.dp)
                        .widthIn(max = 250.dp)
                )

            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun SendMessageView(chatState: AppViewModel.ChatState) {

    val localFocusManager = LocalFocusManager.current

    val canGoBack = chatState.modelChatState.value == ModelChatState.Ready

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    ) {
        var text by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(text = "Input") },
            modifier = Modifier
                .weight(9f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                focusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                unfocusedLabelColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                cursorColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        )
        IconButton(
            onClick = {
                localFocusManager.clearFocus()
                chatState.requestGenerate(text)
                text = ""
            },
            modifier = Modifier
                .aspectRatio(1f)
                .weight(1f),
            enabled = (text.isNotEmpty() && chatState.chatable())
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "send message",
                tint = if(canGoBack) MaterialTheme.colorScheme.onPrimary else Color.Gray,
                modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
