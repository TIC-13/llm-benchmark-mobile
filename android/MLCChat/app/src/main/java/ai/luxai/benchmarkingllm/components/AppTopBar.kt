package ai.luxai.benchmarkingllm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backEnabled: Boolean = true,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
                if(subtitle !== null) {
                    //Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Normal,
                        //fontStyle = MaterialTheme.typography.labelMedium
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
        navigationIcon = {
            if(onBack !== null) {
                IconButton(
                    onClick = { onBack() },
                    enabled = backEnabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "back home page",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        actions = { actions() })
}