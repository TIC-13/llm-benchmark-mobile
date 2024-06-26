package ai.mlc.mlcchat

import ai.mlc.mlcchat.utils.benchmark.DownloadView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeView(
    navController: NavController,
    appViewModel: AppViewModel
) {

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
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(
                onClick = { openDownloadDialog() },
                enabled = !downloadingModels
            ) {
                Text(text = if(downloadingModels)
                    "Downloading models"
                else
                    "Start benchmarking"
                )
            }
            Button(
                onClick = { navController.navigate("home") },
                enabled = !downloadingModels
            ) {
                Text(text = "Chat with LLMs")
            }
        }
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if(downloadingModels) {
                DownloadView(
                    modifier = Modifier
                        .fillMaxSize(),
                    appViewModel = appViewModel,
                    onDownloadsFinished = { onDownloadsFinished() }
                )
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

