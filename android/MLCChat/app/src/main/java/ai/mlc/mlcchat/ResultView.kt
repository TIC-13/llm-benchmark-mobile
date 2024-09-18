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
    resultViewModel: ResultViewModel
) {

    val samples = result.samples

    val prefill = remember { samples.prefill.getMeasurements() }
    val decode = remember { samples.decode.getMeasurements() }

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
        
        if(prefill.median == null || decode.median == null){
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
    val powerResult =  formatDouble(powerDifference, "W")

    val energyConsumptionDifference = getEnergyConsumption(result, resultViewModel.getIdleSamples())
    val energyConsumptionResult = formatDouble(energyConsumptionDifference, "J")

    val loadTimeResult = formatInt(result.loadTime, " ms")

    val prefillMedian = result.samples.prefill.median()
    val decodeMedian = result.samples.decode.median()

    val toksTotal = if (prefillMedian !== null && decodeMedian !== null)
        prefillMedian + decodeMedian
    else
        null

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(10.dp, 0.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if(toksTotal !== null){
            TableRow(
                content = listOf(
                    RowContent(""),
                    RowContent("Tok/s", bold = true),
                    RowContent(formatDouble(toksTotal, " tok/s")),
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
                RowContent(formatInt(result.samples.cpu.average(), "%")),
                RowContent(formatInt(result.samples.cpu.std(), "%")),
                RowContent(formatInt(result.samples.cpu.peak(), "%"))
            )
        )
        TableRow(
            content = listOf(
                RowContent("GPU", bold = true),
                RowContent(formatInt(result.samples.gpu.average(), "%")),
                RowContent(formatInt(result.samples.gpu.std(), "%")),
                RowContent(formatInt(result.samples.gpu.peak(), "%"))
            )
        )
        TableRow(
            content = listOf(
                RowContent("RAM", bold = true),
                RowContent(formatInt(result.samples.ram.average(), "MB")),
                RowContent(formatInt(result.samples.ram.std(), "MB")),
                RowContent(formatInt(result.samples.ram.peak(), "MB"))
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
                    RowContent(formatDouble(result.samples.prefill.median(), "tok/s")),
                    RowContent(formatDouble(result.samples.prefill.std(), " tok/s")),
                    RowContent(formatDouble(result.samples.prefill.peak(), "tok/s"))
                )
            )
            TableRow(
                content = listOf(
                    RowContent("Decode", bold = true),
                    RowContent(formatDouble(result.samples.decode.median(), " tok/s")),
                    RowContent(formatDouble(result.samples.decode.std(), "tok/s")),
                    RowContent(formatDouble(result.samples.decode.peak(), "tok/s"))
                )
            )

        }
    }
}

fun formatDouble(number: Number?, suffix: String): String {
    if(number == null) return "-"
    return String.format("%.1f", number) + suffix
}

fun formatInt(number: Number?, suffix: String): String {
    if(number == null) return "-"
    return "${number.toInt()}${suffix}"
}

fun getPowerConsumption(result: BenchmarkingResult, idleSamples: IdleSamples?): Double? {
    val avgVoltage = result.samples.voltages.average()
    val avgCurrent = result.samples.currents.average()

    if(idleSamples == null || avgCurrent == null || avgVoltage == null)
        return null
    val powerConsumption = avgVoltage * avgCurrent

    val idleVoltage = idleSamples.voltages.average()
    val idleCurrent = idleSamples.currents.average()

    if(idleCurrent == null || idleVoltage == null)
        return null

    val powerIdle = idleVoltage*idleCurrent
    return powerConsumption - powerIdle
}

fun getEnergyConsumption(result: BenchmarkingResult, idleSamples: IdleSamples?): Double? {
    val powerConsumption = getPowerConsumption(result, idleSamples)
    val prefillTimeTotal = result.samples.prefillTime.sum()
    val decodeTimeTotal = result.samples.decodeTime.sum()


    if(idleSamples == null ||
        powerConsumption == null ||
        prefillTimeTotal == null ||
        decodeTimeTotal == null
        )
        return null
    return powerConsumption * (prefillTimeTotal * decodeTimeTotal)
}

@Composable
fun ContinueButton(modifier: Modifier = Modifier, label: String = "CONTINUE") {
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
            text = label,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
