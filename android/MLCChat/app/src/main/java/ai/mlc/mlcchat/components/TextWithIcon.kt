package ai.mlc.mlcchat.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun TextWithIcon(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    imageVector: ImageVector,
    iconColor: Color,
    text: String,
    fontWeight: FontWeight = FontWeight.Normal,
    fontColor: Color = MaterialTheme.colorScheme.onPrimary,
    fontStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = iconModifier
                .size(24.dp),
            imageVector = imageVector,
            contentDescription = "Icon",
            tint = iconColor
        )
        Text(
            modifier = modifier
                .padding(start = 8.dp),
            text = text,
            fontWeight = fontWeight,
            style = fontStyle,
            color = fontColor,
        )
    }
}