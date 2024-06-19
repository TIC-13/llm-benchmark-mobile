package ai.mlc.mlcchat

import ai.mlc.mlcchat.utils.benchmark.DownloadView
import ai.mlc.mlcchat.utils.benchmark.ResultViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@ExperimentalMaterial3Api
@Composable
fun NavView(appViewModel: AppViewModel = viewModel(), resultViewModel: ResultViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { HomeView(navController) }
        composable("download") { DownloadView(navController, appViewModel)}
        composable("home") { StartView(navController, appViewModel) }
        composable("chat") { ChatView(navController, appViewModel.chatState, resultViewModel) }
        composable("result") { ResultView(navController, resultViewModel) }
    }
}