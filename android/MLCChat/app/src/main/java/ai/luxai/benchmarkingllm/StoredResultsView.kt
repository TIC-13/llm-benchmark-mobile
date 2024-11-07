package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.components.AlertCard
import ai.luxai.benchmarkingllm.components.AppTopBar
import ai.luxai.benchmarkingllm.utils.benchmark.getAllPostResults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun StoredResultsView(
    navController: NavController,
    resultViewModel: ResultViewModel
) {

    val localFocusManager = LocalFocusManager.current

    val context = LocalContext.current
    val allPostResultsFlow = remember { getAllPostResults(context) }
    val allPostResults by allPostResultsFlow.collectAsState(initial = emptyList())

    Scaffold(topBar = {
        AppTopBar(
            title = "Last results",
            onBack = { navController.popBackStack() }
        )
    }, modifier = Modifier.pointerInput(Unit) {
        detectTapGestures(onTap = {
            localFocusManager.clearFocus()
        })
    }) {
        paddingValues ->
            HomeScreenBackground {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .fillMaxHeight(0.9f)
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                    Spacer(modifier = Modifier.height(30.dp))
                        allPostResults.map {
                            Spacer(modifier = Modifier.height(30.dp))
                            ResultCard(
                                result = it,
                                resultViewModel = resultViewModel
                            )
                        }
                    if(allPostResults.isEmpty()) {
                        AlertCard(text = "No benchmarking has been done yet")
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
                ContinueButton(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable (
                            onClick = { navController.navigate("modelSelection") },
                            role = Role.Button,
                        ),
                    label = "START BENCHMARKING"
                )
            }
    }
}