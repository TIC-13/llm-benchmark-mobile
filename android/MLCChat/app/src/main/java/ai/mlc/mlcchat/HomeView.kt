package ai.mlc.mlcchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeView(navController: NavController) {

    Column (
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            Button(onClick = { navController.navigate("download") }) {
                Text(text = "Iniciar testes")
            }
            Button(onClick = { navController.navigate("home") }) {
                Text(text = "Conversar com LLM")
            }
        }
    }
}