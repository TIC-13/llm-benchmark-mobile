package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.interfaces.Measurement
import ai.mlc.mlcchat.utils.benchmark.Sampler
import ai.mlc.mlcchat.utils.benchmark.cpuUsage
import ai.mlc.mlcchat.utils.benchmark.gpuUsage
import ai.mlc.mlcchat.utils.benchmark.ramUsage
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalMaterial3Api
@Composable
fun ChatView(
    navController: NavController, chatState: AppViewModel.ChatState, resultViewModel: ResultViewModel
) {

    val localFocusManager = LocalFocusManager.current
    val context = LocalContext.current

    val cpuSamples by remember { mutableStateOf(Sampler()) }
    val gpuSamples by remember { mutableStateOf(Sampler()) }
    val ramSamples by remember { mutableStateOf(Sampler()) }

    LaunchedEffect(Unit) {

            resultViewModel.resetResults()

            withContext(Dispatchers.IO) {
                while(true) {
                    delay(25)
                    if(chatState.modelChatState.value !== ModelChatState.Generating)
                        continue
                    cpuSamples.addSample(cpuUsage(context))
                    gpuSamples.addSample(gpuUsage())
                    ramSamples.addSample(ramUsage())
                }
            }
    }

    fun toResults() {
        resultViewModel.results.add(
            BenchmarkingResult(
                name = chatState.modelName.value,
                cpu = cpuSamples.measurements(),
                gpu = gpuSamples.measurements(),
                ram = ramSamples.measurements(),
                toks = Measurement(0,0,0)
            )
        )
        navController.navigate("result")
    }

    Scaffold(topBar =
    {
        AppTopBar(
            title = chatState.modelName.value.split("-")[0],
            onBack = { navController.popBackStack() },
            backEnabled = chatState.interruptable(),
            actions = {
                IconButton(
                    onClick = { chatState.requestResetChat() },
                    enabled = chatState.interruptable()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = "reset the chat",
                        tint = MaterialTheme.colorScheme.onPrimary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val lazyColumnListState = rememberLazyListState()
                val coroutineScope = rememberCoroutineScope()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(0.dp, 80.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .padding(top = 5.dp)
                        )
                    }
                    BenchmarkView(
                        modifier = Modifier
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Light,
                        textColor = MaterialTheme.colorScheme.onPrimary
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
}

@Composable
fun BenchmarkView(
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    fontWeight: FontWeight = FontWeight.Normal
) {

    val context = LocalContext.current

    var ram by remember { mutableStateOf(0) }
    var cpu by remember { mutableStateOf(0) }
    var gpu by remember { mutableStateOf(0) }

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
        Text(text = "CPU: ${cpu}%", color = textColor, fontWeight = fontWeight)
        Text(text = "GPU: ${gpu}%", color = textColor, fontWeight = fontWeight)
        Text(text = "RAM: ${ram}MB", color = textColor, fontWeight = fontWeight)
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
    SelectionContainer {
        if (messageData.role == MessageRole.Assistant) {
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = messageData.text,
                    textAlign = TextAlign.Left,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(10.dp)
                        .widthIn(max = 250.dp)
                )
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
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.background(color = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
