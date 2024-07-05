package ai.mlc.mlcchat

import ai.mlc.mlcchat.components.AppTopBar
import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ResultView(
    navController: NavController,
    resultViewModel: ResultViewModel
){

    val goToHome = { navController.popBackStack("main", false) }

    Scaffold(topBar =
        {
            AppTopBar(
                title = "Result",
                onBack = { goToHome() }
            )
        }
    ) {
        paddingValues ->
            HomeScreenBackground {
                Column (
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f)
                            .verticalScroll(rememberScrollState())
                            .padding(0.dp, 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        resultViewModel.getResults().map {
                            ResultCard(result = it)
                        }
                    }

                    ContinueButton(
                        modifier = Modifier
                            .clickable {
                                goToHome()
                            }
                    )
                }
            }
    }
}

@Composable
fun ResultCard(
    modifier: Modifier = Modifier,
    result: BenchmarkingResult
) {
    Column(
        modifier = modifier
            .fillMaxWidth(0.8F)
            .clip(RoundedCornerShape(15.dp))
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ){
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Text(
                modifier = Modifier
                    .padding(0.dp, 15.dp),
                text = result.name,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        ResultTable(result = result)

        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
fun ResultTable(result: BenchmarkingResult) {

    @Composable
    fun TableCell(
        modifier: Modifier = Modifier,
        value: String,
        bold: Boolean = false,
        textAlign: TextAlign = TextAlign.Left
    ) {
        Text(
            modifier = modifier,
            text = value,
            fontSize = 12.sp,
            textAlign = textAlign,
            fontWeight = if(bold)
                FontWeight.Bold
            else
                FontWeight.Normal
        )
    }

    data class RowContent(
        val text: String,
        val textAlign: TextAlign = TextAlign.Left,
        val bold: Boolean = false
    )

    @Composable
    fun TableRow(
        modifier: Modifier = Modifier,
        content: List<RowContent>
    ) {
        Row (
            modifier = modifier
                .fillMaxWidth()
        ){
            for((index, rowValue) in content.withIndex()) {
                val isFirst = index == 0

                TableCell(
                    modifier = Modifier
                        .weight(1f),
                    textAlign = rowValue.textAlign,
                    value = rowValue.text,
                    bold = rowValue.bold
                )
            }
        }
    }

    val powerConsumption = result.samples.voltages.average() * result.samples.currents.average()
    val powerIdle = result.idleSamples.voltages.average()*result.idleSamples.currents.average()
    val powerResult =
        if(powerConsumption.isNaN() || powerIdle.isNaN())
            "N/A"
        else
            "${formatDouble(powerConsumption - powerIdle)}W"

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(10.dp, 0.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        TableRow(
            content = listOf(
                RowContent(""),
                RowContent("Average", bold = true),
                RowContent("std", bold = true),
                RowContent("Peak", bold = true)
            )
        )
        TableRow(
            content = listOf(
                RowContent("CPU", bold = true),
                RowContent("${result.samples.cpu.average().toInt()}%"),
                RowContent("${result.samples.cpu.std().toInt()}%"),
                RowContent("${result.samples.cpu.peak().toInt()}%")
            )
        )
        TableRow(
            content = listOf(
                RowContent("GPU", bold = true),
                RowContent("${result.samples.gpu.average().toInt()}%"),
                RowContent("${result.samples.gpu.std().toInt()}%"),
                RowContent("${result.samples.gpu.peak().toInt()}%")
            )
        )
        TableRow(
            content = listOf(
                RowContent("RAM", bold = true),
                RowContent("${result.samples.ram.average().toInt()}MB"),
                RowContent("${result.samples.ram.std().toInt()}MB"),
                RowContent("${result.samples.ram.peak().toInt()}MB")
            )
        )
        TableRow(
            content = listOf(
                RowContent("Potência", bold = true),
                RowContent(powerResult),
                RowContent("   -"),
                RowContent("   -")
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        TableRow(
            content = listOf(
                RowContent(""),
                RowContent("Median", bold = true),
                RowContent("std", bold = true),
                RowContent("Peak", bold = true)
            )
        )
        TableRow(
            content = listOf(
                RowContent("Prefill", bold = true),
                RowContent("${formatDouble(result.samples.prefill.median())} tok/s"),
                RowContent("${formatDouble(result.samples.prefill.std())} tok/s"),
                RowContent("${formatDouble(result.samples.prefill.peak())} tok/s")
            )
        )
        TableRow(
            content = listOf(
                RowContent("Decode", bold = true),
                RowContent("${formatDouble(result.samples.decode.median())} tok/s"),
                RowContent("${formatDouble(result.samples.decode.std())} tok/s"),
                RowContent("${formatDouble(result.samples.decode.peak())} tok/s")
            )
        )
    }
}

fun formatDouble(number: Number): String {
    return String.format("%.1f", number)
}

@Composable
fun ContinueButton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(1f)
            .background(MaterialTheme.colorScheme.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            fontWeight = FontWeight.Bold,
            text = "CONTINUAR",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
