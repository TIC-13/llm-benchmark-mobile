package ai.luxai.benchmarkingllm.actions

import ai.luxai.benchmarkingllm.AppViewModel
import ai.luxai.benchmarkingllm.ModelInitState
import ai.luxai.benchmarkingllm.hooks.useModal
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

data class DownloadModelsActions(
    val isDownloading: Boolean,
    val pendingModels: List<AppViewModel.ModelState>,
    val numModels: Int,
    val startDownload: (models: List<AppViewModel.ModelState>) -> Unit,
)

@Composable
fun useDownloadModels(
    showsWarning: Boolean = false,
    onFinish: () -> Unit
): DownloadModelsActions {

    var isDownloading by remember { mutableStateOf(false) }

    var pendingModels: List<AppViewModel.ModelState> by remember {
        mutableStateOf(emptyList())
    }

    var numModels by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(isDownloading) {

        if(!isDownloading)
            return@LaunchedEffect

        while(pendingModels.isNotEmpty()){
            delay(100)

            val modelState = pendingModels[0]

            if(modelState.modelInitState.value == ModelInitState.Finished){
                pendingModels = pendingModels.subList(1, pendingModels.size)
                continue
            }

            if(modelState.modelInitState.value !== ModelInitState.Downloading){
                modelState.handleStart()
            }
        }
        isDownloading = false
        onFinish()
    }

    fun startDownload() {
        isDownloading = true
    }

    val (showWarning) = useModal(
        title = "Warning",
        text = "The execution of LLMs on Android devices can be very taxing, and can cause crashes, especially on devices with less than 8GB of RAM.",
        onConfirm = { startDownload() },
        confirmLabel = "Continue"
    )

    val (showConfirmationModal) = useModal(
        title = "Download Models",
        text = "To start the benchmarking, we need to download the LLM models.\n" +
                "\n" +
                "This may take some time and require a large download.\n" +
                "\n" +
                "Do you want to continue?",
        onConfirm = { if(showsWarning) showWarning() else startDownload()},
        confirmLabel = "Download"
    )

    fun toggleDownload(models: List<AppViewModel.ModelState>) {
        pendingModels = models
        numModels = models.size
        showConfirmationModal()
    }

    return DownloadModelsActions(
        isDownloading = isDownloading,
        startDownload = { models -> toggleDownload(models) },
        pendingModels = pendingModels,
        numModels = numModels
    )
}