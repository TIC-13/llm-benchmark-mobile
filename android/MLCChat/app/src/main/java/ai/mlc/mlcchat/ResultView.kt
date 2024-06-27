package ai.mlc.mlcchat

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultView(
    navController: NavController,
    resultViewModel: ResultViewModel
){

    val goToHome = { navController.popBackStack("main", false) }

    Scaffold(topBar =
        {
            TopAppBar(
                title = {
                    Text(
                        text = "Resultado",
                        color = MaterialTheme.colorScheme.onPrimary
                    )},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(
                        onClick = { goToHome() },
                        enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "back home page",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
            )
        }
    ) {
        paddingValues ->
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
                    resultViewModel.results.map {
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

@Composable
fun ResultCard(modifier: Modifier = Modifier, result: BenchmarkingResult) {
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
    fun TableRow(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Row (
            modifier = modifier
                .fillMaxWidth()
        ){
            content()
        }
    }

    @Composable
    fun TableCell(
        modifier: Modifier = Modifier,
        value: String,
        bold: Boolean = false
    ) {
        Text(
            modifier = modifier,
            text = value,
            fontSize = 12.sp,
            fontWeight = if(bold)
                FontWeight.Bold
            else
                FontWeight.Normal
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(10.dp, 0.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        TableRow(){
            TableCell(modifier = Modifier.weight(1f), value = "")
            TableCell(modifier = Modifier.weight(1f), value = "Média", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "std", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "Pico", bold = true)
        }
        TableRow(){
            TableCell(modifier = Modifier.weight(1f), value = "CPU", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "${result.cpu.average}%")
            TableCell(modifier = Modifier.weight(1f), value = "${result.cpu.std}%")
            TableCell(modifier = Modifier.weight(1f), value = "${result.cpu.peak}%")
        }
        TableRow(){
            TableCell(modifier = Modifier.weight(1f), value = "GPU", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "${result.gpu.average}%")
            TableCell(modifier = Modifier.weight(1f), value = "${result.gpu.std}%")
            TableCell(modifier = Modifier.weight(1f), value = "${result.gpu.peak}%")
        }
        TableRow(){
            TableCell(modifier = Modifier.weight(1f), value = "RAM", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "${result.ram.average}MB")
            TableCell(modifier = Modifier.weight(1f), value = "${result.ram.std}MB")
            TableCell(modifier = Modifier.weight(1f), value = "${result.ram.peak}MB")
        }
        TableRow(){
            TableCell(modifier = Modifier.weight(1f), value = "tok/s", bold = true)
            TableCell(modifier = Modifier.weight(1f), value = "${result.toks.average} tok/s")
            TableCell(modifier = Modifier.weight(1f), value = "${result.toks.std} tok/s")
            TableCell(modifier = Modifier.weight(1f), value = "${result.toks.peak} tok/s")
        }
    }
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
