package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.LoadingTopBottomIndicator
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun HomeView(
    navController: NavController,
    appViewModel: AppViewModel,
    resultViewModel: ResultViewModel,
) {

    val context = LocalContext.current

    //val isIdleMeasured = useMeasureIdleEnergyConsumption(
    //    context = context,
    //    resultViewModel = resultViewModel
    //)

    val isIdleMeasured = true

    var showStartDownloadDialog by remember { mutableStateOf(false) }
    var downloadingModels by remember { mutableStateOf(false) }

    fun startBenchmarking() {
        navController.navigate("benchmarking")
    }

    fun openDownloadDialog() {
        if(!appViewModel.allBenchmarkingModelsReady()){
            showStartDownloadDialog = true
        }else{
            startBenchmarking()
        }
    }

    fun startDownload() {
        downloadingModels = true
    }

    fun onDownloadsFinished() {
        downloadingModels = false
        startBenchmarking()
    }

    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        HomeScreenBackground {

            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize()
                    .padding(30.dp, 0.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                TitleView()
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                
                LargeRoundedButton(
                    icon = Icons.Default.BarChart,
                    onClick = { openDownloadDialog() },
                    enabled = !downloadingModels && isIdleMeasured,
                    text = "Start benchmarking"
                )

                Spacer(modifier = Modifier.height(15.dp))

                LargeRoundedButton(
                    icon = Icons.Default.Chat,
                    onClick = { navController.navigate("home") },
                    enabled = !downloadingModels && isIdleMeasured,
                    text = "Chat with LLMs"
                )

            }

            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {

                if(!isIdleMeasured) {
                    LoadingTopBottomIndicator(
                        modifier = Modifier
                            .fillMaxHeight(0.5f)
                            .fillMaxWidth(),
                        text = "Measuring idle energy consumption"
                    )
                }

                if(downloadingModels) {
                    DownloadView(
                        modifier = Modifier
                            .fillMaxSize(),
                        appViewModel = appViewModel,
                        onDownloadsFinished = { onDownloadsFinished() }
                    )
                }
            }

        }

        if(showStartDownloadDialog) {
            AlertDialog(
                title = { Text(text = "Download Models") },
                text = { Text(text = "To start the benchmarking, we need to download the LLM models.\n\nThis may take some time and require a large download.\n\nDo you want to continue?") },
                onDismissRequest = { showStartDownloadDialog = false },
                confirmButton = {
                    TextButton(onClick = { startDownload(); showStartDownloadDialog = false }) { Text("Download") }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDownloadDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun useMeasureIdleEnergyConsumption(
    context: Context,
    resultViewModel: ResultViewModel
): Boolean {

    var idleMeasured by remember {
        mutableStateOf(false)
    }

    //Medindo consumo de energia em idle
    LaunchedEffect(Unit) {
        if(resultViewModel.anyIdleSampleCollected()){
            idleMeasured = true
            return@LaunchedEffect
        }

        val initTime = System.currentTimeMillis()
        var currTime = System.currentTimeMillis()
        val durationTime = 3000L

        while(currTime < initTime + durationTime){
            delay(25)
            currTime = System.currentTimeMillis()
            resultViewModel.addEnergySampleIdle(context)
        }
        idleMeasured = true
    }

    return idleMeasured
}


@Composable
fun HomeScreenBackground(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.SpaceAround,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.motherboard_purple),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement
        ) {
            content()
        }
    }
}


@Composable
fun TitleView(modifier: Modifier = Modifier) {
    Column (
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Image(
                painter = painterResource(id = R.drawable.lightning),
                contentDescription = "Icon in the shape of lightning"
            )
        }
        Text(
            text = stringResource(id = R.string.app_description),
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun LargeRoundedButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "Hello"
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(0.dp, 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .weight(2f),
                imageVector = icon,
                contentDescription = null,
            )
            Text(
                modifier = Modifier
                    .weight(3f),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

