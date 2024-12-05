package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.templates.ResultTemplate
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun ResultView(
    navController: NavController,
    chatState: AppViewModel.ChatState,
    resultViewModel: ResultViewModel
){

    val results = resultViewModel.getResults()

    val resultType = resultViewModel.getType()

    fun finish() {
        chatState.requestResetChat()

        when(resultType) {
            ResultType.BENCHMARKING -> navController.popBackStack("main", false)
            ResultType.CONVERSATION -> navController.popBackStack("home", false)
        }
    }

    fun backButton() {
        when(resultType) {
            ResultType.BENCHMARKING -> finish()
            ResultType.CONVERSATION -> navController.popBackStack()
        }
    }

    BackHandler {
        backButton()
    }

    ResultTemplate(
        resultViewModel = resultViewModel,
        results = results,
        title = "Result",
        bottomButtonLabel = "FINISH",
        onBack = { backButton() },
        onContinue = { finish() }
    )
}
