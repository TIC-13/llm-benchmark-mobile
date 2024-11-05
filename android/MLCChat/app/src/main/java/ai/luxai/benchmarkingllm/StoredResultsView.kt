package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.components.AppTopBar
import ai.luxai.benchmarkingllm.utils.benchmark.getAllPostResults
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        allPostResults.map {
                            Spacer(modifier = Modifier.height(30.dp))
                        ResultCard(
                            result = it,
                            resultViewModel = resultViewModel
                        )
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
    }
}