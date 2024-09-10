package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class ModelSelection(
    val model: AppViewModel.ModelState,
    val isChecked: Boolean,
)


@Composable
fun ModelSelectionView(
    navController: NavController,
    viewModel: AppViewModel,
) {

    val context = LocalContext.current

    var selections by remember { mutableStateOf(viewModel.benchmarkingModels.map {
            ModelSelection(
                model = it,
                isChecked = true
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
        val checkedModels = selections.filter { it.isChecked }.map { it.model }
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
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxHeight(0.9f)
                    .fillMaxWidth(),
            ){
                for((index, selection) in selections.withIndex()){
                    SingleCheckbox(
                        modifier = Modifier
                            .padding(5.dp),
                        labelModifier = Modifier
                            .weight(5f),
                        checkboxModifier = Modifier
                            .weight(1f),
                        label = selection.model.modelConfig.modelId,
                        checked = selection.isChecked,
                        setChecked = { toggleSelection(index) }
                    )
                }
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
fun SingleCheckbox(
    modifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier,
    checkboxModifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onPrimary,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    checked: Boolean,
    setChecked: (status: Boolean) -> Unit,
    label: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = labelModifier,
            color = labelColor,
            style = labelStyle,
            text = label
        )
        Checkbox(
            modifier = checkboxModifier,
            checked = checked,
            onCheckedChange = { setChecked(it) }
        )
    }
}