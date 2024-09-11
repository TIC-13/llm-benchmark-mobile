package ai.mlc.mlcchat

import ai.mlc.mlcchat.api.LLMModel
import ai.mlc.mlcchat.api.PostResult
import ai.mlc.mlcchat.api.postResult
import ai.mlc.mlcchat.components.AccordionItem
import ai.mlc.mlcchat.components.AccordionText
import ai.mlc.mlcchat.components.AppTopBar
import ai.mlc.mlcchat.components.Chip
import ai.mlc.mlcchat.interfaces.BenchmarkingResult
import ai.mlc.mlcchat.utils.benchmark.system.getPhoneData
import android.util.Log
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ResultView(
    navController: NavController,
    chatState: AppViewModel.ChatState,
    resultViewModel: ResultViewModel
){

    val results = resultViewModel.getResults()

    fun goToHome() {
        chatState.requestResetChat()
        navController.popBackStack("main", false)
    }

    val resultType = resultViewModel.getType()

    Scaffold(topBar =
        {
            AppTopBar(
                title = "Result",
                onBack = {
                    when(resultType) {
                        ResultType.BENCHMARKING -> goToHome()
                        ResultType.CONVERSATION -> navController.popBackStack()
                        else -> navController.popBackStack()
                    }
                }
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

                        AccordionItem(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            title = "Help"
                        ) {
                            AccordionItem(title = "What is prefill?") {
                                AccordionText(
                                    text = "Prefill tok/s measures how many tokens the model can process per second during the initial setup phase."
                                )
                            }
                            AccordionItem(title = "What is decode?") {
                                AccordionText(
                                    text = "Decode tok/s measures how many tokens the model can generate per second during the decoding phase."
                                )
                            }
                            AccordionItem(title = "Why can't the tok/s values be measured?") {
                                AccordionText(
                                    text = "When the response takes too long, the app assumes that the model is broken or has entered a loop and interrupts the response. In that case, the tok/s values are not measured."
                                )
                            }
                            Chip(text = "STD = Standard Deviation")
                        }

                        results.map {
                            ResultCard(
                                result = it,
                                postResults = resultType == ResultType.BENCHMARKING,
                                resultViewModel = resultViewModel
                            )
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
    result: BenchmarkingResult,
    postResults: Boolean = false,
    resultViewModel: ResultViewModel
) {

    val context = LocalContext.current
    val samples = result.samples

    val prefill = remember { samples.prefill.getMeasurements() }
    val decode = remember { samples.decode.getMeasurements() }

    LaunchedEffect(Unit) {

        if(!postResults) return@LaunchedEffect

        val power = getPowerConsumption(result, resultViewModel.getIdleSamples())
        val energy = getEnergyConsumption(result, resultViewModel.getIdleSamples())
        
        postResult(PostResult(
            phone = getPhoneData(context),
            llm_model = LLMModel(name = result.name),
            load_time = result.loadTime?.toInt(),
            ram = samples.ram.getMeasurements(),
            cpu = samples.cpu.getMeasurements(),
            gpu = samples.gpu.getMeasurements(),
            decode = decode,
            prefill = prefill,
            energyAverage = if(!energy.isNaN()) energy else null,
            powerAverage = if(!power.isNaN()) power else null
        ))
    }

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

        ResultTable(result = result, resultViewModel = resultViewModel)
        
        if(prefill.median.isNaN() || decode.median.isNaN()){
            Chip(
                text = "Tok/s values not measured",
                icon = Icons.Default.Warning
            )
        }
        
        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
fun ResultTable(result: BenchmarkingResult, resultViewModel: ResultViewModel) {

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

    val powerDifference = getPowerConsumption(result, resultViewModel.getIdleSamples())
    val powerResult =
        if(powerDifference.isNaN())
            "N/A"
        else
            "${formatDouble(powerDifference)}W"

    val energyConsumptionDifference = getEnergyConsumption(result, resultViewModel.getIdleSamples())
    val energyConsumptionResult =
        if(energyConsumptionDifference.isNaN())
            "N/A"
        else
            "${formatDouble(energyConsumptionDifference)}J"

    val loadTimeResult =
        if(result.loadTime == null)
            "N/A"
        else
            "${result.loadTime} ms"

    val toksTotal = result.samples.prefill.median() + result.samples.decode.median()

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(10.dp, 0.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if(!toksTotal.isNaN()){
            TableRow(
                content = listOf(
                    RowContent(""),
                    RowContent("Tok/s", bold = true),
                    RowContent("${formatDouble(toksTotal)} tok/s"),
                    RowContent("")
                )
            )
        }
        TableRow(
            content = listOf(
                RowContent(""),
                RowContent("Init time", bold = true),
                RowContent(loadTimeResult),
                RowContent("")
            )
        )
        Spacer(modifier = Modifier.height(15.dp))
        TableRow(
            content = listOf(
                RowContent(""),
                RowContent("Average", bold = true),
                RowContent("STD", bold = true),
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

        /*
        TableRow(
            content = listOf(
                RowContent("Power", bold = true),
                RowContent(powerResult),
                RowContent(""),
                RowContent("")
            )
        )
        TableRow(
            content = listOf(
                RowContent("Energy", bold = true),
                RowContent(energyConsumptionResult),
                RowContent(""),
                RowContent("")
            )
        )
         */

        Spacer(modifier = Modifier.height(5.dp))

        if(result.samples.prefill.getSamples().isNotEmpty() &&
            result.samples.decode.getSamples().isNotEmpty()) {

            TableRow(
                content = listOf(
                    RowContent(""),
                    RowContent("Median", bold = true),
                    RowContent("STD", bold = true),
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
}

fun formatDouble(number: Number): String {
    return String.format("%.1f", number)
}

fun getPowerConsumption(result: BenchmarkingResult, idleSamples: IdleSamples?): Double {
    if(idleSamples == null) return Double.NaN
    val powerConsumption = result.samples.voltages.average() * result.samples.currents.average()
    val powerIdle = idleSamples.voltages.average()*idleSamples.currents.average()
    return powerConsumption - powerIdle
}

fun getEnergyConsumption(result: BenchmarkingResult, idleSamples: IdleSamples?): Double {
    if(idleSamples == null) return Double.NaN
    Log.d("idle", idleSamples.voltages.average().toString())
    return getPowerConsumption(result, idleSamples) * (result.samples.prefillTime.sum() + result.samples.decodeTime.sum())
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
            text = "CONTINUE",
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
