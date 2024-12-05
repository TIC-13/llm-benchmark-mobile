package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.templates.ResultTemplate
import ai.luxai.benchmarkingllm.utils.benchmark.getAllPostResults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun StoredResultsView(
    navController: NavController,
    resultViewModel: ResultViewModel
) {

    val context = LocalContext.current
    val allPostResultsFlow = remember { getAllPostResults(context) }
    val allPostResults by allPostResultsFlow.collectAsState(initial = emptyList())

    ResultTemplate(
        resultViewModel = resultViewModel,
        results = allPostResults,
        title = "Last results",
        bottomButtonLabel = "START BENCHMARKING",
        onBack = { navController.popBackStack() },
        onContinue = { navController.navigate("modelSelection") }
    )

}