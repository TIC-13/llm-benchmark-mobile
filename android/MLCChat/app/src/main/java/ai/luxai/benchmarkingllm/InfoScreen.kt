package ai.luxai.benchmarkingllm

import ai.luxai.benchmarkingllm.components.AccordionItem
import ai.luxai.benchmarkingllm.components.AccordionText
import ai.luxai.benchmarkingllm.components.AppTopBar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun InfoScreen(navController: NavController) {
    Scaffold(topBar = {
        AppTopBar(
            title = "About",
            onBack = { navController.popBackStack() }
        )
    }) { paddingValues ->
        HomeScreenBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                TextSection(
                    modifier = Modifier.padding(15.dp, 30.dp, 15.dp, 15.dp),
                    title = "What does this app do?",
                    content = "This app benchmarks LLMs"
                )

                AccordionItem(title = "How the benchmarking works") {
                    AccordionItem(title = "CPU Measurement") {
                        AccordionText(text = "dsalçkhfçasdlkf")
                    }
                    AccordionItem(title = "GPU Measurement") {
                        AccordionText(text = "dsalçkhfçasdlkf")
                    }
                    AccordionItem(title = "RAM Measurement") {
                        AccordionText(text = "dsalçkhfçasdlkf")
                    }
                }

                TextSection(
                    modifier = Modifier.padding(15.dp, 30.dp, 15.dp, 15.dp),
                    title = "About Lux.AI",
                    content = "Lux.AI is a project",
                    titleIcon = Icons.Default.Camera
                )
                
            }
        }
    }
}

@Composable
fun TextSection(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    titleIcon: ImageVector? = null
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(titleIcon !== null){
                Icon(
                    modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp),
                    imageVector = titleIcon,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    contentDescription = "LuxAI Icon"
                )
            }
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            modifier = Modifier.padding(15.dp),
            text = content,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}