package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.actions.useDownloadModels
import ai.luxai.benchmarkingllm.components.AppTopBar
import ai.luxai.benchmarkingllm.components.IconPosition
import ai.luxai.benchmarkingllm.components.SingleCheckbox
import ai.luxai.benchmarkingllm.components.TextWithIcon
import ai.luxai.benchmarkingllm.utils.benchmark.LogStatus
import ai.luxai.benchmarkingllm.utils.benchmark.ModelStatusLog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ModelSelection(
    val model: AppViewModel.ModelState,
    val isChecked: Boolean,
)

data class StatusInfo(
    val label: String,
    val iconVector: ImageVector,
    val iconColor: Color
)

@Composable
fun ModelSelectionView(
    navController: NavController,
    viewModel: AppViewModel,
) {

    val context = LocalContext.current
    val modelStatusLog = ModelStatusLog(context)


    var selections by remember { mutableStateOf(viewModel.benchmarkingModels.map {
            val modelName = it.modelConfig.modelId
            ModelSelection(
                model = it,
                isChecked = modelStatusLog.getIsSelected(modelName) &&
                            !modelStatusLog.checkWasModelInterrupted(modelName)
            )
        })
    }

    fun toggleSelection(index: Int) {
        fun select(idx: Int, sel: ModelSelection): ModelSelection {
            if(idx == index) {
                modelStatusLog.setIsSelected(sel.model.modelConfig.modelId, !sel.isChecked)
                return ModelSelection(sel.model, !sel.isChecked)
            }else{
                return sel
            }
        }
        selections = selections.mapIndexed(::select)
    }

    fun getCheckedModels(): List<AppViewModel.ModelState> {
        return selections.filter { it.isChecked } .map { it.model }
    }

    fun anySelectedModelNotDownloaded(): Boolean {
        return getCheckedModels().any {it.modelInitState.value !== ModelInitState.Finished }
    }

    fun startBenchmarking() {
        viewModel.updateBenchmarkingModels(
            getCheckedModels()
        )
        navController.navigate("benchmarking")
    }

    val (isDownloading, pendingModels, numModels, startDownload) = useDownloadModels(
        showsWarning = true,
        onFinish = { startBenchmarking() }
    )

    fun onContinue() {
        val checkedModels = getCheckedModels()
        if(checkedModels.isEmpty()){
            Toast.makeText(context, "Select at least one model", Toast.LENGTH_LONG).show()
            return
        }

        if(checkedModels.all { it.modelInitState.value == ModelInitState.Finished}){
            startBenchmarking()
            return;
        }

        startDownload(checkedModels)
    }

    Scaffold(topBar =
    {
        AppTopBar(
            title = "Select the models",
            onBack = {
                navController.popBackStack()
            }
        )
    }
    ) {
        paddingValues ->
        HomeScreenBackground {
            Column (
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier
                    .fillMaxHeight(if (isDownloading) 0.8f else 0.9f)
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 10.dp)
            ){
                LazyColumn() {
                    items(
                        items = selections.withIndex().toList(),
                    ) { (index, selection) ->

                        val modelName = selection.model.modelConfig.modelId
                        val modelStatus = modelStatusLog.getStatus(modelName)
                        val modelDownloaded = selection.model.modelInitState.value == ModelInitState.Finished

                        val statusInfos = mapOf(
                            LogStatus.RUN to StatusInfo(
                                label = "Last run on ${modelStatusLog.getLastExecutionDateReadable(modelName)}",
                                iconVector = Icons.Default.Check,
                                iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            LogStatus.INTERRUPTED to StatusInfo(
                                label = "Crashed or interrupted",
                                iconVector = Icons.Default.Warning,
                                iconColor = MaterialTheme.colorScheme.error,
                            ),
                            LogStatus.NOT_RUN to StatusInfo(
                                label = "Not run yet",
                                iconVector = Icons.Default.Timer,
                                iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )

                        val (label, iconVector, iconColor) = statusInfos[modelStatus] ?:
                            StatusInfo(
                                label = "Unknown",
                                iconVector = Icons.Default.QuestionMark,
                                iconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                        Spacer(modifier = Modifier.height(15.dp))
                        ModelSelector(
                            enabled = !isDownloading,
                            modifier = Modifier
                                .clickable { toggleSelection(index) },
                            modelName = modelName,
                            checked = selection.isChecked,
                            setChecked = { toggleSelection(index) },
                            iconVector = iconVector,
                            iconColor = iconColor,
                            subtitle = label,
                            modelDownloaded = modelDownloaded
                        )
                        if(index == selections.size - 1)
                            Spacer(modifier = Modifier.height(15.dp))
                    }
                }
            }
            if(isDownloading){
                DownloadView(
                    modifier = Modifier
                        .fillMaxSize(),
                    pendingModels = pendingModels,
                    numModels = numModels
                )
            }else{
                ContinueButton(
                    modifier = Modifier
                        .clickable {
                            onContinue()
                        },
                    label =
                        if(anySelectedModelNotDownloaded())
                            "DOWNLOAD AND START" else "START"
                )
            }
        }
    }
}


@Composable
fun ModelSelector(
    modifier: Modifier = Modifier,
    modelName: String,
    checked: Boolean,
    setChecked: (status: Boolean) -> Unit,
    iconVector: ImageVector,
    iconColor: Color,
    subtitle: String,
    enabled: Boolean = true,
    modelDownloaded: Boolean,
) {
    val backgroundColor = if (enabled) MaterialTheme.colorScheme.primaryContainer else Color.Gray.copy(alpha = 0.6f)
    val contentColor = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray.copy(alpha = 0.9f)

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(color = backgroundColor)
            .padding(15.dp, 7.5.dp, 15.dp, 15.dp)
    ) {
        SingleCheckbox(
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Left,
            labelColor = contentColor,
            fontWeight = FontWeight.Normal,
            label = modelName,
            checked = checked,
            setChecked = setChecked,
        )
        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            TextWithIcon(
                fontColor = contentColor,
                fontStyle = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light,
                iconModifier = Modifier.size(16.dp),
                imageVector = iconVector,
                iconColor = if (enabled) iconColor else Color.Gray,
                text = subtitle
            )
            TextWithIcon(
                fontColor = contentColor,
                fontStyle = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                iconModifier = Modifier.size(16.dp),
                imageVector =
                    if(modelDownloaded)
                        Icons.Default.FileDownloadDone
                    else
                        Icons.Default.FileDownloadOff,
                iconColor = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray,
                text = if(modelDownloaded) "Downloaded" else "Not downloaded",
                iconPosition = IconPosition.RIGHT
            )
        }

    }
}