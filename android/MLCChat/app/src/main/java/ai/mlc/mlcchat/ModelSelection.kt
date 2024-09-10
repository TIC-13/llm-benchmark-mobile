package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import ai.mlc.mlcchat.components.SingleCheckbox
import ai.mlc.mlcchat.components.TextWithIcon
import ai.mlc.mlcchat.utils.benchmark.LogStatus
import ai.mlc.mlcchat.utils.benchmark.ModelStatusLog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
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
            ModelSelection(
                model = it,
                isChecked = ! modelStatusLog.checkWasModelInterrupted(it.modelConfig.modelId)
            )
        })
    }

    fun toggleSelection(index: Int) {
        selections = selections.mapIndexed { idx, sel ->
            if(idx == index)
                ModelSelection(sel.model, !sel.isChecked)
            else
                sel
        }
    }

    fun startBenchmarking() {
        val checkedModels = selections.filter { it.isChecked } .map { it.model }
        if(checkedModels.isEmpty()){
            Toast.makeText(context, "Select at least one model", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.benchmarkingModels = checkedModels
        navController.navigate("benchmarking")
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
                    .fillMaxHeight(0.9f)
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
                            modifier = Modifier
                                .clickable { toggleSelection(index) },
                            modelName = modelName,
                            checked = selection.isChecked,
                            setChecked = { toggleSelection(index) },
                            iconVector = iconVector,
                            iconColor = iconColor,
                            subtitle = label
                        )
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
            }
            ContinueButton(
                modifier = Modifier
                    .clickable {
                        startBenchmarking()
                    }
            )
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
    subtitle: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.Start,
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(15.dp, 7.5.dp, 15.dp, 15.dp)
    ) {
        SingleCheckbox(
            modifier = Modifier
                .fillMaxWidth(),
            labelModifier = Modifier
                .weight(5f),
            checkboxModifier = Modifier
                .weight(1f),
            textAlign = TextAlign.Left,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Normal,
            label = modelName,
            checked = checked,
            setChecked = setChecked
        )
        TextWithIcon(
            fontColor = MaterialTheme.colorScheme.onPrimaryContainer,
            fontStyle = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Light,
            iconModifier = Modifier.size(16.dp),
            imageVector = iconVector,
            iconColor = iconColor,
            text = subtitle
        )
    }
}
