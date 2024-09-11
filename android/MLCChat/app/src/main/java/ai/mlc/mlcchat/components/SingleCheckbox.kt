package ai.mlc.mlcchat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SingleCheckbox(
    modifier: Modifier = Modifier,
    labelModifier: Modifier = Modifier,
    checkboxModifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onPrimary,
    fontWeight: FontWeight = FontWeight.Light,
    textAlign: TextAlign = TextAlign.Left,
    labelStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    checked: Boolean,
    setChecked: (status: Boolean) -> Unit,
    enabled: Boolean = true,
    label: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = labelModifier,
            textAlign = textAlign,
            fontWeight = fontWeight,
            color = labelColor,
            style = labelStyle,
            text = label
        )
        Checkbox(
            enabled = enabled,
            modifier = checkboxModifier,
            checked = checked,
            onCheckedChange = { setChecked(it) }
        )
    }
}