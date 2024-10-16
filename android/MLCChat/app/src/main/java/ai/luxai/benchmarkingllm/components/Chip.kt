package ai.luxai.benchmarkingllm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun Chip(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(10.dp),

    textModifier: Modifier = Modifier,
    textPaddingValues: PaddingValues = PaddingValues(10.dp),
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    fontWeight: FontWeight = FontWeight.SemiBold,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    text: String,

    icon: ImageVector? = null, // Default icon, can be customized
    iconDescription: String = "Chip icon",
    iconModifier: Modifier = Modifier.size(16.dp), // Default size, can be customized
    iconTint: Color = textColor // Default tint same as text color, can be customized
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .padding(textPaddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if(icon !== null) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = iconTint,
                modifier = iconModifier
            )
            Spacer(modifier = Modifier.width(4.dp))
        }

        Text(
            modifier = textModifier,
            text = text,
            fontWeight = fontWeight,
            color = textColor,
            style = textStyle
        )
    }
}