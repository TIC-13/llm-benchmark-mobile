package ai.mlc.mlcchat.utils.benchmark

import ai.mlc.mlcchat.AppViewModel
import ai.mlc.mlcchat.ModelInitState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

val modelsLabel = listOf(
    "Qwen2-1.5B-Instruct-q4f16_1-MLC",
    "gemma-2b-q4f16_1-MLC"
)

@Composable
fun DownloadView(
    navController: NavController,
    appViewModel: AppViewModel
) {

    var modelsList by remember {
        mutableStateOf(
            appViewModel.modelList.filter{ modelsLabel.contains(it.modelConfig.modelId) }
        )
    }

    val numModels = remember {
        modelsList.size
    }

    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(modelsList.isNotEmpty()){
            delay(100)

            val modelState = modelsList[0]

            if(modelState.modelInitState.value == ModelInitState.Finished){
                modelsList = modelsList.subList(1, modelsList.size)
                continue
            }

            if(modelState.modelInitState.value !== ModelInitState.Downloading){
                modelState.handleStart()
            }
        }
        finished = true
    }

    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if(modelsList.isNotEmpty()){

            val modelState = modelsList[0]

            Text(
                modifier = Modifier
                    .padding(0.dp, 15.dp),
                text = "Downloading model ${numModels-modelsList.size+1} of $numModels"
            )

            Column (
                modifier = Modifier
                    .fillMaxWidth(0.8F),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ){
                Text(text = modelState.modelConfig.modelId)
                LinearProgressIndicator(
                    progress = modelState.progress.value.toFloat() / modelState.total.value,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
        if(finished){
            Text(text = "Finished downloading all models!")
        }
    }


}