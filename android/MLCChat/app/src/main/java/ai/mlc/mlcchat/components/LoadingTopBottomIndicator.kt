package ai.mlc.mlcchat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun LoadingTopBottomIndicator(
    modifier: Modifier = Modifier,
    text: String,
    subtitleText: String? = null,
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .width(20.dp)
                .padding(0.dp, 20.dp, 0.dp, 0.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column {
            Text(
                modifier = Modifier
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Light
            )
            if(subtitleText !== null){
                Text(
                    modifier = Modifier
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    text = subtitleText,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}