package ai.mlc.mlcchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DownloadView(
    modifier: Modifier = Modifier,
    pendingModels: List<AppViewModel.ModelState>,
    numModels: Int,
) {

    Column (
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        if(pendingModels.isNotEmpty()){

            val modelState = pendingModels[0]

            Text(
                modifier = Modifier,
                text = "Downloading model ${numModels-pendingModels.size+1} of $numModels",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(15.dp))
            Column (
                modifier = Modifier
                    .fillMaxWidth(0.8F),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ){
                Text(
                    text = modelState.modelConfig.modelId,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Light
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = modelState.progress.value.toFloat() / modelState.total.value,
                    color = MaterialTheme.colorScheme.inversePrimary,
                    trackColor = Color.Gray
                )
            }
        }
    }

}